package com.milkhisab.app

import com.milkhisab.app.domain.Validators
import com.milkhisab.app.ui.strings.Strings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Validation rules from the spec: empty / zero / negative / garbage input
 * must be rejected with a Nepali message, sensible decimals accepted.
 */
class ValidatorsTest {

    // ---------------------------------------------------------- quantity

    @Test
    fun `empty quantity is rejected`() {
        assertEquals(Strings.errQuantityRequired, Validators.validateQuantity(""))
        assertEquals(Strings.errQuantityRequired, Validators.validateQuantity("   "))
        assertEquals(Strings.errQuantityRequired, Validators.validateQuantity(null))
    }

    @Test
    fun `zero quantity is rejected`() {
        assertEquals(Strings.errQuantityMustBePositive, Validators.validateQuantity("0"))
        assertEquals(Strings.errQuantityMustBePositive, Validators.validateQuantity("0.0"))
    }

    @Test
    fun `negative quantity is rejected`() {
        assertEquals(Strings.errQuantityMustBePositive, Validators.validateQuantity("-2"))
    }

    @Test
    fun `letters in quantity are rejected`() {
        assertEquals(Strings.errInvalidNumber, Validators.validateQuantity("abc"))
        assertEquals(Strings.errInvalidNumber, Validators.validateQuantity("3L"))
    }

    @Test
    fun `absurdly large quantity is rejected`() {
        assertNotNull(Validators.validateQuantity("5000"))
    }

    @Test
    fun `decimal quantities are accepted`() {
        listOf("1", "1.5", "2", "2.5", "3", "3.25", "3.5", "4").forEach {
            assertNull("expected valid: $it", Validators.validateQuantity(it))
        }
    }

    @Test
    fun `nepali digits are accepted`() {
        assertNull(Validators.validateQuantity("३.५"))
        assertEquals(3.5, Validators.parseNumber("३.५")!!, 0.0001)
    }

    // -------------------------------------------------------------- rate

    @Test
    fun `empty rate is rejected`() {
        assertEquals(Strings.errRateRequired, Validators.validateRate(""))
        assertEquals(Strings.errRateRequired, Validators.validateRate(null))
    }

    @Test
    fun `zero rate is rejected`() {
        assertEquals(Strings.errRateMustBePositive, Validators.validateRate("0"))
    }

    @Test
    fun `negative rate is rejected`() {
        assertEquals(Strings.errRateMustBePositive, Validators.validateRate("-70"))
    }

    @Test
    fun `reasonable rates are accepted`() {
        assertNull(Validators.validateRate("70"))
        assertNull(Validators.validateRate("72.5"))
        assertNull(Validators.validateRate("1,200"))
    }

    // ------------------------------------------------------------ parsing

    @Test
    fun `parseNumber handles blank and junk`() {
        assertNull(Validators.parseNumber(null))
        assertNull(Validators.parseNumber(""))
        assertNull(Validators.parseNumber("  "))
        assertNull(Validators.parseNumber(".."))
        assertNull(Validators.parseNumber("12a"))
    }

    @Test
    fun `form validation reports both errors at once`() {
        val (qError, rError) = Validators.validateForm("", "")
        assertNotNull(qError)
        assertNotNull(rError)

        val (okQ, okR) = Validators.validateForm("3", "70")
        assertNull(okQ)
        assertNull(okR)
    }

    @Test
    fun `validity helpers agree with messages`() {
        assertEquals(Validators.validateQuantity("3") == null, Validators.isValidQuantity("3"))
        assertEquals(Validators.validateRate("70") == null, Validators.isValidRate("70"))
        assertEquals(false, Validators.isValidQuantity("-1"))
        assertEquals(false, Validators.isValidRate("0"))
    }
}
