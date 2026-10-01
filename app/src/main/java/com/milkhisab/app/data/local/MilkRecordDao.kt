package com.milkhisab.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Data access for milk records.
 *
 * All methods are main-thread safe: suspend functions and Flow are used so
 * nothing ever blocks the UI thread.
 */
@Dao
interface MilkRecordDao {

    // ---------------------------------------------------------------- inserts

    /** Inserts a new record. Throws (ABORT) when the date already exists. */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(record: MilkRecordEntity): Long

    /** Inserts or replaces records during backup restore (unique date match). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<MilkRecordEntity>)

    // ---------------------------------------------------------------- updates

    @Update
    suspend fun update(record: MilkRecordEntity)

    @Delete
    suspend fun delete(record: MilkRecordEntity)

    @Query("DELETE FROM milk_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    // ---------------------------------------------------------------- reads

    @Query("SELECT * FROM milk_records WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): MilkRecordEntity?

    @Query("SELECT * FROM milk_records WHERE date = :date LIMIT 1")
    suspend fun getByDate(date: LocalDate): MilkRecordEntity?

    @Query("SELECT * FROM milk_records WHERE date = :date LIMIT 1")
    fun observeByDate(date: LocalDate): Flow<MilkRecordEntity?>

    /** All records, newest date first. */
    @Query("SELECT * FROM milk_records ORDER BY date DESC, id DESC")
    fun observeAll(): Flow<List<MilkRecordEntity>>

    /** Records inside [start, end), oldest first - used for month summaries. */
    @Query(
        "SELECT * FROM milk_records WHERE date >= :start AND date < :end " +
            "ORDER BY date ASC"
    )
    fun observeBetween(start: LocalDate, end: LocalDate): Flow<List<MilkRecordEntity>>

    /** Same range as a one-shot list, used for CSV export. */
    @Query(
        "SELECT * FROM milk_records WHERE date >= :start AND date < :end " +
            "ORDER BY date ASC"
    )
    suspend fun getBetween(start: LocalDate, end: LocalDate): List<MilkRecordEntity>

    /** The most recently used rate (latest recorded date), for prefill. */
    @Query("SELECT rate FROM milk_records ORDER BY date DESC, id DESC LIMIT 1")
    suspend fun getLatestRate(): Double?

    @Query("SELECT * FROM milk_records ORDER BY date ASC")
    suspend fun getAllOnce(): List<MilkRecordEntity>

    @Query("SELECT COUNT(*) FROM milk_records")
    suspend fun count(): Int

    // ------------------------------------------------------------- aggregates

    @Query(
        "SELECT COALESCE(SUM(quantity), 0) FROM milk_records " +
            "WHERE date >= :start AND date < :end"
    )
    suspend fun sumQuantityBetween(start: LocalDate, end: LocalDate): Double

    @Query(
        "SELECT COALESCE(SUM(amount), 0) FROM milk_records " +
            "WHERE date >= :start AND date < :end"
    )
    suspend fun sumAmountBetween(start: LocalDate, end: LocalDate): Double

    @Query(
        "SELECT COUNT(*) FROM milk_records " +
            "WHERE date >= :start AND date < :end"
    )
    suspend fun countBetween(start: LocalDate, end: LocalDate): Int

    /** Flow variants so the UI updates automatically after any change. */
    @Query(
        "SELECT COALESCE(SUM(quantity), 0) FROM milk_records " +
            "WHERE date >= :start AND date < :end"
    )
    fun observeSumQuantityBetween(start: LocalDate, end: LocalDate): Flow<Double>

    @Query(
        "SELECT COALESCE(SUM(amount), 0) FROM milk_records " +
            "WHERE date >= :start AND date < :end"
    )
    fun observeSumAmountBetween(start: LocalDate, end: LocalDate): Flow<Double>

    @Query(
        "SELECT COUNT(*) FROM milk_records " +
            "WHERE date >= :start AND date < :end"
    )
    fun observeCountBetween(start: LocalDate, end: LocalDate): Flow<Int>

    // ------------------------------------------------------------ maintenance

    /** Used by restore: clear everything (called inside a transaction). */
    @Query("DELETE FROM milk_records")
    suspend fun deleteAll()
}
