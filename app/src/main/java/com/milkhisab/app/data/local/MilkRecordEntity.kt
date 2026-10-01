package com.milkhisab.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * A single day's milk record.
 *
 * One record per date is enforced with a UNIQUE index on [date].
 * Numeric values are stored as real numbers (Double), never as text,
 * so totals can be summed correctly by SQLite.
 *
 * The [date] is persisted through [Converters] as an ISO-8601 string
 * (yyyy-MM-dd), which keeps lexicographic order equal to chronological
 * order and makes month-range queries simple and safe.
 */
@Entity(
    tableName = "milk_records",
    indices = [Index(value = ["date"], unique = true)]
)
data class MilkRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val date: LocalDate,

    /** Litres of milk, always > 0. */
    val quantity: Double,

    /** Rate per litre in rupees, always > 0. */
    val rate: Double,

    /** quantity * rate, always recomputed by the app (never user-entered). */
    val amount: Double,

    /** Epoch millis when the row was first created. */
    val createdAt: Long = System.currentTimeMillis(),

    /** Epoch millis when the row was last changed. */
    val updatedAt: Long = System.currentTimeMillis()
)
