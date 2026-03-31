package com.clearhead.app.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clearhead.app.ui.theme.*
import com.clearhead.app.viewmodel.ClearHeadViewModel

@Composable
fun ExportScreen(viewModel: ClearHeadViewModel) {
    val context = LocalContext.current
    val allLogs by viewModel.allLogs.collectAsState()
    val allEvents by viewModel.allMigraineEvents.collectAsState()

    var isExportingLogs by remember { mutableStateOf(false) }
    var isExportingEvents by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Export & Data",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
        )

        // ── Data Summary ─────────────────────────────
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("📊 Your Data Summary",
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DataPill("${allLogs.size}", "Daily Logs", Modifier.weight(1f))
                    DataPill("${allLogs.count { it.hasMigraine }}", "Migraine Days", Modifier.weight(1f))
                    DataPill("${allEvents.size}", "Events", Modifier.weight(1f))
                }
            }
        }

        // ── Export Cards ─────────────────────────────
        Text("Export as CSV", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

        ExportCard(
            title = "Daily Health Logs",
            description = "All daily entries including sleep, food, Apple Watch data, cycle tracking, medications, stress, and weather — one row per day.",
            icon = Icons.Default.TableChart,
            count = allLogs.size,
            countLabel = "entries",
            isLoading = isExportingLogs,
            onExport = {
                isExportingLogs = true
                viewModel.exportLogsAsCsv { intent ->
                    isExportingLogs = false
                    context.startActivity(Intent.createChooser(intent, "Export Daily Logs"))
                }
            }
        )

        ExportCard(
            title = "Migraine Events",
            description = "Individual migraine episodes with start/end time, duration, pain level (1–10), location, symptoms, and notes.",
            icon = Icons.Default.Warning,
            count = allEvents.size,
            countLabel = "events",
            isLoading = isExportingEvents,
            onExport = {
                isExportingEvents = true
                viewModel.exportMigrainesAsCsv { intent ->
                    isExportingEvents = false
                    context.startActivity(Intent.createChooser(intent, "Export Migraine Events"))
                }
            }
        )

        // ── CSV Column Reference ─────────────────────
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Daily Log CSV Columns",
                        style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(12.dp))
                val columns = listOf(
                    "Date" to "YYYY-MM-DD format",
                    "Has Migraine" to "true / false",
                    "Sleep Hours" to "Decimal hours",
                    "Sleep Quality" to "1 (poor) to 5 (great)",
                    "Deep / Light / REM / Awake" to "Apple Watch sleep stage minutes",
                    "Heart Rate Avg/Min/Max" to "BPM from Apple Watch",
                    "HRV" to "Heart rate variability in ms",
                    "Steps" to "Daily step count",
                    "Active Calories / Exercise Min" to "From Apple Watch",
                    "Cycle Day / Phase / Flow" to "Cycle tracker data",
                    "Water / Caffeine / Alcohol" to "oz, mg, drinks",
                    "Stress Level" to "1 (low) to 5 (very high)",
                    "Screen Time" to "Hours",
                    "Weather / Barometric Pressure" to "Condition + hPa",
                    "Food Entries" to "Semicolon-separated names",
                    "Food Trigger Tags" to "Semicolon-separated trigger tags",
                    "Medications" to "Name + dose + time",
                    "Notes" to "Free text"
                )
                columns.forEach { (col, desc) ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("• ", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                        Column {
                            Text(col, style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold)
                            Text(desc, style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // ── Tips ─────────────────────────────────────
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Sage100,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("💡 Analysis Tips", style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold, color = Sage600)
                listOf(
                    "Open the CSV in Excel or Google Sheets for pivot tables",
                    "Filter by 'Has Migraine = true' to isolate migraine days",
                    "Compare sleep hours and HRV on migraine vs. non-migraine days",
                    "Sort by cycle phase to find hormonal correlations",
                    "Share the CSV with your neurologist for pattern analysis"
                ).forEach { tip ->
                    Row {
                        Text("→ ", color = Sage500, style = MaterialTheme.typography.bodySmall)
                        Text(tip, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }

        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun ExportCard(
    title: String,
    description: String,
    icon: ImageVector,
    count: Int,
    countLabel: String,
    isLoading: Boolean,
    onExport: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Icon(icon, null,
                        modifier = Modifier.padding(10.dp).size(22.dp),
                        tint = MaterialTheme.colorScheme.tertiary)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold)
                    Text("$count $countLabel available",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(description, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(14.dp))
            Button(
                onClick = onExport,
                enabled = count > 0 && !isLoading,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onTertiary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Download, null, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(8.dp))
                Text(if (isLoading) "Preparing…" else "Export CSV",
                    style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun DataPill(value: String, label: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
