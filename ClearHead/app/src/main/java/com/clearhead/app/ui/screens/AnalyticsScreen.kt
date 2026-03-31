package com.clearhead.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clearhead.app.data.models.DailyLog
import com.clearhead.app.ui.components.InsightCard
import com.clearhead.app.ui.components.SectionCard
import com.clearhead.app.ui.components.StatTile
import com.clearhead.app.ui.theme.*
import com.clearhead.app.viewmodel.ClearHeadViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@Composable
fun AnalyticsScreen(viewModel: ClearHeadViewModel) {
    val allLogs by viewModel.allLogs.collectAsState()

    var rangeIndex by remember { mutableStateOf(1) } // 0=2wk 1=30d 2=90d 3=All
    val rangeDays = listOf(14, 30, 90, Int.MAX_VALUE)
    val rangeLabels = listOf("2 Weeks", "30 Days", "90 Days", "All Time")

    val cutoff = if (rangeDays[rangeIndex] == Int.MAX_VALUE) LocalDate.MIN
    else LocalDate.now().minusDays(rangeDays[rangeIndex].toLong())

    val logs = remember(allLogs, rangeIndex) {
        allLogs.filter { it.date >= cutoff }
    }

    val migraineLogs = logs.filter { it.hasMigraine }
    val insights = remember(logs) { viewModel.getInsights(logs) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Analytics",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
            )
        }

        // ── Range Selector ───────────────────────────
        item {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                rangeLabels.forEachIndexed { i, label ->
                    SegmentedButton(
                        selected = rangeIndex == i,
                        onClick = { rangeIndex = i },
                        shape = SegmentedButtonDefaults.itemShape(i, rangeLabels.size),
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
        }

        // ── Overview Stats ───────────────────────────
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile(
                    label = "Total Days",
                    value = logs.size.toString(),
                    icon = Icons.Default.CalendarMonth,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    label = "Migraines",
                    value = migraineLogs.size.toString(),
                    icon = Icons.Default.Warning,
                    color = MigraineRed,
                    modifier = Modifier.weight(1f)
                )
                val migraineRate = if (logs.isNotEmpty())
                    (migraineLogs.size * 100f / logs.size).roundToInt() else 0
                StatTile(
                    label = "Rate",
                    value = "$migraineRate%",
                    icon = Icons.Default.Percent,
                    color = WarningAmber,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ── AI Insights ──────────────────────────────
        item {
            SectionCard("Pattern Insights", Icons.Default.Lightbulb) {
                if (insights.isEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🌿", fontSize = 28.sp)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            if (logs.size < 5)
                                "Keep logging! Insights appear after at least 5 days of data."
                            else
                                "No strong patterns detected yet. Keep logging consistently.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        insights.forEach { insight ->
                            InsightCard(insight.title, insight.description, insight.severity)
                        }
                    }
                }
            }
        }

        // ── Sleep Averages ───────────────────────────
        item {
            SectionCard("Sleep Comparison", Icons.Default.Bedtime) {
                val migSleep = migraineLogs.mapNotNull { it.sleepHours }.average()
                    .takeIf { !it.isNaN() }
                val normalSleep = logs.filter { !it.hasMigraine }.mapNotNull { it.sleepHours }.average()
                    .takeIf { !it.isNaN() }
                val allSleep = logs.mapNotNull { it.sleepHours }.average().takeIf { !it.isNaN() }

                if (allSleep == null) {
                    Text("No sleep data logged yet.", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        AverageBar(
                            label = "Avg All Days",
                            value = allSleep,
                            max = 10.0,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        if (normalSleep != null) AverageBar(
                            label = "No Migraine",
                            value = normalSleep,
                            max = 10.0,
                            color = PositiveGreen,
                            modifier = Modifier.weight(1f)
                        )
                        if (migSleep != null) AverageBar(
                            label = "Migraine Days",
                            value = migSleep,
                            max = 10.0,
                            color = MigraineRed,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // ── Top Food Triggers ────────────────────────
        item {
            SectionCard("Food Triggers (Migraine Days)", Icons.Default.Restaurant) {
                val tagCounts = migraineLogs
                    .flatMap { it.foodEntries.flatMap { f -> f.tags } }
                    .groupingBy { it }.eachCount()
                    .entries.sortedByDescending { it.value }.take(6)

                if (tagCounts.isEmpty()) {
                    Text("No food trigger data yet.", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    val maxCount = tagCounts.first().value.toFloat()
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        tagCounts.forEach { (tag, count) ->
                            TriggerBar(
                                label = tag,
                                count = count,
                                fraction = count / maxCount,
                                color = Terracotta400
                            )
                        }
                    }
                }
            }
        }

        // ── Cycle Phase Correlation ──────────────────
        item {
            SectionCard("Cycle Phase & Migraines", Icons.Default.Loop) {
                val phaseCounts = migraineLogs
                    .mapNotNull { it.cyclePhase }
                    .groupingBy { it }.eachCount()

                if (phaseCounts.isEmpty()) {
                    Text("No cycle data logged yet.", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    val maxVal = phaseCounts.values.max().toFloat()
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        phaseCounts.entries.sortedByDescending { it.value }.forEach { (phase, count) ->
                            TriggerBar(
                                label = phase,
                                count = count,
                                fraction = count / maxVal,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }

        // ── Medication Usage ─────────────────────────
        item {
            SectionCard("Medications Used", Icons.Default.Medication) {
                val medCounts = logs.flatMap { it.medications }
                    .groupingBy { it.name }.eachCount()
                    .entries.sortedByDescending { it.value }.take(6)

                if (medCounts.isEmpty()) {
                    Text("No medication data yet.", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    val maxVal = medCounts.first().value.toFloat()
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        medCounts.forEach { (name, count) ->
                            TriggerBar(
                                label = name,
                                count = count,
                                fraction = count / maxVal,
                                color = Sage400
                            )
                        }
                    }
                }
            }
        }

        // ── Apple Watch Averages ─────────────────────
        item {
            SectionCard("Health Averages", Icons.Default.Watch) {
                val avgHR   = logs.mapNotNull { it.heartRateAvg }.average().takeIf { !it.isNaN() }
                val avgHRV  = logs.mapNotNull { it.hrv?.toDouble() }.average().takeIf { !it.isNaN() }
                val avgSteps = logs.mapNotNull { it.steps }.average().takeIf { !it.isNaN() }
                val avgExercise = logs.mapNotNull { it.exerciseMinutes }.average().takeIf { !it.isNaN() }

                if (listOf(avgHR, avgHRV, avgSteps, avgExercise).all { it == null }) {
                    Text("No Apple Watch data yet.", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()) {
                        if (avgHR != null) StatTile(
                            label = "Avg HR",
                            value = avgHR.roundToInt().toString(),
                            unit = "bpm",
                            icon = Icons.Default.Favorite,
                            color = MigraineRed,
                            modifier = Modifier.weight(1f)
                        )
                        if (avgHRV != null) StatTile(
                            label = "Avg HRV",
                            value = avgHRV.roundToInt().toString(),
                            unit = "ms",
                            icon = Icons.Default.MonitorHeart,
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()) {
                        if (avgSteps != null) StatTile(
                            label = "Avg Steps",
                            value = "%,.0f".format(avgSteps),
                            icon = Icons.Default.DirectionsWalk,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        if (avgExercise != null) StatTile(
                            label = "Avg Exercise",
                            value = avgExercise.roundToInt().toString(),
                            unit = "min",
                            icon = Icons.Default.FitnessCenter,
                            color = Sage500,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}

// ── Supporting Composables ────────────────────────

@Composable
private fun AverageBar(
    label: String,
    value: Double,
    max: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "%.1f".format(value) + "h",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color.copy(alpha = 0.15f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth((value / max).coerceIn(0.0, 1.0).toFloat())
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun TriggerBar(label: String, count: Int, fraction: Float, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            label,
            modifier = Modifier.width(120.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(color.copy(alpha = 0.15f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(5.dp))
                    .background(color)
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            count.toString(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
