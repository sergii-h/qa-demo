# QA Demo — Android App

Native Android client for the QA Demo task management API. Same CRUD functionality as the React web app, using the existing Spring Boot backend.

## Stack

- Kotlin · Jetpack Compose · Material 3
- Retrofit + Moshi · ViewModel + StateFlow
- Min SDK 26 · Target SDK 35

## Features

| Screen | API |
|--------|-----|
| Task list | `GET /v1/tasks` |
| Create task | `POST /v1/tasks` |
| Edit task | `PUT /v1/tasks/{id}` |
| Delete task | `DELETE /v1/tasks/{id}` |
| Task info (+ validation) | `GET /v1/tasks/{id}`, `GET /v1/tasks/isValid/{id}` |

## Prerequisites

1. **Android Studio** (latest stable) with SDK 35 and an emulator or physical device
2. **Backend running** — same as the web app:

```bash
docker compose -f docker/docker-compose/run-application.yml up -d qa-demo-mongo qa-demo-kafka qa-demo-wiremock
cd demo-service && mvn spring-boot:run
```

## Open in Android Studio

1. **File → Open** → select the `demo-android` folder
2. Wait for Gradle sync to finish
3. Create/start an emulator (or connect a device)
4. Click **Run**

## API base URL

Default (Android emulator → host machine):

```properties
http://10.0.2.2:8080/v1/
```

Override in `demo-android/local.properties` (create this file — it is gitignored):

```properties
sdk.dir=/Users/you/Library/Android/sdk
api.base.url=http://10.0.2.2:8080/v1/
```

| Environment | URL |
|-------------|-----|
| Emulator | `http://10.0.2.2:8080/v1/` |
| Physical device (same Wi‑Fi) | `http://<your-mac-ip>:8080/v1/` |

After changing `api.base.url`, rebuild the app.

## Build from CLI

```bash
cd demo-android
./gradlew assembleDebug
./gradlew installDebug   # device/emulator connected
```

## Project layout

```
demo-android/
├── app/src/main/java/com/example/demo/
│   ├── data/           # Models + Retrofit API
│   ├── repository/     # TaskRepository
│   └── ui/             # Compose screens + navigation
└── README.md
```

## Notes

- Cleartext HTTP is allowed for local development (`network_security_config.xml`)
- For production, use HTTPS and restrict cleartext traffic
