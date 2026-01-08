# NativeBridge App

## Overview

**NativeBridge** is a production-ready Android application with biometric authentication gate. Users must authenticate with fingerprint/biometrics before accessing any features.

**Package Name:** `com.nativebridge.io`
**Based on:** NativeBridgeApp testing framework
**Key Feature:** Biometric authentication required on app launch

---

## Project Structure

```
NativeBridge/
├── README.md                          (This file)
├── SETUP_GUIDE.md                     (Setup instructions)
├── BIOMETRIC_IMPLEMENTATION.md        (Auth gate documentation)
├── setup.sh                           (Automated setup script)
├── App.tsx                            (Will be created during setup)
├── package.json
├── android/
│   ├── app/
│   │   ├── src/main/java/com/nativebridge/io/
│   │   │   ├── MainActivity.kt
│   │   │   └── MainApplication.kt
│   │   └── build.gradle
│   └── gradlew
└── builds/
    └── nativebridge-v1.apk            (Output APK)
```

---

## Quick Start

### 1. Run Setup Script

```bash
cd /Users/himanshukukreja/autoflow/NativeBridge
chmod +x setup.sh
./setup.sh
```

This will:
- Copy NativeBridgeApp template
- Update package name to com.nativebridge.io
- Add custom logo
- Install dependencies
- Build release APK

### 2. Install & Test

```bash
# Install APK on device
adb install -r builds/nativebridge-v1.apk

# Launch app
adb shell am start -n com.nativebridge.io/.MainActivity

# Monitor logs
adb logcat | grep -E "NativeBridge|Biometric"
```

---

## App Behavior

### Launch Flow

1. **App Starts** → Shows biometric authentication screen
2. **Biometric Prompt** → System fingerprint dialog appears
3. **User Authenticates** → Scans enrolled fingerprint
4. **Success Message** → "Authentication Successful! 🎉"
5. **App Unlocks** → Access to all features

### Features (After Authentication)

- ✅ UI Components Testing
- ✅ Network Operations (GET, POST, Upload, Download)
- ✅ Performance Testing (CPU, Memory)
- ✅ Permissions Management
- ✅ Storage & Clipboard
- ✅ File Operations (Upload, CSV Save)
- ✅ QR Code Scanning
- ✅ Biometric Features

---

## Requirements

### Device Requirements

- Android device with fingerprint sensor
- At least one fingerprint enrolled in Settings
- Screen lock (PIN/Pattern/Password) configured

### Development Requirements

- Node.js >= 18
- React Native 0.74.7
- Android SDK
- Java 17 or 23

---

## Key Differences from NativeBridgeApp

| Feature | NativeBridgeApp | NativeBridge |
|---------|-----------------|--------------|
| **Purpose** | Testing framework | Production app |
| **Package** | com.nativebridgeapp | com.nativebridge.io |
| **Authentication** | Optional (Bio tab) | Required on launch |
| **Access** | Open | Locked until authenticated |
| **Frida Gadget** | ✅ Embedded | ❌ Not included |
| **Logo** | Default | Custom NativeBridge |

---

## CI/CD & Automated Releases

This project includes a fully automated CI/CD pipeline with NativeBridge integration:

- ✅ **Automated Builds** - Android APK & iOS .app built on every release tag
- ✅ **NativeBridge Upload** - Apps automatically uploaded to NativeBridge platform
- ✅ **Auto-Start Sessions** - Optional: Start test sessions on NativeBridge devices
- ✅ **Slack Notifications** - Get notified when sessions are ready
- ✅ **GitHub Releases** - Artifacts and session URLs in release notes

### Quick Release

```bash
# Basic release (no session)
./scripts/release.sh 1.0.0

# Release with auto-session
./scripts/release.sh 1.0.0 --start-session

# Custom device and session duration
./scripts/release.sh 1.0.0 --start-session \
  --device-id abc123 \
  --session-validity 180
```

### Session Parameters

| Option | Description | Default |
|--------|-------------|---------|
| `--start-session` | Auto-start test session | disabled |
| `--device-id <id>` | Device to use | `67a642531a4aa535498192f8` |
| `--session-validity <s>` | Duration in seconds (30-300) | `120` |

### Slack Notifications (Optional)

Add `SLACK_WEBHOOK_URL` secret in GitHub to get notified when sessions start.

See [CICD_GUIDE.md](CICD_GUIDE.md) for complete documentation.

## Documentation

- **[CICD_GUIDE.md](CICD_GUIDE.md)** - Complete CI/CD pipeline documentation
- **[RELEASE_AUTOMATION.md](RELEASE_AUTOMATION.md)** - Automated release guide
- **SETUP_GUIDE.md** - Complete setup instructions
- **BIOMETRIC_IMPLEMENTATION.md** - How biometric authentication works
- **BUILD_GUIDE.md** - Manual build instructions

---

## Troubleshooting

### App Won't Launch

```bash
# Check logs
adb logcat | grep AndroidRuntime

# Common issues:
# 1. Package name mismatch in AndroidManifest
# 2. Missing dependencies
```

### Biometric Not Available

**Solution:**
1. Go to Settings → Security → Fingerprint
2. Enroll at least one fingerprint
3. Restart app

### Build Fails

```bash
cd android
./gradlew clean
./gradlew assembleRelease
```

---

## Support

For issues, check:
1. Logs: `adb logcat | grep -i biometric`
2. Package name: `grep -r "com.nativebridge.io" android/`
3. Dependencies: `npm list`

---

**Ready to build secure, authenticated Android apps! 🔒**
