package com.milkhisab.app.domain

import com.milkhisab.app.ui.strings.Strings

/**
 * Input validation for the add/edit form.
 *
 * Returns a localized error message, or null when the value is acceptable.
 * Pure Kotlin so it can be unit tested without Android.
 */
object Validators {

    /** Anything above this is a typo, not a real household milk amount. */
    const val MAX_QUANTITY = 1000.0

    /** Sanity cap for the rate per litre. */
    const val MAX_RATE = 100000.0

    /**
     * Parses user text into a number. Understands both Western digits
     * (3.5) and Nepali digits (३.५) plus comma/space separators.
     * Returns null when the text is not a usable number.
     */
    fun parseNumber(raw: String?): Double? {
        if (raw.isNullOrBlank()) return null
        var seenSign = false
        val normalized = buildString {
            for (ch in raw.trim()) {
                when {
                    ch in '0'..'9' -> append(ch)
                    ch in '०'..'९' -> append('0' + (ch - '०'))
                    ch == '.' -> append('.')
                    ch == '-' && !seenSign && isEmpty() -> {
                        // Leading minus: parse it so validation can say
                        // "must be greater than 0" instead of "not a number".
                        seenSign = true
                        append('-')
                    }
                    ch == '+' -> Unit
                    ch == ',' -> Unit          // thousands separator: ignore
                    ch == ' ' || ch == '\t' -> Unit
                    else -> return null         // letters etc. are invalid
                }
            }
        }
        if (normalized.isEmpty() || normalized == "-") return null
        val value = normalized.toDoubleOrNull() ?: return null
        if (!value.isFinite()) return null
        return value
    }

    /** null = valid, otherwise the message to show under the field. */
    fun validateQuantity(raw: String?, strings: Strings = Strings.NEPALI): String? {
        if (raw.isNullOrBlank()) return strings.errQuantityRequired
        val value = parseNumber(raw) ?: return strings.errInvalidNumber
        if (value <= 0.0) return strings.errQuantityMustBePositive
        if (value > MAX_QUANTITY) return strings.errQuantityTooLarge
        return null
    }

    fun validateRate(raw: String?, strings: Strings = Strings.NEPALI): String? {
        if (raw.isNullOrBlank()) return strings.errRateRequired
        val value = parseNumber(raw) ?: return strings.errInvalidNumber
        if (value <= 0.0) return strings.errRateMustBePositive
        if (value > MAX_RATE) return strings.errRateTooLarge
        return null
    }

    /** Validate both fields at once. Returns (quantityError, rateError). */
    fun validateForm(
        quantityRaw: String?,
        rateRaw: String?,
        strings: Strings = Strings.NEPALI
    ): Pair<String?, String?> = validateQuantity(quantityRaw, strings) to
        validateRate(rateRaw, strings)

    fun isValidQuantity(raw: String?, strings: Strings = Strings.NEPALI): Boolean =
        validateQuantity(raw, strings) == null

    fun isValidRate(raw: String?, strings: Strings = Strings.NEPALI): Boolean =
        validateRate(raw, strings) == null
}
