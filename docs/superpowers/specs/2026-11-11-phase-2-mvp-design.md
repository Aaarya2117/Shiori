# Design Specification: Shiori Phase 2 (MVP Complete)

**Date:** 2026-11-11  
**Status:** Approved by User  
**Target Milestone:** Phase 2 (MVP Complete / Demo-Ready) per PRD & Implementation Plan  
**Authors:** Shiori Engineering Team  

---

## 1. Overview & Objective

Shiori is an offline-first cognitive gaming and memory assistance platform designed for elderly individuals with Mild Cognitive Impairment (MCI) and early-stage dementia, along with their caregivers.

While Phase 1 established Person 1's core architecture, Room DB encryption scaffolding, and exact-alarm reminder engine, Phase 2 completes the demo-ready MVP across all four architectural roles:
- **Person 2 (Patient UI & Games):** Integrates 3 cognitive games (`MemoryMatch`, `PatternSequence`, `FaceRecall`), persists gameplay results into Room DB, and delivers the Daily Routine checklist screen.
- **Person 3 (AI & Voice):** Delivers the `Tier1StaircaseDifficultyAdapter` (deterministic on-device algorithm) and `AudioPromptManager` with multilingual voice synthesis in English, Hindi, and Kannada.
- **Person 4 (Caregiver Mode & Local Analytics):** Implements a 4-digit PIN gate, read-only local Caregiver Dashboard with medication adherence and cognitive accuracy trends, and a DPDP Act 2023 proxy-consent disclaimer.

The target system is fully functional without internet access, ensuring privacy, offline resilience, and immediate responsiveness.

---

## 2. Architecture & Component Structure

```
com.shiori.app
├── alarm/                  # (Person 1) AlarmScheduler, ReminderReceiver, BootReceiver
├── data/
│   ├── local/              # Room DB, DAOs, Entities, Converters
│   └── repository/         # ReminderRepositoryImpl, PatientProfileRepositoryImpl, GameRepositoryImpl
├── domain/
│   ├── model/              # GameSession, PatientProfile, ReminderRecord, MedicationSchedule, DailyRoutineItem
│   └── repository/         # IGameRepository, IPatientRepository, IReminderRepository
├── ml/                     # (Person 3) DifficultyAdapterModel, Tier1StaircaseDifficultyAdapter
├── security/               # (Person 1) KeystoreManager
├── sync/                   # (Person 4) SyncWorker (local-only fallback)
├── ui/
│   ├── caregiver/          # (Person 4) CaregiverPinScreen, CaregiverDashboardScreen, DpdpDisclaimerDialog
│   ├── navigation/         # NavRoutes, ShioriNavHost
│   ├── patient/            # (Person 2) PatientHomeScreen, MemoryMatchScreen, PatternSequenceScreen, FaceRecallScreen, DailyRoutineScreen
│   └── theme/              # Color, Type, Theme (High contrast, ≥64dp touch targets)
└── voice/                  # (Person 3) AudioPromptManager (TTS for EN, HI, KN)
```

---

## 3. Subsystem Detailed Specifications

### 3.1 Person 2 — Patient UI, Games & Daily Routine

1. **Cognitive Games Integration & Room DB Persistence**:
   - Merge `feature/person-2-patient-ui` into `main`.
   - Each game receives `IGameRepository` and `Tier1StaircaseDifficultyAdapter` (via ViewModel or Composable state).
   - Upon session completion, a `GameSession` is saved to Room DB:
     - `gameType`: `"MEMORY_MATCH"`, `"PATTERN_SEQUENCE"`, or `"FACE_RECALL"`
     - `difficultyLevel`: 1..5
     - `score`: integer (0..100)
     - `accuracy`: float (0.0..1.0)
     - `durationSeconds`: integer
     - `completedAt`: System.currentTimeMillis()
     - `syncStatus`: `SyncStatus.LOCAL_ONLY`
   - Plays spoken verbal praise via `AudioPromptManager` (e.g. "Great job!", "Shabash!", "Uttama!").

2. **Daily Routine Checklist Screen (`DailyRoutineScreen`)**:
   - Organized into three chronological buckets:
     - **Morning**: Morning Walk, Hydration, Morning Medication.
     - **Afternoon**: Healthy Lunch, Hydration, Cognitive Brain Game.
     - **Evening**: Light Dinner, Evening Medication, Rest & Sleep Prep.
   - Large touch chips (≥64dp height) with clear checkboxes and tactile sound/audio prompt feedback.
   - Completion rate is persisted and displayed on both Patient Home and Caregiver Dashboard.

---

### 3.2 Person 3 — AI / Adaptive Difficulty & Multilingual Audio

1. **Deterministic Staircase Algorithm (`Tier1StaircaseDifficultyAdapter`)**:
   - Implements `DifficultyAdapterModel.calculateNextDifficulty(recentSessions: List<GameSession>): Int`.
   - Rules:
     - Minimum Level: 1. Maximum Level: 5.
     - **Step Up (+1)**: If the patient's last 2 completed sessions for that specific game both achieved $\ge 80\%$ accuracy.
     - **Step Down (-1)**: If the patient's most recent completed session had $< 50\%$ accuracy.
     - **Hold**: Retain current difficulty if performance is between $50\%$ and $79\%$.
   - **Game Parameter Scaling**:
     - *Memory Match*:
       - Level 1: 6 cards (3 pairs)
       - Level 2: 8 cards (4 pairs)
       - Level 3: 12 cards (6 pairs)
       - Level 4–5: 16 cards (8 pairs, 4x4 grid)
     - *Pattern Sequence*:
       - Level 1: 3-step sequence
       - Level 2: 4-step sequence
       - Level 3: 5-step sequence
       - Level 4: 6-step sequence
       - Level 5: 7-step sequence
     - *Face Recall*:
       - Level 1–2: 8s preview, 2 choice options
       - Level 3–4: 5s preview, 3 choice options
       - Level 5: 3s preview, 4 choice options

2. **Multilingual Audio Engine (`AudioPromptManager`)**:
   - Integrates native Android `TextToSpeech` with graceful fallback.
   - Supports 3 core locales:
     - English: `Locale.US` / `Locale.UK`
     - Hindi: `Locale("hi", "IN")`
     - Kannada: `Locale("kn", "IN")`
   - Supported prompts:
     - `WELCOME`: "Welcome to Shiori. Let's exercise your memory today." / "शिओरी में आपका स्वागत है। चलिए अपनी याददाश्त का अभ्यास करें।" / "ಶಿಯೋರಿಗೆ ಸುಸ್ವಾಗತ. ನಿಮ್ಮ ನೆನಪಿನ ಶಕ್ತಿಯನ್ನು ಅಭ್ಯಾಸ ಮಾಡೋಣ."
     - `PRAISE`: "Excellent work!" / "शाबाश! बहुत बढ़िया!" / "ಉತ್ತಮ ಕೆಲಸ!"
     - `ENCOURAGE`: "Good try, let's keep going!" / "अच्छा प्रयास, जारी रखें!" / "ಉತ್ತಮ ಪ್ರಯತ್ನ, ಮುಂದುವರಿಸಿ!"
     - `REMINDER`: "Time for your scheduled medication." / "दवा लेने का समय हो गया है।" / "ನಿಮ್ಮ ಔಷಧಿ ತೆಗೆದುಕೊಳ್ಳುವ ಸಮಯ."
   - Language selector toggle on `PatientHomeScreen` top bar.

---

### 3.3 Person 4 — Caregiver Dashboard, PIN Gate & Local Analytics

1. **Caregiver PIN Gate (`CaregiverPinScreen`)**:
   - 4-digit PIN authentication protecting caregiver access.
   - Default PIN: `"1234"` (configurable in settings).
   - Large numeric keypad (0–9, Backspace, Clear).
   - Clear error states for wrong PIN; auto-clears on 4 digits.

2. **Caregiver Dashboard (`CaregiverDashboardScreen`)**:
   - **Patient Header**: Active patient profile, current language, last activity.
   - **Medication Adherence Widget**: Today's medication schedules and reminder records from `IReminderRepository`. Badges: `TAKEN`, `PENDING`, `MISSED`.
   - **Cognitive Trend Widget**:
     - Total games played today.
     - Average cognitive accuracy percentage.
     - Per-game current difficulty level (Levels 1–5).
     - Recent session history cards.
   - **Daily Routine Adherence**: Visual progress indicator of completed routine checklist items for today.
   - **Navigation & Controls**: "Back to Patient Mode", "View DPDP & Medical Disclaimer".

3. **DPDP Compliance & Medical Disclaimer**:
   - Modal dialog / view per India's Digital Personal Data Protection (DPDP) Act 2023.
   - Informs the caregiver that patient health and gaming data is retained strictly on-device in Local-Only Mode.
   - Prominent disclaimer: *"Shiori is an assistive cognitive stimulation and reminder platform. It is not a clinical medical device and does not diagnose, treat, or prevent neurological disorders."*

---

## 4. Navigation & Flow

```
                     ┌──────────────────┐
                     │ PatientHomeScreen│
                     └────────┬─────────┘
        ┌─────────────┼───────────────┼────────────────┐
        ▼             ▼               ▼                ▼
┌──────────────┐┌──────────────┐┌──────────────┐┌──────────────┐
│ MemoryMatch  ││PatternSequence│FaceRecall    ││ DailyRoutine │
└───────┬──────┘└──────┬───────┘└──────┬───────┘└──────────────┘
        │              │               │
        └──────────────┴───────┬───────┘
                               │ (Game completion)
                               ▼
                    ┌──────────────────────┐
                    │ Room DB (GameSession)│
                    └──────────┬───────────┘
                               │
                               ▼
                    ┌──────────────────────┐
                    │  CaregiverPinScreen  │
                    └──────────┬───────────┘
                               │ (Correct PIN: 1234)
                               ▼
                    ┌────────────────────────┐
                    │CaregiverDashboardScreen│
                    └────────────────────────┘
```

---

## 5. Testing & Verification

1. **`Tier1StaircaseDifficultyAdapterTest`**:
   - Verify 2 consecutive sessions $\ge 80\%$ increments difficulty.
   - Verify 1 session $< 50\%$ decrements difficulty.
   - Verify boundaries: does not decrement below 1 or increment above 5.
   - Verify neutral scores ($50..79\%$) retain current level.
2. **`CaregiverDashboardTest` & `DailyRoutineTest`**:
   - Test adherence percentage calculation for routines.
   - Test PIN validation (correct PIN vs invalid PIN).
   - Test aggregation of game sessions into average accuracy.
3. **Build & Integration Test**:
   - `./gradlew testDebugUnitTest` must pass with zero failures.
   - App must build clean APK (`./gradlew assembleDebug`).
