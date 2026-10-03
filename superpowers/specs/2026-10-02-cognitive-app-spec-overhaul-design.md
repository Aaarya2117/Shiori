# System Specification & Architecture Overhaul Design
## Project: Shiori (AI Cognitive Gaming & Memory Assistance Platform)
**Date:** 2026-10-02  
**Status:** Approved & Implemented into Specifications  

---

## 1. Overview & Objectives

This design document consolidates the comprehensive architectural and requirements overhaul for **Shiori**, an AI-powered cognitive therapy, memory assistance, and daily routine platform for elderly dementia and MCI patients in India.

The design resolves all 12 architectural drawbacks identified in `drawbacks_and_fixes.md` and integrates the complete navigation structure and offline-first workflows from `cognitive_app_flowcharts.md`.

---

## 2. Core Subsystem Architecture

### 2.1 Dual-Mode Navigation & Flow Isolation
- **Patient Mode:** Zero-friction, high-contrast, audio-guided interface featuring:
  - 1. Play Games (Cognitive stimulation via Compose Canvas)
  - 2. Memory Training (Family photos, familiar objects)
  - 3. Daily Routine (Morning/Afternoon/Evening visual checklist)
  - 4. Reminders (Medication & hydration alarms)
  - 5. Profile & Settings (Language selection: Hindi, English, Kannada; emergency contact card)
- **Caregiver Mode (In-App PIN Gate):**
  - 1. Patient Progress (Cognitive engagement trends)
  - 2. Game Performance (Accuracy & response latency curves)
  - 3. Medication & Routine Management (Schedules and dosages)
  - 4. Alerts & Reports (Missed dose audit logs, exportable doctor summary report)
  - 5. Data Freshness Banner (`"Last synced: X minutes ago"`)

### 2.2 Life-Critical Reminders Engine
- **Engine:** Android `AlarmManager` utilizing `setExactAndAllowWhileIdle()`.
- **Permissions:** `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM`.
- **Doze & Battery Resilience:** Wakes CPU even under Android Doze mode.
- **Reboot Resilience:** `BootReceiver` intercepts `ACTION_BOOT_COMPLETED` and recalculates/re-registers all pending alarms.
- **OEM Whitelist Guide:** Interactive onboarding instructions for Xiaomi (MIUI/HyperOS), Oppo (ColorOS), Vivo (FuntouchOS), and Samsung (OneUI).

### 2.3 Offline-First Synchronization & Data Integrity
- **Single Source of Truth (SSoT):** Local Room database.
- **Sync Replica:** Firebase Firestore.
- **Worker:** Jetpack `WorkManager` enqueued with `NetworkType.CONNECTED` constraint.
- **Conflict Resolution:** Last-Write-Wins (LWW) via UTC millisecond timestamps.
- **UI Decoupling:** UI reads exclusively from Room via Kotlin Flow; no network calls on render path.

### 2.4 Hybrid Caregiver Emergency Alert System
- **Grace Window:** 30-minute unacknowledged timer on critical medication.
- **Online:** Instant high-priority push notification via Firebase Cloud Messaging (FCM).
- **Offline / Cellular Fallback:** Direct SMS via Android `SmsManager` sent to registered caregiver phone number.

### 2.5 Two-Tier Adaptive AI Engine
- **Contract:** Domain interface `DifficultyAdapterModel`.
- **Tier 1 (v1 MVP):** Deterministic Staircase Heuristic (accuracy band 65%–80%, reaction latency thresholds).
- **Tier 2 (v2 Extension):** On-device TensorFlow Lite (`org.tensorflow:tensorflow-lite`) model trained on simulated cognitive trajectories.

### 2.6 Audio & Multilingual Engine
- **Primary Languages:** Hindi, English, Kannada.
- **Offline Audio:** High-quality studio-recorded `.mp3`/`.ogg` audio prompts in `res/raw/` for 100% offline consistency.
- **Voice I/O:** Native Android `SpeechRecognizer` and `TextToSpeech` APIs. (Corrected: ML Kit removed from STT/TTS).
- **Cloud Voice:** Bhashini REST API reserved as an optional online enhancement.

### 2.7 Security, Privacy & DPDP Act 2023
- **Database Encryption:** SQLCipher (AES-256) integrated with Room.
- **Key Storage:** Android Keystore System with hardware TEE/StrongBox backing.
- **Transport Security:** TLS 1.3 enforced for cloud endpoints.
- **Legal Compliance:** Guardian / Caregiver Proxy Consent workflow aligned with India DPDP Act 2023; mandatory non-diagnostic medical disclaimer.

---

## 3. Tech Stack Optimization (Bloat Elimination)

| Component | Previous State | Revised State |
|---|---|---|
| Game Graphics | libGDX (15MB+ APK bloat) | **Jetpack Compose Canvas API** |
| Reminders | WorkManager (delayed by Doze) | **AlarmManager (`setExactAndAllowWhileIdle`)** |
| Database Security | Keystore alone (unencrypted SQLite) | **SQLCipher (AES-256) + Keystore Master Key** |
| Voice Engine | ML Kit (unsupported for STT/TTS) | **Native Speech APIs + Bundled Offline Audio** |
| Networking | Retrofit + OkHttp (redundant) | **Firebase Firestore Native SDK** |
| Caregiver Alerts | Cloud-only (fails in rural areas) | **Hybrid FCM Push + Android `SmsManager` SMS Fallback** |

---

## 4. Verification & Validation Checklist

- [x] All 12 items from `drawbacks_and_fixes.md` addressed and reflected across all files.
- [x] All navigation paths from `cognitive_app_flowcharts.md` represented in architecture and PRD.
- [x] PRD milestones reordered: Reminders prioritized ahead of adaptive difficulty for early device testing.
- [x] No placeholder text, conflicting requirements, or unresolved open questions.
- [x] Technical language is polished, clear, and production-ready.
