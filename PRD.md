# Product Requirements Document (PRD)
## AI-Based Cognitive Gaming and Memory Assistance Platform for Elderly Dementia Patients

---

## 1. Overview

### 1.1 Problem Statement
India is witnessing a gradual rise in age-related cognitive disorders such as dementia and memory loss among its elderly population. Many families, particularly in rural, remote, and underserved areas, face challenges accessing specialized neurological care, cognitive therapy, and long-term elderly support services due to limited healthcare infrastructure, geographical barriers, and shortage of trained caregivers.

Elderly dementia patients experience memory decline, confusion, anxiety, and social isolation, while caregivers struggle with continuous monitoring and engagement. There is limited availability of affordable, culturally inclusive digital therapeutic solutions tailored for elderly individuals across India's diverse linguistic and cultural landscape.

### 1.2 Vision
Build an accessible, AI-enabled cognitive gaming and memory assistance platform that improves quality of life for elderly dementia patients across India, while giving caregivers visibility and control — even in low-connectivity environments.

### 1.3 Target Users
- **Primary:** Elderly patients with dementia / mild cognitive impairment
- **Secondary:** Family caregivers
- **Tertiary:** Healthcare workers / community health monitors (especially in rural/remote settings)

### 1.4 Platform
Native Android application (mobile/tablet), built in Android Studio using Kotlin.

---

## 2. Goals & Success Metrics

| Goal | Metric |
|---|---|
| Improve cognitive engagement | Avg. daily active game sessions per patient |
| Support early intervention | Trend of cognitive performance scores over time |
| Reduce caregiver burden | Reminder adherence rate; caregiver dashboard usage |
| Ensure accessibility | Successful offline session completion rate |
| Multilingual reach | Number of regional languages actively used |

---

## 3. Features & Requirements

### 3.1 Cognitive Gaming Module (Core)
- Memory improvement games (memory match, recall exercises)
- Attention and concentration games
- Daily routine recall activities
- Pattern and object recognition games
- **AI/ML adaptive difficulty:** adjusts in real time based on patient performance and cognitive condition, using on-device TensorFlow Lite inference

### 3.2 Voice & Multilingual Interaction
- Voice-assisted navigation and gameplay (Speech-to-Text / Text-to-Speech)
- Support for major Indian regional languages
- Culturally familiar themes, visuals, and sounds — regionally adaptable rather than hardcoded to one region

### 3.3 Reminders
- Medicine reminders
- Hydration reminders
- Daily activity reminders
- Medical appointment reminders
- Delivered via local notifications (WorkManager), functional offline

### 3.4 Caregiver Dashboard
- Patient progress tracking (game scores, trends over time)
- Activity level monitoring
- Alerts for missed reminders / concerning performance drops
- Accessible in-app ("Caregiver Mode") or as a lightweight companion view

### 3.5 Offline-First Functionality
- Core gameplay, reminders, and data entry work fully offline
- Local data queued and synced to backend when connectivity is available
- Designed for low-connectivity rural and remote environments

### 3.6 Data & Security
- Secure local storage (Android Keystore encryption)
- Secure cloud sync (Firebase Auth + Firestore security rules)
- Patient data privacy considerations built in from the start

### 3.7 Accessibility / UX
- Large touch targets, high-contrast UI, simple navigation
- Elderly-friendly font sizes and iconography
- Minimal cognitive load per screen (one primary action at a time)

---

## 4. Non-Functional Requirements
- **Offline reliability:** app must remain fully usable with zero connectivity
- **Performance:** on-device ML inference must run smoothly on mid-range/low-end Android devices (common in target demographic)
- **Battery efficiency:** background reminder services must be battery-optimization resilient across OEMs (Xiaomi, Oppo, etc.)
- **Data privacy:** patient health-adjacent data handled with encryption at rest and in transit

---

## 5. Out of Scope (for initial hackathon build)
- iOS version
- Full clinical diagnostic capability (this is a cognitive engagement tool, not a diagnostic medical device)
- Integration with hospital EHR systems (future roadmap item)

---

## 6. Milestones (Hackathon-oriented)
1. Core game modules (2–3 games) functional offline
2. Adaptive difficulty via TFLite integrated
3. Reminder system (WorkManager) working
4. Basic caregiver dashboard (local or Firebase-synced)
5. Voice + at least one regional language demo-ready
6. Architecture diagram + demo video for SIH submission

---

## 7. Open Questions
- Which regional languages to prioritize for the MVP/demo?
- Will caregiver dashboard be in-app or a separate module/web view?
- Data retention and consent flow for patient health data — needs a simple, elderly/caregiver-friendly consent screen
