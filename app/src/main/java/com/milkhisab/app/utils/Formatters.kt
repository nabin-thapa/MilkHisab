package com.milkhisab.app.utils

import com.milkhisab.app.ui.strings.Strings
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Formatting helpers: money, litres and dates.
 *
 * Pure Kotlin (no Android) so every format rule stays unit testable.
 *
 * Every method takes the active [Strings] so the same number can be shown
 * in Nepali or English. The default is the Nepali catalogue, which keeps
 * every existing caller (and the existing tests) working unchanged.
 */
object Formatters {

    private val isoDate: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val locale = Locale.US

    // ----------------------------------------------------------------- money

    /**
     * "Rs. 210", "Rs. 6,125", "Rs. 1,00,000" (Nepali/Indian lakh grouping),
     * "Rs. 175.50" when there is a real fraction.
     */
    fun money(value: Double, strings: Strings = Strings.NEPALI): String {
        if (!value.isFinite()) return "${strings.currencyPrefix} 0"
        val rounded = BigDecimal(value.toString()).setScale(2, RoundingMode.HALF_UP)
        val isWhole = rounded.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0
        val body = if (isWhole) rounded.toBigInteger().toString() else rounded.toPlainString()
        return "${strings.currencyPrefix} ${grouped(body)}"
    }

    /** "70", "72.5" - the bare rate, used inside expressions. */
    fun ratePlain(value: Double): String {
        if (!value.isFinite()) return "0"
        val rounded = BigDecimal(value.toString())
            .setScale(2, RoundingMode.HALF_UP)
            .stripTrailingZeros()
        return if (rounded.scale() <= 0) {
            rounded.toBigInteger().toString()
        } else {
            rounded.toPlainString()
        }
    }

    /** "Rs.70 / L" - the compact rate line on cards. */
    fun ratePerLitre(rate: Double, strings: Strings = Strings.NEPALI): String =
        "${strings.currencyPrefix}${ratePlain(rate)} / ${strings.litreShort}"

    // --------------------------------------------------------------- litres

    /**
     * "3.0", "87.5", "2.92" - always at least one decimal so parents can
     * see at a glance that it is litres, not rupees.
     */
    fun litres(value: Double): String {
        if (!value.isFinite()) return "0.0"
        val rounded = BigDecimal(value.toString())
            .setScale(2, RoundingMode.HALF_UP)
            .stripTrailingZeros()
        val plain = if (rounded.scale() <= 0) {
            rounded.toBigInteger().toString()
        } else {
            rounded.toPlainString()
        }
        return if (plain.contains('.')) plain else "$plain.0"
    }

    /** "3.0 L" - the short form used on cards. */
    fun litresWithUnit(value: Double, strings: Strings = Strings.NEPALI): String =
        "${litres(value)} ${strings.litreShort}"

    /**
     * "3.0 L × Rs.70" - the single expression that explains how the
     * amount was reached, reused on every screen so it never varies.
     */
    fun quantityTimesRate(
        quantity: Double,
        rate: Double,
        strings: Strings = Strings.NEPALI
    ): String = "${litresWithUnit(quantity, strings)} × ${strings.currencyPrefix}${ratePlain(rate)}"

    /** "30 दिन" / "30 days" - value plus its unit. */
    fun withUnit(value: Int, unit: String): String = "$value $unit"

    /**
     * "1 day" / "2 days" / "1 दिन" - the day count picks the right form,
     * which English needs and Nepali does not.
     */
    fun dayCount(count: Int, strings: Strings = Strings.NEPALI): String {
        val unit = if (count == 1) strings.daysUnitSingular else strings.daysUnit
        return "$count $unit"
    }

    // ---------------------------------------------------------------- dates

    /** "2026-10-01" - stable, sortable, used inside files. */
    fun iso(date: LocalDate): String = date.format(isoDate)

    /** "1 अक्टोबर 2026" / "1 October 2026" - the Home screen header. */
    fun dateFull(date: LocalDate, strings: Strings = Strings.NEPALI): String {
        val month = strings.months[date.monthValue - 1]
        return "${date.dayOfMonth} $month ${date.year}"
    }

    /** "01 अक्टोबर" / "आज" / "हिजो" - compact label used in lists. */
    fun dateShort(
        date: LocalDate,
        today: LocalDate,
        strings: Strings = Strings.NEPALI
    ): String = when (date) {
        today -> strings.today
        today.minusDays(1) -> strings.yesterday
        else -> {
            val month = strings.months[date.monthValue - 1]
            val day = String.format(locale, "%02d", date.dayOfMonth)
            "$day $month"
        }
    }

    /** "01 अक्टोबर" - always numeric (used inside the month summary). */
    fun dateDayMonth(date: LocalDate, strings: Strings = Strings.NEPALI): String {
        val month = strings.months[date.monthValue - 1]
        val day = String.format(locale, "%02d", date.dayOfMonth)
        return "$day $month"
    }

    /** "अक्टोबर 2026" / "October 2026" - month heading. */
    fun monthTitle(month: YearMonth, strings: Strings = Strings.NEPALI): String {
        val name = strings.months[month.monthValue - 1]
        return "$name ${month.year}"
    }

    /** "आइतबार, 1 अक्टोबर 2026" - friendly weekday line. */
    fun dateWithWeekday(date: LocalDate, strings: Strings = Strings.NEPALI): String {
        val weekday = strings.weekdays[date.dayOfWeek.value % 7]
        return "$weekday, ${dateFull(date, strings)}"
    }

    /** "शुभ प्रभात" / "Good morning" - a small, warm touch on the Home screen. */
    fun greeting(time: LocalTime, strings: Strings = Strings.NEPALI): String = when {
        time.hour < 12 -> strings.greetingMorning
        time.hour < 17 -> strings.greetingAfternoon
        else -> strings.greetingEvening
    }

    // ------------------------------------------------------------ internals

    /**
     * South-Asian (lakh/crore) grouping as used in Nepal and India:
     * 6125 -> "6,125", 100000 -> "1,00,000".
     */
    private fun grouped(plain: String): String {
        val negative = plain.startsWith("-")
        val body = plain.removePrefix("-")
        val dot = body.indexOf('.')
        val intPart = if (dot >= 0) body.substring(0, dot) else body
        val fraction = if (dot >= 0) body.substring(dot) else ""

        val groupedInt = when {
            intPart.length <= 3 -> intPart
            else -> {
                val last3 = intPart.takeLast(3)
                val rest = intPart.dropLast(3)
                val head = StringBuilder()
                var i = rest.length
                while (i > 0) {
                    val start = if (i - 2 > 0) i - 2 else 0
                    head.insert(0, rest.substring(start, i))
                    i = start
                    if (i > 0) head.insert(0, ',')
                }
                head.append(',').append(last3).toString()
            }
        }
        return (if (negative) "-" else "") + groupedInt + fraction
    }
}
