# CPEN321_26W1_ProjectName

_Keep this README up to date with the steps required to build and run the frontend and backend (including any scripts, config files, and environment variables). TAs ill follow these instructions._

## Requirements

Install the following before the frontend or backend setup steps:

- [git](https://git-scm.com/install/)


--- 

## Frontend Setup

### Requirements

- [Android Studio](https://developer.android.com/studio) (latest version)
- [Java 17](https://adoptium.net/temurin/releases/?version=17) (optional: if no JDK is installed, `run-frontend.sh`/`.ps1` falls back to the one bundled with Android Studio)
- [Android SDK](https://developer.android.com/studio#command-tools) with API level 36+ (Android 16)

### Setup

1. **Open project**: Open the `frontend/` directory in Android Studio
2. **Sync Gradle**: Android Studio will automatically prompt you to sync the project. Click "Sync Now". You can also manually run `cd frontend && ./gradlew build` to trigger the sync and download the necessary dependencies.
3. **Configure Android SDK**: Ensure you have Android SDK 36 installed.
4. **Set up emulator/device**:
   - Create a new AVD (Android Virtual Device) by selecting Pixel 9 as the device and Android Baklava (API level 36) as the system image.
   - Alternatively, connect a physical Android device running Android 16 (API level 36).
5. **Setup app config**: Copy the example file, then fill in local values:
   ```bash
   cp frontend/local.properties.example frontend/local.properties
   ```
   Set at least:
   - `sdk.dir`: path to your Android SDK. Android Studio usually writes this the first time you open `frontend/`. On Mac it is often `sdk.dir=/Users/<username>/Library/Android/sdk`.
   - `API_BASE_URL`: backend URL baked into the APK. Use the deployed backend, `https://8.235.107.218`. To use a backend running on your own machine instead, use `http://10.0.2.2:3000` for the emulator (`10.0.2.2` is the host machine). Only these two hosts are allowed by `res/xml/network_security_config.xml`; any other host (e.g. a LAN IP) needs an entry added there.
   - `GOOGLE_CLIENT_ID`: the Google OAuth **web** client ID used for Sign-In. It must be the same client ID configured as `GOOGLE_CLIENT_ID` in `backend/.env`, and the SHA-1 of the key that signs the APK (the debug key for debug builds) must be registered on that project in the Google API Console, or Google Sign-In will fail.


### Build and Run

- **Debug build**: Click the green play button in the toolbar, to compile the code, package a debug APK, and install it on the connected device or running emulator.
- **Script**: From the project root, run `./scripts/run-frontend.sh` (macOS/Linux) or `.\scripts\run-frontend.ps1` (Windows). It starts the emulator if none is running, then builds, installs and launches the app. The emulator defaults to an AVD named `Pixel_9`; use another with `AVD_NAME=YourAvdName ./scripts/run-frontend.sh` (PowerShell: `$env:AVD_NAME = 'YourAvdName'; .\scripts\run-frontend.ps1`).
- **Release build**: From `frontend/`, run `./gradlew assembleRelease`. The APK is written to `frontend/app/build/outputs/apk/release/app-release.apk` and is signed with the shared debug key at `frontend/app/debug.keystore` (SHA-1 `07:F2:24:33:6A:E2:5D:C1:2F:71:A1:55:82:7E:73:AE:B5:C3:51:1E`), the same key used by debug builds. That SHA-1 is already registered on the Google project, so Google Sign-In works for any build made from this repository. Install it with `adb install -r app/build/outputs/apk/release/app-release.apk`.


### Backend Configuration

The app talks to the backend at `API_BASE_URL` (see above). Button 2 (Live Updates) is relayed through the backend, which itself must be able to reach the course server at `wss://8.229.22.124`.

---
## Backend Setup

You can run the backend in one of two ways:
* Locally via Node.js 
* Via Docker Compose

Both ways use the same `backend/.env` file (see below). The backend does not use a database; the MongoDB container that Docker Compose starts is not needed by the current features.

### Environment configuration

From the project root:

```bash
cp backend/.env.example backend/.env
```

Set at least:
- `GOOGLE_CLIENT_ID`: the Google OAuth web client ID. The backend uses it to verify the Google ID token sent by the app on every Button 1 request, so it must match the app's `GOOGLE_CLIENT_ID`.
- `SERVER_PUBLIC_IP`: the IP returned by `GET /api/server-ip`. Required locally, since there is no GCP metadata server; leave unset when deployed on GCP.
- `DEVELOPER_FIRST_NAME` / `DEVELOPER_LAST_NAME`: returned by `GET /api/name`.
- `PORT` (optional): defaults to `3000` if unset.
- `SSL_CERT_PATH` / `SSL_KEY_PATH` (optional): paths to a certificate and key to serve HTTPS (as on the deployed server). Leave empty for plain HTTP locally.

`MONGODB_URI` and `JWT_SECRET` appear in `.env.example` but are not read by the backend.


### Option 1: Run locally

**Requirements:** 
- [Node.js](https://nodejs.org/en/download/) 22+
- [npm](https://docs.npmjs.com/downloading-and-installing-node-js-and-npm) 10+

**Setup:** 
1. Install dependencies:

   ```bash
   cd backend
   npm install
   ```

2. **Development** (TypeScript with auto-reload):

   ```bash
   npm run dev
   ```

3. **Production build** (optional):

   ```bash
   npm run build
   npm start
   ```

### Option 2: Run with Docker Compose

**Requirements:** 
- [Docker](https://docs.docker.com/desktop/setup/install) and [Docker Compose](https://docs.docker.com/desktop/setup/install) v2.24+
- [curl](https://curl.se/download.html)

**Setup**
1. **Start** (from the project root):

   ```bash
   ./scripts/run-backend.sh      # Windows: .\scripts\run-backend.ps1
   ```

   This builds and starts the containers and waits until `http://localhost:3000/health` responds.

   Or run Compose directly:

   ```bash
   docker compose up --build -d
   ```

2. **Stop**:

   ```bash
   docker compose down
   ```

The backend is then reachable at `http://localhost:3000` (`http://10.0.2.2:3000` from the emulator).

## Additional Setup

_Please specify any other additional setup steps non-specific to either frontend nor backend_