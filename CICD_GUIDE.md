# CI/CD Pipeline Guide

This guide explains how to use the automated CI/CD pipeline for building and releasing NativeBridge application.

## 🚀 Overview

The CI/CD pipeline automatically builds Android APK and iOS .app when you push a release tag to GitHub.

**Workflow File:** [`.github/workflows/release-build.yml`](.github/workflows/release-build.yml)

## 📋 What Gets Built

When you create and push a release tag:

1. **Android APK** (`NativeBridge-v{VERSION}.apk`)
   - Production-ready signed APK
   - Compatible with emulators and physical devices
   - Optimized with Hermes

2. **iOS .app** (`NativeBridge-iOS-v{VERSION}.app.zip`)
   - Simulator build (.app directory)
   - Zipped for easy download
   - Ready for Appium testing

3. **GitHub Release**
   - Automatic release creation
   - Both artifacts attached
   - Release notes generated

## 🏷️ Triggering a Build

### Creating a Release Tag

```bash
# Make sure you're on the main branch and up to date
git checkout main
git pull origin main

# Create a tag with version number (v prefix required)
git tag v1.0.0

# Push the tag to GitHub
git push origin v1.0.0
```

### Tag Naming Convention

**Required format:** `v{MAJOR}.{MINOR}.{PATCH}`

**Examples:**
- ✅ `v1.0.0` - Production release
- ✅ `v1.2.3` - Production release
- ✅ `v2.0.0-beta` - Beta release (marked as pre-release)
- ✅ `v1.5.0-alpha.1` - Alpha release (marked as pre-release)
- ❌ `1.0.0` - Missing 'v' prefix (won't trigger)
- ❌ `release-1.0.0` - Wrong format (won't trigger)

### Using GitHub UI

1. Go to your repository on GitHub
2. Click "Releases" → "Create a new release"
3. Click "Choose a tag"
4. Type your tag name (e.g., `v1.0.0`)
5. Click "Create new tag"
6. Fill in release title and description (optional - will be auto-generated)
7. Click "Publish release"

The pipeline will automatically trigger!

## 🔧 Pipeline Jobs

### 1. Build Android APK

**Runner:** `ubuntu-latest`

**Steps:**
1. Checkout code
2. Extract version from tag
3. Setup Node.js and install dependencies
4. Setup JDK for Android build
5. Configure keystore (from secrets or local)
6. Update version in `build.gradle`
7. Build release APK with Gradle
8. Rename APK with version number
9. Upload APK as artifact
10. Upload to NativeBridge Platform
11. **[NEW]** Start NativeBridge session (if enabled)
12. **[NEW]** Send Slack notification (if configured)

**Output:** `NativeBridge-v{VERSION}.apk`
**Additional Outputs:** Session ID and URL (if session started)

### 2. Build iOS .app

**Runner:** `macos-latest`

**Steps:**
1. Checkout code
2. Extract version from tag
3. Setup Node.js and install dependencies
4. Install CocoaPods dependencies
5. Setup Xcode (latest stable)
6. Update iOS version
7. Build .app for simulator
8. Zip the .app directory
9. Upload as artifact

**Output:** `NativeBridge-iOS-v{VERSION}.app.zip`

### 3. Create GitHub Release

**Runner:** `ubuntu-latest`

**Dependencies:** Waits for both Android and iOS builds

**Steps:**
1. Download both artifacts
2. Create GitHub Release
3. Attach APK and .app.zip
4. Generate release notes
5. Publish release

## 🔐 GitHub Secrets Setup

For production builds with secure keystores, you need to configure GitHub Secrets.

### Required Secrets (Optional)

Navigate to: **Repository → Settings → Secrets and variables → Actions**

| Secret Name | Description | Example |
|-------------|-------------|---------|
| `ANDROID_KEYSTORE_BASE64` | Base64-encoded keystore file | (base64 string) |
| `ANDROID_KEYSTORE_PASSWORD` | Keystore password | `your-secure-password` |
| `ANDROID_KEY_ALIAS` | Key alias | `nativebridge-production` |
| `ANDROID_KEY_PASSWORD` | Key password | `your-secure-password` |
| `NATIVEBRIDGE_API_KEY` | NativeBridge API key for uploads | `Nb-c3uo.a1233f7f-...` |
| `SLACK_WEBHOOK_URL` | Slack webhook for notifications (optional) | `https://hooks.slack.com/...` |

### How to Create Keystore Secret

```bash
# Base64 encode your production keystore
base64 -i android/app/nativebridge-production.keystore | pbcopy

# On Linux:
base64 -w 0 android/app/nativebridge-production.keystore | xclip -selection clipboard

# Paste the output as ANDROID_KEYSTORE_BASE64 secret in GitHub
```

### Fallback Behavior

If secrets are NOT configured:
- ✅ Pipeline will use the local development keystore
- ✅ Builds will still succeed
- ⚠️ APK will be signed with development key

## 🎮 NativeBridge Session Configuration

You can automatically start a test session on a NativeBridge device after uploading your Android app using the release script.

### Using the Release Script

The easiest way to enable sessions is through command-line arguments:

```bash
# Basic release with session
./scripts/release.sh 1.0.0 --start-session

# Custom device and duration
./scripts/release.sh 1.0.0 --start-session --device-id abc123 --session-validity 180

# Quick 30-second test session
./scripts/release.sh 1.0.0 --start-session --session-validity 30
```

### Available Session Options

| Option | Description | Default | Example |
|--------|-------------|---------|---------|
| `--start-session` | Enable session auto-start | disabled | `--start-session` |
| `--device-id <id>` | NativeBridge device ID | `67a642531a4aa535498192f8` | `--device-id abc123` |
| `--session-validity <seconds>` | Session duration (30-300) | `120` | `--session-validity 180` |

### How It Works

1. You run the release script with session options
2. Script embeds session config in git tag message
3. CI/CD workflow reads config from tag
4. Session starts automatically after app upload
5. Session URL added to release notes and summary

### Session Features

When enabled, the pipeline will:
- ✅ Automatically start a session on NativeBridge device
- ✅ Generate a direct session URL
- ✅ Include session info in GitHub Release notes
- ✅ Send Slack notification (if webhook configured)
- ✅ Add session URL to build summary

### Session Validity

- **Minimum:** 30 seconds
- **Maximum:** 300 seconds (5 minutes)
- **Default:** 120 seconds (2 minutes)
- Values outside this range will be automatically adjusted to the nearest valid value

### Examples

```bash
# Standard release with default session (2 minutes)
./scripts/release.sh 1.0.0 --start-session

# Extended testing session (5 minutes)
./scripts/release.sh 1.0.1 --start-session --session-validity 300

# Quick smoke test (30 seconds)
./scripts/release.sh 1.0.2 --start-session --session-validity 30

# Specific device with custom duration
./scripts/release.sh 1.1.0 --start-session \
  --device-id 67a642531a4aa535498192f8 \
  --session-validity 240

# Dry run to see what would happen
./scripts/release.sh 1.0.0 --start-session --dry-run
```

## 📦 Downloading Build Artifacts

### From GitHub Actions

1. Go to repository → **Actions** tab
2. Click on the workflow run (triggered by your tag)
3. Scroll down to **Artifacts** section
4. Download:
   - `NativeBridge-Android-v{VERSION}` (APK)
   - `NativeBridge-iOS-v{VERSION}` (.app.zip)

**Retention:** Artifacts are kept for 90 days

### From GitHub Releases

1. Go to repository → **Releases** tab
2. Find your version release
3. Download from **Assets** section:
   - `NativeBridge-v{VERSION}.apk`
   - `NativeBridge-iOS-v{VERSION}.app.zip`

**Retention:** Permanent (until manually deleted)

## 📱 Installing Built Apps

### Android APK

```bash
# Install via ADB
adb install NativeBridge-v1.0.0.apk

# Or drag and drop onto emulator
```

### iOS .app (Simulator)

```bash
# Extract the .app directory
unzip NativeBridge-iOS-v1.0.0.app.zip

# Install to running simulator
xcrun simctl install booted NativeBridgeDebugApplication.app

# Or drag and drop onto simulator
```

### For Appium Testing

**Android:**
```javascript
{
  platformName: 'Android',
  app: '/path/to/NativeBridge-v1.0.0.apk',
  automationName: 'UiAutomator2'
}
```

**iOS:**
```javascript
{
  platformName: 'iOS',
  app: '/path/to/NativeBridgeDebugApplication.app',
  automationName: 'XCUITest'
}
```

## 🔍 Monitoring Build Status

### Build Progress

1. Go to **Actions** tab
2. Click on the running workflow
3. View real-time logs for each job

### Build Notifications

GitHub will show build status on:
- The commit that triggered the build
- The tag/release page
- Pull requests (if applicable)

### Build Summary

After build completes, check:
- **Summary** tab for quick overview
- **Artifacts** section for downloads
- **Release** page for final artifacts

## ⚙️ Customizing the Pipeline

### Changing Node.js Version

Edit `.github/workflows/release-build.yml`:

```yaml
env:
  NODE_VERSION: '18'  # Change to '20' or other version
```

### Changing Java Version

```yaml
env:
  JAVA_VERSION: '17'  # Change to '11' or other version
```

### Adding Additional Build Steps

Add steps in the respective job:

```yaml
- name: Run tests
  run: npm test

- name: Run linting
  run: npm run lint
```

### Changing Artifact Retention

```yaml
- name: Upload Android APK
  uses: actions/upload-artifact@v4
  with:
    retention-days: 90  # Change to desired days (1-90)
```

## 🐛 Troubleshooting

### Build Fails: "Keystore not found"

**Solution:**
1. Check if keystore exists in `android/app/`
2. Add keystore to GitHub Secrets
3. Or ensure local keystore is committed (for dev builds)

### Build Fails: "Pod install failed"

**Solution:**
```yaml
# Add to iOS build job
- name: Clean CocoaPods cache
  run: |
    cd ios
    pod cache clean --all
    pod deintegrate
    pod install
```

### Build Fails: "Gradle task failed"

**Solution:**
1. Check Gradle version compatibility
2. Review error logs in Actions tab
3. Test build locally first:
   ```bash
   cd android && ./gradlew assembleRelease
   ```

### iOS Build: ".app not found"

**Solution:**
1. Check Xcode version compatibility
2. Verify scheme name matches
3. Clean build:
   ```bash
   cd ios && xcodebuild clean
   ```

### Release Not Created

**Possible causes:**
1. Missing `contents: write` permission
2. Tag format incorrect (must start with 'v')
3. One of the build jobs failed

**Solution:**
- Check Actions logs
- Verify tag format
- Ensure both builds succeed

## 📊 Pipeline Performance

**Typical build times:**
- Android: ~5-8 minutes
- iOS: ~10-15 minutes
- Total: ~15-20 minutes

**Optimization tips:**
1. Use dependency caching (already configured)
2. Use Gradle daemon (already configured)
3. Limit to necessary architectures
4. Run jobs in parallel (already configured)

## 🔄 Version Management

### Automatic Version Updates

The pipeline automatically:
1. Extracts version from git tag
2. Updates `versionCode` in Android
3. Updates `versionName` in Android
4. Sets `MARKETING_VERSION` in iOS

### Version Code Generation

Android `versionCode` is generated from version:
- `v1.0.0` → versionCode: `100`
- `v1.2.3` → versionCode: `123`
- `v2.5.1` → versionCode: `251`

### Pre-release Versions

Tags with `-beta`, `-alpha`, `-rc` are marked as pre-release:
- `v1.0.0-beta` → Pre-release
- `v2.0.0-alpha.1` → Pre-release
- `v1.5.0-rc.2` → Pre-release

## 📝 Best Practices

### Before Creating a Release

```bash
# 1. Test locally
npm test
npm run lint

# 2. Test Android build
cd android && ./gradlew assembleRelease

# 3. Test iOS build (on macOS)
cd ios && xcodebuild -workspace ... -scheme ... build

# 4. Update CHANGELOG.md (if you have one)

# 5. Commit all changes
git add .
git commit -m "Prepare for release v1.0.0"
git push

# 6. Create and push tag
git tag v1.0.0
git push origin v1.0.0
```

### Semantic Versioning

Follow [Semantic Versioning](https://semver.org/):

- **MAJOR** (v2.0.0): Breaking changes
- **MINOR** (v1.1.0): New features, backwards compatible
- **PATCH** (v1.0.1): Bug fixes, backwards compatible

### Release Checklist

- [ ] All tests passing
- [ ] Code reviewed and merged
- [ ] Version number decided
- [ ] CHANGELOG updated (if applicable)
- [ ] Local builds successful
- [ ] Tag created and pushed
- [ ] CI/CD pipeline completed successfully
- [ ] Artifacts verified
- [ ] Release notes reviewed
- [ ] Stakeholders notified

## 🔔 Slack Notifications

Configure Slack notifications to get notified when sessions start.

### Setup Slack Webhook

1. Create a Slack webhook URL:
   - Go to [Slack API](https://api.slack.com/messaging/webhooks)
   - Create an incoming webhook for your channel
   - Copy the webhook URL
2. Add to GitHub Secrets:
   - **Name:** `SLACK_WEBHOOK_URL`
   - **Value:** Your webhook URL (e.g., `https://hooks.slack.com/services/...`)

### Notification Content

When a session starts, you'll receive a Slack message with:
- 🚀 App version
- 📱 Session ID and URL
- 🔧 Device ID
- ⏱️ Session validity duration
- 🔗 Direct link to GitHub Actions workflow

## 📚 Related Documentation

- [BUILD_GUIDE.md](BUILD_GUIDE.md) - Manual build instructions
- [SECURITY.md](SECURITY.md) - Security best practices
- [KEYSTORE_INFO.md](KEYSTORE_INFO.md) - Keystore details
- [RELEASE_AUTOMATION.md](RELEASE_AUTOMATION.md) - Automated releases
- [GitHub Actions Documentation](https://docs.github.com/en/actions)

## 🆘 Getting Help

If you encounter issues:

1. Check the **Actions** tab for detailed logs
2. Review this guide
3. Check [BUILD_GUIDE.md](BUILD_GUIDE.md) for manual build steps
4. Verify your tag format
5. Check GitHub Secrets configuration
6. Review error messages in workflow logs

## 📧 Example: Complete Release Flow

```bash
# Step 1: Prepare your code
git checkout main
git pull origin main

# Step 2: Make final changes (if any)
# ... edit files ...
git add .
git commit -m "Final changes for v1.0.0"
git push origin main

# Step 3: Create and push tag
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0

# Step 4: Monitor build
# Go to: https://github.com/{your-repo}/actions

# Step 5: Download artifacts (after build completes)
# Go to: https://github.com/{your-repo}/releases/tag/v1.0.0

# Step 6: Test the builds
adb install NativeBridge-v1.0.0.apk
unzip NativeBridge-iOS-v1.0.0.app.zip
xcrun simctl install booted NativeBridgeDebugApplication.app

# Done! 🎉
```

---

**Created:** 2025-11-27
**Last Updated:** 2025-11-27

For questions or issues with the CI/CD pipeline, check the workflow logs in the Actions tab.
