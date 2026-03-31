package com.clearhead.app.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate
import java.time.LocalDateTime

// ──────────────────────────────────────────────
// Type Converters
// ──────────────────────────────────────────────

class Converters {
    private val gson = Gson()

    @TypeConverter fun fromLocalDate(value: String?): LocalDate? =
        value?.let { LocalDate.parse(it) }
    @TypeConverter fun toLocalDate(date: LocalDate?): String? = date?.toString()

    @TypeConverter fun fromLocalDateTime(value: String?): LocalDateTime? =
        value?.let { LocalDateTime.parse(it) }
    @TypeConverter fun toLocalDateTime(dt: LocalDateTime?): String? = dt?.toString()

    @TypeConverter fun fromStringList(value: String?): List<String> =
        value?.let { gson.fromJson(it, object : TypeToken<List<String>>() {}.type) } ?: emptyList()
    @TypeConverter fun toStringList(list: List<String>?): String =
        gson.toJson(list ?: emptyList<String>())
}

// ──────────────────────────────────────────────
// Migraine Event
// ──────────────────────────────────────────────

@Entity(tableName = "migraine_events")
@TypeConverters(Converters::class)
data class MigraineEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime?,
    val painLevel: Int,              // 1–10
    val location: String,            // e.g. "Left temple", "Behind eyes"
    val symptoms: List<String>,      // e.g. ["Aura", "Nausea", "Light sensitivity"]
    val notes: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now()
)

// ──────────────────────────────────────────────
// Daily Log (the main composite entry)
// ──────────────────────────────────────────────

@Entity(tableName = "daily_logs")
@TypeConverters(Converters::class)
data class DailyLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,

    // Sleep
    val sleepHours: Float?,
    val sleepQuality: Int?,          // 1–5
    val sleepStages: SleepStages?,

    // Food & Drink
    val foodEntries: List<FoodEntry>,
    val waterOz: Float?,
    val caffeinesMg: Int?,
    val alcoholDrinks: Float?,

    // Apple Watch Health
    val heartRateAvg: Int?,
    val heartRateMin: Int?,
    val heartRateMax: Int?,
    val hrv: Float?,                 // ms
    val steps: Int?,
    val activeCalories: Int?,
    val exerciseMinutes: Int?,

    // Cycle Tracker
    val cycleDay: Int?,
    val cyclePhase: String?,         // "Menstrual","Follicular","Ovulation","Luteal"
    val periodFlow: String?,         // "None","Spotting","Light","Medium","Heavy"

    // Medications
    val medications: List<MedicationEntry>,

    // Environment / Lifestyle
    val stressLevel: Int?,           // 1–5
    val weatherCondition: String?,
    val barometricPressure: Float?,  // hPa
    val screenTimeHours: Float?,

    // Meta
    val hasMigraine: Boolean = false,
    val notes: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

// ──────────────────────────────────────────────
// Embedded sub-models (stored as JSON columns)
// ──────────────────────────────────────────────

data class SleepStages(
    val awakeMinutes: Int = 0,
    val remMinutes: Int = 0,
    val lightMinutes: Int = 0,
    val deepMinutes: Int = 0
)

data class FoodEntry(
    val name: String,
    val tags: List<String> = emptyList(),  // e.g. ["Caffeine","Alcohol","Aged cheese","Processed"]
    val mealTime: String = "",             // "Breakfast","Lunch","Dinner","Snack"
    val time: String = ""
)

data class MedicationEntry(
    val name: String,
    val doseMg: Float?,
    val time: String,
    val type: String = "",   // "Preventive","Abortive","Supplement","OTC"
    val notes: String = ""
)

// ──────────────────────────────────────────────
// Common trigger tags (reference data)
// ──────────────────────────────────────────────

object CommonTriggers {
    val foodTriggers = listOf(
        "Aged cheese", "Red wine", "Beer", "Chocolate", "Caffeine", "Aspartame",
        "MSG", "Nitrates/nitrites", "Citrus", "Nuts", "Onions", "Processed meat",
        "Skipped meal", "Fasting", "Alcohol"
    )
    val symptoms = listOf(
        "Aura", "Nausea", "Vomiting", "Light sensitivity", "Sound sensitivity",
        "Smell sensitivity", "Throbbing pain", "Neck stiffness", "Fatigue",
        "Brain fog", "Visual disturbance", "Tingling", "Yawning", "Irritability"
    )
    val migraineLocations = listOf(
        "Left temple", "Right temple", "Both temples", "Behind eyes",
        "Forehead", "Back of head", "Top of head", "Entire head"
    )
    val cyclePhases = listOf("Menstrual", "Follicular", "Ovulation", "Luteal")
    val flowLevels = listOf("None", "Spotting", "Light", "Medium", "Heavy")
    val weatherConditions = listOf(
        "Sunny", "Cloudy", "Rainy", "Stormy", "Humid", "Dry", "Windy", "Cold", "Hot"
    )
    val medicationTypes = listOf("Preventive", "Abortive", "Supplement", "OTC", "Other")
}

// ──────────────────────────────────────────────
// Converters for embedded JSON objects
// ──────────────────────────────────────────────

class EmbeddedConverters {
    private val gson = Gson()

    @TypeConverter fun sleepStagesToJson(v: SleepStages?): String? = gson.toJson(v)
    @TypeConverter fun jsonToSleepStages(v: String?): SleepStages? =
        v?.let { gson.fromJson(it, SleepStages::class.java) }

    @TypeConverter fun foodListToJson(v: List<FoodEntry>?): String = gson.toJson(v ?: emptyList<FoodEntry>())
    @TypeConverter fun jsonToFoodList(v: String?): List<FoodEntry> =
        v?.let { gson.fromJson(it, object : TypeToken<List<FoodEntry>>() {}.type) } ?: emptyList()

    @TypeConverter fun medListToJson(v: List<MedicationEntry>?): String = gson.toJson(v ?: emptyList<MedicationEntry>())
    @TypeConverter fun jsonToMedList(v: String?): List<MedicationEntry> =
        v?.let { gson.fromJson(it, object : TypeToken<List<MedicationEntry>>() {}.type) } ?: emptyList()
}
