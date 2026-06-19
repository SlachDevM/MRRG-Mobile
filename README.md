# MRRG-Mobile

> Android application used by field workers within the MRRG ecosystem.

[![MRRG Backend](https://img.shields.io/badge/MRRG-Backend-6DB33F?logo=springboot)](https://github.com/SlachDevM/MRRG)
![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android)
![Kotlin](https://img.shields.io/badge/Kotlin-2.1-blue?logo=kotlin)
![Jetpack Compose](https://img.shields.io/badge/Jetpack-Compose-4285F4)
![Architecture](https://img.shields.io/badge/Architecture-MVVM-success)

---

## Overview

MRRG-Mobile is the Android application used by field workers within the **MRRG** ecosystem.

The application allows workers to consult assigned jobs, update their progress, capture site photos and continue working even without network connectivity.

The application is intentionally lightweight. Business rules remain on the backend while Android focuses on user interaction, offline capabilities and synchronization.

---

## MRRG Ecosystem

MRRG-Mobile is one component of the MRRG ecosystem.

Both the Android application and the React web application share the same Spring Boot backend, ensuring a single source of truth for business logic, data persistence and notification management.

```
                         ┌────────────────────────────┐
                         │        PostgreSQL          │
                         │      Persistent Data       │
                         └────────────▲───────────────┘
                                      │
                                      ▼
                         ┌────────────────────────────┐
                         │      Spring Boot API       │
                         │      Business Logic        │
                         └──────────┬───────┬─────────┘
                                    │       │
                             REST API│       │REST API
                                    │       │
                    ┌───────────────▼──┐   ┌▼─────────────────┐
                    │   MRRG-Mobile    │   │ React Web Client │
                    │  Field Workers   │   │ Managers/Admins  │
                    └─────────┬────────┘   └──────────────────┘
                              │
                              ▼
                    ┌──────────────────────┐
                    │ Firebase Cloud       │
                    │ Messaging            │
                    └─────────▲────────────┘
                              │
                    Notification Service
```

---

## Features

- JWT authentication
- Account activation via Android deep links
- Dashboard with daily and weekly views
- Job management
- Before and after photo capture
- Offline Room cache
- Offline synchronization with retry and queue coalescing
- Material Motion animations
- Firebase push notifications
- Material 3 dynamic theming
- Profile management
- Settings

---

## Account Activation

MRRG-Mobile supports account activation through Android deep links.

Users are created by administrators from the React web application and receive an activation email.

```text
Admin (React)
      │
      ▼
Create User
      │
      ▼
Activation Email
      │
      ▼
mrrg://activate-account?token=...
      │
      ▼
MRRG-Mobile
      │
      ▼
Choose Password
      │
      ▼
Account Activated
      │
      ▼
    Login
```
The activation token is validated by the backend. Android only handles the user interface and forwards the activation request.

---

## Architecture

MRRG-Mobile follows a straightforward MVVM architecture where repositories are responsible for data access while the backend remains the single source of truth.

```
Compose UI
      │
      ▼
ViewModel
      │
      ▼
Repository
      │
 ┌────┴────┐
 │         │
Retrofit  Room
      │
      ▼
Spring Boot Backend
```

Repositories decide whether data comes from the backend or the local Room database, while ViewModels remain focused on UI state management.

---

## Offline Strategy

The backend always remains the source of truth.

Room is used exclusively as a local cache and never becomes the authoritative data source.

This design guarantees that data consistency always remains under the control of the backend.

```
Repository
      │
      ├── API success
      │       │
      │       ▼
      │    Update Room
      │
      └── API failure
              │
              ▼
         Return Room cache
```

When the API is unavailable, repositories transparently fall back to the local cache.

The UI remains unaware of the data source and only displays an offline indicator when cached data is shown.

---

## Synchronization

Offline changes are stored in a local synchronization queue before being replayed.

Synchronization responsibilities are isolated within SyncRepository, allowing feature repositories to focus exclusively on data retrieval and local persistence.

```
User

↓

Repository

↓

PendingSync

↓

SyncRepository

↓

Spring Boot Backend
```

Current implementation includes:

- Retry mechanism
- Failed synchronization tracking
- Queue coalescing
- Manual synchronization

---

## Push Notifications

Notifications are persisted in PostgreSQL.

Firebase Cloud Messaging is used only as a delivery layer.

The backend never depends on Firebase delivery success.

```
Spring Boot

↓

Notification Service

↓

Firebase

↓

Android
```

---

## Project Structure

```text
app
├── data
│   ├── api           # Retrofit services
│   ├── dto           # Network transfer objects
│   ├── local         # Room database, DAOs and entities
│   ├── model         # Shared application models
│   ├── preferences   # DataStore preferences
│   ├── repository    # Data access and offline handling
│   ├── session       # Authentication session management
│   └── sync          # Offline synchronization
│
├── domain            # Shared models and enums
│
├── fcm               # Firebase Cloud Messaging
│
├── ui
│   ├── auth          # Login & account activation
│   ├── components    # Reusable Compose components
│   ├── jobs          # Dashboard and job management
│   ├── navigation    # Navigation graph
│   ├── notifications # Notification center
│   ├── permissions   # Runtime permissions
│   ├── profile       # User profile
│   ├── settings      # Application settings
│   └── theme         # Material 3 theme
│
└── MainActivity.kt
```

---

## Tech Stack

- Kotlin
- Jetpack Compose
- MVVM
- Retrofit
- Room
- DataStore
- Firebase Cloud Messaging
- Material 3
- Coil
- Spring Boot REST API
- PostgreSQL

---

## Project Philosophy

MRRG-Mobile is developed incrementally, with each milestone completed before introducing new features. Architectural decisions are driven by current business requirements rather than hypothetical future needs. Simplicity, readability and maintainability are consistently preferred over unnecessary abstractions.

---

## Design Decisions

This project intentionally avoids unnecessary abstractions.

### No Dependency Injection

Repositories are manually instantiated.

The project currently does not justify introducing Hilt or Koin.

### No Clean Architecture

The project favors simplicity over theoretical layering.

Every abstraction must solve a real problem.

### Backend as the Source of Truth

Business rules and data consistency remain centralized in the Spring Boot backend. Android focuses on presentation, offline support and synchronization rather than duplicating backend logic.

### No Background Synchronization

Background synchronization is intentionally omitted. Additional complexity such as WorkManager will only be introduced when justified by business requirements.

### Manual Dependency Management

Dependencies are created explicitly inside the navigation layer rather than through a dependency injection framework. This keeps object creation simple, visible and appropriate for the current requirements of the project.

---

## Screenshots

> Screenshots and GIF demonstrations will be added soon.

---

## Related Projects

### MRRG Backend

Spring Boot REST API powering both the Android and React applications.

GitHub: https://github.com/SlachDevM/MRRG

---

## Author

Developed by **SlachDevM**

GitHub: https://github.com/SlachDevM

---

## License

This repository is publicly available for demonstration and portfolio purposes.

The MRRG application and its business logic were developed for the Margaret River Re-Gutter business and remain proprietary.

Source code is published to showcase software engineering practices and architecture, but redistribution or commercial use of the application is not permitted without permission.
