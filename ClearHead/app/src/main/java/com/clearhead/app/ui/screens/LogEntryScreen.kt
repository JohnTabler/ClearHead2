package com.clearhead.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.clearhead.app.data.models.*
import com.clearhead.app.ui.components.*
import com.clearhead.app.ui.theme.*
import com.clearhead.app.viewmodel.ClearHeadViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogEntryScreen(viewModel: ClearHeadViewModel) {
    val date by viewModel.selectedDate.collectAsState()
    val existingLog by viewModel.currentLog.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()

    // ── Form state ──────────────────────────────────
    var hasMigraine by remember(existingLog) { mutableStateOf(existingLog?.hasMigraine ?: false) }

    // Sleep
    var sleepHours by remember(existingLog) { mutableStateOf(existingLog?.sleepHours?.toString() ?: "") }
    var sleepQuality by remember(existingLog) { mutableStateOf((existingLog?.sleepQuality ?: 3).toFloat()) }
    var deepMin by remember(existingLog) { mutableStateOf(existingLog?.sleepStages?.deepMinutes?.toString() ?: "") }
    var remMin by remember(existingLog) { mutableStateOf(existingLog?.sleepStages?.remMinutes?.toString() ?: "") }
    var lightMin by remember(existingLog) { mutableStateOf(existingLog?.sleepStages?.lightMinutes?.toString() ?: "") }
    var awakeMin by remember(existingLog) { mutableStateOf(existingLog?.sleepStages?.awakeMinutes?.toString() ?: "") }

    // Food
    var foodText by remember { mutableStateOf("") }
    var foodItems by remember(existingLog) {
        mutableStateOf(existingLog?.foodEntries?.toMutableList() ?: mutableListOf())
    }
    var selectedTriggerTags by remember { mutableStateOf(setOf<String>()) }
    var waterOz by remember(existingLog) { mutableStateOf(existingLog?.waterOz?.toString() ?: "") }
    var caffeineMg by remember(existingLog) { mutableStateOf(existingLog?.caffeinesMg?.toString() ?: "") }
    var alcoholDrinks by remember(existingLog) { mutableStateOf(existingLog?.alcoholDrinks?.toString() ?: "") }

    // Apple Watch / Health
    var hrAvg by remember(existingLog) { mutableStateOf(existingLog?.heartRateAvg?.toString() ?: "") }
    var hrMin by remember(existingLog) { mutableStateOf(existingLog?.heartRateMin?.toString() ?: "") }
    var hrMax by remember(existingLog) { mutableStateOf(existingLog?.heartRateMax?.toString() ?: "") }
    var hrv by remember(existingLog) { mutableStateOf(existingLog?.hrv?.toString() ?: "") }
    var steps by remember(existingLog) { mutableStateOf(existingLog?.steps?.toString() ?: "") }
    var activeCal by remember(existingLog) { mutableStateOf(existingLog?.activeCalories?.toString() ?: "") }
    var exerciseMin by remember(existingLog) { mutableStateOf(existingLog?.exerciseMinutes?.toString() ?: "") }

    // Cycle
    var cycleDay by remember(existingLog) { mutableStateOf(existingLog?.cycleDay?.toString() ?: "") }
    var cyclePhase by remember(existingLog) { mutableStateOf(existingLog?.cyclePhase) }
    var periodFlow by remember(existingLog) { mutableStateOf(existingLog?.periodFlow) }

    // Medications
    var medName by remember { mutableStateOf("") }
    var medDose by remember { mutableStateOf("") }
    var medTime by remember { mutableStateOf("") }
    var medType by remember { mutableStateOf("") }
    var medications by remember(existingLog) {
        mutableStateOf(existingLog?.medications?.toMutableList() ?: mutableListOf())
    }

    // Lifestyle
    var stressLevel by remember(existingLog) { mutableStateOf((existingLog?.stressLevel ?: 2).toFloat()) }
    var screenTime by remember(existingLog) { mutableStateOf(existingLog?.screenTimeHours?.toString() ?: "") }
    var weather by remember(existingLog) { mutableStateOf(existingLog?.weatherCondition) }
    var baroPressure by remember(existingLog) { mutableStateOf(existingLog?.barometricPressure?.toString() ?: "") }
    var notes by remember(existingLog) { mutableStateOf(existingLog?.notes ?: "") }

    LaunchedEffect(saveSuccess) {
        if (saveSuccess) viewModel.clearSaveSuccess()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Date Header ─────────────────────────────
        val fmt = DateTimeFormatter.ofPattern("EEEE, MMMM d")
        Text(
            date.format(fmt),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
        )

        // ── Migraine Toggle ─────────────────────────
        MigraineToggle(hasMigraine) { hasMigraine = !hasMigraine }

        // ── Sleep ────────────────────────────────────
        SectionCard("Sleep", Icons.Default.Bedtime) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NumberInputField(
                    "Hours slept", sleepHours, { sleepHours = it }, "hrs",
                    Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(12.dp))
            LabeledSlider(
                "Sleep Quality", sleepQuality, { sleepQuality = it },
                min = 1f, max = 5f, steps = 3,
                valueLabel = when (sleepQuality.toInt()) {
                    1 -> "Poor"; 2 -> "Fair"; 3 -> "OK"; 4 -> "Good"; else -> "Great"
                }
            )
            Spacer(Modifier.height(12.dp))
            Text("Apple Watch Sleep Stages", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberInputField("Deep", deepMin, { deepMin = it }, "min", Modifier.weight(1f))
                NumberInputField("REM", remMin, { remMin = it }, "min", Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberInputField("Light", lightMin, { lightMin = it }, "min", Modifier.weight(1f))
                NumberInputField("Awake", awakeMin, { awakeMin = it }, "min", Modifier.weight(1f))
            }
        }

        // ── Food & Drink ─────────────────────────────
        SectionCard("Food & Drink", Icons.Default.Restaurant) {
            // Add food item
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = foodText,
                    onValueChange = { foodText = it },
                    label = { Text("Add food item") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                IconButton(
                    onClick = {
                        if (foodText.isNotBlank()) {
                            foodItems = (foodItems + FoodEntry(
                                name = foodText.trim(),
                                tags = selectedTriggerTags.toList()
                            )).toMutableList()
                            foodText = ""
                            selectedTriggerTags = emptySet()
                        }
                    },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
                        .size(56.dp)
                ) {
                    Icon(Icons.Default.Add, "Add", tint = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("Trigger tags for this item:", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            ChipGroup(
                CommonTriggers.foodTriggers,
                selectedTriggerTags,
                { tag -> selectedTriggerTags = if (tag in selectedTriggerTags)
                    selectedTriggerTags - tag else selectedTriggerTags + tag }
            )

            if (foodItems.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
                foodItems.toList().forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(item.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            if (item.tags.isNotEmpty()) {
                                Text(item.tags.joinToString(", "),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                        IconButton(onClick = {
                            foodItems = foodItems.toMutableList().also { it.remove(item) }
                        }) {
                            Icon(Icons.Default.Close, "Remove", modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberInputField("Water", waterOz, { waterOz = it }, "oz", Modifier.weight(1f))
                NumberInputField("Caffeine", caffeineMg, { caffeineMg = it }, "mg", Modifier.weight(1f))
                NumberInputField("Alcohol", alcoholDrinks, { alcoholDrinks = it }, "drinks", Modifier.weight(1f))
            }
        }

        // ── Apple Watch Health ────────────────────────
        SectionCard("Apple Watch Health", Icons.Default.Watch) {
            Text("Heart Rate", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberInputField("Avg", hrAvg, { hrAvg = it }, "bpm", Modifier.weight(1f))
                NumberInputField("Min", hrMin, { hrMin = it }, "bpm", Modifier.weight(1f))
                NumberInputField("Max", hrMax, { hrMax = it }, "bpm", Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            NumberInputField("HRV", hrv, { hrv = it }, "ms", Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberInputField("Steps", steps, { steps = it }, "", Modifier.weight(1f))
                NumberInputField("Active Cal", activeCal, { activeCal = it }, "kcal", Modifier.weight(1f))
                NumberInputField("Exercise", exerciseMin, { exerciseMin = it }, "min", Modifier.weight(1f))
            }
        }

        // ── Cycle Tracker ─────────────────────────────
        SectionCard("Cycle Tracker", Icons.Default.Loop) {
            NumberInputField("Cycle Day", cycleDay, { cycleDay = it }, "", Modifier.fillMaxWidth(0.4f))
            Spacer(Modifier.height(12.dp))
            Text("Phase", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            SingleSelectChips(CommonTriggers.cyclePhases, cyclePhase, { cyclePhase = it })
            Spacer(Modifier.height(12.dp))
            Text("Period Flow", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            SingleSelectChips(CommonTriggers.flowLevels, periodFlow, { periodFlow = it })
        }

        // ── Medications ───────────────────────────────
        SectionCard("Medications", Icons.Default.Medication) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = medName,
                        onValueChange = { medName = it },
                        label = { Text("Medication name") },
                        modifier = Modifier.weight(2f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    NumberInputField("Dose", medDose, { medDose = it }, "mg", Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = medTime,
                        onValueChange = { medTime = it },
                        label = { Text("Time (e.g. 8:00 AM)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            if (medName.isNotBlank()) {
                                medications = (medications + MedicationEntry(
                                    name = medName.trim(),
                                    doseMg = medDose.toFloatOrNull(),
                                    time = medTime.trim(),
                                    type = medType
                                )).toMutableList()
                                medName = ""; medDose = ""; medTime = ""; medType = ""
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Add")
                    }
                }
                Text("Type:", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                SingleSelectChips(CommonTriggers.medicationTypes, medType.ifEmpty { null }, { medType = it })
            }

            if (medications.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider()
                medications.toList().forEach { med ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Medication, null, modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.secondary)
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text("${med.name}${if (med.doseMg != null) " — ${med.doseMg}mg" else ""}",
                                style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            if (med.time.isNotEmpty() || med.type.isNotEmpty()) {
                                Text("${med.time}${if (med.type.isNotEmpty()) " · ${med.type}" else ""}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        IconButton(onClick = {
                            medications = medications.toMutableList().also { it.remove(med) }
                        }) {
                            Icon(Icons.Default.Close, "Remove", modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // ── Lifestyle / Environment ───────────────────
        SectionCard("Lifestyle & Environment", Icons.Default.Spa) {
            LabeledSlider(
                "Stress Level", stressLevel, { stressLevel = it },
                min = 1f, max = 5f, steps = 3,
                valueLabel = when (stressLevel.toInt()) {
                    1 -> "Low"; 2 -> "Mild"; 3 -> "Moderate"; 4 -> "High"; else -> "Very High"
                },
                accentColor = MaterialTheme.colorScheme.secondary
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberInputField("Screen Time", screenTime, { screenTime = it }, "hrs", Modifier.weight(1f))
                NumberInputField("Barometric", baroPressure, { baroPressure = it }, "hPa", Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            Text("Weather", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            SingleSelectChips(CommonTriggers.weatherConditions, weather, { weather = it })
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                minLines = 3,
                maxLines = 5
            )
        }

        // ── Save Button ──────────────────────────────
        Button(
            onClick = {
                val log = DailyLog(
                    id = existingLog?.id ?: 0,
                    date = date,
                    hasMigraine = hasMigraine,
                    sleepHours = sleepHours.toFloatOrNull(),
                    sleepQuality = sleepQuality.toInt(),
                    sleepStages = if (deepMin.isNotBlank() || remMin.isNotBlank()) SleepStages(
                        deepMinutes = deepMin.toIntOrNull() ?: 0,
                        remMinutes = remMin.toIntOrNull() ?: 0,
                        lightMinutes = lightMin.toIntOrNull() ?: 0,
                        awakeMinutes = awakeMin.toIntOrNull() ?: 0
                    ) else null,
                    foodEntries = foodItems,
                    waterOz = waterOz.toFloatOrNull(),
                    caffeinesMg = caffeineMg.toIntOrNull(),
                    alcoholDrinks = alcoholDrinks.toFloatOrNull(),
                    heartRateAvg = hrAvg.toIntOrNull(),
                    heartRateMin = hrMin.toIntOrNull(),
                    heartRateMax = hrMax.toIntOrNull(),
                    hrv = hrv.toFloatOrNull(),
                    steps = steps.toIntOrNull(),
                    activeCalories = activeCal.toIntOrNull(),
                    exerciseMinutes = exerciseMin.toIntOrNull(),
                    cycleDay = cycleDay.toIntOrNull(),
                    cyclePhase = cyclePhase,
                    periodFlow = periodFlow,
                    medications = medications,
                    stressLevel = stressLevel.toInt(),
                    screenTimeHours = screenTime.toFloatOrNull(),
                    weatherCondition = weather,
                    barometricPressure = baroPressure.toFloatOrNull(),
                    notes = notes,
                    updatedAt = LocalDateTime.now()
                )
                viewModel.saveLog(log)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Save, null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Save Daily Log", style = MaterialTheme.typography.titleMedium)
        }

        if (saveSuccess) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = PositiveGreen.copy(alpha = 0.12f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = PositiveGreen)
                    Spacer(Modifier.width(8.dp))
                    Text("Log saved!", style = MaterialTheme.typography.bodyMedium,
                        color = PositiveGreen, fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(Modifier.height(80.dp)) // nav bar breathing room
    }
}
