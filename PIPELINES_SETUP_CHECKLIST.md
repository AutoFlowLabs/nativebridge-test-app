

# Pipelines Setup Checklist

Quick checklist to get your DEV and QA pipelines running.

## ✅ Pre-Setup Checklist

Before you start, ensure you have:

- [ ] GitHub repository with Actions enabled
- [ ] NativeBridge account and API key
- [ ] Admin access to repository settings
- [ ] (Optional) Slack webhook URL for notifications

---

## 📝 Step-by-Step Setup

### Step 1: Configure GitHub Secrets ⚙️

- [ ] Go to: **Settings** → **Secrets and variables** → **Actions**
- [ ] Click: **"New repository secret"**

#### Add Required Secrets:

- [ ] **`NATIVEBRIDGE_API_KEY`**
  - Name: `NATIVEBRIDGE_API_KEY`
  - Value: Your NativeBridge API key (e.g., `Nb-xxxx.yyyy-zzzz`)
  - Click "Add secret"

#### Add Optional Secrets:

- [ ] **`SLACK_WEBHOOK_URL`** (Optional but recommended)
  - Name: `SLACK_WEBHOOK_URL`
  - Value: Your Slack incoming webhook URL
  - Click "Add secret"

### Step 2: Configure Workflow Permissions 🔐

- [ ] Go to: **Settings** → **Actions** → **General**
- [ ] Scroll to: **"Workflow permissions"**
- [ ] Select: ✅ **"Read and write permissions"**
  - This allows DEV Pipeline to trigger QA Pipeline
- [ ] Click: **"Save"**

### Step 3: Add Pre-built APK (Temporary) 📦

Until you add actual build steps to DEV Pipeline:

```bash
# Run these commands in your terminal:

# 1. Create builds folder
mkdir -p builds/

# 2. Copy your APK (must be named exactly: NativeBridge-Production.apk)
cp /path/to/your-app.apk builds/NativeBridge-Production.apk

# 3. Verify it exists
ls -lh builds/NativeBridge-Production.apk

# 4. Add to git
git add builds/NativeBridge-Production.apk

# 5. Commit
git commit -m "Add production APK for nativebridge testing"

# 6. Push
git push
```

- [ ] APK added to `builds/` folder
- [ ] Committed and pushed to repository

### Step 4: Verify Workflow Files Exist 📄

Check these files exist in your repository:

- [ ] `.github/workflows/dev-pipeline.yml`
- [ ] `.github/workflows/qa-pipeline.yml`
- [ ] `appium_tests/src/main/java/SessionlessAppLaunchTest.java`
- [ ] `appium_tests/pom.xml`

If any are missing, they should be in your repository already.

---

## 🧪 Testing the Setup

### Test 1: Manual DEV Pipeline Run

- [ ] Go to: **Actions** tab
- [ ] Click: **"DEV Pipeline - Build and Upload to NativeBridge"**
- [ ] Click: **"Run workflow"**
- [ ] Select branch: `main` (or your default branch)
- [ ] Fill inputs (use defaults):
  ```
  upload_to_nativebridge: true
  trigger_qa_pipeline: true
  device_name: Samsung Galaxy S22 Ultra
  region: ind
  ```
- [ ] Click: **"Run workflow"** button
- [ ] Wait for completion (~8-12 minutes)

**Expected Result:**
- [ ] ✅ Build job succeeds
- [ ] ✅ Upload job succeeds
- [ ] ✅ See `app_id` and `nb_version` in summary
- [ ] ✅ QA Pipeline automatically triggered

**If it fails:**
- Check logs in the failed job
- Verify `NATIVEBRIDGE_API_KEY` secret is set correctly
- Ensure APK exists at `builds/NativeBridge-Production.apk`

### Test 2: Check QA Pipeline Auto-Triggered

After DEV Pipeline completes:

- [ ] Go to: **Actions** tab
- [ ] Look for: **"QA Pipeline - Run Appium Tests"** run
- [ ] It should start automatically after DEV Pipeline
- [ ] Click on the run to see details
- [ ] Wait for completion (~8-12 minutes)

**Expected Result:**
- [ ] ✅ QA Pipeline started automatically
- [ ] ✅ Tests run successfully
- [ ] ✅ See test results in summary
- [ ] ✅ Test logs uploaded as artifact

**If it fails:**
- Check if workflow permissions are set to "Read and write"
- Verify QA Pipeline inputs were passed correctly
- Check test logs artifact for detailed errors

### Test 3: Manual QA Pipeline Run (Independent)

Now test QA Pipeline independently:

- [ ] Note the `app_id` from Test 1 (e.g., `XyZ9`)
- [ ] Note the `nb_version` from Test 1 (e.g., `1.2.3`)
- [ ] Go to: **Actions** tab
- [ ] Click: **"QA Pipeline - Run Appium Tests"**
- [ ] Click: **"Run workflow"**
- [ ] Fill inputs:
  ```
  app_id: XyZ9  (use your actual app_id)
  nb_version: 1.2.3  (use your actual version)
  device_name: Samsung Galaxy S22 Ultra
  region: ind
  triggered_by: manual
  ```
- [ ] Click: **"Run workflow"** button
- [ ] Wait for completion (~8-12 minutes)

**Expected Result:**
- [ ] ✅ QA Pipeline runs independently
- [ ] ✅ No dependency on DEV Pipeline
- [ ] ✅ Tests complete successfully
- [ ] ✅ Can see "Triggered By: manual" in summary

### Test 4: Git Push Trigger

Test automatic trigger via git commit:

```bash
# 1. Make a small change
echo "# Test Pipeline" >> README.md

# 2. Add to git
git add README.md

# 3. Commit with "nativebridge" keyword
git commit -m "Test pipeline trigger for nativebridge"

# 4. Push
git push origin main  # or your default branch
```

- [ ] Committed with "nativebridge" in message
- [ ] Pushed to main/develop/staging branch
- [ ] Go to Actions tab
- [ ] DEV Pipeline should trigger automatically

**Expected Result:**
- [ ] ✅ DEV Pipeline starts automatically
- [ ] ✅ Checks commit message and finds "nativebridge"
- [ ] ✅ Uploads to NativeBridge
- [ ] ✅ QA Pipeline triggers automatically

### Test 5: Git Push WITHOUT Upload

Test that pipeline skips upload when commit doesn't have keyword:

```bash
# 1. Make a change
echo "# Another test" >> README.md

# 2. Commit WITHOUT "nativebridge" keyword
git add README.md
git commit -m "Update README"

# 3. Push
git push
```

- [ ] Pushed commit without "nativebridge" keyword
- [ ] Go to Actions tab
- [ ] DEV Pipeline should trigger but skip upload

**Expected Result:**
- [ ] ✅ DEV Pipeline starts
- [ ] ✅ Build job runs
- [ ] ⏭️ Upload job is skipped
- [ ] ⏭️ QA Pipeline is NOT triggered
- [ ] ℹ️ Summary shows "Upload skipped"

---

## 🎯 Verification Checklist

After all tests pass:

### DEV Pipeline Verification

- [ ] ✅ Can trigger via git push (with "nativebridge" keyword)
- [ ] ✅ Can trigger via GitHub UI (manual)
- [ ] ✅ Builds APK successfully
- [ ] ✅ Uploads to NativeBridge successfully
- [ ] ✅ Returns `app_id`, `nb_version`, `magic_link`
- [ ] ✅ Triggers QA Pipeline automatically
- [ ] ✅ Can skip upload when needed
- [ ] ✅ Can disable QA trigger when needed

### QA Pipeline Verification

- [ ] ✅ Triggered automatically by DEV Pipeline
- [ ] ✅ Can run independently (manual trigger)
- [ ] ✅ Validates all required inputs
- [ ] ✅ Compiles test code successfully
- [ ] ✅ Runs sessionless Appium tests
- [ ] ✅ Generates test reports
- [ ] ✅ Uploads test logs as artifact
- [ ] ✅ Shows pass/fail status clearly

### Secrets Verification

- [ ] ✅ `NATIVEBRIDGE_API_KEY` is set and working
- [ ] ✅ (Optional) `SLACK_WEBHOOK_URL` is set and notifications work

### Permissions Verification

- [ ] ✅ Workflow permissions are "Read and write"
- [ ] ✅ DEV Pipeline can trigger QA Pipeline

---

## 🔧 Optional Configuration

### Enable Slack Notifications

If you haven't set up Slack yet:

1. Create Slack Incoming Webhook:
   - [ ] Go to: https://api.slack.com/messaging/webhooks
   - [ ] Create webhook for your workspace
   - [ ] Copy webhook URL

2. Add to GitHub Secrets:
   - [ ] Settings → Secrets → Actions
   - [ ] Add `SLACK_WEBHOOK_URL`
   - [ ] Paste webhook URL

3. Test:
   - [ ] Run QA Pipeline
   - [ ] Check Slack channel for notification

### Add Real Build Steps to DEV Pipeline

Replace pre-built APK with actual build:

- [ ] Open `.github/workflows/dev-pipeline.yml`
- [ ] Find Job 2: "Build Application"
- [ ] Replace "Prepare APK (Using Pre-built)" step with actual build steps
- [ ] See DEV_QA_PIPELINES_GUIDE.md for examples

### Customize Test Devices

Add more device options to QA Pipeline:

- [ ] Update `device_name` default in `qa-pipeline.yml`
- [ ] QA team can specify any device from NativeBridge dashboard

---

## 📊 Success Criteria

You're fully set up when:

- [ ] ✅ DEV Pipeline runs on git push (with "nativebridge" keyword)
- [ ] ✅ DEV Pipeline can be triggered manually
- [ ] ✅ QA Pipeline triggers automatically after DEV Pipeline
- [ ] ✅ QA Pipeline can run independently
- [ ] ✅ All tests pass on default device
- [ ] ✅ Test logs are available in artifacts
- [ ] ✅ GitHub summaries show clear results
- [ ] ✅ (Optional) Slack notifications working

---

## 🚨 Common Issues During Setup

### Issue: DEV Pipeline fails at upload

**Symptoms:**
```
❌ ERROR: NATIVEBRIDGE_API_KEY not configured
```

**Solution:**
- [ ] Verify secret is named exactly: `NATIVEBRIDGE_API_KEY`
- [ ] Check there are no extra spaces in the secret value
- [ ] Ensure secret is in the correct repository

### Issue: QA Pipeline not triggered automatically

**Symptoms:**
- DEV Pipeline succeeds
- QA Pipeline doesn't start

**Solution:**
- [ ] Check workflow permissions: Settings → Actions → General
- [ ] Ensure "Read and write permissions" is selected
- [ ] Verify `trigger_qa_pipeline` input is `true` (manual trigger)
- [ ] Check DEV Pipeline output shows "QA Trigger: success"

### Issue: Tests fail with "device not available"

**Symptoms:**
```
❌ Test failed
Error: Device not available
```

**Solution:**
- [ ] Verify device name matches NativeBridge dashboard exactly
- [ ] Check device is online in the specified region
- [ ] Try different device or region

### Issue: APK not found

**Symptoms:**
```
❌ ERROR: Pre-built APK not found at builds/NativeBridge-Production.apk
```

**Solution:**
- [ ] Ensure APK is committed to repository
- [ ] Check file is named exactly: `NativeBridge-Production.apk`
- [ ] Verify file is in `builds/` folder (not `build/`)
- [ ] Run: `git pull` to get latest changes

---

## 📚 Next Steps

After successful setup:

1. [ ] Read: `DEV_QA_PIPELINES_GUIDE.md` for detailed usage
2. [ ] Train team on:
   - How to trigger pipelines
   - How to read results
   - When to use manual vs automatic
3. [ ] Set up Slack notifications (if not done)
4. [ ] Replace pre-built APK with actual build steps
5. [ ] Create team documentation for your specific workflow
6. [ ] Test on multiple devices
7. [ ] Set up monitoring for pipeline failures

---

## ✅ Setup Complete!

If all items above are checked, your pipelines are ready! 🎉

**Quick Reference:**

| Task | Pipeline | Method |
|------|----------|--------|
| Deploy and test | DEV Pipeline | Git push with "nativebridge" |
| Upload and test | DEV Pipeline | Manual trigger (GitHub UI) |
| Test existing app | QA Pipeline | Manual trigger with app_id |
| Re-test | QA Pipeline | Manual trigger with same app_id |

**Key URLs:**
- Actions: `https://github.com/YOUR_ORG/YOUR_REPO/actions`
- Settings: `https://github.com/YOUR_ORG/YOUR_REPO/settings/secrets/actions`
- NativeBridge Dashboard: `https://dashboard.nativebridge.io`

---

**Need help?** Check `DEV_QA_PIPELINES_GUIDE.md` for:
- Detailed usage examples
- Troubleshooting guide
- Best practices
- Advanced configuration
