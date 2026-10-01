package com.milkhisab.app

import com.milkhisab.app.domain.MilkCalculator
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Business-logic tests from the spec:
 *   3 x 70   = 210
 *   2.5 x 70 = 175
 *   3.25 x 72 = 234
 *   month    = 619 rupees, 8.75 litres
 */
class MilkCalculatorTest {

    // ------------------------------------------------------------ amounts

    @Test
    fun `3 liters at 70 equals 210`() {
        assertEquals(210.0, MilkCalculator.calculateAmount(3.0, 70.0), 0.001)
    }

    @Test
    fun `2_5 liters at 70 equals 175`() {
        assertEquals(175.0, MilkCalculator.calculateAmount(2.5, 70.0), 0.001)
    }

    @Test
    fun `3_25 liters at 72 equals 234`() {
        assertEquals(234.0, MilkCalculator.calculateAmount(3.25, 72.0), 0.001)
    }

    @Test
    fun `repeating decimals stay money-safe`() {
        // 0.1 x 3 would be 0.30000000000000004 with plain doubles.
        assertEquals(0.30, MilkCalculator.calculateAmount(0.1, 3.0), 0.0)
        assertEquals(1.10, MilkCalculator.calculateAmount(1.1, 1.0), 0.0)
    }

    @Test
    fun `zero or negative inputs produce zero amount`() {
        assertEquals(0.0, MilkCalculator.calculateAmount(0.0, 70.0), 0.0)
        assertEquals(0.0, MilkCalculator.calculateAmount(-2.0, 70.0), 0.0)
        assertEquals(0.0, MilkCalculator.calculateAmount(3.0, -70.0), 0.0)
    }

    // -------------------------------------------------------------- sums

    @Test
    fun `monthly amount sums each record's own amount`() {
        // Mixed rates in one month: 210 + 175 + 234 = 619
        val month = listOf(
            MilkCalculator.calculateAmount(3.0, 70.0),
            MilkCalculator.calculateAmount(2.5, 70.0),
            MilkCalculator.calculateAmount(3.25, 72.0)
        )
        assertEquals(619.0, MilkCalculator.sumAmounts(month), 0.001)
    }

    @Test
    fun `monthly milk sums quantities`() {
        assertEquals(8.75, MilkCalculator.sumQuantities(listOf(3.0, 2.5, 3.25)), 0.001)
    }

    @Test
    fun `empty month totals are zero`() {
        assertEquals(0.0, MilkCalculator.sumAmounts(emptyList()), 0.0)
        assertEquals(0.0, MilkCalculator.sumQuantities(emptyList()), 0.0)
    }

    @Test
    fun `long month of small amounts does not drift`() {
        // 30 days of 3.25 L at 70 = 6825.00 exactly.
        val amounts = List(30) { MilkCalculator.calculateAmount(3.25, 70.0) }
        assertEquals(6825.0, MilkCalculator.sumAmounts(amounts), 0.0)
    }

    // ----------------------------------------------------------- averages

    @Test
    fun `daily average divides by recorded days, not calendar days`() {
        // 8.75 L over 3 recorded days = 2.917 (rounded to 3 decimals)
        assertEquals(2.917, MilkCalculator.averageDailyMilk(8.75, 3), 0.001)
    }

    @Test
    fun `average of 87_5 over 30 days is 2_917`() {
        assertEquals(2.917, MilkCalculator.averageDailyMilk(87.5, 30), 0.001)
    }

    @Test
    fun `average with no recorded days is zero`() {
        assertEquals(0.0, MilkCalculator.averageDailyMilk(87.5, 0), 0.0)
    }
}
