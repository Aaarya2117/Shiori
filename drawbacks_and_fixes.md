# Drawbacks and Fixes: AI Cognitive Gaming & Memory Assistance Platform

Issues found in the PRD, Architecture Document, and Tech Stack, with recommended fixes, ordered by what to tackle first.

## Comparison Table

| # | Area | Drawback (and where it appears) | Fix | Priority | Effort |
|---|------|---------------------------------|-----|----------|--------|
| 1 | Reminders | WorkManager is used for medicine, hydration, and appointment reminders (PRD 3.3, Architecture 2.5, Tech Stack). Android can delay it under Doze and OEM battery savers, so doses could be missed. | Use **AlarmManager** (`setExactAndAllowWhileIdle`) with the exact-alarm permission and a **boot receiver** to reschedule after restart. Keep WorkManager only for sync. Add an onboarding step for disabling battery optimization on Xiaomi/Oppo/Vivo. | Critical | Medium |
| 2 | Voice | ML Kit is listed for speech-to-text and text-to-speech (Tech Stack), but it doesn't provide those. | Remove that from the ML Kit row. Use **SpeechRecognizer** and **TextToSpeech** natively. | High | Low |
| 3 | Voice (offline) | Offline recognition needs downloaded language packs, and Indian language coverage varies by device. Bhashini needs internet. | Add **pre-recorded audio prompts** per supported language for core flows. Use Bhashini only as an online enhancement. | High | Medium |
| 4 | Adaptive AI | There's no patient data to train a TFLite model, so the "AI" claim is weak (PRD 3.1). | Build **v1 as a rule/staircase algorithm** (accuracy and response time) behind the `DifficultyAdapterModel` interface. Add a TFLite model trained on simulated data as a pluggable upgrade, and document the real-data plan. | High | Medium |
| 5 | Security | The Keystore alone doesn't encrypt a Room database (PRD 3.6, Architecture 5). | Use **SQLCipher** or field-level encryption, with the key stored in the Keystore. | High | Medium |
| 6 | Privacy and legal | The consent flow is only an open question, and consent by a caregiver on behalf of a patient isn't addressed. | Build a simple consent screen aligned with India's **DPDP Act 2023**, covering guardian/caregiver consent. Add a "not a diagnostic device" notice. | High | Low–Medium |
| 7 | Caregiver alerts | Alerts need the patient's phone to be online, which conflicts with the rural, low-connectivity goal. | State this limitation openly. Add an optional **SMS fallback**, and show "last synced" on the dashboard so caregivers know how fresh the data is. | Medium | Medium |
| 8 | Tech stack bloat | libGDX adds build complexity that Compose Canvas already covers. | **Drop libGDX.** Use Compose Canvas for all MVP games. | Medium | Low (saves time) |
| 9 | Networking | Retrofit is redundant if Firebase handles sync. | Remove it for now. Add it only if you integrate the Bhashini API. | Low | Low |
| 10 | Sync design | Room and Firestore each act as a data store, so the source of truth is unclear (Architecture 4). | Keep **Room as the single source of truth**, Firestore as the sync target only, with **last-write-wins** conflict handling for the MVP. | Medium | Low |
| 11 | Open questions | Dashboard location and language priority are undecided (PRD 7). | Decide on **in-app Caregiver Mode** for the MVP. Languages: **Hindi + English + Kannada**, tested well. | Medium | Low |
| 12 | Milestones | The adaptive difficulty milestone comes before reminders (PRD 6). | Reorder so **reminders come before adaptive difficulty**, since reminders need the most device testing. | Low | Low |

## Suggested Working Order

1. **Quick doc fixes** (rows 2, 8, 9, 10, 11, 12): about an hour, and they clean up the plan for everyone.
2. **Reminders** (row 1): start early because it needs testing on real devices.
3. **Games with rule-based difficulty** (row 4), then the TFLite upgrade.
4. **Security and consent** (rows 5, 6) before any real data touches Firebase.
5. **Offline voice fallback and SMS alerts** (rows 3, 7) as polish for the demo.

## Owner Tracker (fill in)

| # | Area | Owner | Status | Notes |
|---|------|-------|--------|-------|
| 1 | Reminders | | Not started | |
| 2 | Voice | | Not started | |
| 3 | Voice (offline) | | Not started | |
| 4 | Adaptive AI | | Not started | |
| 5 | Security | | Not started | |
| 6 | Privacy and legal | | Not started | |
| 7 | Caregiver alerts | | Not started | |
| 8 | Tech stack bloat | | Not started | |
| 9 | Networking | | Not started | |
| 10 | Sync design | | Not started | |
| 11 | Open questions | | Not started | |
| 12 | Milestones | | Not started | |
