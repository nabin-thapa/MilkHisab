package com.milkhisab.app.data.backup

import com.milkhisab.app.data.local.MilkRecordEntity
import com.milkhisab.app.domain.MilkCalculator
import com.milkhisab.app.utils.Formatters

/**
 * Builds a plain-text monthly report (CSV, UTF-8) for one month.
 *
 * Example:
 *   Milk Report,अक्टोबर 2026
 *   Date,Quantity (L),Rate (Rs/L),Amount (Rs)
 *   2026-10-01,3.0,70,210.00
 *   ...
 *   Total Milk (L),87.50
 *   Total Amount (Rs),6125.00
 *
 * Pure Kotlin so it can be unit tested.
 */
object CsvExporter {

    fun exportMonth(monthTitle: String, records: List<MilkRecordEntity>): String {
        val sb = StringBuilder()
        sb.append("Milk Report,")
        sb.append(escape(monthTitle)).append('\n')
        sb.append("Date,Quantity (L),Rate (Rs/L),Amount (Rs)\n")

        records.sortedBy { it.date }.forEach { r ->
            sb.append(Formatters.iso(r.date)).append(',')
            sb.append(formatDecimal(r.quantity)).append(',')
            sb.append(formatDecimal(r.rate)).append(',')
            sb.append(formatDecimal(r.amount)).append('\n')
        }

        val totalMilk = MilkCalculator.sumQuantities(records.map { it.quantity })
        val totalAmount = MilkCalculator.sumAmounts(records.map { it.amount })

        sb.append("Total Milk (L),").append(formatDecimal(totalMilk)).append('\n')
        sb.append("Total Amount (Rs),").append(formatDecimal(totalAmount)).append('\n')
        return sb.toString()
    }

    private fun formatDecimal(value: Double): String {
        val v = if (value.isFinite()) value else 0.0
        return String.format(java.util.Locale.US, "%.2f", v)
    }

    private fun escape(field: String): String =
        if (field.contains(',') || field.contains('"')) {
            "\"" + field.replace("\"", "\"\"") + "\""
        } else field
}
