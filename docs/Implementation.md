# Implementation Plan — Shiori
## AI Cognitive Gaming & Memory Assistance Platform

**Team size:** 4
**Approach:** Build a working MVP first (hackathon-ready), then harden toward production.

---

## Phase Overview

```
Phase 0        Phase 1         Phase 2          Phase 3           Phase 4           Phase 5
Setup      ──► Core MVP   ──►  MVP Complete ──► Production    ──► Scale &       ──► Launch &
& Skeleton     (Single          (Demo-Ready)     Hardening         Multi-Device      Monitoring
               Mode)                                               Readiness
```

- **Phases 0–2 = MVP track** (get a working, demoable app)
- **Phases 3–5 = Production track** (harden, scale, ship)

---

## Phase 0 — Setup & Architectural Skeleton
**Goal:** Everyone can build and run the app; architecture is agreed and scaffolded so 4 people can work in parallel without collisions.

**Duration:** 2–3 days

### Tasks
- Create Git repo, branching strategy (`main` / `dev` / feature branches), PR review rule
- Set up Gradle project: Kotlin, Jetpack Compose, Hilt, version catalog (`libs.versions.toml`)
- Scaffold package structure exactly as per `ARCHITECTURE.md` (`ui/`, `domain/`, `data/`, `alarm/`, `ml/`, `voice/`, `sync/`)
- Set up Room database (unencrypted for now — SQLCipher added in Phase 3)
- Set up empty Dual-Mode navigation shell: Patient Mode landing + placeholder PIN-gated Caregiver Mode screen
- Define core domain models: `PatientProfile`, `GameSession`, `MedicationSchedule`, `AlertEvent`
- Agree on coding conventions, commit message format, and CI lint check (ktlint/detekt optional)

### Exit Criteria
- App builds and runs on a physical/emulated device, showing empty Patient/Caregiver shells
- All 4 members can run the project locally without conflicts

---

## Phase 1 — Core MVP (Single Priority Path)
**Goal:** One cognitive game, one reminder, and local storage working end-to-end, fully offline. This is the thinnest possible vertical slice.

**Duration:** 1–1.5 weeks

### Tasks
- **Memory Match game** built on Jetpack Compose Canvas, with basic scoring
- Tier 1 **Staircase Adaptive Difficulty** (deterministic, on-device) wired to the one game
- Room DB: store game sessions locally (no encryption yet)
- **One reminder type** (medication) using `AlarmManager.setExactAndAllowWhileIdle()`
- Basic `ReminderReceiver` with a simple notification + "TAKEN" button
- Basic Patient Mode UI: large-touch tiles, high-contrast theme (first pass)
- Basic Caregiver Mode: read-only list of today's game scores and medication status (local data only, no sync yet)

### Exit Criteria
- A patient can play Memory Match, difficulty adapts, a medication reminder fires and can be marked "taken" — all fully offline, all visible in Caregiver Mode on the same device

---

## Phase 2 — MVP Complete (Demo-Ready)
**Goal:** Expand breadth to match the PRD's MVP feature set — enough for a convincing hackathon/judge demo.

**Duration:** 1.5–2 weeks

### Tasks
- Add **Pattern Sequence** and **Face Recall** games (reuse Canvas game framework from Phase 1)
- Add **Daily Routine** checklist screen (morning/afternoon/evening, single-tap completion)
- Expand reminders to hydration + daily activity + appointments
- `BootReceiver` — re-arm alarms after device reboot
- Bundle pre-recorded audio prompts for **Hindi, English, Kannada**; wire `AudioPromptManager`
- Native `SpeechRecognizer` / `TextToSpeech` integration for optional voice commands
- Caregiver Mode: basic charts/trends (accuracy over time, routine completion rate) using local data
- Lottie micro-animations for positive reinforcement
- DPDP-aligned consent screen + medical disclaimer (basic version, text-only is fine for MVP)

### Exit Criteria
- Full PRD Patient Mode feature set works offline
- Caregiver Mode shows meaningful trends from local data
- App is demo-ready: can be shown end-to-end without a network connection

---

## Phase 3 — Production Hardening
**Goal:** Take the MVP from "demo works" to "safe to put in front of real patients and caregivers."

**Duration:** 2–3 weeks

### Tasks
- Migrate Room → **SQLCipher** (AES-256) with Android Keystore-backed master key
- Implement **Firebase Firestore** sync (Last-Write-Wins), gated behind `WorkManager` opportunistic sync
- Firebase Auth for caregiver login (OTP/email)
- Firebase Cloud Messaging for online alerts
- **SMS fallback** via `SmsManager` for unacknowledged medication alerts when offline
- "Last synced: X minutes ago" freshness banner in Caregiver Mode
- Full DPDP Act 2023 proxy-consent flow (not just a disclaimer screen)
- "Local-Only Mode" toggle (disable cloud sync entirely)
- OEM battery-optimization onboarding (Xiaomi/Oppo/Vivo/Samsung guidance screens)
- Accessibility audit: WCAG AAA contrast check, 64dp+ touch target audit across all screens
- Security review: encrypted-at-rest check, TLS 1.3 + certificate pinning for network calls

### Exit Criteria
- Patient data is encrypted at rest and in transit
- Caregiver alerts work in both online and fully offline (SMS) scenarios
- App survives a device reboot with all alarms intact
- Passes an internal accessibility + security checklist review

---

## Phase 4 — Scale & Multi-Device Readiness
**Goal:** Make the app robust across the device/OEM diversity it will actually face in the field.

**Duration:** 2 weeks

### Tasks
- Device testing matrix: low-end (2GB RAM) to mid-range, across Xiaomi/Samsung/Vivo/Oppo
- Performance pass: confirm 60 FPS game rendering, <100ms DB queries, <3% battery/24h
- APK size audit (<25MB excluding audio packs)
- Tier 2: integrate **TensorFlow Lite** `DifficultyAdapterModel` behind the existing interface, A/B-able against Tier 1
- Expand caregiver reporting: exportable PDF summary for doctor visits
- Stress-test sync conflict resolution (multiple devices, intermittent connectivity)
- Crash reporting + analytics (Firebase Crashlytics) wired in
- Internal beta with a small group of real (or simulated) patient/caregiver pairs

### Exit Criteria
- App performs acceptably on low-end target hardware
- Tier 2 AI difficulty engine is live and switchable from Tier 1
- No critical bugs from internal beta feedback

---

## Phase 5 — Launch & Monitoring
**Goal:** Ship and keep it healthy.

**Duration:** Ongoing

### Tasks
- Play Store listing, privacy policy, DPDP compliance documentation finalized
- Staged rollout (closed → open beta → full release)
- Monitor Crashlytics, sync failure rates, alarm delivery reliability in production
- Set up a feedback loop from caregivers (in-app feedback form or support contact)
- Plan post-launch roadmap: additional languages, additional games, iOS evaluation

### Exit Criteria
- Live on Play Store with monitoring in place
- Clear backlog for v2 features

---

## Summary Timeline

| Phase | Focus | Approx. Duration |
|---|---|---|
| 0 | Setup & Skeleton | 2–3 days |
| 1 | Core MVP (vertical slice) | 1–1.5 weeks |
| 2 | MVP Complete (demo-ready) | 1.5–2 weeks |
| 3 | Production Hardening | 2–3 weeks |
| 4 | Scale & Multi-Device Readiness | 2 weeks |
| 5 | Launch & Monitoring | Ongoing |

**MVP milestone (Phases 0–2): ~4–5 weeks**
**Production-ready milestone (Phases 0–4): ~10–12 weeks**
