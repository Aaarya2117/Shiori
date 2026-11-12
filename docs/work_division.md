# Work Division — Shiori (4-Person Team)

Roles are split by architectural layer (per `ARCHITECTURE.md`) so each person owns a vertical slice end-to-end and can work in parallel with minimal blocking. Pair up for integration points (noted below).

---

## Person 1 — Core Architecture, Security & Reminder Engine
**Owns:** `data/`, `alarm/`, `security/`, app foundation

### Responsibilities
- Project scaffolding, Hilt DI setup, Gradle/version catalog (Phase 0)
- Room database schema, DAOs (Phase 0–1)
- `AlarmManager` exact-alarm scheduling (`setExactAndAllowWhileIdle`) (Phase 1)
- `ReminderReceiver`, `BootReceiver` (Phase 1–2)
- SQLCipher migration + Android Keystore master key (Phase 3)
- TLS 1.3 / certificate pinning, security review (Phase 3)
- OEM battery-optimization onboarding flow (Phase 3)

### Deliverables by MVP
- Working encrypted-ready local DB
- Medication/hydration/activity reminders firing reliably, surviving reboot

---

## Person 2 — Cognitive Games, Graphics & Patient UI
**Owns:** `ui/patient/`, `ui/theme/`, `ui/components/`

### Responsibilities
- Compose Canvas game framework (reusable base for all games) (Phase 1)
- Memory Match game (Phase 1), Pattern Sequence + Face Recall (Phase 2)
- Daily Routine checklist UI (Phase 2)
- High-contrast, large-touch-target accessible theme (Phase 1–3)
- Lottie micro-animations for positive reinforcement (Phase 2)
- Accessibility audit: WCAG AAA contrast, 64dp+ touch targets (Phase 3)
- Performance pass: 60 FPS rendering across devices (Phase 4)

### Deliverables by MVP
- 3 playable cognitive games + daily routine screen, fully offline, elderly-accessible UI

---

## Person 3 — AI/Adaptive Difficulty, Voice & Multilingual
**Owns:** `ml/`, `voice/`

### Responsibilities
- `DifficultyAdapterModel` interface definition (Phase 1)
- Tier 1 Staircase deterministic difficulty algorithm (Phase 1)
- Bundled pre-recorded audio prompts (Hindi/English/Kannada) + `AudioPromptManager` (Phase 2)
- Native `SpeechRecognizer` / `TextToSpeech` integration (Phase 2)
- Tier 2: TensorFlow Lite model integration, training data prep, `TFLiteDifficultyAdapter` (Phase 4)
- Language expansion planning (Phase 5)

### Deliverables by MVP
- Adaptive difficulty working on all games; full offline voice/audio in 3 languages

---

## Person 4 — Caregiver Dashboard, Cloud Sync & Backend
**Owns:** `ui/caregiver/`, `data/remote/`, `sync/`

### Responsibilities
- Caregiver Mode UI shell + PIN-gating (Phase 0–1)
- Read-only local dashboard (scores, medication status) (Phase 1)
- Trends/charts (accuracy, routine completion) from local data (Phase 2)
- Firebase Firestore integration + `SyncWorker` (opportunistic, WorkManager-driven) (Phase 3)
- Firebase Auth (caregiver login), FCM push alerts (Phase 3)
- SMS fallback via `SmsManager` for offline alerts (Phase 3)
- "Last synced" freshness banner, Local-Only Mode toggle (Phase 3)
- DPDP proxy-consent flow, medical disclaimer, PDF export for doctor visits (Phase 3–4)
- Crashlytics + analytics wiring (Phase 4)

### Deliverables by MVP
- Caregiver can view patient progress and medication status on-device (sync/alerts come in Phase 3)

---

## Shared / Integration Points (pair up as needed)
| Integration | Owners |
|---|---|
| Game session data flowing into Caregiver dashboard | Person 2 + Person 4 |
| Reminder events triggering both local alert AND sync/SMS | Person 1 + Person 4 |
| Difficulty engine reading game metrics from Canvas games | Person 2 + Person 3 |
| Encrypted DB schema supporting both local and sync layers | Person 1 + Person 4 |
| Consent flow gating voice/data collection features | Person 3 + Person 4 |

---

## Suggested Working Rhythm
- **Daily/alternate-day standup:** 10–15 min — blockers, integration needs
- **End of each phase:** merge to `dev`, run a full integration test together (all 4 present)
- **Code reviews:** cross-review across pairs (Person 1 ↔ Person 4, Person 2 ↔ Person 3) to keep architectural knowledge shared, not siloed

---

## Phase-to-Owner Quick Reference

| Phase | Person 1 (Core/Security) | Person 2 (Games/UI) | Person 3 (AI/Voice) | Person 4 (Caregiver/Cloud) |
|---|---|---|---|---|
| 0 — Setup | Scaffolding, DB schema | Theme base | Interface definitions | Caregiver shell + PIN |
| 1 — Core MVP | Alarm engine | Memory Match | Tier 1 difficulty | Read-only dashboard |
| 2 — MVP Complete | Boot recovery | 2 more games + routine | Audio/voice (3 langs) | Trends/charts |
| 3 — Hardening | SQLCipher, security | Accessibility audit | — (support testing) | Firebase sync, SMS, consent |
| 4 — Scale | Device testing support | Performance pass | Tier 2 TFLite | Crashlytics, PDF export |
| 5 — Launch | — | — | Language expansion | Play Store, monitoring |
