package com.clearhead.app.data.repository

import com.clearhead.app.data.database.ClearHeadDatabase
import com.clearhead.app.data.models.DailyLog
import com.clearhead.app.data.models.MigraineEvent
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class ClearHeadRepository(private val db: ClearHeadDatabase) {

    // ── Daily Logs ──────────────────────────────────

    fun getAllLogs(): Flow<List<DailyLog>> = db.dailyLogDao().getAllLogs()

    fun getRecentLogs(limit: Int = 30): Flow<List<DailyLog>> =
        db.dailyLogDao().getRecentLogs(limit)

    suspend fun getLogByDate(date: LocalDate): DailyLog? =
        db.dailyLogDao().getLogByDate(date.toString())

    fun getLogsBetween(start: LocalDate, end: LocalDate): Flow<List<DailyLog>> =
        db.dailyLogDao().getLogsBetween(start.toString(), end.toString())

    fun getMigraineDays(): Flow<List<DailyLog>> = db.dailyLogDao().getMigraineDays()

    suspend fun countMigraineDays(start: LocalDate, end: LocalDate): Int =
        db.dailyLogDao().countMigraineDays(start.toString(), end.toString())

    suspend fun saveLog(log: DailyLog): Long = db.dailyLogDao().insertLog(log)

    suspend fun updateLog(log: DailyLog) = db.dailyLogDao().updateLog(log)

    suspend fun deleteLog(log: DailyLog) = db.dailyLogDao().deleteLog(log)

    suspend fun getAllLogsForExport(): List<DailyLog> = db.dailyLogDao().getAllLogsSync()

    // ── Migraine Events ─────────────────────────────

    fun getAllMigraineEvents(): Flow<List<MigraineEvent>> =
        db.migraineEventDao().getAllEvents()

    fun getMigraineEventsBetween(start: LocalDate, end: LocalDate): Flow<List<MigraineEvent>> =
        db.migraineEventDao().getEventsBetween(start.toString(), end.toString())

    suspend fun saveMigraineEvent(event: MigraineEvent): Long =
        db.migraineEventDao().insertEvent(event)

    suspend fun updateMigraineEvent(event: MigraineEvent) =
        db.migraineEventDao().updateEvent(event)

    suspend fun deleteMigraineEvent(event: MigraineEvent) =
        db.migraineEventDao().deleteEvent(event)

    suspend fun getAllMigraineEventsForExport(): List<MigraineEvent> =
        db.migraineEventDao().getAllEventsSync()
}
