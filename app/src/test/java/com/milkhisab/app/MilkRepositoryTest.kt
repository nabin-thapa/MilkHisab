package com.milkhisab.app

import com.milkhisab.app.data.local.MilkRecordDao
import com.milkhisab.app.data.local.MilkRecordEntity
import com.milkhisab.app.data.repository.DuplicateRecordException
import com.milkhisab.app.data.repository.MilkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

/**
 * An in-memory implementation of [MilkRecordDao] so the repository's rules
 * (one record per day, monthly totals, restore safety) can be tested on the
 * JVM without an Android device or emulator.
 *
 * The UNIQUE(date) behaviour of SQLite is reproduced here, because that is
 * exactly what the duplicate-date feature relies on.
 */
private class FakeMilkRecordDao : MilkRecordDao {

    private val rows = LinkedHashMap<Long, MilkRecordEntity>()
    private var nextId = 1L

    private fun insertInternal(record: MilkRecordEntity, replace: Boolean): Long {
        val conflict = rows.values.firstOrNull { it.date == record.date && it.id != record.id }
        if (conflict != null) {
            if (!replace) {
                // SQLite ABORT: nothing is written.
                throw IllegalStateException("UNIQUE constraint failed: milk_records.date")
            }
            rows.remove(conflict.id)
        }
        val id = if (record.id == 0L) nextId++ else record.id
        rows[id] = record.copy(id = id)
        return id
    }

    override suspend fun insert(record: MilkRecordEntity): Long = insertInternal(record, false)

    override suspend fun insertAll(records: List<MilkRecordEntity>) {
        records.forEach { insertInternal(it, replace = true) }
    }

    override suspend fun update(record: MilkRecordEntity) {
        rows[record.id] = record
    }

    override suspend fun delete(record: MilkRecordEntity) {
        rows.remove(record.id)
    }

    override suspend fun deleteById(id: Long) {
        rows.remove(id)
    }

    override suspend fun getById(id: Long): MilkRecordEntity? = rows[id]

    override suspend fun getByDate(date: LocalDate): MilkRecordEntity? =
        rows.values.firstOrNull { it.date == date }

    override fun observeByDate(date: LocalDate): Flow<MilkRecordEntity?> =
        flowOf(rows.values.firstOrNull { it.date == date })

    override fun observeAll(): Flow<List<MilkRecordEntity>> =
        flowOf(rows.values.sortedWith(compareByDescending<MilkRecordEntity> { it.date }.thenByDescending { it.id }))

    override fun observeBetween(start: LocalDate, end: LocalDate): Flow<List<MilkRecordEntity>> =
        flowOf(range(start, end))

    override suspend fun getBetween(start: LocalDate, end: LocalDate): List<MilkRecordEntity> =
        range(start, end)

    private fun range(start: LocalDate, end: LocalDate): List<MilkRecordEntity> =
        rows.values.filter { !it.date.isBefore(start) && it.date.isBefore(end) }
            .sortedBy { it.date }

    override suspend fun getLatestRate(): Double? =
        rows.values.maxByOrNull { it.date }?.rate

    override suspend fun getAllOnce(): List<MilkRecordEntity> =
        rows.values.sortedBy { it.date }

    override suspend fun count(): Int = rows.size

    override suspend fun sumQuantityBetween(start: LocalDate, end: LocalDate): Double =
        range(start, end).sumOf { it.quantity }

    override suspend fun sumAmountBetween(start: LocalDate, end: LocalDate): Double =
        range(start, end).sumOf { it.amount }

    override suspend fun countBetween(start: LocalDate, end: LocalDate): Int =
        range(start, end).size

    override fun observeSumQuantityBetween(start: LocalDate, end: LocalDate): Flow<Double> =
        flowOf(range(start, end).sumOf { it.quantity })

    override fun observeSumAmountBetween(start: LocalDate, end: LocalDate): Flow<Double> =
        flowOf(range(start, end).sumOf { it.amount })

    override fun observeCountBetween(start: LocalDate, end: LocalDate): Flow<Int> =
        flowOf(range(start, end).size)

    override suspend fun deleteAll() {
        rows.clear()
    }
}

/**
 * Repository rules under test:
 *  - duplicate dates are refused (never silently duplicated)
 *  - monthly totals use each record's own rate
 *  - daily average counts only recorded days
 *  - latest rate is available for prefill
 *  - restore replaces content only after validation
 */
class MilkRepositoryTest {

    private lateinit var dao: FakeMilkRecordDao
    private lateinit var repository: MilkRepository

    private val oct1 = LocalDate.of(2026, 10, 1)
    private val oct2 = LocalDate.of(2026, 10, 2)
    private val oct3 = LocalDate.of(2026, 10, 3)

    @Before
    fun setUp() {
        dao = FakeMilkRecordDao()
        repository = MilkRepository(dao)
    }

    private fun record(date: LocalDate, qty: Double, rate: Double) = MilkRecordEntity(
        date = date,
        quantity = qty,
        rate = rate,
        amount = com.milkhisab.app.domain.MilkCalculator.calculateAmount(qty, rate)
    )

    @Test
    fun `inserting two records for the same date throws DuplicateRecordException`() =
        runTest {
            repository.insertRecord(record(oct1, 3.0, 70.0))
            try {
                repository.insertRecord(record(oct1, 2.0, 70.0))
                fail("Expected DuplicateRecordException")
            } catch (expected: DuplicateRecordException) {
                assertEquals(oct1, expected.date)
            }
            // The first record must still be intact - nothing was overwritten.
            assertEquals(1, repository.countRecords())
            assertEquals(3.0, repository.getRecordForDate(oct1)!!.quantity, 0.0)
        }

    @Test
    fun `different dates do not conflict`() = runTest {
        repository.insertRecord(record(oct1, 3.0, 70.0))
        repository.insertRecord(record(oct2, 2.5, 70.0))
        repository.insertRecord(record(oct3, 3.25, 72.0))
        assertEquals(3, repository.countRecords())
    }

    @Test
    fun `monthly totals use each record's own rate`() = runTest {
        repository.insertRecord(record(oct1, 3.0, 70.0))   // 210
        repository.insertRecord(record(oct2, 2.5, 70.0))   // 175
        repository.insertRecord(record(oct3, 3.25, 72.0))  // 234

        val month = YearMonth.of(2026, 10)
        val totals = repository.observeMonthTotals(month).first()

        assertEquals(619.0, totals.totalAmount, 0.001)   // not 8.75 x 72
        assertEquals(8.75, totals.totalMilk, 0.001)
        assertEquals(3, totals.recordedDays)
        assertEquals(2.917, totals.averageDailyMilk, 0.001)
    }

    @Test
    fun `totals only include the selected month`() = runTest {
        repository.insertRecord(record(oct1, 3.0, 70.0))
        repository.insertRecord(record(LocalDate.of(2026, 9, 30), 5.0, 70.0))

        val october = repository.observeMonthTotals(YearMonth.of(2026, 10)).first()
        val september = repository.observeMonthTotals(YearMonth.of(2026, 9)).first()

        assertEquals(1, october.recordedDays)
        assertEquals(210.0, october.totalAmount, 0.001)
        assertEquals(1, september.recordedDays)
        assertEquals(350.0, september.totalAmount, 0.001)
    }

    @Test
    fun `empty month reports zeros instead of failing`() = runTest {
        val totals = repository.observeMonthTotals(YearMonth.of(2026, 1)).first()
        assertEquals(0, totals.recordedDays)
        assertEquals(0.0, totals.totalMilk, 0.0)
        assertEquals(0.0, totals.totalAmount, 0.0)
        assertEquals(0.0, totals.averageDailyMilk, 0.0)
    }

    @Test
    fun `update keeps one row per date and recalculates amount`() = runTest {
        repository.insertRecord(record(oct1, 3.0, 70.0))
        val existing = repository.getRecordForDate(oct1)!!

        repository.updateRecord(
            existing.copy(quantity = 3.5, amount = 3.5 * 70.0)
        )

        assertEquals(1, repository.countRecords())
        assertEquals(3.5, repository.getRecordForDate(oct1)!!.quantity, 0.0)
        assertEquals(245.0, repository.getRecordForDate(oct1)!!.amount, 0.001)
    }

    @Test
    fun `moving an edit onto another record's date is refused`() = runTest {
        repository.insertRecord(record(oct1, 3.0, 70.0))
        repository.insertRecord(record(oct2, 2.5, 70.0))

        val first = repository.getRecordForDate(oct1)!!
        try {
            repository.updateRecord(first.copy(date = oct2))
            fail("Expected DuplicateRecordException")
        } catch (expected: DuplicateRecordException) {
            // expected
        }
        assertEquals(2.5, repository.getRecordForDate(oct2)!!.quantity, 0.0)
    }

    @Test
    fun `delete removes only that day and totals update`() = runTest {
        repository.insertRecord(record(oct1, 3.0, 70.0))
        repository.insertRecord(record(oct2, 2.5, 70.0))

        val target = repository.getRecordForDate(oct2)!!
        repository.deleteRecord(target)

        assertNull(repository.getRecordForDate(oct2))
        val totals = repository.observeMonthTotals(YearMonth.of(2026, 10)).first()
        assertEquals(1, totals.recordedDays)
        assertEquals(210.0, totals.totalAmount, 0.001)
    }

    @Test
    fun `latest rate is the one from the newest date`() = runTest {
        assertNull(repository.getLatestRate()) // empty database
        repository.insertRecord(record(oct1, 3.0, 70.0))
        repository.insertRecord(record(oct3, 3.0, 75.0))
        repository.insertRecord(record(oct2, 3.0, 72.0)) // older date added last
        assertEquals(75.0, repository.getLatestRate()!!, 0.001)
    }

    @Test
    fun `restore replaces content in one step`() = runTest {
        repository.insertRecord(record(oct1, 3.0, 70.0))

        val backup = listOf(
            record(LocalDate.of(2026, 11, 1), 2.0, 80.0),
            record(LocalDate.of(2026, 11, 2), 4.0, 80.0)
        )
        repository.restoreAll(backup)

        assertEquals(2, repository.countRecords())
        assertNull(repository.getRecordForDate(oct1)) // old content replaced
        assertNotNull(repository.getRecordForDate(LocalDate.of(2026, 11, 1)))
        assertEquals(
            480.0,
            repository.observeMonthTotals(YearMonth.of(2026, 11)).first().totalAmount,
            0.001
        )
    }

    @Test
    fun `records survive being read back (persistence contract)`() = runTest {
        // Simulates "close app, reopen app": a fresh repository over the same
        // storage must still return the same rows.
        repository.insertRecord(record(oct1, 3.0, 70.0))

        val reopened = MilkRepository(dao)
        val record = reopened.getRecordForDate(oct1)
        assertNotNull(record)
        assertEquals(3.0, record!!.quantity, 0.0)
        assertEquals(210.0, record.amount, 0.001)
        assertTrue(reopened.getAllRecords().isNotEmpty())
    }
}
