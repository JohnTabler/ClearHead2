package com.clearhead.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clearhead.app.data.models.DailyLog
import com.clearhead.app.ui.components.StatTile
import com.clearhead.app.ui.theme.*
import com.clearhead.app.viewmodel.ClearHeadViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as JTextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: ClearHeadViewModel, onDateSelected: (LocalDate) -> Unit) {
    val allLogs by viewModel.allLogs.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    var displayMonth by remember { mutableStateOf(YearMonth.now()) }

    val logsByDate = remember(allLogs) { allLogs.associateBy { it.date } }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Month Summary Strip ──────────────────────
        item {
            val monthLogs = allLogs.filter {
                YearMonth.from(it.date) == displayMonth
            }
            val migraineCount = monthLogs.count { it.hasMigraine }
            val logCount = monthLogs.size

            Text(
                "History",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
            )

            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile(
                    label = "Days Logged",
                    value = logCount.toString(),
                    icon = Icons.Default.CalendarToday,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    label = "Migraine Days",
                    value = migraineCount.toString(),
                    icon = Icons.Default.Warning,
                    color = MigraineRed,
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    label = "Migraine-Free",
                    value = (logCount - migraineCount).toString(),
                    icon = Icons.Default.CheckCircle,
                    color = PositiveGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ── Calendar ─────────────────────────────────
        item {
            CalendarView(
                month = displayMonth,
                logsByDate = logsByDate,
                selectedDate = selectedDate,
                onDayClick = { date ->
                    viewModel.selectDate(date)
                    onDateSelected(date)
                },
                onPrevMonth = { displayMonth = displayMonth.minusMonths(1) },
                onNextMonth = { displayMonth = displayMonth.plusMonths(1) }
            )
        }

        // ── Recent Log Cards ─────────────────────────
        item {
            Text(
                "Recent Entries",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        val sortedLogs = allLogs.sortedByDescending { it.date }
        if (sortedLogs.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🌿", fontSize = 40.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("No logs yet", style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Start by logging today's data",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(sortedLogs) { log ->
                LogHistoryCard(
                    log = log,
                    isSelected = log.date == selectedDate,
                    onClick = {
                        viewModel.selectDate(log.date)
                        onDateSelected(log.date)
                    }
                )
            }
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}

// ── Calendar Component ──────────────────────────

@Composable
fun CalendarView(
    month: YearMonth,
    logsByDate: Map<LocalDate, DailyLog>,
    selectedDate: LocalDate,
    onDayClick: (LocalDate) -> Unit,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Month header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPrevMonth) {
                    Icon(Icons.Default.ChevronLeft, "Previous month")
                }
                Text(
                    month.month.getDisplayName(JTextStyle.FULL, Locale.getDefault()) + " ${month.year}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = onNextMonth) {
                    Icon(Icons.Default.ChevronRight, "Next month")
                }
            }

            Spacer(Modifier.height(8.dp))

            // Day-of-week headers
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("Su","Mo","Tu","We","Th","Fr","Sa").forEach { day ->
                    Text(
                        day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Days grid
            val firstDay = month.atDay(1)
            val startOffset = firstDay.dayOfWeek.value % 7 // Sunday=0
            val daysInMonth = month.lengthOfMonth()
            val totalCells = startOffset + daysInMonth
            val rows = (totalCells + 6) / 7

            repeat(rows) { row ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    repeat(7) { col ->
                        val cellIndex = row * 7 + col
                        val dayNum = cellIndex - startOffset + 1
                        if (dayNum < 1 || dayNum > daysInMonth) {
                            Spacer(Modifier.weight(1f))
                        } else {
                            val date = month.atDay(dayNum)
                            val log = logsByDate[date]
                            val isSelected = date == selectedDate
                            val isToday = date == LocalDate.now()

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(2.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isSelected -> MaterialTheme.colorScheme.primary
                                            log?.hasMigraine == true -> MigraineRed.copy(alpha = 0.18f)
                                            log != null -> PositiveGreen.copy(alpha = 0.15f)
                                            else -> Color.Transparent
                                        }
                                    )
                                    .clickable { onDayClick(date) },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        dayNum.toString(),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = when {
                                            isSelected -> Color.White
                                            isToday -> MaterialTheme.colorScheme.primary
                                            else -> MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                    if (log != null && !isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (log.hasMigraine) MigraineRed else PositiveGreen
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
            }

            // Legend
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                LegendDot(PositiveGreen, "Logged, no migraine")
                LegendDot(MigraineRed.copy(alpha = 0.6f), "Migraine day")
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ── Log History Card ─────────────────────────────

@Composable
fun LogHistoryCard(log: DailyLog, isSelected: Boolean, onClick: () -> Unit) {
    val dateFmt = DateTimeFormatter.ofPattern("EEE, MMM d")
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surface,
        border = if (log.hasMigraine) BorderStroke(1.5.dp, MigraineRed.copy(alpha = 0.4f)) else null,
        tonalElevation = if (isSelected) 0.dp else 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Date badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (log.hasMigraine) MigraineRed.copy(alpha = 0.12f)
                else MaterialTheme.colorScheme.tertiaryContainer
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        log.date.dayOfMonth.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (log.hasMigraine) MigraineRed
                        else MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        log.date.month.getDisplayName(JTextStyle.SHORT, Locale.getDefault()),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        log.date.format(dateFmt),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (log.hasMigraine) {
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MigraineRed.copy(alpha = 0.12f)
                        ) {
                            Text(
                                "Migraine",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MigraineRed,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                // Quick stats row
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (log.sleepHours != null) {
                        MiniStat("💤", "${log.sleepHours}h")
                    }
                    if (log.heartRateAvg != null) {
                        MiniStat("❤️", "${log.heartRateAvg}bpm")
                    }
                    if (log.steps != null) {
                        MiniStat("👟", "${log.steps}")
                    }
                    if (log.cyclePhase != null) {
                        MiniStat("🔄", log.cyclePhase!!)
                    }
                }
                if (log.medications.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "💊 ${log.medications.joinToString(", ") { it.name }}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun MiniStat(emoji: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(emoji, fontSize = 11.sp)
        Spacer(Modifier.width(2.dp))
        Text(value, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
