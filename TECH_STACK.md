# Tech Stack — AI Cognitive Gaming & Memory Assistance Platform

## Project Type
Native Android application, built and maintained in Android Studio.

## Language
- **Kotlin** — official Google-recommended language for Android
  - Required for Jetpack Compose
  - Coroutines for async work (API calls, DB queries, ML inference)
  - Null safety reduces runtime crashes (important for a healthcare-adjacent app)

## Architecture Pattern
- **MVVM (Model-View-ViewModel)** with Jetpack components
- **Clean Architecture layering:** `data/` → `domain/` → `ui/`
- **Hilt** for Dependency Injection

## UI Layer
| Component | Choice | Purpose |
|---|---|---|
| UI Toolkit | Jetpack Compose | Declarative UI; easier to build large-touch-target, elderly-friendly screens |
| Navigation | Navigation Component (Compose) | Screen flow management |
| Theming | Custom Compose theme | High-contrast, large-font, elderly-accessible design |
| Animations | Lottie | Lightweight character/feedback animations for positive reinforcement |

## Computer Graphics / Game Layer
| Component | Choice | Purpose |
|---|---|---|
| 2D Graphics | Jetpack Compose Canvas API | Memory-match, drag-drop, pattern recognition games |
| Game Engine (optional, richer games) | libGDX | Sprite-based games, physics, richer animation needs |

## AI / ML Layer
| Component | Choice | Purpose |
|---|---|---|
| On-device inference | TensorFlow Lite | Adaptive difficulty engine based on patient performance |
| ML utilities | ML Kit | On-device Speech-to-Text, Text-to-Speech, additional ML APIs |
| Model training | Python + TensorFlow (offline) | Train models, convert to `.tflite` for deployment |

## Voice & Multilingual Support
- **Android SpeechRecognizer API** — native, offline-capable speech input
- **Android TextToSpeech API** — native voice output
- **Bhashini** (Government of India multilingual AI initiative) — regional Indian language support
- Locale-specific string resources (`values-hi/`, `values-bn/`, `values-ta/`, etc.) for pan-India rollout

## Local Storage (Offline-First)
| Component | Choice | Purpose |
|---|---|---|
| Local DB | Room | Structured local storage — patient data, game scores, reminders |
| Preferences | DataStore | Lightweight settings (language, difficulty level) |
| Background tasks | WorkManager | Reminders (medicine, hydration, appointments) + offline sync queue |

## Backend & Sync (Caregiver Dashboard)
| Component | Choice | Purpose |
|---|---|---|
| Backend | Firebase | Firestore (real-time sync), Auth, Cloud Messaging |
| Networking | Retrofit + OkHttp | REST API calls when online |
| Sync strategy | Offline-first via Room + WorkManager | Writes locally, syncs to Firebase on reconnect |

## Security
- **Android Keystore System** — encrypt sensitive patient data locally
- **Firebase App Check + Firestore security rules** — protect backend data access
- Compliance consideration: data handling aligned with healthcare data sensitivity norms

## Decision Notes
- **Kotlin chosen over Flutter:** project is Android-only via Android Studio, so Flutter's cross-platform advantage doesn't apply; native Kotlin gives tighter integration with TFLite/ML Kit, WorkManager reliability, and native speech APIs with broader Indian-language voice pack support.
- **Kotlin chosen over Java:** Jetpack Compose is Kotlin-only; coroutines and null-safety reduce bugs in the async-heavy, reliability-sensitive areas (reminders, offline sync, voice processing) this project depends on.

## Suggested Stack Summary (for SIH slide)
Kotlin + Jetpack Compose + Room + WorkManager + Hilt (MAD) | Compose Canvas / libGDX + Lottie (Graphics) | TensorFlow Lite + ML Kit (AI/Adaptive Difficulty) | Android Speech APIs + Bhashini (Voice/Multilingual) | Firebase (Backend/Sync) | Android Keystore (Security)
