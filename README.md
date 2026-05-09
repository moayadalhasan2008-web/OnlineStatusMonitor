# Online Status Monitor (Android / Kotlin)

Production-ready Android project that **simulates real-time “online/offline” monitoring** for multiple phone numbers, with:
- Cyber-style dark UI + smooth animations
- Country code picker with flags
- Foreground monitoring service
- Push notifications + alarm sound + vibration on “ONLINE”
- Live activity log (timestamped)
- Dashboard statistics (online/offline totals + last active)
- Local history persistence using **Room (SQLite)**
- Settings page (dark/light, notifications, alarm, vibration)

> Important: this project **does not spy on real apps**. True presence monitoring requires an authorized server/API from the target platform. The architecture here is designed so you can later plug in a real data source.

## Project Structure (key classes)
- `MainActivity` → hosts the dashboard
- `DashboardFragment` → stats + multiple numbers + activity log
- `MonitorService` → foreground service running the real-time simulation loop
- `NotificationManager` → notification + alarm + vibration helper
- `LocalDatabase` → Room database (numbers + logs)
- `SettingsActivity` / `SettingsFragment` → settings UI backed by DataStore

## Requirements
- Android Studio (recommended: latest stable)
- JDK 17 (Android Studio provides it)

## Build & Run (Android Studio)
1. Open Android Studio → **Open** → select the `OnlineStatusMonitor` folder.
2. Wait for Gradle sync to finish.
3. Connect a device (Samsung S22 Ultra recommended) or start an emulator.
4. Click **Run** ▶️.

## Build APK
### Option A (Android Studio)
1. **Build** → **Build Bundle(s) / APK(s)** → **Build APK(s)**.
2. Android Studio will show “APK(s) generated successfully” with a link to locate it.

### Option B (Command line)
If you have a Gradle wrapper in the project, run:
```bash
./gradlew assembleDebug
```
The debug APK will be at:
`app/build/outputs/apk/debug/app-debug.apk`

## How the monitoring simulation works
`MonitorService` runs as a foreground service and periodically:
1. Picks a monitored number.
2. Randomly flips its state (ONLINE/OFFLINE).
3. Updates totals (online/offline duration) in Room.
4. Writes a timestamped log entry.
5. If it became ONLINE → triggers alert (notification + optional alarm/vibration) based on Settings.

## Notes for production hardening
- Replace `MonitorService` simulation with your real data source (server polling / websockets).
- Add number removal / editing UI if needed.
- Add encryption if you store sensitive data.
- Add analytics/crash reporting if desired.

