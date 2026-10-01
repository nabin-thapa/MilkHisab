package com.milkhisab.app.domain

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Money and quantity math.
 *
 * Everything that matters for rupees goes through [BigDecimal] so that
 * 2.5 x 70 is exactly 175.00 and never 174.99999999999997.
 *
 * This object is pure Kotlin (no Android imports) so it can be unit tested
 * on the JVM.
 */
object MilkCalculator {

    private const val MONEY_SCALE = 2
    private const val LITRES_SCALE = 3

    /** amount = quantity x rate, rounded to 2 decimals (paisa precision). */
    fun calculateAmount(quantity: Double, rate: Double): Double {
        if (!quantity.isFinite() || !rate.isFinite()) return 0.0
        if (quantity <= 0.0 || rate <= 0.0) return 0.0
        return BigDecimal(quantity.toString())
            .multiply(BigDecimal(rate.toString()))
            .setScale(MONEY_SCALE, RoundingMode.HALF_UP)
            .toDouble()
    }

    /** Sum of amounts, done in BigDecimal to avoid float drift. */
    fun sumAmounts(amounts: List<Double>): Double {
        var total = BigDecimal.ZERO
        amounts.forEach { value ->
            if (value.isFinite()) {
                total = total.add(BigDecimal(value.toString()))
            }
        }
        return total.setScale(MONEY_SCALE, RoundingMode.HALF_UP).toDouble()
    }

    /** Sum of quantities (litres), rounded to 3 decimals. */
    fun sumQuantities(quantities: List<Double>): Double {
        var total = BigDecimal.ZERO
        quantities.forEach { value ->
            if (value.isFinite()) {
                total = total.add(BigDecimal(value.toString()))
            }
        }
        return total.setScale(LITRES_SCALE, RoundingMode.HALF_UP).toDouble()
    }

    /**
     * Average daily milk over the days that actually have a record.
     * Returns 0 when there are no recorded days (never divide by zero).
     */
    fun averageDailyMilk(totalMilk: Double, recordedDays: Int): Double {
        if (recordedDays <= 0 || !totalMilk.isFinite()) return 0.0
        if (totalMilk <= 0.0) return 0.0
        return BigDecimal(totalMilk.toString())
            .divide(BigDecimal(recordedDays), LITRES_SCALE, RoundingMode.HALF_UP)
            .toDouble()
    }
}
