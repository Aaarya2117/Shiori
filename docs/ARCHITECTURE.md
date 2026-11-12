# Architecture Document
## AI Cognitive Gaming & Memory Assistance Platform (Shiori)

---

## 1. System Topology & Dual-Mode Navigation

Shiori is built on a **Dual-Mode, Offline-First Clean Architecture**. The application provides two distinct operational modes within a single unified APK:
1. **Patient Mode (Default):** Ultra-simplified, high-contrast, zero-cognitive-load experience for elderly users.
2. **Caregiver Mode (PIN-Protected):** Management and monitoring portal for family caregivers.

### 1.1 Complete Application Flowchart

```text
                                SHIORI APPLICATION
                                         │
        ┌────────────────────────────────┴────────────────────────────────┐
        │                                                                 │
  PATIENT EXPERIENCE (Default)                               CAREGIVER EXPERIENCE (PIN-Gated)
        │                                                                 │
        ├── 1. Play Games                                                 ├── 1. Patient Progress
        │     ├── Memory Match (Canvas)                                   │     ├── Cognitive Engagement Index
        │     ├── Pattern Sequence                                        │     └── Daily Routine Completion Rate
        │     └── Face Recall                                             ├── 2. Game Performance
        ├── 2. Memory Training                                            │     ├── Accuracy Trends Over Time
        │     ├── Family Album Recall                                     │     └── Reaction Latency Curves
        │     └── Everyday Object Association                             ├── 3. Medication & Routine
        ├── 3. Daily Routine                                              │     ├── Add / Edit Prescriptions
        │     ├── Morning (Hygiene, Breakfast)                            │     └── Configure Daily Checklist
        │     ├── Afternoon (Walk, Hydration)                             ├── 4. Alerts & Reports
        │     └── Evening (Dinner, Sleep)                                 │     ├── Missed Doses Audit Log
        ├── 4. Reminders                                                  │     ├── Data Freshness ("Last Synced: Xm")
        │     ├── Medication Heads-Up Display                             │     └── Export Doctor Summary (PDF)
        │     └── Hydration Audio Prompts                                 └── 5. Cloud Sync & Security
        └── 5. Profile & Settings                                               └── Local-Only vs. Cloud Toggle
              ├── Language Switch (HI / EN / KN)
              └── Emergency Caregiver Contact Card
        │                                                                 │
        └────────────────────────────────┬────────────────────────────────┘
                                         │
                                         ▼
                            OFFLINE-FIRST DATA ENGINE
                                         │
                    ┌────────────────────┼────────────────────┐
                    │                    │                    │
              ROOM DATABASE      ANDROID KEYSTORE       ALARM MANAGER
         (Encrypted w/ SQLCipher) (AES-256 Master Key)  (setExact While Idle)
                    │                                         │
                    ▼                                         ▼
            JETPACK WORKMANAGER                        BOOT RECEIVER
        (Opportunistic Sync on WiFi/4G)              (Restores Alarms on Boot)
                    │                                         │
                    ▼                                         ▼
            FIREBASE FIRESTORE                       HYBRID ALERT ENGINE
           (Cloud Sync Replica)                      (FCM Push + SMS Fallback)
```

---

## 2. Layered Clean Architecture & Module Breakdown

The codebase follows Google's recommended **Clean Architecture** with unidirectional data flow:

```text
┌────────────────────────────────────────────────────────────────────────┐
│                              UI LAYER                                  │
│   • Jetpack Compose Screens (Patient & Caregiver)                      │
│   • Stateful ViewModels (StateFlow / SharedFlow)                       │
│   • Elderly Accessible Theme (High contrast, large touch bounds)       │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ observes UIState / triggers Events
┌───────────────────────────────────▼────────────────────────────────────┐
│                            DOMAIN LAYER                                │
│   • Pure Kotlin (Zero Android framework imports)                       │
│   • Use Cases: AdaptDifficultyUseCase, ScheduleAlarmUseCase,           │
│                RecordMedicationUseCase, SyncDataUseCase                │
│   • Domain Repository Interfaces & Core Data Models                    │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ implemented by
┌───────────────────────────────────▼────────────────────────────────────┐
│                             DATA LAYER                                 │
│  ┌────────────────────────┐  ┌─────────────────┐  ┌─────────────────┐  │
│  │ Encrypted Local Room   │  │ Remote Cloud    │  │ Adaptive AI /   │  │
│  │ (SQLCipher AES-256)    │  │ (Firestore SDK) │  │ Audio Engine    │  │
│  │ • Single Source of Truth│  │ • Sync Replica  │  │ • Staircase v1  │  │
│  │ • Last-Write-Wins (LWW)│  │ • Caregiver Push│  │ • Pre-recorded  │  │
│  └────────────────────────┘  └─────────────────┘  └─────────────────┘  │
└────────────────────────────────────────────────────────────────────────┘
```

### 2.1 Package Organization
```text
com.shiori.app/
 ├── ui/
 │    ├── patient/           # Game, Routine, Reminder, and Memory screens
 │    ├── caregiver/         # Dashboard, Analytics, Medication setup, Alerts
 │    ├── theme/             # High-contrast accessible color palettes & typography
 │    └── components/        # Reusable large-touch buttons, audio tiles, dialogs
 ├── domain/
 │    ├── model/             # PatientProfile, GameSession, MedicationSchedule, AlertEvent
 │    ├── repository/        # IPatientRepository, IGameRepository, IReminderRepository
 │    └── usecase/           # AdaptDifficultyUseCase, ProcessMedicationStatusUseCase
 ├── data/
 │    ├── local/             # Room Database, DAOs, SQLCipher passphrase manager
 │    ├── remote/            # Firebase Firestore data source, FCM service
 │    ├── repository/        # Repository implementations enforcing offline-first logic
 │    └── security/          # Android Keystore wrapper for hardware AES-256 key
 ├── alarm/
 │    ├── AlarmScheduler.kt  # AlarmManager exact scheduling wrapper
 │    ├── ReminderReceiver.kt# BroadcastReceiver for heads-up alert notifications
 │    └── BootReceiver.kt    # Re-arms all active reminders on system boot
 ├── ml/
 │    ├── DifficultyAdapter.kt        # Interface for difficulty algorithms
 │    ├── StaircaseDifficultyAdapter.kt# Tier-1 rule/staircase heuristic engine
 │    └── TFLiteDifficultyAdapter.kt   # Tier-2 pluggable ML inference engine
 ├── voice/
 │    ├── AudioPromptManager.kt       # Pre-recorded offline audio playback controller
 │    └── SpeechManager.kt            # Native Android SpeechRecognizer / TTS wrapper
 └── sync/
      └── SyncWorker.kt      # WorkManager worker executing opportunistic background sync
```

---

## 3. Offline-First Synchronization & Single Source of Truth

Shiori enforces a strict **Local-First Architecture**. The local database is the **Single Source of Truth (SSoT)**. The remote cloud database is strictly an opportunistic replica.

```text
                           USER / SYSTEM EVENT
                       (Game Score, Taken Pill, etc.)
                                    │
                                    ▼
                         ROOM LOCAL DATABASE (SSoT)
                        (Encrypted via SQLCipher)
                 Writes immediately; sync_status = "PENDING"
                                    │
                                    ▼
                  IS INTERNET / DATA CONNECTIVITY AVAILABLE?
                                    │
                  ┌─────────────────┴─────────────────┐
                  │ YES                               │ NO
                  ▼                                   ▼
          TRIGGER SYNC WORKER                 QUEUE WORKMANAGER JOB
       (Pushes pending records               (Runs automatically when
        to Firebase Firestore)                network is restored)
                  │                                   │
                  ▼                                   ▼
        UPDATE LOCAL RECORD STATUS              REMAIN IN LOCAL DB
         sync_status = "SYNCED"               (Zero patient impact,
                                               100% features work)
```

### 3.1 Synchronization Logic & Conflict Resolution
- **Write Path:** Every mutation (completing a routine, recording a game score, confirming medication) writes immediately to the local encrypted Room database with three audit fields:
  - `sync_status`: `PENDING`, `SYNCING`, `SYNCED`, or `FAILED`
  - `updated_at`: UTC timestamp (milliseconds)
  - `device_id`: Hardware identifier
- **Read Path:** The UI layer observes Kotlin `Flow` directly from Room DAOs. The UI **never** makes network requests to display patient data.
- **Conflict Resolution:** In the event of dual updates (e.g., caregiver updates a dosage remotely while patient marks a dose offline), Shiori applies **Last-Write-Wins (LWW)** using the `updated_at` UTC timestamp.
- **Data Freshness Banner:** The Caregiver Dashboard queries the latest `updated_at` timestamp from the synced patient records and displays a prominent status:
  - `"🟢 Synced 3 minutes ago"` (Fresh data)
  - `"🟡 Patient device offline — Last synced 4 hours ago"` (Warning to caregiver)

---

## 4. Life-Critical Exact Reminder Engine

To eliminate missed medication doses caused by Android Doze Mode and aggressive OEM battery savers, reminders bypass WorkManager and utilize hardware-level `AlarmManager` mechanisms:

```text
┌────────────────────────┐
│ Medication Schedule In │
│   Encrypted Room DB    │
└───────────┬────────────┘
            │
            ▼
┌────────────────────────┐
│     AlarmScheduler     │
│ setExactAndAllowWhile  │◄─────────────┐
│         Idle()         │              │
└───────────┬────────────┘              │
            │ Fires at exact second     │ On Reboot (ACTION_BOOT_COMPLETED)
            ▼                           │
┌────────────────────────┐              │
│    ReminderReceiver    │      ┌───────┴────────┐
│  (BroadcastReceiver)   │      │  BootReceiver  │
└───────────┬────────────┘      └────────────────┘
            │
            ▼
┌──────────────────────────────────────────────┐
│  Full-Screen Heads-Up Notification + Audio   │
│  • Distinct high-contrast pill imagery       │
│  • Pre-recorded voice: "Please take medicine"│
│  • Big Green Button: "TAKEN"                 │
└──────────────────────────────────────────────┘
```

### 4.1 Alarm Execution Sequence
1. **Scheduling:** `AlarmScheduler` calculates the exact epoch millisecond and calls:
   ```kotlin
   alarmManager.setExactAndAllowWhileIdle(
       AlarmManager.RTC_WAKEUP,
       triggerAtMillis,
       pendingIntent
   )
   ```
2. **Execution:** Even if the phone is asleep in deep Doze, the CPU wakes up and dispatches the intent to `ReminderReceiver`.
3. **User Notification:** `ReminderReceiver` builds a high-priority Heads-Up Notification with `NotificationCompat.PRIORITY_MAX` and `CATEGORY_ALARM`, triggering full-screen takeover, loud custom audio, and vibration.
4. **Boot Recovery:** `BootReceiver` intercepts device restarts, re-reads all pending schedules from Room, and calls `AlarmScheduler` to re-arm every alarm.
5. **OEM Whitelisting:** An interactive onboarding module guides the caregiver to disable background restrictions on aggressive vendor skins (Xiaomi MIUI/HyperOS, Oppo ColorOS, Vivo FuntouchOS, Samsung OneUI).

---

## 5. Hybrid Emergency Alert Pipeline

In rural environments, internet connectivity may drop for extended periods. Shiori uses a **dual-channel alerting pipeline** to guarantee caregiver notification when life-critical medication is unacknowledged.

```text
                        MEDICATION ALARM FIRES
                                   │
                     30-Minute Grace Window Begins
                                   │
                    DID PATIENT TAP "TAKEN"?
                                   │
                  ┌────────────────┴────────────────┐
                  │ YES                             │ NO (Unacknowledged)
                  ▼                                 ▼
         Dismiss Alarm Notification          CHECK INTERNET STATUS
          Update Room to "TAKEN"                    │
          Queue Normal Data Sync      ┌─────────────┴─────────────┐
                                      │ ONLINE                    │ OFFLINE / NO DATA
                                      ▼                           ▼
                              FIREBASE FCM PUSH            ANDROID SMS MANAGER
                           (Caregiver Phone Alert)        (Direct Cellular SMS to
                                                          Caregiver Mobile Number)
```

- **Online Path:** Dispatches an immediate high-priority Firebase Cloud Messaging (FCM) push notification to the caregiver's registered smartphone.
- **Offline / Cellular Fallback:** If internet is unavailable but the device detects cellular signal, Android `SmsManager` sends an automated SMS directly to the caregiver:
  > `"[Shiori Alert] Urgent: [Patient Name] has not taken their scheduled [Time] dose of [Medicine Name]. Please call or check on them."`

---

## 6. Two-Tier Adaptive AI Engine

The cognitive adaptation system is isolated behind a clean domain abstraction:

```kotlin
interface DifficultyAdapterModel {
    suspend fun calculateNextDifficulty(
        patientHistory: List<GameSessionSummary>
    ): DifficultyParameters
}
```

```text
                             GAME ENGINE (Compose Canvas)
                                          │
                               Emits Game Metrics:
                        • Accuracy (% correct moves)
                        • Reaction Latency (milliseconds)
                        • Hesitation / Hint Requests
                                          │
                                          ▼
                             DifficultyAdapterModel Interface
                                          │
                  ┌───────────────────────┴───────────────────────┐
                  │                                               │
           [Tier 1: v1 MVP]                              [Tier 2: v2 Upgrade]
     StaircaseDifficultyAdapter                      TFLiteDifficultyAdapter
  (Deterministic Staircase Algorithm)             (On-Device Machine Learning)
  • Accuracy > 80% (3 runs): Step Up              • Loads quantized .tflite model
  • Accuracy < 50%: Step Down                     • Evaluates multi-session curves
  • Long latency: Trigger visual hints            • Predicts customized game grids
                  │                                               │
                  └───────────────────────┬───────────────────────┘
                                          │
                                          ▼
                               NEW DIFFICULTY PARAMETERS
                          (Grid Size, Card Pairs, Speed)
```

---

## 7. Security Architecture & India DPDP Act 2023 Compliance

### 7.1 Data Protection at Rest
- **SQLCipher AES-256:** The entire SQLite database file is encrypted on disk using SQLCipher. Plaintext queries and unencrypted cache files are never written to device flash memory.
- **Hardware-Backed Keystore:** The 256-bit AES encryption passphrase is generated using `KeyGenerator` and stored in the **Android Keystore System**. On devices supporting it, keys are stored inside a hardware-isolated security module (Trusted Execution Environment - TEE, or StrongBox).

### 7.2 Data Protection in Transit
- Strict **TLS 1.3** transport encryption enforced for all Firebase Firestore and FCM communication.
- Certificate pinning prevents man-in-the-middle (MITM) attacks on untrusted public/rural Wi-Fi networks.

### 7.3 India DPDP Act 2023 & Healthcare Governance
- **Guardian / Caregiver Proxy Consent:** Because elderly patients experiencing dementia may have diminished cognitive capacity, the onboarding architecture includes a structured proxy consent workflow where family caregivers or legal guardians record legal consent.
- **Purpose Limitation:** Health-adjacent cognitive metrics are used strictly for local game adaptation and caregiver tracking. Data is never shared with third-party advertisers.
- **Medical Disclaimer:** Visible on setup and dashboard:
  > *"Shiori is an assistive cognitive stimulation and daily routine tool. It is not a certified diagnostic medical device."*
- **Right to Erasure & Local-Only Mode:** Caregivers can toggle "Local-Only Storage", disabling all cloud syncing, or trigger a full wipe of all encrypted local and cloud records with a single tap.
