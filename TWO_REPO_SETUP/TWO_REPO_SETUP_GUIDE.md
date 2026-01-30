# Two-Repository Setup Guide

Complete guide for setting up separate DEV and QA repositories for NativeBridge testing.

## 📋 Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Repository Setup](#repository-setup)
- [Configuration](#configuration)
- [Testing the Setup](#testing-the-setup)
- [Usage Examples](#usage-examples)
- [Troubleshooting](#troubleshooting)

---

## Overview

### What is Two-Repo Setup?

Instead of having both pipelines in one repository, you split them into two separate repositories:

1. **DEV Repository** (Android App Repo)
   - Contains: Android app source code
   - Workflow: Build APK → Upload to NativeBridge → Trigger QA repo
   - Team: Developers

2. **QA Repository** (Test Automation Repo)
   - Contains: Appium test code (Java)
   - Workflow: Run tests on NativeBridge
   - Team: QA Engineers

### Benefits

✅ **Separation of Concerns** - Dev code separate from test code
✅ **Independent Teams** - Dev and QA work independently
✅ **Flexible Access** - Different permissions per repo
✅ **Clean Organization** - Each repo has single responsibility
✅ **Scalable** - Easy to add more test repos for different apps

---

## Architecture

```
┌───────────────────────────────────────────────────────────┐
│  DEV REPOSITORY (android-app-repo)                        │
│                                                           │
│  Contents:                                                │
│  ├── Android app source code                             │
│  ├── .github/workflows/build-and-upload.yml              │
│  └── builds/ (pre-built APKs)                            │
│                                                           │
│  Workflow:                                                │
│  1. Build APK                                             │
│  2. Upload to NativeBridge                                │
│  3. Get: app_id, nb_version                               │
│  4. Trigger QA Repository ───────────────────────────────┐│
└───────────────────────────────────────────────────────────┘│
                                                            │
                                          Cross-Repo        │
                                          Trigger           │
                                          (GitHub API)      │
                                                            │
┌───────────────────────────────────────────────────────────┘
│
│
▼
┌───────────────────────────────────────────────────────────┐
│  QA REPOSITORY (qa-automation-repo)                       │
│                                                           │
│  Contents:                                                │
│  ├── Appium test code (Java)                             │
│  ├── .github/workflows/qa-tests.yml                      │
│  └── appium_tests/                                        │
│                                                           │
│  Workflow:                                                │
│  1. Receive: app_id, nb_version, device, region          │
│  2. Setup test environment                                │
│  3. Run Appium tests                                      │
│  4. Generate reports                                      │
│  5. Send notifications                                    │
└───────────────────────────────────────────────────────────┘
```

---

## Repository Setup

### Step 1: Create DEV Repository

1. **Create new GitHub repository:**
   ```
   Name: android-app-repo
   Description: Android application with NativeBridge CI/CD
   Visibility: Private (recommended)
   ```

2. **Copy files from `TWO_REPO_SETUP/DEV_REPO/`:**
   ```bash
   # In your new android-app-repo
   mkdir -p .github/workflows

   # Copy workflow file
   cp TWO_REPO_SETUP/DEV_REPO/.github/workflows/build-and-upload.yml \
      .github/workflows/

   # Copy README
   cp TWO_REPO_SETUP/DEV_REPO/README.md .

   # Create builds folder for pre-built APK
   mkdir -p builds/
   ```

3. **Add your Android app code:**
   ```
   android-app-repo/
   ├── .github/workflows/build-and-upload.yml  ✅
   ├── android/                                 ← Your Android code
   ├── builds/                                  ✅
   └── README.md                                ✅
   ```

4. **Commit and push:**
   ```bash
   git add .
   git commit -m "Initial setup with NativeBridge CI/CD"
   git push
   ```

### Step 2: Create QA Repository

1. **Create new GitHub repository:**
   ```
   Name: qa-automation-repo
   Description: QA test automation for NativeBridge
   Visibility: Private (recommended)
   ```

2. **Copy files from `TWO_REPO_SETUP/QA_REPO/`:**
   ```bash
   # In your new qa-automation-repo
   mkdir -p .github/workflows

   # Copy workflow file
   cp TWO_REPO_SETUP/QA_REPO/.github/workflows/qa-tests.yml \
      .github/workflows/

   # Copy README
   cp TWO_REPO_SETUP/QA_REPO/README.md .

   # Copy test code
   cp -r appium_tests/ .
   ```

3. **Verify structure:**
   ```
   qa-automation-repo/
   ├── .github/workflows/qa-tests.yml  ✅
   ├── appium_tests/                    ✅
   │   ├── src/main/java/
   │   └── pom.xml
   └── README.md                        ✅
   ```

4. **Commit and push:**
   ```bash
   git add .
   git commit -m "Initial QA automation setup"
   git push
   ```

---

## Configuration

### DEV Repository Configuration

#### 1. Create Personal Access Token (PAT)

The DEV repo needs a PAT to trigger the QA repo:

1. Go to: **GitHub Settings** (your profile) → **Developer settings** → **Personal access tokens** → **Tokens (classic)**
2. Click: **Generate new token (classic)**
3. Name: `NativeBridge QA Trigger Token`
4. Expiration: Choose appropriate duration (90 days recommended)
5. Select scopes:
   - ✅ `repo` (Full control of private repositories)
   - ✅ `workflow` (Update GitHub Action workflows)
6. Click: **Generate token**
7. **Copy the token immediately** (you won't see it again!)

#### 2. Add Secrets to DEV Repository

Go to DEV repo: **Settings → Secrets and variables → Actions → Secrets**

| Secret Name | Value | Description |
|-------------|-------|-------------|
| `NATIVEBRIDGE_API_KEY` | `Nb-xxxx.yyyy-zzzz` | Your NativeBridge API key |
| `QA_REPO_PAT` | `ghp_xxxxx...` | PAT from step 1 |
| `SLACK_WEBHOOK_URL` | `https://hooks.slack.com/...` | (Optional) Slack webhook |

#### 3. Add Variables to DEV Repository

Go to DEV repo: **Settings → Secrets and variables → Actions → Variables**

| Variable Name | Value | Description |
|---------------|-------|-------------|
| `QA_REPO_OWNER` | `YourGitHubOrg` or `YourUsername` | QA repository owner |
| `QA_REPO_NAME` | `qa-automation-repo` | QA repository name |
| `DEFAULT_DEVICE` | `Samsung Galaxy S22 Ultra` | Default test device |
| `DEFAULT_REGION` | `ind` | Default region |

**Example:**
- If QA repo URL is: `https://github.com/AutoFlowLabs/qa-automation-repo`
- Then: `QA_REPO_OWNER=AutoFlowLabs`, `QA_REPO_NAME=qa-automation-repo`

### QA Repository Configuration

#### Add Secrets to QA Repository

Go to QA repo: **Settings → Secrets and variables → Actions → Secrets**

| Secret Name | Value | Description |
|-------------|-------|-------------|
| `NATIVEBRIDGE_API_KEY` | `Nb-xxxx.yyyy-zzzz` | Your NativeBridge API key (same as DEV) |
| `SLACK_WEBHOOK_URL` | `https://hooks.slack.com/...` | (Optional) Slack webhook |

**Note:** QA repo does NOT need `QA_REPO_PAT` - only DEV repo needs it!

---

## Testing the Setup

### Test 1: Manual DEV Pipeline

Let's verify DEV repo can build and upload:

1. Go to DEV repo → **Actions** tab
2. Click: **"Build and Upload to NativeBridge"**
3. Click: **"Run workflow"**
4. Fill inputs:
   ```
   upload_to_nativebridge: true
   trigger_qa_tests: false  ← Disable for now
   device_name: (leave empty)
   region: (leave empty)
   ```
5. Click: **"Run workflow"**

**Expected Result:**
- ✅ Build job succeeds
- ✅ Upload job succeeds
- ✅ See `app_id` and `nb_version` in summary
- ⏭️ QA trigger skipped (we disabled it)

**If it fails:** Check troubleshooting section below.

### Test 2: Manual QA Pipeline

Now verify QA repo can run tests:

1. Note the `app_id` from Test 1 (e.g., `XyZ9`)
2. Go to QA repo → **Actions** tab
3. Click: **"Run QA Tests"**
4. Click: **"Run workflow"**
5. Fill inputs:
   ```
   app_id: XyZ9  (use your actual app_id)
   nb_version: 1.0.0  (or your version)
   device_name: Samsung Galaxy S22 Ultra
   region: ind
   triggered_by: manual
   ```
6. Click: **"Run workflow"**

**Expected Result:**
- ✅ Validation succeeds
- ✅ Tests compile
- ✅ Tests run
- ✅ Test logs uploaded

### Test 3: Cross-Repo Trigger

Now test the full flow:

1. Go to DEV repo → **Actions**
2. Click: **"Build and Upload to NativeBridge"**
3. Click: **"Run workflow"**
4. Fill inputs:
   ```
   upload_to_nativebridge: true
   trigger_qa_tests: true  ← Enable this time!
   device_name: Samsung Galaxy S22 Ultra
   region: ind
   ```
5. Click: **"Run workflow"**
6. Wait for DEV workflow to complete
7. Go to QA repo → **Actions** tab
8. You should see a new workflow run starting automatically!

**Expected Result:**
- ✅ DEV repo: Build → Upload → Trigger QA
- ✅ QA repo: Workflow starts automatically
- ✅ QA repo: Tests run with app_id from DEV
- ✅ Both show success ✅

### Test 4: Git Push Trigger

Final test - automatic trigger:

```bash
# In DEV repository
git checkout -b test-nativebridge-trigger

# Make a change
echo "# Test" >> README.md

# Commit with "nativebridge" keyword
git add README.md
git commit -m "Test automatic NativeBridge deployment"

# Push
git push origin test-nativebridge-trigger
```

**Expected Result:**
- ✅ DEV workflow triggers automatically
- ✅ Uploads to NativeBridge
- ✅ QA workflow triggers automatically
- ✅ Tests run

---

## Usage Examples

### Example 1: Developer Workflow (Full Auto)

**Scenario:** Developer adds new feature and wants it tested.

**Steps:**
```bash
# In DEV repository
git checkout develop
git pull

# Make changes...
git add .
git commit -m "feat: Add login screen - test on nativebridge"
git push origin develop
```

**Result:**
1. DEV workflow triggers (commit has "nativebridge")
2. Builds APK
3. Uploads to NativeBridge → gets `app_id: ABC1`
4. Triggers QA workflow with `app_id: ABC1`
5. QA tests run automatically
6. Results in Slack

**Timeline:** 15-20 minutes total

### Example 2: QA Independent Testing

**Scenario:** QA wants to test on different device.

**Steps:**
1. Get `app_id: ABC1` from DEV team or previous run
2. QA repo → Actions → "Run QA Tests"
3. Fill:
   ```
   app_id: ABC1
   nb_version: 1.2.3
   device_name: Google Pixel 7 Pro
   region: usa
   ```
4. Run workflow

**Result:**
- Tests run on Pixel 7 Pro
- Independent of DEV team

**Timeline:** 8-12 minutes

### Example 3: Build Without Testing

**Scenario:** Developer wants to upload but QA will test later.

**Steps:**
1. DEV repo → Actions → Manual trigger
2. Set `trigger_qa_tests: false`
3. Run workflow
4. Share `app_id` with QA team

**Result:**
- APK uploaded
- QA can test when ready

---

## Troubleshooting

### DEV Repository Issues

#### Error: "QA_REPO_PAT not configured"

**Solution:**
1. Create PAT (see configuration section)
2. Add to DEV repo secrets as `QA_REPO_PAT`

#### Error: "Could not trigger QA workflow"

**Possible causes:**
1. PAT doesn't have `workflow` scope
2. QA_REPO_OWNER or QA_REPO_NAME variable is wrong
3. PAT has expired

**Solution:**
1. Verify PAT has `repo` and `workflow` scopes
2. Check variables match QA repo URL exactly
3. Regenerate PAT if expired

#### Error: "APK not found"

**Solution:**
Add pre-built APK:
```bash
cp your-app.apk builds/NativeBridge-Production.apk
git add builds/
git commit -m "Add APK"
git push
```

Or configure Android build steps in workflow.

### QA Repository Issues

#### Error: "NATIVEBRIDGE_API_KEY not configured"

**Solution:**
Add secret in QA repo settings.

#### Tests fail: "device not available"

**Solution:**
1. Check device name matches NativeBridge dashboard exactly
2. Verify device is online in region

#### Not triggered by DEV repo

**Solution:**
1. Check DEV repo can access QA repo (PAT permissions)
2. Verify workflow file name is `qa-tests.yml`
3. Check QA repo variables in DEV repo are correct

### Cross-Repo Trigger Issues

#### Error: "Resource not accessible" (403)

**Cause:** PAT doesn't have correct permissions.

**Solution:**
1. Regenerate PAT with `repo` and `workflow` scopes
2. Update `QA_REPO_PAT` secret in DEV repo

#### QA workflow doesn't start

**Checklist:**
- [ ] DEV workflow shows "Trigger QA Repository" job succeeded
- [ ] `QA_REPO_OWNER` and `QA_REPO_NAME` variables are correct
- [ ] PAT is valid and has correct scopes
- [ ] Workflow file exists at `.github/workflows/qa-tests.yml` in QA repo
- [ ] Workflow is on `main` branch (or update `ref` in DEV workflow)

---

## Advanced Configuration

### Different Branches

If your QA repo uses `develop` instead of `main`:

Edit DEV repo workflow, find this line:
```json
"ref": "main",
```

Change to:
```json
"ref": "develop",
```

### Multiple QA Repositories

To trigger multiple QA repos:

1. Add more secrets/variables:
   - `QA_REPO_2_PAT`
   - `QA_REPO_2_OWNER`
   - `QA_REPO_2_NAME`

2. Duplicate "Trigger QA Repository" job in DEV workflow

### Custom Test Suites

In QA workflow, add input:
```yaml
test_suite:
  description: 'Test suite to run (smoke, regression, full)'
  required: false
  default: 'smoke'
```

Then pass to tests as environment variable.

---

## Security Best Practices

### PAT Management

1. ✅ Use PAT with minimal required scopes
2. ✅ Set expiration (90 days recommended)
3. ✅ Rotate regularly
4. ✅ Store in GitHub Secrets only
5. ❌ Never commit PAT to code

### Repository Access

1. ✅ Use private repositories
2. ✅ Limit team access appropriately
3. ✅ DEV team: Access to DEV repo
4. ✅ QA team: Access to QA repo
5. ✅ Admins: Access to both

### API Keys

1. ✅ Use separate NativeBridge API keys per repo (optional)
2. ✅ Store in GitHub Secrets
3. ✅ Rotate periodically
4. ✅ Monitor usage

---

## Comparison: Two-Repo vs Single-Repo

| Aspect | Two-Repo Setup | Single-Repo Setup |
|--------|----------------|-------------------|
| **Separation** | ✅ Clean separation | ❌ Mixed concerns |
| **Team Independence** | ✅ Dev and QA independent | ⚠️ Shared repository |
| **Setup Complexity** | ⚠️ More complex (PAT needed) | ✅ Simpler |
| **Access Control** | ✅ Fine-grained | ⚠️ Same for all |
| **Scalability** | ✅ Easy to scale | ⚠️ Can get cluttered |
| **Maintenance** | ⚠️ Two repos to maintain | ✅ One repo |

**Recommendation:**
- **Small teams / Single app:** Single-repo setup
- **Large teams / Multiple apps:** Two-repo setup ✅

---

## Quick Reference

### Required Secrets & Variables

**DEV Repository:**
- Secrets: `NATIVEBRIDGE_API_KEY`, `QA_REPO_PAT`, `SLACK_WEBHOOK_URL` (optional)
- Variables: `QA_REPO_OWNER`, `QA_REPO_NAME`, `DEFAULT_DEVICE`, `DEFAULT_REGION`

**QA Repository:**
- Secrets: `NATIVEBRIDGE_API_KEY`, `SLACK_WEBHOOK_URL` (optional)
- Variables: None required

### Workflow Files

- DEV: `.github/workflows/build-and-upload.yml`
- QA: `.github/workflows/qa-tests.yml`

### Key Outputs

**DEV → QA:**
- `app_id` - NativeBridge app ID
- `nb_version` - App version
- `device_name` - Test device
- `region` - Region

---

## Support

### Documentation

- DEV Repo: See `DEV_REPO/README.md`
- QA Repo: See `QA_REPO/README.md`
- This Guide: `TWO_REPO_SETUP_GUIDE.md`

### Common Commands

```bash
# Check PAT permissions
curl -H "Authorization: Bearer YOUR_PAT" \
  https://api.github.com/user

# Manually trigger QA workflow via API
curl -X POST \
  -H "Authorization: Bearer YOUR_PAT" \
  -H "Accept: application/vnd.github+json" \
  https://api.github.com/repos/OWNER/REPO/actions/workflows/qa-tests.yml/dispatches \
  -d '{"ref":"main","inputs":{"app_id":"XyZ9","nb_version":"1.0.0","device_name":"Samsung Galaxy S22 Ultra","region":"ind"}}'
```

---

## What's Next?

After setup:

1. ✅ Test all three scenarios (manual DEV, manual QA, cross-repo)
2. ✅ Train DEV team on commit message keywords
3. ✅ Train QA team on independent testing
4. ✅ Set up Slack notifications
5. ✅ Add real Android build steps to DEV workflow
6. ✅ Add more test cases to QA repo
7. ✅ Monitor and optimize

**You're ready to go!** 🚀
