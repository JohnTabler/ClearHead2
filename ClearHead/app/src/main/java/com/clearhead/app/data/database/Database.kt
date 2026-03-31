package com.clearhead.app.data.database

import android.content.Context
import androidx.room.*
import com.clearhead.app.data.models.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

// ──────────────────────────────────────────────
// DAOs
// ──────────────────────────────────────────────

@Dao
interface DailyLogDao {

    @Query("SELECT * FROM daily_logs ORDER BY date DESC")
    fun getAllLogs(): Flow<List<DailyLog>>

    @Query("SELECT * FROM daily_logs WHERE date = :date LIMIT 1")
    suspend fun getLogByDate(date: String): DailyLog?

    @Query("SELECT * FROM daily_logs WHERE date BETWEEN :start AND :end ORDER BY date DESC")
    fun getLogsBetween(start: String, end: String): Flow<List<DailyLog>>

    @Query("SELECT * FROM daily_logs WHERE hasMigraine = 1 ORDER BY date DESC")
    fun getMigraineDays(): Flow<List<DailyLog>>

    @Query("SELECT COUNT(*) FROM daily_logs WHERE hasMigraine = 1 AND date BETWEEN :start AND :end")
    suspend fun countMigraineDays(start: String, end: String): Int

    @Query("SELECT * FROM daily_logs ORDER BY date DESC LIMIT :limit")
    fun getRecentLogs(limit: Int): Flow<List<DailyLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: DailyLog): Long

    @Update
    suspend fun updateLog(log: DailyLog)

    @Delete
    suspend fun deleteLog(log: DailyLog)

    @Query("DELETE FROM daily_logs WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM daily_logs WHERE date >= :startDate ORDER BY date ASC")
    suspend fun getLogsFromDate(startDate: String): List<DailyLog>

    @Query("SELECT * FROM daily_logs ORDER BY date ASC")
    suspend fun getAllLogsSync(): List<DailyLog>
}

@Dao
interface MigraineEventDao {

    @Query("SELECT * FROM migraine_events ORDER BY startTime DESC")
    fun getAllEvents(): Flow<List<MigraineEvent>>

    @Query("SELECT * FROM migraine_events WHERE date = :date")
    suspend fun getEventsForDate(date: String): List<MigraineEvent>

    @Query("SELECT * FROM migraine_events WHERE date BETWEEN :start AND :end ORDER BY startTime DESC")
    fun getEventsBetween(start: String, end: String): Flow<List<MigraineEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: MigraineEvent): Long

    @Update
    suspend fun updateEvent(event: MigraineEvent)

    @Delete
    suspend fun deleteEvent(event: MigraineEvent)

    @Query("SELECT * FROM migraine_events ORDER BY startTime ASC")
    suspend fun getAllEventsSync(): List<MigraineEvent>
}

// ──────────────────────────────────────────────
// Database
// ──────────────────────────────────────────────

@Database(
    entities = [DailyLog::class, MigraineEvent::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class, EmbeddedConverters::class)
abstract class ClearHeadDatabase : RoomDatabase() {

    abstract fun dailyLogDao(): DailyLogDao
    abstract fun migraineEventDao(): MigraineEventDao

    companion object {
        @Volatile private var INSTANCE: ClearHeadDatabase? = null

        fun getInstance(context: Context): ClearHeadDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    ClearHeadDatabase::class.java,
                    "clearhead_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
