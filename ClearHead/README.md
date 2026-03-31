# 🌿 ClearHead — Migraine Tracker

A warm, calm Android app to help track daily health data and identify migraine triggers.

## Features

### 📝 Daily Log
Log everything that matters each day:
- **Sleep** — hours, quality (1–5), and Apple Watch sleep stages (Deep/REM/Light/Awake)
- **Food & Drink** — individual items with trigger tag chips (aged cheese, red wine, MSG, caffeine, etc.), water, caffeine (mg), alcohol
- **Apple Watch Health** — heart rate (avg/min/max), HRV, steps, active calories, exercise minutes
- **Cycle Tracker** — cycle day, phase (Menstrual/Follicular/Ovulation/Luteal), period flow level
- **Medications** — name, dose (mg), time, type (Preventive/Abortive/Supplement/OTC)
- **Lifestyle** — stress level, screen time, weather, barometric pressure, free-form notes

### 📅 History
- Month-view calendar with color-coded dots (green = logged, red = migraine day)
- Tap any day to navigate to its log entry
- Summary cards showing key stats at a glance

### 📊 Insights
- Selectable time range: 2 weeks / 30 days / 90 days / all time
- Automatic pattern detection:
  - Sleep comparison (migraine days vs. non-migraine days)
  - Top food triggers on migraine days
  - Cycle phase correlations
  - HRV signal analysis
  - Caffeine and stress pattern warnings
- Bar charts for food triggers, cycle phases, medications used
- Apple Watch health averages

### 📤 Export
- Export **Daily Logs CSV** — one row per day, 28 columns covering all tracked fields
- Export **Migraine Events CSV** — individual episodes with duration, pain level, symptoms
- CSV column reference guide built in
- Share to email, Google Drive, Dropbox, or any app via Android share sheet

---

## Setup Instructions

### Requirements
- **Android Studio** Hedgehog (2023.1.1) or newer
- **JDK 17**
- **Android device or emulator** running Android 8.0 (API 26) or higher

### Steps
1. **Clone / unzip** this project folder
2. Open **Android Studio** → `File → Open` → select the `ClearHead` folder
3. Wait for Gradle sync to complete (first sync downloads dependencies, takes ~2–3 min)
4. Connect an Android device via USB (with Developer Mode + USB Debugging enabled), or launch an emulator
5. Press **▶ Run** (Shift+F10)

### First-time Gradle sync tips
- If asked to upgrade AGP (Android Gradle Plugin), click **"Don't remind me again"** and keep the version in `build.gradle`
- If you see a JDK error: `File → Project Structure → SDK Location → Gradle JDK → select JDK 17`

---

## Project Structure

```
ClearHead/
├── app/src/main/java/com/clearhead/app/
│   ├── MainActivity.kt              # Navigation scaffold
│   ├── data/
│   │   ├── models/Models.kt         # Room entities + data classes
│   │   ├── database/Database.kt     # Room DB + DAOs
│   │   └── repository/
│   │       ├── Repository.kt        # Data access layer
│   │       └── CsvExporter.kt       # CSV generation + file sharing
│   ├── viewmodel/ViewModel.kt       # State + business logic + insight engine
│   └── ui/
│       ├── theme/Theme.kt           # Earthy color palette + typography
│       ├── components/Components.kt # Reusable Compose components
│       └── screens/
│           ├── LogEntryScreen.kt    # Daily log form
│           ├── HistoryScreen.kt     # Calendar + log list
│           ├── AnalyticsScreen.kt   # Charts + insights
│           └── ExportScreen.kt      # CSV export
```

---

## Apple Watch Health Data

Since this is an Android app, Apple Watch data needs to be **manually entered** from the Health app on iPhone:

1. Open the **Health app** on iPhone
2. Navigate to Browse → Heart, Sleep, Activity, or Cycle Tracking
3. Find yesterday's summary values
4. Enter them in the ClearHead daily log

**Tip:** Apple Health exports (Settings → Health → Export All Health Data) can produce a large XML file that a developer could use to build an automatic import feature in a future version.

---

## Migraine Trigger Reference

The app includes chips for the most clinically documented migraine triggers:
- Aged cheese, red wine, beer, chocolate, caffeine, aspartame, MSG, nitrates/nitrites, citrus, nuts, onions, processed meat, skipped meals, alcohol

---

## Privacy

All data is stored **locally on the device** using Room (SQLite). Nothing is sent to any server. Exports only leave the device when you explicitly share them.

---

*Built with ❤️ using Jetpack Compose, Material 3, and Room.*
