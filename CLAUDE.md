# GPS Jamming/Spoofing Detector

Android app that continuously records GPS location and detects anomalies indicating GPS spoofing or jamming.

## Tech Stack
- **Language**: Kotlin
- **UI**: Jetpack Compose + Material3
- **Architecture**: MVVM + Repository pattern
- **DI**: Hilt
- **Database**: Room
- **Maps**: Google Maps (maps-compose)
- **Preferences**: DataStore
- **Build**: Gradle Kotlin DSL with version catalog

## Project Structure
```
com.gpsjammingdetector/
├── data/          # Room DB, DAOs, entities, repository, preferences
├── di/            # Hilt DI modules (AppModule, LocationModule)
├── domain/        # Pure Kotlin models and detection logic
│   ├── model/     # AnomalyType, Severity, DetectionConfig, GpsReading, Anomaly
│   └── detection/ # AnomalyDetector, SpeedCalculator (Haversine)
├── service/       # GpsTrackingService (foreground), NotificationHelper
├── ui/            # Compose UI
│   ├── theme/     # Material3 theme with severity colors
│   ├── navigation/# Bottom nav, Screen sealed class
│   ├── components/# Reusable composables (PermissionHandler)
│   └── screens/   # Dashboard, Map, Anomalies, Settings
└── util/          # CsvExporter
```

## Build Commands

Requires JAVA_HOME to be set. On this machine:
```bash
export JAVA_HOME=/Users/shawn/dev-tools/jdk-17.0.18+8/Contents/Home
export ANDROID_HOME=/Users/shawn/Library/Android/sdk
```

- `./gradlew assembleDebug` — build debug APK
- `./gradlew test` — run unit tests
- `./gradlew connectedAndroidTest` — run instrumented tests

## Key Configuration
- Google Maps API key goes in `local.properties` as `MAPS_API_KEY=your_key`
- compileSdk 35, minSdk 26, targetSdk 35
- Java 17

## Anomaly Detection
Detects 4 types: teleportation (>5km jump), impossible speed (>300 km/h), altitude spike (>500m), mock location flag.
Severity is computed as ratio of value/threshold: LOW (1-2x), MEDIUM (2-5x), HIGH (5-10x), CRITICAL (>10x).
