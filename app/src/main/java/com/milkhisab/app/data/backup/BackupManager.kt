package com.milkhisab.app.data.backup

import com.milkhisab.app.data.local.MilkRecordEntity
import com.milkhisab.app.domain.MilkCalculator
import com.milkhisab.app.utils.Formatters
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

/**
 * Local JSON backup / restore.
 *
 * Format (version 1):
 * {
 *   "app": "MilkHisab",
 *   "version": 1,
 *   "exportedAt": 1790000000000,
 *   "records": [ { "date": "2026-10-01", "quantity": 3.0, "rate": 70.0 }, ... ]
 * }
 *
 * Imported data is validated field by field; a malformed file can never
 * crash the app or write nonsense rows into the database. The amount is
 * always recomputed (quantity x rate) - never trusted from the file.
 */
object BackupManager {

    private const val APP_MARKER = "MilkHisab"
    private const val VERSION = 1
    private const val MAX_RECORDS = 100_000

    /** Serialises all records to pretty JSON. */
    fun exportToJson(records: List<MilkRecordEntity>): String {
        val root = JSONObject()
        root.put("app", APP_MARKER)
        root.put("version", VERSION)
        root.put("exportedAt", System.currentTimeMillis())

        val array = JSONArray()
        records.sortedBy { it.date }.forEach { record ->
            val item = JSONObject()
            item.put("date", Formatters.iso(record.date))
            item.put("quantity", record.quantity)
            item.put("rate", record.rate)
            item.put("amount", record.amount)
            array.put(item)
        }
        root.put("records", array)
        return root.toString(2)
    }

    /**
     * Parses and validates a backup file.
     *
     * @throws BackupFormatException with a user-friendly (already Nepali,
     * see callers) reason when the content cannot be trusted.
     */
    @Throws(BackupFormatException::class)
    fun importFromJson(json: String): List<MilkRecordEntity> {
        val root = try {
            JSONObject(json)
        } catch (e: Exception) {
            throw BackupFormatException("not-json", e)
        }

        if (root.optString("app") != APP_MARKER) {
            throw BackupFormatException("wrong-app")
        }
        if (root.optInt("version", -1) != VERSION) {
            throw BackupFormatException("wrong-version")
        }

        val array = root.optJSONArray("records")
            ?: throw BackupFormatException("no-records")
        if (array.length() > MAX_RECORDS) {
            throw BackupFormatException("too-many")
        }

        val records = ArrayList<MilkRecordEntity>(array.length())
        val seenDates = HashSet<String>()
        val now = System.currentTimeMillis()

        for (i in 0 until array.length()) {
            val item = array.optJSONObject(i) ?: throw BackupFormatException("bad-item")

            val date = parseDate(item.optString("date"))
                ?: throw BackupFormatException("bad-date")
            if (!seenDates.add(date.toString())) continue // duplicate day: keep first

            val quantity = item.optDouble("quantity", Double.NaN)
            val rate = item.optDouble("rate", Double.NaN)
            if (!quantity.isFinite() || quantity <= 0.0 || quantity > 1000.0) {
                throw BackupFormatException("bad-quantity")
            }
            if (!rate.isFinite() || rate <= 0.0 || rate > 100000.0) {
                throw BackupFormatException("bad-rate")
            }

            records.add(
                MilkRecordEntity(
                    date = date,
                    quantity = quantity,
                    rate = rate,
                    amount = MilkCalculator.calculateAmount(quantity, rate),
                    createdAt = now,
                    updatedAt = now
                )
            )
        }

        if (records.isEmpty()) throw BackupFormatException("empty")
        return records
    }

    private fun parseDate(raw: String?): LocalDate? {
        if (raw.isNullOrBlank()) return null
        return try {
            LocalDate.parse(raw)
        } catch (e: Exception) {
            null
        }
    }
}

/** Signals that a backup file failed validation. */
class BackupFormatException(val reason: String, cause: Throwable? = null) :
    Exception("Backup format error: $reason", cause)
