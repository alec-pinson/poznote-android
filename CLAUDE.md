# Poznote Android — Claude Instructions

## Build

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew assembleDebug
./gradlew installDebug
```

> System Java is v25, which is incompatible with the Kotlin compiler. Always use JAVA_HOME pointing to Android Studio's bundled JDK 17.

## Project Layout

```
app/src/main/java/com/poznote/android/
├── data/
│   ├── local/AuthPreferences.kt       ← EncryptedSharedPreferences
│   ├── remote/api/PoznoteApi.kt        ← Retrofit interface
│   ├── remote/interceptor/             ← BasicAuth, UserId, UrlOverride
│   ├── remote/model/Dtos.kt            ← All API DTOs + request bodies
│   └── repository/                     ← Auth, Workspace, Folder, Note repos
├── di/NetworkModule.kt                 ← Hilt singleton bindings
└── ui/
    ├── navigation/AppNavHost.kt
    ├── auth/ workspaces/ folders/
    ├── notes/  (List, Viewer, Editor)
    ├── search/ favorites/ trash/
    └── theme/ components/
```

## Architecture

- **MVVM + Repository**: each screen has a `@HiltViewModel` with a `StateFlow<UiState>`
- **No local cache**: all data fetched fresh; pull-to-refresh available on all list screens
- **Dynamic base URL**: `UrlOverrideInterceptor` rewrites the host on every request from `AuthPreferences.serverUrl`
- **Auth headers**: `BasicAuthInterceptor` adds `Authorization: Basic …`; `UserIdInterceptor` adds `X-User-ID` on all routes except `/users/me` and `/users/profiles`

## Key Libraries

| Purpose | Library |
|---|---|
| UI | Jetpack Compose BOM 2024.09.00 + Material3 |
| DI | Hilt 2.51.1 |
| Networking | Retrofit 2.11.0 + OkHttp 4.12.0 |
| JSON | Moshi 1.15.1 (KSP codegen) |
| Secure storage | EncryptedSharedPreferences 1.1.0-alpha06 |
| Markdown | Markwon 4.6.2 |
| HTML notes | Accompanist WebView 0.34.0 |

## Adding a New Screen

1. Create `ui/<feature>/FeatureViewModel.kt` — `@HiltViewModel`, `StateFlow<UiState>`
2. Create `ui/<feature>/FeatureScreen.kt` — `@Composable` accepting VM via `hiltViewModel()`
3. Add route to `Screen.kt` sealed class
4. Wire composable in `AppNavHost.kt`

## API Conventions

All repository methods return `Result<T>` via `runCatching { api.call() }`. Call sites use `.fold(onSuccess = …, onFailure = …)`.

## Note Types

| Type | Viewer | Editor |
|---|---|---|
| `markdown` / `tasklist` | Markwon `TextView` | `BasicTextField` + preview toggle |
| `note` (HTML) | `WebView` | `BasicTextField` (raw HTML) |
| `excalidraw` | "Open in browser" placeholder | Not supported |
