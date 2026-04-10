# Workspace

## Overview

This workspace contains a native Android application called **Vocalize** — an advanced voice reminder app built with Kotlin and Jetpack Compose. It follows MVVM + Clean Architecture principles.

## Android App: Vocalize

**Location**: `vocalize-android/`
**Package**: `com.vocalize.app`
**Min SDK**: 26 (Android 8.0)
**Target SDK**: 34 (Android 14)

### Tech Stack
- **UI**: Jetpack Compose (fully declarative)
- **Architecture**: MVVM + Clean Architecture
- **DI**: Dagger Hilt
- **Database**: Room (SQLite)
- **Audio**: MediaRecorder (recording), MediaPlayer (playback)
- **Transcription**: Vosk (offline speech-to-text, ~40MB model)
- **Notifications**: WorkManager + AlarmManager
- **Backup**: Google Drive AppDataFolder API
- **Animations**: Lottie + Compose animations
- **Navigation**: Jetpack Navigation Compose

### Screens Implemented
- **SplashScreen** — branded loading with Lottie animation
- **HomeScreen** — tabs (Recents, All Memos, Playlists), animated FAB, bottom nav
- **RecorderScreen** — real-time waveform, amplitude visualization, voice-to-text
- **MemoDetailScreen** — playback controls, seek bar, speed selector, reminder setup
- **CalendarScreen** — month calendar grid with memo/reminder dots per day
- **SearchScreen** — full-text search with category/reminder filters
- **SettingsScreen** — Google Drive backup, Vosk model management, dark mode, snooze
- **PlaylistScreen** — playlist playback with mini player bar, shuffle, next/prev

### Key Files
- `app/src/main/java/com/vocalize/app/`
  - `presentation/` — all Compose screens and ViewModels
  - `data/local/` — Room entities, DAOs, AppDatabase
  - `data/repository/` — MemoRepository
  - `util/` — AudioRecorderManager, AudioPlayerManager, VoskTranscriber, BackupManager, etc.
  - `service/` — PlaybackService (foreground), VoskService (transcription)
  - `di/` — Hilt AppModule

### Build
To build the debug APK locally:
```bash
cd vocalize-android
./gradlew assembleDebug
```
Output: `app/build/outputs/apk/debug/app-debug.apk`

### GitHub Actions
A workflow at `.github/workflows/build-debug-apk.yml` automatically builds the debug APK on every push to main/develop that touches `vocalize-android/`. The APK is uploaded as a workflow artifact.

## Also in this workspace
- `artifacts/` — web artifact scaffolding (unused, Android app is primary)
- `lib/` — shared TypeScript library (unused for Android)
