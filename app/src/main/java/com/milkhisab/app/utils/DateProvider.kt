package com.milkhisab.app.utils

import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

/**
 * Central place to decide "what day is it".
 *
 * The family lives in Nepal, so every daily record uses the device clock
 * interpreted in Asia/Kathmandu. That stops records from sliding to the
 * wrong day when the phone timezone or UTC offset changes.
 *
 * Injectable so unit tests can pin a fixed date.
 */
class DateProvider(
    private val zone: ZoneId = ZoneId.of("Asia/Kathmandu"),
    private val clock: java.time.Clock = java.time.Clock.system(zone)
) {
    fun today(): LocalDate = LocalDate.now(clock)

    fun monthNow(): YearMonth = YearMonth.from(today())
}
