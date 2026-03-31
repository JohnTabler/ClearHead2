package com.clearhead.app.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.clearhead.app.data.database.ClearHeadDatabase
import com.clearhead.app.data.models.*
import com.clearhead.app.data.repository.ClearHeadRepository
import com.clearhead.app.data.repository.CsvExporter
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

class ClearHeadViewModel(application: Application) : AndroidViewModel(application) {

    private val db = ClearHeadDatabase.getInstance(application)
    private val repo = ClearHeadRepository(db)

    // ── State ───────────────────────────────────────

    val allLogs: StateFlow<List<DailyLog>> = repo.getAllLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentLogs: StateFlow<List<DailyLog>> = repo.getRecentLogs(90)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val migraineDays: StateFlow<List<DailyLog>> = repo.getMigraineDays()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMigraineEvents: StateFlow<List<MigraineEvent>> = repo.getAllMigraineEvents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _currentLog = MutableStateFlow<DailyLog?>(null)
    val currentLog: StateFlow<DailyLog?> = _currentLog.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    init {
        loadLogForDate(LocalDate.now())
    }

    // ── Date selection ──────────────────────────────

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        loadLogForDate(date)
    }

    fun loadLogForDate(date: LocalDate) {
        viewModelScope.launch {
            _currentLog.value = repo.getLogByDate(date)
        }
    }

    // ── Save / Update ───────────────────────────────

    fun saveLog(log: DailyLog) {
        viewModelScope.launch {
            repo.saveLog(log)
            _currentLog.value = log
            _saveSuccess.value = true
        }
    }

    fun clearSaveSuccess() { _saveSuccess.value = false }

    fun deleteLog(log: DailyLog) {
        viewModelScope.launch {
            repo.deleteLog(log)
            if (_selectedDate.value == log.date) _currentLog.value = null
        }
    }

    // ── Migraine Events ─────────────────────────────

    fun saveMigraineEvent(event: MigraineEvent) {
        viewModelScope.launch { repo.saveMigraineEvent(event) }
    }

    fun updateMigraineEvent(event: MigraineEvent) {
        viewModelScope.launch { repo.updateMigraineEvent(event) }
    }

    fun deleteMigraineEvent(event: MigraineEvent) {
        viewModelScope.launch { repo.deleteMigraineEvent(event) }
    }

    // ── Analytics ───────────────────────────────────

    fun getInsights(logs: List<DailyLog>): List<Insight> {
        val insights = mutableListOf<Insight>()
        val migraineLogs = logs.filter { it.hasMigraine }
        if (migraineLogs.isEmpty()) return insights

        // Sleep analysis
        val migraineSleep = migraineLogs.mapNotNull { it.sleepHours }.average()
        val normalSleep = logs.filter { !it.hasMigraine }.mapNotNull { it.sleepHours }.average()
        if (!migraineSleep.isNaN() && !normalSleep.isNaN()) {
            val diff = normalSleep - migraineSleep
            if (diff > 0.5) insights.add(Insight(
                "💤 Sleep Pattern",
                "On migraine days you sleep ${String.format("%.1f", diff)}h less than usual (${String.format("%.1f", migraineSleep)}h vs ${String.format("%.1f", normalSleep)}h avg).",
                InsightSeverity.WARNING
            ))
        }

        // Caffeine
        val migraineCaff = migraineLogs.mapNotNull { it.caffeinesMg }.average()
        val normalCaff = logs.filter { !it.hasMigraine }.mapNotNull { it.caffeinesMg }.average()
        if (!migraineCaff.isNaN() && !normalCaff.isNaN() && migraineCaff > normalCaff * 1.3) {
            insights.add(Insight(
                "☕ Caffeine",
                "Caffeine intake is ${String.format("%.0f", migraineCaff)}mg on migraine days vs ${String.format("%.0f", normalCaff)}mg normally.",
                InsightSeverity.WARNING
            ))
        }

        // Stress
        val migraineStress = migraineLogs.mapNotNull { it.stressLevel?.toDouble() }.average()
        val normalStress = logs.filter { !it.hasMigraine }.mapNotNull { it.stressLevel?.toDouble() }.average()
        if (!migraineStress.isNaN() && !normalStress.isNaN() && migraineStress > normalStress + 0.8) {
            insights.add(Insight(
                "😰 Stress Level",
                "Stress is notably higher on migraine days (${String.format("%.1f", migraineStress)}/5 vs ${String.format("%.1f", normalStress)}/5).",
                InsightSeverity.WARNING
            ))
        }

        // Top food triggers
        val allFoodTags = migraineLogs.flatMap { log ->
            log.foodEntries.flatMap { it.tags }
        }
        val topTriggers = allFoodTags.groupingBy { it }.eachCount()
            .entries.sortedByDescending { it.value }.take(3)
        if (topTriggers.isNotEmpty()) {
            insights.add(Insight(
                "🍽️ Food Triggers",
                "Most common foods before migraines: ${topTriggers.joinToString(", ") { "${it.key} (${it.value}x)" }}.",
                InsightSeverity.INFO
            ))
        }

        // Cycle phase correlation
        val cyclePhases = migraineLogs.mapNotNull { it.cyclePhase }
        if (cyclePhases.isNotEmpty()) {
            val topPhase = cyclePhases.groupingBy { it }.eachCount().maxByOrNull { it.value }
            if (topPhase != null && topPhase.value >= 2) {
                insights.add(Insight(
                    "🔄 Cycle Pattern",
                    "${topPhase.value} of your recent migraines occurred during the ${topPhase.key} phase.",
                    InsightSeverity.INFO
                ))
            }
        }

        // HRV drop
        val migraineHrv = migraineLogs.mapNotNull { it.hrv?.toDouble() }.average()
        val normalHrv = logs.filter { !it.hasMigraine }.mapNotNull { it.hrv?.toDouble() }.average()
        if (!migraineHrv.isNaN() && !normalHrv.isNaN() && normalHrv - migraineHrv > 5) {
            insights.add(Insight(
                "💓 HRV Signal",
                "HRV is lower on migraine days (${String.format("%.0f", migraineHrv)}ms vs ${String.format("%.0f", normalHrv)}ms). This may be an early warning sign.",
                InsightSeverity.INFO
            ))
        }

        return insights
    }

    // ── CSV Export ──────────────────────────────────

    fun exportLogsAsCsv(callback: (Intent) -> Unit) {
        viewModelScope.launch {
            val logs = repo.getAllLogsForExport()
            val intent = CsvExporter.exportDailyLogs(getApplication(), logs)
            callback(intent)
        }
    }

    fun exportMigrainesAsCsv(callback: (Intent) -> Unit) {
        viewModelScope.launch {
            val events = repo.getAllMigraineEventsForExport()
            val intent = CsvExporter.exportMigraineEvents(getApplication(), events)
            callback(intent)
        }
    }
}

data class Insight(
    val title: String,
    val description: String,
    val severity: InsightSeverity
)

enum class InsightSeverity { INFO, WARNING, POSITIVE }
