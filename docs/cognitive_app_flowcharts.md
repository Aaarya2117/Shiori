# AI Cognitive Gaming & Memory Assistance Platform — Flowcharts

## 1. App Structure

```text
Home
 ├── Play Games
 ├── Memory
 ├── Daily Routine
 ├── Reminders
 └── Profile

Caregiver
 ├── Patient Progress
 ├── Game Performance
 ├── Medication
 └── Alerts
```

## 2. Overall Application Flow

```text
                 YOUR APP
                    │
       ┌────────────┴────────────┐
       │                         │
   PATIENT APP              CAREGIVER APP
       │                         │
       ├── Cognitive Games       ├── Dashboard
       ├── Memory Training       ├── Progress
       ├── Reminders             ├── Alerts
       ├── Voice                 └── Reports
       └── Daily Routine
                    │
                    ▼
              BACKEND / DB
                    │
        ┌───────────┼───────────┐
        │           │           │
      Firebase    Room        APIs
        │
        ▼
       AI/ML
        │
        ▼
 Adaptive Difficulty
```

## 3. Offline-First Flow

```text
                 APP
                  │
           ┌──────┴──────┐
           │             │
       Internet        No Internet
           │             │
       Firebase        Room DB
           │             │
           └──────┬──────┘
                  │
             Sync later
```
