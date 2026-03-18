# poznote-android

Native Android client for [poznote](https://github.com/timothepoznanski/poznote) — a self-hosted note-taking app.

## Features

- Browse workspaces, folders, and notes
- Read notes: Markdown/tasklist (Markwon), HTML (WebView), Excalidraw (browser link)
- Create and edit notes with auto-save (1s debounce)
- Search across all notes
- Favorites management
- Trash: move, restore, and permanently delete notes
- HTTP Basic Auth against any self-hosted poznote instance (HTTP or HTTPS)
- Credentials stored securely via Android Keystore (EncryptedSharedPreferences)

## Requirements

- Android 8.0+ (API 26)
- A running [poznote](https://github.com/timothepoznanski/poznote) server

## Build

> **Note:** Requires JDK 17. If your system Java is newer (v21+), use Android Studio's bundled JDK:
> ```bash
> export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
> ```

```bash
# Debug APK
./gradlew assembleDebug

# Install on connected device/emulator
./gradlew installDebug
```

The APK is output to `app/build/outputs/apk/debug/app-debug.apk`.

## Tech Stack

| Component | Library |
|---|---|
| Language | Kotlin 2.0.21 |
| UI | Jetpack Compose (BOM 2024.09.00) + Material3 |
| Navigation | Navigation Compose 2.8.x |
| DI | Hilt 2.51.1 |
| Networking | Retrofit 2.11.0 + OkHttp 4.12.0 |
| JSON | Moshi 1.15.1 (KSP codegen) |
| Secure storage | EncryptedSharedPreferences 1.1.0-alpha06 |
| Markdown | Markwon 4.6.2 |
| HTML notes | Accompanist WebView 0.34.0 |
| Min SDK | 26 (Android 8) |

## Architecture

MVVM + Repository pattern.

- Each screen has a `@HiltViewModel` exposing a single `StateFlow<UiState>`
- Repositories call the Retrofit `PoznoteApi` interface and return `Result<T>`
- No local database cache — all data fetched fresh (pull-to-refresh on all list screens)
- Dynamic base URL: a `UrlOverrideInterceptor` reads `server_url` from `AuthPreferences` on every request, so Retrofit never needs to be recreated after login

## Authentication

On first launch the Login screen prompts for **Server URL**, **Username**, and **Password**. On successful login (`GET /api/v1/users/me`) the credentials and user ID are persisted to `EncryptedSharedPreferences` backed by the Android Keystore. Subsequent launches go directly to the Workspaces screen.

## Project Structure

```
app/src/main/java/com/poznote/android/
├── data/
│   ├── local/AuthPreferences.kt
│   ├── remote/api/PoznoteApi.kt
│   ├── remote/interceptor/
│   ├── remote/model/Dtos.kt
│   └── repository/
├── di/NetworkModule.kt
└── ui/
    ├── navigation/
    ├── auth/
    ├── workspaces/
    ├── folders/
    ├── notes/
    ├── search/
    ├── favorites/
    ├── trash/
    ├── theme/
    └── components/
```
