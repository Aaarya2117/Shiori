# Technology Stack Document
## AI Cognitive Gaming & Memory Assistance Platform (Shiori)

---

## 1. Platform & Language Foundation

| Component | Technology | Rationale & Technical Details |
|---|---|---|
| **Target OS** | Android (API 26+ / Android 8.0 to Android 14+) | Covers 95%+ of active smartphones and tablets in India, including older low-cost devices. |
| **Language** | **Kotlin (v1.9+)** | Official modern Android standard. Kotlin Coroutines and Flows enable structured, non-blocking asynchronous operations (database encryption, audio playback, exact timer ticks). Strong null-safety guarantees prevent runtime crashes in a healthcare-critical app. |
| **Architecture** | **MVVM + Clean Architecture** | Strict separation of concerns: `ui/` (presentation) → `domain/` (pure Kotlin use cases & business logic) → `data/` (Room DB, Keystore, Firebase sync). Ensures every subsystem can be unit-tested without Android framework mocking. |
| **Dependency Injection** | **Dagger Hilt** | Standard Android DI framework. Simplifies constructor injection across ViewModels, UseCases, Repositories, and Background Workers. |

---

## 2. Presentation & User Experience (UI) Layer

| Layer Component | Library / API | Rationale & Technical Details |
|---|---|---|
| **UI Framework** | **Jetpack Compose** (Material 3) | Modern declarative UI toolkit. Makes building dynamic, large-touch-target interfaces, adaptive screen layouts, and high-contrast themes trivial compared to legacy XML. |
| **Navigation** | **Compose Navigation** (`androidx.navigation:navigation-compose`) | Single-Activity architecture. Manages type-safe screen transitions between Patient Mode and PIN-gated Caregiver Mode. |
| **Design & Accessibility** | **Custom Accessible Theme** | Follows WCAG AAA contrast ratios, supports dynamic text scaling, enforces 64dp+ touch surfaces, and eliminates complex multi-touch gestures (tap-only interactions). |
| **Micro-Animations** | **Airbnb Lottie for Compose** (`com.airbnb.android:lottie-compose`) | Renders lightweight vector animations for positive reinforcement (e.g., celebratory stars, gentle clapping hands) with near-zero CPU and memory overhead. |

---

## 3. Game & Graphics Engine (Streamlined)

| Component | Choice | Status & Technical Rationale |
|---|---|---|
| **2D Graphics Rendering** | **Jetpack Compose Canvas API** (`androidx.compose.ui.graphics`) | **Selected for all games.** Memory Match, Pattern Sequence, and Face Recall are 2D card/grid games. Compose Canvas provides hardware-accelerated drawing, 60 FPS frame rates, simple touch-hit detection, and zero APK overhead. |
| *Removed Engine* | *libGDX* | **Removed.** Introducing a separate C++/Java game engine creates build complications, doubles APK size, complicates lifecycle management, and creates battery drain without delivering any visual benefit for simple cognitive exercises. |

---

## 4. Life-Critical Reminders & Background Scheduling

| Component | Library / API | Status & Technical Rationale |
|---|---|---|
| **Exact Alarm Engine** | **Android `AlarmManager`** (`setExactAndAllowWhileIdle()`) | **Critical Fix.** Replaces WorkManager for medication and hydration alerts. Android Doze Mode and OEM power-savers frequently defer WorkManager tasks by 30 to 120 minutes. `AlarmManager` with `setExactAndAllowWhileIdle()` forces CPU wake-up to ring life-critical alarms at the exact scheduled second. |
| **Reboot Recovery** | **Android `BroadcastReceiver`** (`ACTION_BOOT_COMPLETED`) | Listens for system boot events, queries the local encrypted database, and re-registers all scheduled alarms so no reminders are lost after phone restart or battery death. |
| **Opportunistic Sync** | **Jetpack WorkManager** (`androidx.work:work-runtime-ktx`) | **Re-assigned exclusively to background sync.** Executes cloud sync jobs opportunistically when unmetered or stable network connectivity (`NetworkType.CONNECTED`) is available. |

---

## 5. Voice & Multilingual Architecture

| Subsystem | Technology | Technical Implementation Details |
|---|---|---|
| **Core Offline Audio** | **Pre-recorded Audio Bundles** (Compressed `.ogg` / `.mp3` in `res/raw/`) | **Primary Voice Engine.** Indian language coverage and offline TTS voice pack availability varies wildly across low-end OEM devices. Bundling high-quality pre-recorded human voice prompts for all system instructions, buttons, and rewards in **Hindi, English, and Kannada** ensures 100% offline consistency. |
| **Voice Input (STT)** | **Android `SpeechRecognizer` API** | Native on-device speech-to-text. Activated on demand for optional hands-free commands. Fallbacks cleanly if offline packs are missing. |
| **Voice Output (TTS)** | **Android `TextToSpeech` API** | Native system text-to-speech for dynamic text strings (e.g., customized medication names). Configured with localized Indian English (`en-IN`), Hindi (`hi-IN`), and Kannada (`kn-IN`). |
| *Removed Tool* | *ML Kit STT/TTS* | **Removed.** Google ML Kit does not provide speech synthesis or speech-to-text APIs. Corrected to native Android speech libraries. |
| **Cloud Voice API** | **Bhashini API** (Optional Enhancement) | Documented as an optional cloud enhancement to expand vocabulary and regional dialects when high-speed internet is available. |

---

## 6. Local Storage, Security & Encryption

| Component | Technology | Rationale & Technical Details |
|---|---|---|
| **Local Database** | **Room Database** (`androidx.room:room-runtime`) | Object-relational mapping over SQLite. Serves as the application's Single Source of Truth (SSoT). |
| **Database Encryption** | **SQLCipher for Android** (`net.zetetic:android-database-sqlcipher`) | **Security Fix.** Standard Room and Keystore do not encrypt SQLite tables on disk. SQLCipher provides full AES-256 database-level encryption at rest, protecting sensitive patient health and cognitive data. |
| **Key Management** | **Android Keystore System** | Generates and safeguards the 256-bit AES master key used to unlock SQLCipher. Operates in hardware-backed secure storage (TEE / StrongBox) so keys cannot be extracted even if the OS is rooted. |
| **Preferences Storage** | **Jetpack DataStore (Preferences)** | Type-safe, coroutine-native key-value store for app configuration, active locale, Caregiver PIN hash, and consent records. |

---

## 7. Cloud Backend, Sync & Emergency Telephony

| Component | Technology | Rationale & Technical Details |
|---|---|---|
| **Remote Database** | **Google Firebase Firestore** | Cloud NoSQL document store acting as the remote sync replica for Caregiver dashboard data. |
| **Authentication** | **Firebase Auth** | Provides secure phone OTP / email authentication for caregiver access. |
| **Cloud Push Alerts** | **Firebase Cloud Messaging (FCM)** | Pushes instant high-priority notifications to caregiver devices when online. |
| **Offline SMS Fallback** | **Android `SmsManager`** (`android.telephony.SmsManager`) | **Rural Reliability Fix.** If an urgent medication reminder is unacknowledged after 30 minutes and the device has no internet access, the app uses direct cellular SMS to alert the caregiver's mobile number. |
| *Removed Library* | *Retrofit & OkHttp* | **Removed for MVP.** The Firebase Firestore SDK natively manages its own gRPC connection, local cache, offline queuing, and retry logic. Standalone Retrofit is redundant unless Bhashini cloud APIs are integrated. |

---

## 8. Summary of Stack Refinements (Drawback Corrections)

| Previous Stack Item | Revised Stack Decision | Impact on Project |
|---|---|---|
| WorkManager for Reminders | **AlarmManager (`setExactAndAllowWhileIdle`)** | Eliminates missed doses caused by Android Doze Mode and OEM battery optimizations. |
| ML Kit for Speech/Voice | **Native SpeechRecognizer & TextToSpeech + Pre-recorded Audio** | Corrects library capabilities; guarantees 100% offline natural voice in Hindi, English, and Kannada. |
| Keystore alone for Storage | **SQLCipher (AES-256) + Keystore master key** | Delivers true end-to-end database encryption at rest. |
| libGDX Game Engine | **Jetpack Compose Canvas API** | Drops unnecessary 15MB+ engine bloat; simplifies build and lifecycle management. |
| Retrofit + OkHttp | **Firebase Native SDK** | Removes redundant networking layer; keeps dependencies minimal and clean. |
| Cloud-only Alerts | **Hybrid FCM + Direct Android `SmsManager` SMS Fallback** | Delivers life-critical alerts even in rural areas without mobile internet. |
