# DEV & QA Pipelines Guide

Complete guide for the two-pipeline CI/CD system for NativeBridge Appium testing.

## 📋 Table of Contents

- [Overview](#overview)
- [Pipeline Architecture](#pipeline-architecture)
- [DEV Pipeline](#dev-pipeline)
- [QA Pipeline](#qa-pipeline)
- [Setup Guide](#setup-guide)
- [Usage Examples](#usage-examples)
- [Troubleshooting](#troubleshooting)

---

## Overview

### What Are These Pipelines?

This repository contains **two independent but integrated CI/CD pipelines**:

1. **DEV Pipeline** (`dev-pipeline.yml`)
   - Builds the application
   - Uploads to NativeBridge
   - Automatically triggers QA Pipeline

2. **QA Pipeline** (`qa-pipeline.yml`)
   - Runs sessionless Appium tests
   - Can run independently or triggered by DEV Pipeline
   - Generates test reports

### Key Features

✅ **Back-to-back execution** - DEV automatically triggers QA
✅ **Independent execution** - QA can run separately
✅ **Flexible triggers** - Git commits or manual UI
✅ **Configurable parameters** - Customize device, region, etc.
✅ **Comprehensive reporting** - Test logs, screenshots, summaries

---

## Pipeline Architecture

```
╔══════════════════════════════════════════════════════════════════╗
║                        PIPELINE FLOW                             ║
╚══════════════════════════════════════════════════════════════════╝

┌─────────────────────────────────────────────────────────────────┐
│                       DEV PIPELINE                               │
│  File: .github/workflows/dev-pipeline.yml                       │
└─────────────────────────────────────────────────────────────────┘
         │
         │  Trigger 1: Git Push (if commit has "nativebridge")
         │  Trigger 2: Manual (GitHub UI)
         │
         ▼
    ┌─────────────────────────────┐
    │  1. Check Upload Trigger     │
    │     - Push: Check commit msg │
    │     - Manual: Use input      │
    └─────────────────────────────┘
         │
         ▼
    ┌─────────────────────────────┐
    │  2. Build Application        │
    │     - Compile APK            │
    │     - Upload as artifact     │
    └─────────────────────────────┘
         │
         ▼  (if should_upload == true)
    ┌─────────────────────────────┐
    │  3. Upload to NativeBridge   │
    │     Output:                  │
    │     ✓ app_id                 │
    │     ✓ nb_version             │
    │     ✓ magic_link             │
    └─────────────────────────────┘
         │
         ▼  (if upload succeeded)
    ┌─────────────────────────────┐
    │  4. Trigger QA Pipeline      │
    │     Pass:                    │
    │     • app_id                 │
    │     • nb_version             │
    │     • device_name            │
    │     • region                 │
    └─────────────────────────────┘
         │
         │
         ▼
┌─────────────────────────────────────────────────────────────────┐
│                       QA PIPELINE                                │
│  File: .github/workflows/qa-pipeline.yml                        │
└─────────────────────────────────────────────────────────────────┘
         │
         │  Trigger 1: Called by DEV Pipeline
         │  Trigger 2: Manual (QA team via GitHub UI)
         │
         ▼
    ┌─────────────────────────────┐
    │  1. Validate Inputs          │
    │     - app_id                 │
    │     - nb_version             │
    │     - device_name            │
    │     - region                 │
    └─────────────────────────────┘
         │
         ▼
    ┌─────────────────────────────┐
    │  2. Setup Environment        │
    │     - Checkout code          │
    │     - Setup Java 17          │
    │     - Compile tests          │
    └─────────────────────────────┘
         │
         ▼
    ┌─────────────────────────────┐
    │  3. Run Appium Tests         │
    │     - Execute sessionless    │
    │     - Capture logs           │
    │     - Take screenshots       │
    └─────────────────────────────┘
         │
         ▼
    ┌─────────────────────────────┐
    │  4. Generate Report          │
    │     - Test summary           │
    │     - Upload artifacts       │
    │     - Send notifications     │
    └─────────────────────────────┘
```

---

## DEV Pipeline

### Purpose

Build application and upload to NativeBridge, then trigger QA tests.

### File Location

`.github/workflows/dev-pipeline.yml`

### Triggers

#### 1. Git Push (Automatic)

Triggers when you push to:
- `main` branch
- `develop` branch
- `staging` branch

**Important:** Upload to NativeBridge only happens if commit message contains `"nativebridge"` (case-insensitive).

**Examples:**

```bash
# ✅ WILL upload to NativeBridge and trigger QA
git commit -m "Add new feature for nativebridge testing"
git push

# ✅ WILL upload
git commit -m "Fix bug - deploy to NativeBridge"
git push

# ❌ Will NOT upload (no "nativebridge" in message)
git commit -m "Update README"
git push
```

#### 2. Manual Trigger (GitHub UI)

1. Go to **Actions** tab
2. Select **"DEV Pipeline - Build and Upload to NativeBridge"**
3. Click **"Run workflow"**
4. Fill parameters (see below)

### Parameters (Manual Trigger Only)

| Parameter | Required | Default | Description |
|-----------|----------|---------|-------------|
| `upload_to_nativebridge` | Yes | `true` | Upload APK to NativeBridge? |
| `trigger_qa_pipeline` | Yes | `true` | Trigger QA Pipeline after upload? |
| `device_name` | No | `Samsung Galaxy S22 Ultra` | Device for QA tests |
| `region` | No | `ind` | Region (ind, usa, etc.) |

### Jobs

#### Job 1: Check Upload Trigger
- Determines if upload should happen
- For push: Checks if commit message contains "nativebridge"
- For manual: Uses `upload_to_nativebridge` parameter

#### Job 2: Build Application
- **TODO:** Add your actual build steps here
- Currently uses pre-built APK from `builds/` folder
- Uploads APK as GitHub artifact

**Adding Real Build Steps:**

```yaml
# Replace the "Prepare APK" step with:

- name: Set up JDK 17
  uses: actions/setup-java@v4
  with:
    distribution: 'temurin'
    java-version: '17'

- name: Setup Android SDK
  uses: android-actions/setup-android@v3

- name: Build APK
  run: |
    cd android
    ./gradlew assembleRelease

- name: Copy APK
  run: |
    mkdir -p builds/
    cp android/app/build/outputs/apk/release/app-release.apk \
       builds/NativeBridge-Production.apk
```

#### Job 3: Upload to NativeBridge
- Only runs if `should_upload == true`
- Uploads APK via NativeBridge API
- Extracts outputs:
  - `app_id` - NativeBridge app ID
  - `nb_version` - NativeBridge version
  - `magic_link` - Installation link

#### Job 4: Trigger QA Pipeline
- Only runs if upload succeeded
- Calls QA Pipeline with parameters:
  - `app_id` from upload
  - `nb_version` from upload
  - `device_name` from input or default
  - `region` from input or default
  - `triggered_by: 'dev-pipeline'`

### Outputs

The pipeline creates outputs that are passed to QA Pipeline:

```yaml
app_id: "HgWp"           # NativeBridge app ID
nb_version: "1.2.3"      # Version from NativeBridge
magic_link: "https://..." # Installation link
```

---

## QA Pipeline

### Purpose

Run sessionless Appium tests on NativeBridge-hosted apps.

### File Location

`.github/workflows/qa-pipeline.yml`

### Triggers

#### 1. Automatic (Called by DEV Pipeline)

When DEV Pipeline successfully uploads an app, it automatically triggers QA Pipeline with all required parameters.

**You don't need to do anything** - it happens automatically!

#### 2. Manual (QA Team Independent Execution)

QA team can run tests independently without waiting for DEV:

1. Go to **Actions** tab
2. Select **"QA Pipeline - Run Appium Tests"**
3. Click **"Run workflow"**
4. Fill parameters (see below)

### Parameters

| Parameter | Required | Default | Description |
|-----------|----------|---------|-------------|
| `app_id` | **Yes** | - | NativeBridge app ID (e.g., `HgWp`) |
| `nb_version` | **Yes** | - | NativeBridge version (e.g., `1.2.3`) |
| `device_name` | **Yes** | `Samsung Galaxy S22 Ultra` | Device to test on |
| `region` | No | `ind` | Region (ind, usa, etc.) |
| `triggered_by` | No | `manual` | Auto-set by DEV or manual |

### Jobs

#### Job 1: Validate Inputs
- Validates all required parameters
- Ensures no empty values
- Displays test configuration

#### Job 2: Setup Test Environment
- Checkout repository
- Setup Java 17
- Cache Maven dependencies
- Compile test code
- Upload compiled tests as artifact

#### Job 3: Run Appium Tests
- Downloads compiled tests
- Sets environment variables:
  - `NATIVEBRIDGE_API_KEY`
  - `APP_ID`
  - `DEVICE_NAME`
  - `REGION`
- Runs: `mvn exec:java -P sessionless`
- Captures test output to log file
- Calculates test duration

#### Job 4: Generate Test Report
- Creates comprehensive test summary
- Shows pass/fail status
- Lists all parameters used
- Links to artifacts (logs, screenshots)

#### Job 5: Send Slack Notification (Optional)
- Sends Slack notification if webhook configured
- Includes test result, duration, device info
- Links to full report

### Outputs

```yaml
test_result: "passed" or "failed"
test_duration: "5m 32s"
```

### Artifacts

QA Pipeline uploads these artifacts:

1. **Test Logs** (`test-logs-{app_id}-{run_id}`)
   - Complete console output
   - Appium driver logs
   - Test execution details

2. **Screenshots** (`test-screenshots-{app_id}-{run_id}`)
   - Screenshots captured during tests
   - Only if tests capture screenshots

Retention: 30 days

---

## Setup Guide

### Prerequisites

- GitHub repository with Actions enabled
- NativeBridge account with API key
- (Optional) Slack webhook for notifications

### Step 1: Configure GitHub Secrets

Go to: **Settings** → **Secrets and variables** → **Actions** → **New repository secret**

#### Required Secrets:

1. **`NATIVEBRIDGE_API_KEY`**
   - Your NativeBridge API key
   - Format: `Nb-xxxx.yyyy-zzzz`
   - Used by both pipelines

#### Optional Secrets:

2. **`SLACK_WEBHOOK_URL`**
   - Slack incoming webhook URL
   - Used for test notifications

### Step 2: Add Pre-built APK (Temporary)

Until you add actual build steps:

```bash
# Create builds folder
mkdir -p builds/

# Add your APK (must be named exactly this)
cp /path/to/your-app.apk builds/NativeBridge-Production.apk

# Commit and push
git add builds/
git commit -m "Add production APK"
git push
```

### Step 3: Test DEV Pipeline (Manual)

1. Go to **Actions** → **"DEV Pipeline - Build and Upload to NativeBridge"**
2. Click **"Run workflow"**
3. Use defaults:
   - `upload_to_nativebridge: true`
   - `trigger_qa_pipeline: true`
   - `device_name: Samsung Galaxy S22 Ultra`
   - `region: ind`
4. Click **"Run workflow"**
5. Wait for completion

**Expected Result:**
- ✅ Build succeeds
- ✅ Upload to NativeBridge succeeds
- ✅ QA Pipeline automatically triggered
- ✅ Tests run and pass

### Step 4: Test QA Pipeline (Independent)

1. Get `app_id` from previous DEV Pipeline run
2. Go to **Actions** → **"QA Pipeline - Run Appium Tests"**
3. Click **"Run workflow"**
4. Fill parameters:
   - `app_id: HgWp` (use your actual app_id)
   - `nb_version: 1.0.0` (use your version)
   - `device_name: Samsung Galaxy S22 Ultra`
   - `region: ind`
5. Click **"Run workflow"**

**Expected Result:**
- ✅ Tests run independently
- ✅ No dependency on DEV Pipeline

### Step 5: Test Git Push Trigger

```bash
# Make a change
echo "test" >> README.md

# Commit with "nativebridge" in message
git add .
git commit -m "Update README for nativebridge deployment"
git push

# DEV Pipeline should automatically trigger!
```

---

## Usage Examples

### Example 1: DEV Makes a Change (Full Flow)

**Scenario:** Developer adds new feature and wants to test on NativeBridge.

**Steps:**

```bash
# 1. Make code changes
git add .

# 2. Commit with "nativebridge" keyword
git commit -m "Add new login feature - test on nativebridge"

# 3. Push to main/develop branch
git push origin develop
```

**What Happens:**
1. DEV Pipeline triggers automatically
2. Checks commit message → finds "nativebridge"
3. Builds APK
4. Uploads to NativeBridge → gets `app_id: XyZ9`, `nb_version: 1.2.3`
5. Triggers QA Pipeline with these parameters
6. QA Pipeline runs tests on `Samsung Galaxy S22 Ultra`
7. Results posted to GitHub summary and Slack

**Timeline:** ~10-15 minutes total

### Example 2: QA Tests Different Device (Independent)

**Scenario:** QA team wants to test existing app on different device.

**Steps:**

1. Go to Actions → "QA Pipeline - Run Appium Tests"
2. Click "Run workflow"
3. Fill parameters:
   ```
   app_id: XyZ9
   nb_version: 1.2.3
   device_name: Google Pixel 7 Pro
   region: usa
   ```
4. Click "Run workflow"

**What Happens:**
1. QA Pipeline runs independently
2. Uses existing app (no upload needed)
3. Tests on Google Pixel 7 Pro in USA region
4. Results available in ~8-12 minutes

**Timeline:** ~8-12 minutes

### Example 3: DEV Builds Without Testing (Manual)

**Scenario:** Developer wants to build and upload but not run tests yet.

**Steps:**

1. Go to Actions → "DEV Pipeline - Build and Upload to NativeBridge"
2. Click "Run workflow"
3. Fill parameters:
   ```
   upload_to_nativebridge: true
   trigger_qa_pipeline: false  ← Disable QA trigger
   device_name: (ignored)
   region: (ignored)
   ```
4. Click "Run workflow"

**What Happens:**
1. DEV Pipeline builds and uploads
2. QA Pipeline is NOT triggered
3. Get `app_id` and `nb_version` in summary
4. QA team can test later using these values

**Timeline:** ~5-8 minutes

### Example 4: QA Tests Multiple Devices (Parallel)

**Scenario:** QA wants to test same app on 3 different devices.

**Steps:**

Run QA Pipeline 3 times in parallel with different devices:

**Run 1:**
```
app_id: XyZ9
nb_version: 1.2.3
device_name: Samsung Galaxy S22 Ultra
region: ind
```

**Run 2:**
```
app_id: XyZ9
nb_version: 1.2.3
device_name: Google Pixel 7 Pro
region: usa
```

**Run 3:**
```
app_id: XyZ9
nb_version: 1.2.3
device_name: iPhone 14 Pro Max
region: usa
```

**What Happens:**
- All 3 pipelines run simultaneously
- Each tests on different device
- Results available independently

**Timeline:** ~8-12 minutes (parallel, not sequential!)

### Example 5: Git Push Without Upload

**Scenario:** Developer makes documentation change.

**Steps:**

```bash
git add .
git commit -m "Update README"  # No "nativebridge" keyword
git push
```

**What Happens:**
1. DEV Pipeline triggers
2. Checks commit message → no "nativebridge" found
3. Skips upload to NativeBridge
4. Skips QA Pipeline trigger
5. Just builds APK as artifact

**Timeline:** ~3-5 minutes

---

## Troubleshooting

### DEV Pipeline Issues

#### Issue: "APK not found at builds/NativeBridge-Production.apk"

**Cause:** Pre-built APK is missing.

**Solution:**
```bash
# Add APK to repository
cp your-app.apk builds/NativeBridge-Production.apk
git add builds/
git commit -m "Add APK"
git push
```

**Or:** Add actual build steps (see Job 2 documentation above).

#### Issue: "NATIVEBRIDGE_API_KEY not configured"

**Cause:** Secret not set.

**Solution:**
1. Go to Settings → Secrets and variables → Actions
2. Add `NATIVEBRIDGE_API_KEY` with your API key

#### Issue: Upload succeeded but QA Pipeline not triggered

**Cause 1:** `trigger_qa_pipeline` is set to `false` (manual trigger).

**Solution:** Set to `true` in manual trigger inputs.

**Cause 2:** QA Pipeline workflow file is missing or has errors.

**Solution:** Ensure `qa-pipeline.yml` exists and is valid.

#### Issue: "Could not extract nb_version from response"

**Cause:** NativeBridge API response doesn't include version field.

**Solution:** Pipeline will use `"latest"` as fallback. This is okay - tests will still run.

### QA Pipeline Issues

#### Issue: "app_id is required"

**Cause:** Missing `app_id` parameter.

**Solution:**
1. Get `app_id` from DEV Pipeline output or NativeBridge dashboard
2. Provide in manual trigger

#### Issue: Tests timeout after 15 minutes

**Cause:** Sessionless mode can take time for device session creation.

**Solution:**
- This is expected for first test on a device
- Subsequent tests on same device are faster
- If consistently timing out, check device availability

#### Issue: "Test failed" but logs show "device not available"

**Cause:** Device name is incorrect or device is offline.

**Solution:**
1. Check exact device name in NativeBridge dashboard
2. Ensure device name matches exactly (case-sensitive)
3. Verify device is online in the region

#### Issue: Screenshots not uploaded

**Cause:** Tests don't generate screenshots or path is wrong.

**Solution:**
- Ensure tests save screenshots to `appium_tests/screenshots/`
- This is optional - tests can run without screenshots

### Common Issues (Both Pipelines)

#### Issue: Maven compilation fails

**Cause:** Dependencies not downloaded or Java version mismatch.

**Solution:**
- Pipeline uses Java 17 and caches Maven dependencies
- Check `pom.xml` is valid
- Clear GitHub Actions cache if needed

#### Issue: "GITHUB_TOKEN permissions error" when triggering QA

**Cause:** Insufficient permissions for workflow dispatch.

**Solution:**
1. Go to Settings → Actions → General
2. Under "Workflow permissions", select:
   - ✅ "Read and write permissions"
3. Save

---

## Advanced Configuration

### Customizing Build Steps

Edit `dev-pipeline.yml` Job 2 to add your build process:

```yaml
- name: Build Application
  run: |
    # Your build commands here
    ./gradlew assembleRelease

- name: Run Tests
  run: |
    ./gradlew test

- name: Sign APK
  run: |
    # APK signing
```

### Adding Test Stages

You can modify QA Pipeline to run different test suites:

```yaml
# In qa-pipeline.yml, add new jobs:

smoke-tests:
  name: Smoke Tests
  # ... run smoke tests

regression-tests:
  name: Regression Tests
  needs: [smoke-tests]
  # ... run full regression
```

### Custom Device Matrix

Test on multiple devices automatically:

```yaml
# In qa-pipeline.yml:

strategy:
  matrix:
    device:
      - Samsung Galaxy S22 Ultra
      - Google Pixel 7 Pro
      - iPhone 14 Pro Max
```

---

## Pipeline Outputs Summary

### DEV Pipeline Outputs

| Output | Description | Example | Used By |
|--------|-------------|---------|---------|
| `app_id` | NativeBridge app ID | `XyZ9` | QA Pipeline |
| `nb_version` | App version in NativeBridge | `1.2.3` | QA Pipeline |
| `magic_link` | Installation link | `https://...` | Manual testing |
| `upload_success` | Upload status | `true/false` | QA trigger decision |

### QA Pipeline Outputs

| Output | Description | Example |
|--------|-------------|---------|
| `test_result` | Test pass/fail status | `passed` |
| `test_duration` | How long tests took | `5m 32s` |

---

## Best Practices

### For Developers

1. ✅ **Always include "nativebridge" in commit message** when you want to deploy and test
2. ✅ **Use meaningful commit messages** to track what's being tested
3. ✅ **Check DEV Pipeline output** before assuming tests ran
4. ✅ **Add actual build steps** instead of using pre-built APK

**Good commit messages:**
```bash
git commit -m "feat: Add dark mode - test on nativebridge"
git commit -m "fix: Login bug - deploy to nativebridge for QA"
```

**Bad commit messages:**
```bash
git commit -m "changes"  # Too vague
git commit -m "fix bug"  # No "nativebridge" keyword
```

### For QA Team

1. ✅ **Test on multiple devices** using parallel QA Pipeline runs
2. ✅ **Keep `app_id` and `nb_version` handy** for independent testing
3. ✅ **Check test logs** in artifacts for detailed debugging
4. ✅ **Verify device names** match exactly with NativeBridge dashboard

### For Both

1. ✅ **Monitor Slack notifications** for quick feedback
2. ✅ **Use GitHub Actions summary** for detailed reports
3. ✅ **Download artifacts** when troubleshooting failures
4. ✅ **Keep secrets updated** (API keys, webhooks)

---

## Quick Reference

### DEV Pipeline Triggers

| Method | Command/Action | Upload Condition |
|--------|---------------|------------------|
| Git Push | `git push` | Commit message contains "nativebridge" |
| Manual | Actions → Run workflow | Set `upload_to_nativebridge: true` |

### QA Pipeline Triggers

| Method | Command/Action | Requirements |
|--------|---------------|--------------|
| Auto | DEV Pipeline success | DEV Pipeline uploads successfully |
| Manual | Actions → Run workflow | `app_id` and `nb_version` |

### Required Secrets

| Secret | Required By | Purpose |
|--------|-------------|---------|
| `NATIVEBRIDGE_API_KEY` | Both | API authentication |
| `SLACK_WEBHOOK_URL` | QA (optional) | Slack notifications |

### File Locations

| File | Purpose |
|------|---------|
| `.github/workflows/dev-pipeline.yml` | DEV Pipeline |
| `.github/workflows/qa-pipeline.yml` | QA Pipeline |
| `appium_tests/src/main/java/SessionlessAppLaunchTest.java` | Test code |
| `builds/NativeBridge-Production.apk` | Pre-built APK (temporary) |

---

## Support

### Documentation Files

- This guide: `DEV_QA_PIPELINES_GUIDE.md`
- Workflow diagram: `PIPELINES_WORKFLOW_DIAGRAM.md`
- Setup checklist: `PIPELINES_SETUP_CHECKLIST.md`

### Getting Help

1. Check troubleshooting section above
2. Review workflow logs in Actions tab
3. Check artifact logs for detailed errors
4. Verify all secrets are configured

### Common Commands

```bash
# Check if APK exists
ls -lh builds/NativeBridge-Production.apk

# View commit messages (check for "nativebridge")
git log --oneline -10

# Test locally (requires env vars)
cd appium_tests
export NATIVEBRIDGE_API_KEY="your-key"
export APP_ID="your-app-id"
export DEVICE_NAME="Samsung Galaxy S22 Ultra"
export REGION="ind"
mvn exec:java -P sessionless
```

---

## What's Next?

After setup:

1. ✅ Run DEV Pipeline manually to verify it works
2. ✅ Verify QA Pipeline runs automatically
3. ✅ Test QA Pipeline independently
4. ✅ Replace pre-built APK with actual build steps
5. ✅ Configure Slack notifications
6. ✅ Train team on usage

**You're all set!** 🚀
