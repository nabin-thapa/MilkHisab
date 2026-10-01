package com.milkhisab.app

import com.milkhisab.app.data.backup.BackupFormatException
import com.milkhisab.app.data.backup.BackupManager
import com.milkhisab.app.data.backup.CsvExporter
import com.milkhisab.app.data.local.MilkRecordEntity
import com.milkhisab.app.utils.Formatters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

/**
 * Formatting, monthly CSV export and backup validation.
 * These are the "file" features, so they must never corrupt or lose data.
 */
class FormattingAndBackupTest {

    private fun record(date: LocalDate, qty: Double, rate: Double, amount: Double) =
        MilkRecordEntity(
            date = date,
            quantity = qty,
            rate = rate,
            amount = amount,
            createdAt = 0L,
            updatedAt = 0L
        )

    // ------------------------------------------------------------- money

    @Test
    fun `money formats with grouping and no decimals when whole`() {
        assertEquals("Rs. 210", Formatters.money(210.0))
        assertEquals("Rs. 6,125", Formatters.money(6125.0))
        assertEquals("Rs. 1,00,000", Formatters.money(100000.0))
        assertEquals("Rs. 0", Formatters.money(0.0))
    }

    @Test
    fun `money keeps paisa only when needed`() {
        assertEquals("Rs. 175.50", Formatters.money(175.5))
        assertEquals("Rs. 234", Formatters.money(234.00))
    }

    // ----------------------------------------------------------- litres

    @Test
    fun `litres always show at least one decimal`() {
        assertEquals("3.0", Formatters.litres(3.0))
        assertEquals("87.5", Formatters.litres(87.5))
        assertEquals("2.92", Formatters.litres(2.9166))
        assertEquals("3.25", Formatters.litres(3.25))
        assertEquals("0.0", Formatters.litres(0.0))
    }

    @Test
    fun `litres with unit`() {
        assertEquals("3.0 L", Formatters.litresWithUnit(3.0))
        assertEquals("87.5 L", Formatters.litresWithUnit(87.5))
    }

    // ------------------------------------------------------------- dates

    @Test
    fun `date labels use today and yesterday shortcuts`() {
        val today = LocalDate.of(2026, 10, 1)
        assertEquals("आज", Formatters.dateShort(today, today))
        assertEquals("हिजो", Formatters.dateShort(today.minusDays(1), today))
        // 2026-10-01 minus two days is 2026-09-29.
        assertEquals("29 सेप्टेम्बर", Formatters.dateShort(today.minusDays(2), today))
    }

    @Test
    fun `full and month titles are Nepali`() {
        assertEquals("1 अक्टोबर 2026", Formatters.dateFull(LocalDate.of(2026, 10, 1)))
        assertEquals("अक्टोबर 2026", Formatters.monthTitle(YearMonth.of(2026, 10)))
        assertEquals("2026-10-01", Formatters.iso(LocalDate.of(2026, 10, 1)))
    }

    // -------------------------------------------------------------- CSV

    @Test
    fun `monthly csv contains every row and correct totals`() {
        val rows = listOf(
            record(LocalDate.of(2026, 10, 1), 3.0, 70.0, 210.00),
            record(LocalDate.of(2026, 10, 2), 2.5, 70.0, 175.00),
            record(LocalDate.of(2026, 10, 3), 3.25, 72.0, 234.00)
        )
        val csv = CsvExporter.exportMonth("अक्टोबर 2026", rows)

        assertTrue(csv.contains("Date,Quantity (L),Rate (Rs/L),Amount (Rs)"))
        assertTrue(csv.contains("2026-10-01,3.00,70.00,210.00"))
        assertTrue(csv.contains("2026-10-02,2.50,70.00,175.00"))
        assertTrue(csv.contains("2026-10-03,3.25,72.00,234.00"))
        assertTrue(csv.contains("Total Milk (L),8.75"))
        assertTrue(csv.contains("Total Amount (Rs),619.00"))
    }

    @Test
    fun `empty month csv still has header and zero totals`() {
        val csv = CsvExporter.exportMonth("जनवरी 2026", emptyList())
        assertTrue(csv.contains("Total Milk (L),0.00"))
        assertTrue(csv.contains("Total Amount (Rs),0.00"))
    }

    // ----------------------------------------------------------- backup

    @Test
    fun `backup round trip keeps every record`() {
        val original = listOf(
            record(LocalDate.of(2026, 10, 1), 3.0, 70.0, 210.00),
            record(LocalDate.of(2026, 10, 2), 2.5, 70.0, 175.00)
        )
        val json = BackupManager.exportToJson(original)
        val restored = BackupManager.importFromJson(json)

        assertEquals(2, restored.size)
        assertEquals(LocalDate.of(2026, 10, 1), restored[0].date)
        assertEquals(3.0, restored[0].quantity, 0.0)
        assertEquals(70.0, restored[0].rate, 0.0)
        assertEquals(210.00, restored[0].amount, 0.001)
    }

    @Test
    fun `amount inside backup is recalculated, never trusted`() {
        // A tampered file claims amount = 1 but 2 x 70 = 140.
        val json = """
            {"app":"MilkHisab","version":1,"records":[
              {"date":"2026-10-01","quantity":2,"rate":70,"amount":1}
            ]}
        """.trimIndent()
        val restored = BackupManager.importFromJson(json)
        assertEquals(140.0, restored[0].amount, 0.001)
    }

    @Test
    fun `malformed json is rejected without crashing`() {
        listOf(
            "not json at all",
            "",
            "{}",
            "{\"app\":\"SomeoneElse\",\"version\":1,\"records\":[]}",
            "{\"app\":\"MilkHisab\",\"version\":99,\"records\":[]}",
            "{\"app\":\"MilkHisab\",\"version\":1,\"records\":\"oops\"}"
        ).forEach { bad ->
            try {
                BackupManager.importFromJson(bad)
                fail("Expected BackupFormatException for: $bad")
            } catch (expected: BackupFormatException) {
                // friendly failure - caller shows a Nepali message
            }
        }
    }

    @Test
    fun `records with invalid values are rejected`() {
        val badDates = """
            {"app":"MilkHisab","version":1,"records":[
              {"date":"not-a-date","quantity":3,"rate":70}
            ]}
        """.trimIndent()
        val badQuantity = """
            {"app":"MilkHisab","version":1,"records":[
              {"date":"2026-10-01","quantity":-3,"rate":70}
            ]}
        """.trimIndent()
        val badRate = """
            {"app":"MilkHisab","version":1,"records":[
              {"date":"2026-10-01","quantity":3,"rate":"abc"}
            ]}
        """.trimIndent()
        listOf(badDates, badQuantity, badRate).forEach { bad ->
            try {
                BackupManager.importFromJson(bad)
                fail("Expected rejection for: $bad")
            } catch (expected: BackupFormatException) {
                // expected
            }
        }
    }

    @Test
    fun `duplicate dates inside a backup are collapsed to one`() {
        val json = """
            {"app":"MilkHisab","version":1,"records":[
              {"date":"2026-10-01","quantity":3,"rate":70},
              {"date":"2026-10-01","quantity":9,"rate":70}
            ]}
        """.trimIndent()
        val restored = BackupManager.importFromJson(json)
        assertEquals(1, restored.size)
        assertEquals(3.0, restored[0].quantity, 0.0)
    }
}
