# Two-Repo Setup - Quick Start

Get your two-repository CI/CD system running in 30 minutes!

## 🎯 Goal

Set up two separate repositories:
1. **DEV Repo** - Build Android app, upload to NativeBridge
2. **QA Repo** - Run Appium tests

They work together: DEV triggers QA automatically, but QA can also run independently.

---

## ⚡ Quick Setup (30 minutes)

### Part 1: DEV Repository (15 minutes)

#### 1. Create Repository
```bash
# Create new GitHub repo: android-app-repo
# Clone it
git clone https://github.com/YOUR_ORG/android-app-repo.git
cd android-app-repo
```

#### 2. Copy Files
```bash
# Copy workflow
mkdir -p .github/workflows
cp path/to/TWO_REPO_SETUP/DEV_REPO/.github/workflows/build-and-upload.yml .github/workflows/

# Copy README
cp path/to/TWO_REPO_SETUP/DEV_REPO/README.md .

# Create builds folder
mkdir -p builds/
cp your-app.apk builds/NativeBridge-Production.apk
```

#### 3. Create PAT Token
1. GitHub Settings → Developer settings → Personal access tokens → Generate new token (classic)
2. Scopes: ✅ `repo`, ✅ `workflow`
3. Copy token: `ghp_xxxxx...`

#### 4. Configure Secrets
Go to repo Settings → Secrets → Actions:

```
NATIVEBRIDGE_API_KEY = Nb-xxxx.yyyy-zzzz
QA_REPO_PAT = ghp_xxxxx...
```

#### 5. Configure Variables
Go to repo Settings → Secrets → Variables:

```
QA_REPO_OWNER = YourGitHubOrg
QA_REPO_NAME = qa-automation-repo
DEFAULT_DEVICE = Samsung Galaxy S22 Ultra
DEFAULT_REGION = ind
```

#### 6. Push
```bash
git add .
git commit -m "Setup NativeBridge CI/CD"
git push
```

✅ **DEV Repo Ready!**

---

### Part 2: QA Repository (15 minutes)

#### 1. Create Repository
```bash
# Create new GitHub repo: qa-automation-repo
# Clone it
git clone https://github.com/YOUR_ORG/qa-automation-repo.git
cd qa-automation-repo
```

#### 2. Copy Files
```bash
# Copy workflow
mkdir -p .github/workflows
cp path/to/TWO_REPO_SETUP/QA_REPO/.github/workflows/qa-tests.yml .github/workflows/

# Copy README
cp path/to/TWO_REPO_SETUP/QA_REPO/README.md .

# Copy test code
cp -r path/to/appium_tests/ .
```

#### 3. Configure Secrets
Go to repo Settings → Secrets → Actions:

```
NATIVEBRIDGE_API_KEY = Nb-xxxx.yyyy-zzzz
```

(Same API key as DEV repo)

#### 4. Push
```bash
git add .
git commit -m "Setup QA automation"
git push
```

✅ **QA Repo Ready!**

---

## 🧪 Test It (5 minutes)

### Test 1: Manual DEV Run

1. DEV repo → Actions → "Build and Upload to NativeBridge"
2. Run workflow:
   ```
   upload_to_nativebridge: true
   trigger_qa_tests: false
   ```
3. Should succeed and show `app_id`

### Test 2: Manual QA Run

1. Note the `app_id` from Test 1
2. QA repo → Actions → "Run QA Tests"
3. Run workflow:
   ```
   app_id: YOUR_APP_ID
   nb_version: 1.0.0
   device_name: Samsung Galaxy S22 Ultra
   region: ind
   ```
4. Should run tests successfully

### Test 3: Cross-Repo Trigger

1. DEV repo → Actions → "Build and Upload to NativeBridge"
2. Run workflow:
   ```
   upload_to_nativebridge: true
   trigger_qa_tests: true  ← Enable this!
   ```
3. DEV should finish and trigger QA
4. Check QA repo Actions → should see new run!

✅ **All tests pass? You're done!** 🎉

---

## 📁 What You Created

```
DEV Repository (android-app-repo)
├── .github/workflows/build-and-upload.yml
├── builds/NativeBridge-Production.apk
└── README.md

Secrets:
- NATIVEBRIDGE_API_KEY
- QA_REPO_PAT

Variables:
- QA_REPO_OWNER
- QA_REPO_NAME
- DEFAULT_DEVICE
- DEFAULT_REGION
```

```
QA Repository (qa-automation-repo)
├── .github/workflows/qa-tests.yml
├── appium_tests/
│   ├── src/main/java/SessionlessAppLaunchTest.java
│   └── pom.xml
└── README.md

Secrets:
- NATIVEBRIDGE_API_KEY
```

---

## 🚀 Daily Usage

### For Developers

```bash
# Commit with "nativebridge" to trigger tests
git commit -m "feat: New login - test on nativebridge"
git push

# Result: Build → Upload → QA tests run automatically
```

### For QA Team

1. Go to QA repo → Actions
2. Run QA Tests manually
3. Fill app_id and device
4. Get results in ~10 minutes

**QA is independent!** No need to wait for DEV.

---

## 🆘 Troubleshooting

### "QA_REPO_PAT not configured"
→ Add PAT to DEV repo secrets

### "Could not trigger QA workflow"
→ Check PAT has `workflow` scope
→ Verify QA_REPO_OWNER and QA_REPO_NAME variables

### "APK not found"
→ Add APK to `builds/NativeBridge-Production.apk`

### "NATIVEBRIDGE_API_KEY not configured"
→ Add to repo secrets (both repos need it)

### QA doesn't trigger
→ Check DEV workflow "Trigger QA Repository" job logs
→ Verify PAT permissions

---

## 📚 Full Documentation

- **Complete Guide:** `TWO_REPO_SETUP_GUIDE.md`
- **DEV Repo Guide:** `DEV_REPO/README.md`
- **QA Repo Guide:** `QA_REPO/README.md`

---

## ✅ Checklist

**DEV Repository:**
- [ ] Repository created
- [ ] Workflow file added
- [ ] APK added to builds/
- [ ] PAT created
- [ ] NATIVEBRIDGE_API_KEY secret added
- [ ] QA_REPO_PAT secret added
- [ ] QA_REPO_OWNER variable added
- [ ] QA_REPO_NAME variable added
- [ ] DEFAULT_DEVICE variable added
- [ ] DEFAULT_REGION variable added
- [ ] Manual test run succeeds

**QA Repository:**
- [ ] Repository created
- [ ] Workflow file added
- [ ] Test code added
- [ ] NATIVEBRIDGE_API_KEY secret added
- [ ] Manual test run succeeds

**Integration:**
- [ ] DEV triggers QA successfully
- [ ] QA runs with app_id from DEV
- [ ] Both workflows show success

---

## 🎯 Key Points

1. **Two repos**: Dev and QA separated
2. **PAT required**: DEV needs PAT to trigger QA
3. **Same API key**: Both repos use same NativeBridge key
4. **Cross-repo trigger**: DEV calls QA via GitHub API
5. **QA independence**: QA can run without DEV

---

**Ready to go!** If something doesn't work, check the full guide: `TWO_REPO_SETUP_GUIDE.md`
