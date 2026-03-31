package com.clearhead.app.data.repository

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.clearhead.app.data.models.DailyLog
import com.clearhead.app.data.models.MigraineEvent
import java.io.File
import java.io.FileWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object CsvExporter {

    private val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    fun exportDailyLogs(context: Context, logs: List<DailyLog>): Intent {
        val fileName = "clearhead_daily_logs_${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"))}.csv"
        val file = File(context.filesDir.resolve("exports").also { it.mkdirs() }, fileName)

        FileWriter(file).use { writer ->
            // Header
            writer.appendLine(
                "Date,Has Migraine,Sleep Hours,Sleep Quality,Deep Sleep Min,Light Sleep Min," +
                "REM Min,Awake Min,Heart Rate Avg,Heart Rate Min,Heart Rate Max,HRV (ms)," +
                "Steps,Active Calories,Exercise Min,Cycle Day,Cycle Phase,Period Flow," +
                "Water (oz),Caffeine (mg),Alcohol Drinks,Stress Level,Screen Time (hrs)," +
                "Weather,Barometric Pressure,Food Entries,Food Trigger Tags,Medications,Notes"
            )

            for (log in logs.sortedBy { it.date }) {
                val foodNames = log.foodEntries.joinToString("; ") { it.name }
                val foodTags = log.foodEntries.flatMap { it.tags }.distinct().joinToString("; ")
                val meds = log.medications.joinToString("; ") {
                    "${it.name}${if (it.doseMg != null) " ${it.doseMg}mg" else ""} @ ${it.time}"
                }

                writer.appendLine(
                    listOf(
                        log.date.toString(),
                        log.hasMigraine.toString(),
                        log.sleepHours?.toString() ?: "",
                        log.sleepQuality?.toString() ?: "",
                        log.sleepStages?.deepMinutes?.toString() ?: "",
                        log.sleepStages?.lightMinutes?.toString() ?: "",
                        log.sleepStages?.remMinutes?.toString() ?: "",
                        log.sleepStages?.awakeMinutes?.toString() ?: "",
                        log.heartRateAvg?.toString() ?: "",
                        log.heartRateMin?.toString() ?: "",
                        log.heartRateMax?.toString() ?: "",
                        log.hrv?.toString() ?: "",
                        log.steps?.toString() ?: "",
                        log.activeCalories?.toString() ?: "",
                        log.exerciseMinutes?.toString() ?: "",
                        log.cycleDay?.toString() ?: "",
                        log.cyclePhase ?: "",
                        log.periodFlow ?: "",
                        log.waterOz?.toString() ?: "",
                        log.caffeinesMg?.toString() ?: "",
                        log.alcoholDrinks?.toString() ?: "",
                        log.stressLevel?.toString() ?: "",
                        log.screenTimeHours?.toString() ?: "",
                        log.weatherCondition ?: "",
                        log.barometricPressure?.toString() ?: "",
                        csvEscape(foodNames),
                        csvEscape(foodTags),
                        csvEscape(meds),
                        csvEscape(log.notes)
                    ).joinToString(",")
                )
            }
        }

        return buildShareIntent(context, file, "text/csv")
    }

    fun exportMigraineEvents(context: Context, events: List<MigraineEvent>): Intent {
        val fileName = "clearhead_migraine_events_${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"))}.csv"
        val file = File(context.filesDir.resolve("exports").also { it.mkdirs() }, fileName)

        FileWriter(file).use { writer ->
            writer.appendLine("Date,Start Time,End Time,Duration (hrs),Pain Level (1-10),Location,Symptoms,Notes")

            for (event in events.sortedBy { it.startTime }) {
                val duration = if (event.endTime != null) {
                    val mins = java.time.Duration.between(event.startTime, event.endTime).toMinutes()
                    String.format("%.1f", mins / 60f)
                } else ""

                writer.appendLine(
                    listOf(
                        event.date.toString(),
                        event.startTime.format(fmt),
                        event.endTime?.format(fmt) ?: "",
                        duration,
                        event.painLevel.toString(),
                        csvEscape(event.location),
                        csvEscape(event.symptoms.joinToString("; ")),
                        csvEscape(event.notes)
                    ).joinToString(",")
                )
            }
        }

        return buildShareIntent(context, file, "text/csv")
    }

    private fun csvEscape(value: String): String {
        return if (value.contains(',') || value.contains('"') || value.contains('\n')) {
            "\"${value.replace("\"", "\"\"")}\""
        } else value
    }

    private fun buildShareIntent(context: Context, file: File, mimeType: String): Intent {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        return Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
