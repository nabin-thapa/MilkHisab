package com.milkhisab.app.data.repository

import com.milkhisab.app.data.local.AppDatabase
import com.milkhisab.app.data.local.MilkRecordDao
import com.milkhisab.app.data.local.MilkRecordEntity
import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.time.YearMonth

/**
 * Single source of truth for milk data.
 *
 * The ViewModels never talk to the DAO directly; they go through this
 * repository so the data layer can evolve without touching the UI.
 *
 * [database] is only needed for operations that must be atomic (restore);
 * unit tests construct the repository with the DAO alone.
 */
class MilkRepository(
    private val dao: MilkRecordDao,
    private val database: AppDatabase? = null
) {

    // ----------------------------------------------------------------- today

    fun observeRecordForDate(date: LocalDate): Flow<MilkRecordEntity?> =
        dao.observeByDate(date)

    suspend fun getRecordForDate(date: LocalDate): MilkRecordEntity? =
        dao.getByDate(date)

    // ----------------------------------------------------------------- lists

    fun observeAllRecords(): Flow<List<MilkRecordEntity>> = dao.observeAll()

    fun observeRecordsForMonth(month: YearMonth): Flow<List<MilkRecordEntity>> =
        dao.observeBetween(month.atDay(1), month.plusMonths(1).atDay(1))

    suspend fun getRecordsForMonth(month: YearMonth): List<MilkRecordEntity> =
        dao.getBetween(month.atDay(1), month.plusMonths(1).atDay(1))

    suspend fun getRecordById(id: Long): MilkRecordEntity? = dao.getById(id)

    // ----------------------------------------------------------------- write

    /**
     * Inserts a brand-new record.
     *
     * @throws DuplicateRecordException when the date already has a record,
     * so the caller can ask the user instead of silently duplicating a day.
     */
    @Throws(DuplicateRecordException::class)
    suspend fun insertRecord(record: MilkRecordEntity) {
        val existing = dao.getByDate(record.date)
        if (existing != null && existing.id != record.id) {
            throw DuplicateRecordException(record.date)
        }
        dao.insert(record)
    }

    suspend fun updateRecord(record: MilkRecordEntity) {
        // Guard against moving an edit onto a date that already has a record.
        val existing = dao.getByDate(record.date)
        if (existing != null && existing.id != record.id) {
            throw DuplicateRecordException(record.date)
        }
        dao.update(record)
    }

    suspend fun deleteRecord(record: MilkRecordEntity) = dao.delete(record)

    suspend fun deleteRecordById(id: Long) = dao.deleteById(id)

    // ------------------------------------------------------------- prefill

    /** Most recently used rate, used to prefill the add screen. */
    suspend fun getLatestRate(): Double? = dao.getLatestRate()

    // ------------------------------------------------------------- summaries

    fun observeMonthTotals(month: YearMonth): Flow<MonthTotals> {
        val start = month.atDay(1)
        val end = month.plusMonths(1).atDay(1)
        return combine(
            dao.observeSumQuantityBetween(start, end),
            dao.observeSumAmountBetween(start, end),
            dao.observeCountBetween(start, end)
        ) { totalMilk, totalAmount, days ->
            MonthTotals(
                totalMilk = totalMilk,
                totalAmount = totalAmount,
                recordedDays = days,
                averageDailyMilk = if (days > 0) totalMilk / days else 0.0
            )
        }
    }

    // ---------------------------------------------------------------- backup

    suspend fun getAllRecords(): List<MilkRecordEntity> = dao.getAllOnce()

    suspend fun countRecords(): Int = dao.count()

    /** Full replace, used only after the user confirms a backup restore. */
    suspend fun restoreAll(records: List<MilkRecordEntity>) {
        val db = database
        if (db != null) {
            // Atomic: if anything fails the old records stay untouched.
            db.withTransaction {
                dao.deleteAll()
                dao.insertAll(records)
            }
        } else {
            dao.deleteAll()
            dao.insertAll(records)
        }
    }
}

/** Monthly aggregate values shown on Home and the Summary screen. */
data class MonthTotals(
    val totalMilk: Double,
    val totalAmount: Double,
    val recordedDays: Int,
    val averageDailyMilk: Double
) {
    companion object {
        val EMPTY = MonthTotals(0.0, 0.0, 0, 0.0)
    }
}

/** Thrown when a save would create a second record for the same date. */
class DuplicateRecordException(val date: LocalDate) :
    Exception("Record already exists for $date")
