package com.milkhisab.app.data.local

import androidx.room.TypeConverter
import java.time.LocalDate

/**
 * Room type converters.
 *
 * Dates are stored as ISO-8601 text ("yyyy-MM-dd"). This keeps the date in
 * the device's local calendar day (no timezone shifting), allows a UNIQUE
 * index per date, and makes month range queries a simple string comparison.
 */
class Converters {

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? = date?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }
}
