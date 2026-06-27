# Student Attendance & Analytics App

A modern, high-performance Android application designed to streamline student attendance management. Built with an offline-first approach, the app ensures that attendance can be marked anytime, anywhere, while providing seamless cloud synchronization for data durability.

## 🚀 Key Features

*   **Offline-First Experience**: Mark attendance without an internet connection using local Room Database storage.
*   **Real-time Cloud Sync**: Automatic and manual synchronization with Firebase Firestore to ensure data is never lost.
*   **Biometric Security**: Protect sensitive student data with Fingerprint and Face Unlock integration.
*   **Comprehensive Analytics**: Visualize student performance through attendance percentages, recent session history, and detailed reports.
*   **Class Management**: Easily create, update, and manage multiple classes and sections.
*   **Smart Imports**: Import student lists from external files to get started quickly.
*   **Modern UI/UX**: Immersive edge-to-edge design built with Jetpack Compose and Material 3.
*   **Dark Mode Support**: Fully responsive theme that adapts to system preferences.

## 🛠 Tech Stack

*   **Language**: [Kotlin](https://kotlinlang.org/)
*   **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (Declarative UI)
*   **Architecture**: MVVM (Model-View-ViewModel) with Clean Architecture principles.
*   **Dependency Injection**: [Dagger Hilt](https://dagger.dev/hilt/)
*   **Local Database**: [Room Persistence Library](https://developer.android.com/training/data-storage/room)
*   **Backend/Cloud**: 
    *   **Firebase Authentication**: Secure user login and registration.
    *   **Firebase Firestore**: Real-time NoSQL database for cloud synchronization.
*   **Concurrency**: Kotlin Coroutines & Flow for reactive data streams.
*   **Jetpack Libraries**:
    *   Navigation Compose
    *   Biometric API
    *   Core Splashscreen API
    *   DataStore Preferences

## 🏗 Architecture

The project follows a modularized clean architecture pattern:
*   **Data Layer**: Handles database operations (Room DAOs), Firebase service implementations, and repository logic.
*   **Domain Layer**: Contains business logic, models, and repository interfaces (Pure Kotlin).
*   **Presentation Layer**: UI logic using ViewModels and Compose screens.

## ⚙️ Setup & Installation

### Prerequisites
*   Android Studio Ladybug or newer.
*   JDK 17 or higher.
*   A Firebase Project.

### Steps
1.  **Clone the repository**:
    ```bash
    git clone https://github.com/yourusername/Student-Attendance-App.git
    ```
2.  **Firebase Configuration**:
    *   Create a new project in the [Firebase Console](https://console.firebase.google.com/).
    *   Add an Android app with the package name `com.attendance.app`.
    *   Download the `google-services.json` file and place it in the `app/` directory.
    *   Enable **Email/Password Authentication** and **Cloud Firestore**.
3.  **Build & Run**:
    *   Open the project in Android Studio.
    *   Sync Gradle files.
    *   Click **Run** to install the app on your emulator or physical device.

<img width="720" height="1600" alt="image" src="https://github.com/user-attachments/assets/8555f548-eada-4137-88a3-8b7df4e30d7f" />
<img width="720" height="1600" alt="image" src="https://github.com/user-attachments/assets/7ddd222b-a5d6-45a9-8840-24b5343fadb8" />
<img width="720" height="1600" alt="image" src="https://github.com/user-attachments/assets/1692f15c-9af5-4969-a710-3d86a33dcf28" />
<img width="720" height="1600" alt="image" src="https://github.com/user-attachments/assets/1f86572b-e9a8-420d-a904-a4966bd59517" />
<img width="720" height="1600" alt="image" src="https://github.com/user-attachments/assets/2b8325ed-24c7-4b11-a6ac-1d137345ec80" />
<img width="720" height="1600" alt="image" src="https://github.com/user-attachments/assets/6615c639-e08e-4026-862b-65521b84675e" />
<img width="720" height="1600" alt="image" src="https://github.com/user-attachments/assets/a3f722a9-51eb-4afa-8b93-9a23cd066ce4" />
<img width="720" height="1600" alt="image" src="https://github.com/user-attachments/assets/043df7a1-4c58-46b7-9942-ce2ac76379a9" />
<img width="720" height="1600" alt="image" src="https://github.com/user-attachments/assets/3f945c09-b99d-4573-8279-9720f4079384" />
