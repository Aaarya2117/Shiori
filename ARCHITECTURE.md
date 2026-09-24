# Architecture Document
## AI Cognitive Gaming & Memory Assistance Platform — Android App

---

## 1. High-Level Architecture

The app follows an **offline-first, layered MVVM architecture**:

```
┌─────────────────────────────────────────────────────────┐
│                        UI Layer                         │
│   Jetpack Compose Screens (Games, Reminders, Caregiver)  │
└───────────────────────┬───────────────────────────────────┘
                         │ observes StateFlow / LiveData
┌───────────────────────▼───────────────────────────────────┐
│                     ViewModel Layer                       │
│        (per-feature ViewModels, Hilt-injected)            │
└───────────────────────┬───────────────────────────────────┘
                         │ calls
┌───────────────────────▼───────────────────────────────────┐
│                     Domain Layer                          │
│   Use Cases (AdaptDifficultyUseCase, SyncDataUseCase...)  │
│              Repository Interfaces                        │
└───────────────────────┬───────────────────────────────────┘
                         │ implements
┌───────────────────────▼───────────────────────────────────┐
│                      Data Layer                           │
│  ┌───────────────┐  ┌───────────────┐  ┌───────────────┐  │
│  │  Local (Room)  │  │ Remote (API/  │  │  ML (TFLite/  │  │
│  │  + DataStore   │  │  Firebase)    │  │  ML Kit)      │  │
│  └───────────────┘  └───────────────┘  └───────────────┘  │
└─────────────────────────────────────────────────────────┘
```

---

## 2. Module Breakdown

### 2.1 UI Layer (`ui/`)
- Jetpack Compose screens grouped by feature: `games/`, `reminders/`, `caregiver/`, `onboarding/`
- Elderly-friendly theme (large fonts, high contrast) defined centrally in `ui/theme/`
- Navigation handled via Compose Navigation Component

### 2.2 Domain Layer (`domain/`)
- Pure Kotlin, no Android dependencies
- **Use Cases** encapsulate business logic (e.g., `AdaptDifficultyUseCase`, `SyncCaregiverDataUseCase`, `ScheduleReminderUseCase`)
- **Repository interfaces** define contracts implemented by the data layer

### 2.3 Data Layer (`data/`)
- **Local:** Room database (patients, game sessions, reminders) + DataStore (preferences)
- **Remote:** Retrofit/Firebase Firestore for caregiver dashboard sync
- **Repository implementations:** merge local + remote sources, enforce offline-first logic (write local first, queue sync)
- **Sync:** WorkManager-driven background sync workers, retried on connectivity restore

### 2.4 ML Layer (`ml/`)
- Isolated from UI/domain — swappable without touching business logic
- `DifficultyAdapterModel.kt` — wraps TFLite model for real-time difficulty adjustment
- `SpeechRecognitionManager.kt` — wraps Android SpeechRecognizer / ML Kit STT
- `TTSManager.kt` — wraps Android TextToSpeech

### 2.5 Worker Layer (`worker/`)
- `ReminderWorker` — fires medicine/hydration/activity/appointment reminders, works fully offline
- `SyncWorker` — pushes queued local changes to Firebase when connectivity is available

---

## 3. Data Flow — Adaptive Gameplay Example
1. Patient plays a cognitive game (e.g., memory match) in `ui/games/`
2. Performance data (response time, accuracy) sent to `AdaptDifficultyUseCase`
3. Use case invokes `DifficultyAdapterModel` (TFLite, on-device) to compute next difficulty level
4. Result written to Room (`GameSessionDao`) immediately — no network dependency
5. `SyncWorker` later pushes session data to Firestore when online
6. Caregiver dashboard reads synced data from Firestore for trend visualization

---

## 4. Offline-First Strategy
- **Single source of truth:** Room database
- All writes (game scores, reminder completions, patient inputs) go to Room first
- WorkManager queues a sync job whenever local data changes
- Sync job runs opportunistically when network is available, retried with backoff on failure
- UI always reads from local DB — never blocks on network

---

## 5. Security Architecture
- Sensitive fields (patient identity info) encrypted at rest via **Android Keystore**
- Firebase Auth for caregiver/healthcare worker login
- Firestore security rules scoped per patient/caregiver relationship
- No raw patient data transmitted without encryption in transit (HTTPS enforced)

---

## 6. Caregiver Dashboard Architecture
Two possible approaches (decision pending — see PRD open questions):
- **Option A (in-app):** `caregiver/` Compose screens within the same app, gated by caregiver login
- **Option B (separate module):** `:caregiver-dashboard` Gradle module or lightweight web dashboard (React/Next.js + Firebase), for independent iteration and build times

---

## 7. Multilingual & Voice Architecture
- String resources localized per language (`values-hi/`, `values-bn/`, `values-ta/`, etc.)
- Voice I/O routed through `ml/SpeechRecognitionManager` and `ml/TTSManager`, which select the appropriate language model/voice pack at runtime
- Bhashini integration point reserved in the remote data layer for future expansion beyond on-device voice packs

---

## 8. Deployment / Build Notes
- Single Gradle module (`app/`) for hackathon MVP; caregiver dashboard can be split into its own module post-MVP
- Hilt for dependency injection across all layers
- Recommended: pin dependency versions via `libs.versions.toml` (Gradle version catalog) for consistency across team members' builds
