# GPS Jamming/Spoofing Detector

Android app that continuously records GPS location and detects anomalies indicating GPS spoofing or jamming.

## Download

**[Download Latest APK](https://github.com/zkclaudecoder/gpsjammingdetector/releases/latest/download/app-debug.apk)**

Or browse all releases [here](https://github.com/zkclaudecoder/gpsjammingdetector/releases).

## Features

- **GPS-only tracking** — pure satellite fixes via `LocationManager.GPS_PROVIDER`
- **Anomaly detection** — teleportation, impossible speed, altitude spikes, mock location
- **Severity levels** — LOW, MEDIUM, HIGH, CRITICAL based on value/threshold ratio
- **OpenStreetMap** — map visualization with GPS track and anomaly markers (no API key needed)
- **Audio alerts** — 6 sound options with configurable toggle
- **Last coordinates** — displayed on dashboard with tap-to-copy
- **CSV export** — export readings and anomalies
- **Background tracking** — foreground service keeps recording with screen off
- **Configurable thresholds** — adjust speed, distance, altitude limits in settings

## Building from Source

Requires JDK 17 and Android SDK.

```bash
./gradlew assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`
