# Product Requirements Document (PRD)
## AI-Powered Cognitive Therapy, Memory Assistance & Daily Routine Platform for Elderly Dementia Patients

---

## 1. Executive Summary & Vision

### 1.1 Problem Statement
India is experiencing a significant rise in age-related cognitive disorders, including mild cognitive impairment (MCI), dementia, and Alzheimer's disease. Families—particularly in rural, semi-urban, and remote regions—face formidable obstacles in securing timely care:
- **Severe Shortage of Specialists:** Neurologists, geriatricians, and specialized cognitive therapists are concentrated almost entirely in Tier-1 metropolitan centers.
- **Caregiver Burnout & Anxiety:** Family members face emotional and physical exhaustion from 24/7 caregiving, continuous supervision, medication tracking, and dealing with memory-loss-induced confusion.
- **Connectivity & Language Barriers:** Most digital health apps require continuous high-speed internet and cater primarily to urban English speakers, rendering them unusable in rural households with intermittent 2G/3G/4G connectivity.
- **Lack of Non-Pharmacological Interventions:** Daily routine stability, cognitive stimulation, and familiar cultural recall exercises significantly improve quality of life and slow functional decline, yet accessible, low-friction digital tools are virtually absent.

### 1.2 Vision
To create **Shiori**, an accessible, culturally resonant, native Android platform that empowers elderly individuals with cognitive decline to maintain dignity, stimulate cognitive faculties, and follow daily routines independently—while giving family caregivers continuous visibility, peace of mind, and emergency safety nets, even in zero-connectivity environments.

### 1.3 Target Personas
1. **Primary: The Elderly Patient (Mild-to-Moderate Dementia / MCI)**
   - Needs: Large high-contrast touch surfaces, zero complex menus, audio-guided interactions, familiar cultural imagery, and positive, reassuring feedback without judgment.
2. **Secondary: The Family Caregiver (Spouse or Adult Child)**
   - Needs: Dependable medication adherence tracking, activity logs, cognitive stability trends, instant emergency notifications, and effortless setup.
3. **Tertiary: Community Healthcare Workers (ASHA / ANM / Rural Health Monitors)**
   - Needs: Fast, offline-capable verification of patient routine adherence and cognitive trends during periodic village home visits.

### 1.4 Target Platform
- **Platform:** Native Android (Optimized for Android 8.0 Oreo / API 26 through Android 14+).
- **Form Factors:** Android Smartphones and Tablets (entry-level devices with 2GB+ RAM commonly found in rural households).

---

## 2. Core Application Structure & User Flows

The application features a **Dual-Mode Architecture** designed to completely isolate patient simplicity from caregiver controls.

```
                         SHIORI APPLICATION
                                  │
         ┌────────────────────────┴────────────────────────┐
         │                                                 │
   PATIENT MODE (Default)                       CAREGIVER MODE (PIN-Gated)
         │                                                 │
   ┌─────┴───────────────────────┐                   ┌─────┴───────────────────────┐
   ├── 1. Play Games (Cognitive)  │                   ├── 1. Patient Progress        │
   ├── 2. Memory Training        │                   ├── 2. Game Performance       │
   ├── 3. Daily Routine          │                   ├── 3. Medication & Routine   │
   ├── 4. Reminders              │                   ├── 4. Alerts & Reports       │
   └── 5. Profile & Settings     │                   └── 5. Cloud Sync & Settings  │
         │                                                 │
         └────────────────────────┬────────────────────────┘
                                  │
                          LOCAL-FIRST ENGINE
                       (Room DB + SQLCipher)
                                  │
                 ┌────────────────┴────────────────┐
          [Offline / No Data]             [Online / Connected]
                 │                                 │
         • Pre-recorded Audio              • Firebase Sync (LWW)
         • Exact Alarms (AlarmManager)     • Cloud FCM Alerts
         • Direct SMS Fallback             • Remote Caregiver View
```

### 2.1 Patient Mode (Zero-Cognitive-Friction Interface)
Patient Mode is the default landing experience. It presents an uncluttered, high-contrast dashboard with five primary tiles:
1. **Play Games (Cognitive Stimulation):** Simple, engaging 2D exercises targeting focus, attention, and motor coordination.
2. **Memory Training:** Photo-name matching, familiar household object identification, and historical/cultural memory prompts.
3. **Daily Routine:** Visual step-by-step checklist of the patient's day (morning hygiene, meals, walks, rest) with single-tap completion.
4. **Reminders:** Clear visual cards displaying upcoming and pending medication and hydration schedules.
5. **Profile:** Simple view displaying emergency contact information, active language, and volume control.

### 2.2 Caregiver Mode (In-App, PIN-Protected Hub)
Accessible via a discreet, PIN-protected icon on the main screen (eliminating the need for caregivers to install a separate app during MVP):
1. **Patient Progress:** Longitudinal cognitive engagement index, routine completion rates, and daily active session history.
2. **Game Performance:** Reaction times, accuracy rates, and game-specific performance curves over weeks and months.
3. **Medication & Routine Management:** Interface to add, modify, or delete medication schedules, dosages, instructions, and routine checklist items.
4. **Alerts & Reports:** Comprehensive log of missed medications or routine lapses, with an option to generate an exportable PDF/summary report for doctor appointments.
5. **Data Freshness Indicator:** Prominent banner showing `"Last synced from patient phone: [Timestamp / X minutes ago]"`, ensuring caregivers are always aware of connectivity delays.

---

## 3. Detailed Functional Requirements

### 3.1 Cognitive & Memory Gaming Modules
- **Game Variety:**
  - *Memory Match:* Card-flip recall using culturally familiar Indian icons (fruits, regional landmarks, traditional items).
  - *Pattern & Sequence Recall:* Simple visual and sound sequences that patients repeat at their own pace.
  - *Face-Name Association:* Caregivers upload photos of family members, allowing patients to practice identifying close relations.
  - *Object Identification:* Everyday items (keys, spectacles, water tumbler) matched to their everyday purposes.
- **Graphics & Rendering:** Built natively using **Jetpack Compose Canvas API** to ensure 60 FPS performance without heavy external engine dependencies.
- **Positive Reinforcement:** Cheerful audio chimes and subtle, calming Lottie animations celebrate completions; the app **never** displays harsh failure messages or countdown stress timers.

### 3.2 Two-Tier Adaptive Difficulty Engine
To prevent patient frustration (causing abandonment) or boredom (causing lack of stimulation):
- **Tier 1 (v1 MVP - Deterministic Staircase Heuristic):**
  - Runs 100% on-device without internet or training requirements.
  - Dynamically evaluates a rolling window of recent moves based on two metrics:
    - *Accuracy Rate:* Target comfort band is 65%–80%. If accuracy exceeds 80% across 3 consecutive sessions, difficulty steps up. If accuracy drops below 50%, difficulty steps down.
    - *Reaction Latency:* Extended hesitation triggers gentle contextual hints rather than penalizing scores.
- **Tier 2 (v2 Upgrade - TensorFlow Lite Model):**
  - Pluggable replacement implementing the identical `DifficultyAdapterModel` domain interface.
  - Trained on simulated cognitive trajectory datasets to predict optimal personalized game parameters (grid size, distraction elements, hint frequency).

### 3.3 Life-Critical Exact Reminder Engine
- **Hardware-Level Alarm Scheduling:**
  - Powered by **Android `AlarmManager`** using `setExactAndAllowWhileIdle()` paired with `SCHEDULE_EXACT_ALARM` permissions.
  - Alarms fire at the exact scheduled second, even when the device is locked in Android Doze mode.
- **Reboot Resilience:**
  - An Android `BootReceiver` intercepts `ACTION_BOOT_COMPLETED`, queries the encrypted local database, and re-registers all active alarms immediately upon device power-on.
- **High-Priority Alert UI:**
  - Full-screen Heads-Up Notification accompanied by high-volume distinctive audio chime, vibration, and large, clear medication imagery with an unmistakable `"TAKEN"` button.
- **OEM Battery Optimization Guidance:**
  - An onboarding checklist instructs caregivers how to disable aggressive vendor battery killers (Xiaomi MIUI/HyperOS, Oppo ColorOS, Vivo FuntouchOS, Samsung OneUI) to prevent background process termination.

### 3.4 Multilingual Audio & Voice Guidance
- **Priority Launch Languages:** **Hindi, English, and Kannada**.
- **100% Offline Pre-Recorded Audio Prompts:**
  - All core system interactions, button titles, navigation guidance, and game instructions are bundled as pre-recorded studio-quality audio assets (`.mp3`/`.ogg`) in the app.
  - Guarantees natural, culturally familiar voice instructions on all low-end devices regardless of whether the OEM installed regional voice packs.
- **Native Voice Input/Output:**
  - Integrates native Android `SpeechRecognizer` and `TextToSpeech` for optional hands-free commands when supported by the device.
  - Cloud Bhashini API is maintained as an optional enhancement for expanded regional dialect support when high-speed internet is available.

### 3.5 Hybrid Caregiver Alert System
Designed specifically for rural environments where internet access may be absent for days:
- **Cloud Notification (Online):** When data connectivity is present, missed medication events dispatch instant push notifications via Firebase Cloud Messaging (FCM).
- **Direct Cellular SMS Fallback (Offline):**
  - If a life-critical medication reminder remains unacknowledged after a configurable grace period (default: 30 minutes) and the device has no internet access, the app uses Android `SmsManager` to dispatch a direct cellular SMS to the caregiver’s registered phone number:
    > `"[Shiori Alert] [Patient Name] has not marked their [Time] dose of [Medicine Name] as taken. Please verify."`
- **Dashboard Transparency:** Caregivers are presented with a clear freshness timestamp (`"Last updated: 2 hours ago"`), avoiding false assumptions of well-being when devices are offline.

### 3.6 Offline-First Data Synchronization
- **Single Source of Truth (SSoT):**
  - The local Room Database is the sole authority for the patient's device.
  - Every game score, medication log, and routine checkbox is written to Room first.
- **Opportunistic Background Sync:**
  - Jetpack `WorkManager` monitors network connectivity constraints (`NetworkType.CONNECTED`).
  - When connection is detected, un-synced local records are pushed to Firebase Firestore.
- **Conflict Resolution:** Last-Write-Wins (LWW) utilizing device UTC timestamps.

### 3.7 Privacy, Consent & Legal Compliance (India DPDP Act 2023)
- **Guardian / Caregiver Proxy Consent:**
  - Recognizes that elderly individuals with cognitive impairment cannot independently execute digital consent.
  - Provides a legally aligned Guardian Consent flow where family caregivers or legal guardians record proxy authorization for data collection.
- **Mandatory Medical Disclaimer:**
  - Prominently stated during onboarding and on dashboard footers:
    > *"Shiori is an assistive cognitive stimulation and daily routine tool. It is not a certified diagnostic medical device and does not substitute for clinical neurological evaluation or medical treatment."*
- **Local-Only Mode:** Caregivers can toggle off cloud synchronization entirely, keeping 100% of data encrypted locally on the physical device.

---

## 4. Non-Functional Requirements

| Metric | Target | Rationale |
|---|---|---|
| **Offline Autonomy** | 100% core features functional offline | Rural India suffers frequent network dropouts; patients must never be locked out of games, routines, or alarms. |
| **Alarm Reliability** | 99.9% on-time delivery across Doze | Medication timing in geriatric care is life-critical. |
| **Touch Accessibility** | Minimum 64dp touch targets; WCAG AAA contrast | Compensates for tremors, failing eyesight, and reduced tactile precision in elderly users. |
| **UI Responsiveness** | < 16ms frame render (60 FPS); < 100ms DB queries | Smooth animations prevent confusion; sluggish interfaces cause distress. |
| **Binary Footprint** | APK size < 25 MB (excluding audio packs) | Downloadable on constrained rural mobile networks. |
| **Storage Security** | AES-256 encrypted database at rest | Protects sensitive health-adjacent cognitive and medical data. |
| **Battery Consumption**| < 3% battery usage over 24-hour cycle | Ensures elderly patients do not wake up to discharged devices. |

---

## 5. Development Milestones & Phased Execution

```
[Milestone 1] ──► [Milestone 2] ──► [Milestone 3] ──► [Milestone 4] ──► [Milestone 5] ──► [Milestone 6]
 Foundation        Exact Alarms       2D Games &        Multilingual       Cloud Sync &       TFLite AI &
 & Security       & Boot Engine      Staircase AI       Audio Engine        SMS Alerts        Submission
```

1. **Milestone 1: Architectural Foundation & Encrypted Storage**
   - Clean Architecture skeleton, Hilt dependency injection, Compose UI base theme.
   - Dual-Mode navigation router (Patient Mode + PIN-protected Caregiver Hub).
   - Room Database initialized with SQLCipher AES-256 encryption via Android Keystore.
2. **Milestone 2: Life-Critical Exact Alarm Engine**
   - `AlarmManager` integration (`setExactAndAllowWhileIdle()`) for medication and hydration.
   - `BootReceiver` for automated alarm recovery upon device reboot.
   - Full-screen high-priority alert UI; OEM battery optimization onboarding flow.
3. **Milestone 3: Cognitive & Memory Games with Tier 1 AI**
   - Compose Canvas games: Memory Match, Familiar Face Recall, Object Association.
   - Daily Routine visual checklist.
   - Tier 1 Deterministic Staircase Adaptive Difficulty algorithm running in real time.
4. **Milestone 4: Multilingual Voice & Audio Guidance**
   - Bundled pre-recorded audio prompts for Hindi, English, and Kannada.
   - Native `SpeechRecognizer` and `TextToSpeech` integration.
   - Audio playback manager with volume normalization.
5. **Milestone 5: Offline-First Synchronization & Caregiver Hub**
   - WorkManager opportunistic background sync to Firebase Firestore.
   - Caregiver dashboard with cognitive trends, adherence charts, and "Last Synced" freshness banner.
   - Hybrid SMS emergency fallback alert via `SmsManager`.
6. **Milestone 6: Tier 2 TFLite Adaptive Upgrade & Compliance Polish**
   - TensorFlow Lite model integration behind `DifficultyAdapterModel`.
   - DPDP Act 2023 proxy consent flow and medical disclaimer screen.
   - End-to-end device testing across OEM brands (Xiaomi, Samsung, Vivo).
