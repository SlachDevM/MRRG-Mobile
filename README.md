# MRRG-Mobile

> Android application used by field workers within the MRRG ecosystem.

[![MRRG Backend](https://img.shields.io/badge/MRRG-Backend-6DB33F?logo=springboot)](https://github.com/SlachDevM/MRRG)
![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android)
![Kotlin](https://img.shields.io/badge/Kotlin-2.1-blue?logo=kotlin)
![Jetpack Compose](https://img.shields.io/badge/Jetpack-Compose-4285F4)
![Architecture](https://img.shields.io/badge/Architecture-MVVM-success)

### Login:  

<img width="602" height="1264" alt="image" src="https://github.com/user-attachments/assets/ea49ac12-535c-4ed1-b1d7-98035823b5ff" />

### Dashboard:  

<img width="484" height="1044" alt="image" src="https://github.com/user-attachments/assets/98bf900e-c264-477e-a5f7-1b6453cd26d6" />

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

## Account Creation And Log In

<img width="600" height="1262" alt="image" src="https://github.com/user-attachments/assets/cbd3314e-ff07-4b04-b4bf-1eef90a38fa2" />
<img width="604" height="1274" alt="image" src="https://github.com/user-attachments/assets/9f96d7cd-6494-45d3-82f7-ac303c66c91a" />

### Profile

<img width="590" height="1264" alt="image" src="https://github.com/user-attachments/assets/6063d58e-c781-477e-bcf3-3ab677ff151b" />

### Settings

<img width="598" height="1256" alt="image" src="https://github.com/user-attachments/assets/d89e5558-d628-4554-b05e-6217fcc29d8f" />

### Notifications
<img width="576" height="1244" alt="image" src="https://github.com/user-attachments/assets/f86ddb60-9d28-42f2-a256-f6da8ea24db1" />
<img width="584" height="1274" alt="image" src="https://github.com/user-attachments/assets/2f266505-e2ef-49f1-9ad8-e409aa5248e3" />


### Job Update

<img width="596" height="1264" alt="image" src="https://github.com/user-attachments/assets/8af77143-7d33-49fb-9c84-59841d69fd62" />
<img width="610" height="1262" alt="image" src="https://github.com/user-attachments/assets/06ae19b9-ddb2-4ba4-bfe2-f711ba913a43" />
<img width="506" height="1070" alt="image" src="https://github.com/user-attachments/assets/7ae707ea-cdce-4714-93f1-3a1bbabab6ab" />

## Job Validation

<img width="502" height="1078" alt="image" src="https://github.com/user-attachments/assets/5be05308-35ad-4f07-b466-5c9831cb90e9" />
<img width="508" height="1068" alt="image" src="https://github.com/user-attachments/assets/1df24ef1-c454-4997-a4be-66845ccd5531" />


### Offline and Sync

<img width="482" height="1026" alt="image" src="https://github.com/user-attachments/assets/4e3aa1ca-c195-4a9c-a4f7-d08bf76ec633" />
<img width="482" height="1032" alt="image" src="https://github.com/user-attachments/assets/b670b2a3-7e97-45b0-8394-e3e4e579846c" />
<img width="488" height="1018" alt="image" src="https://github.com/user-attachments/assets/a03ebf93-e480-4224-a3fd-9b32b08332cd" />


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
