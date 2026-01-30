# Two-Repository Setup for NativeBridge Testing

Complete setup for separating DEV and QA into two independent repositories.

## 📦 What's Included

This folder contains everything you need to set up a two-repository CI/CD system:

```
TWO_REPO_SETUP/
├── README.md                      ← You are here
├── QUICK_START.md                 ← 30-minute setup guide
├── TWO_REPO_SETUP_GUIDE.md        ← Complete documentation
│
├── DEV_REPO/                      ← Files for Developer Repository
│   ├── README.md
│   └── .github/workflows/
│       └── build-and-upload.yml
│
└── QA_REPO/                       ← Files for QA Repository
    ├── README.md
    └── .github/workflows/
        └── qa-tests.yml
```

---

## 🎯 What is Two-Repo Setup?

Instead of having both pipelines in one repository, you split them into **two separate repositories**:

### Repository 1: DEV (android-app-repo)
- **Team:** Developers
- **Contains:** Android app source code
- **Workflow:** Build APK → Upload to NativeBridge → Trigger QA repo
- **Triggers:** Git commit (with "nativebridge" keyword) or Manual

### Repository 2: QA (qa-automation-repo)
- **Team:** QA Engineers
- **Contains:** Appium test code (Java)
- **Workflow:** Run tests on NativeBridge
- **Triggers:** Auto-triggered by DEV repo or Manual (independent)

### How They Work Together

```
Developer commits code with "nativebridge"
         ↓
DEV Repo builds APK
         ↓
DEV Repo uploads to NativeBridge
         ↓
DEV Repo gets: app_id, nb_version
         ↓
DEV Repo triggers QA Repo ──────────→ QA Repo runs tests
         ↓                                     ↓
Results in GitHub + Slack            Results in GitHub + Slack
```

---

## ✨ Key Benefits

✅ **Clean Separation** - Dev code separate from test code
✅ **Team Independence** - Dev and QA teams work independently
✅ **Flexible Access Control** - Different permissions per repo
✅ **QA Independence** - QA can test without waiting for dev
✅ **Scalability** - Easy to add more repos for different apps
✅ **Clear Ownership** - Each team owns their repo

---

## 🚀 Getting Started

### Option 1: Quick Start (30 minutes)

Follow: **[QUICK_START.md](QUICK_START.md)**

Perfect for: Getting up and running fast

### Option 2: Complete Guide (1 hour)

Follow: **[TWO_REPO_SETUP_GUIDE.md](TWO_REPO_SETUP_GUIDE.md)**

Perfect for: Understanding every detail

---

## 📋 Setup Overview

### 1. Create DEV Repository

```bash
# 1. Create GitHub repo: android-app-repo
# 2. Copy files from DEV_REPO/
# 3. Add secrets: NATIVEBRIDGE_API_KEY, QA_REPO_PAT
# 4. Add variables: QA_REPO_OWNER, QA_REPO_NAME
# 5. Push and test
```

**Time:** ~15 minutes

### 2. Create QA Repository

```bash
# 1. Create GitHub repo: qa-automation-repo
# 2. Copy files from QA_REPO/
# 3. Add secrets: NATIVEBRIDGE_API_KEY
# 4. Push and test
```

**Time:** ~15 minutes

### 3. Test Integration

```bash
# 1. Test DEV repo manual run
# 2. Test QA repo manual run
# 3. Test cross-repo trigger
```

**Time:** ~5 minutes

**Total:** ~35 minutes

---

## 📁 File Structure

### DEV Repository (android-app-repo)

```
android-app-repo/
├── .github/workflows/
│   └── build-and-upload.yml       ← Main workflow
├── android/                        ← Your Android code
│   ├── app/
│   ├── build.gradle
│   └── settings.gradle
├── builds/                         ← Pre-built APKs (temporary)
│   └── NativeBridge-Production.apk
└── README.md                       ← Usage guide
```

**Secrets Required:**
- `NATIVEBRIDGE_API_KEY` - NativeBridge API key
- `QA_REPO_PAT` - GitHub PAT to trigger QA repo

**Variables Required:**
- `QA_REPO_OWNER` - QA repo owner
- `QA_REPO_NAME` - QA repo name
- `DEFAULT_DEVICE` - Default test device
- `DEFAULT_REGION` - Default region

### QA Repository (qa-automation-repo)

```
qa-automation-repo/
├── .github/workflows/
│   └── qa-tests.yml                ← Test workflow
├── appium_tests/
│   ├── src/main/java/
│   │   └── SessionlessAppLaunchTest.java
│   └── pom.xml
└── README.md                        ← Usage guide
```

**Secrets Required:**
- `NATIVEBRIDGE_API_KEY` - NativeBridge API key

**No Variables Required!**

---

## 🔑 Required Setup

### Personal Access Token (PAT)

DEV repo needs a PAT to trigger QA repo:

1. GitHub Settings → Developer settings → Personal access tokens
2. Generate token with scopes: `repo`, `workflow`
3. Add to DEV repo as secret: `QA_REPO_PAT`

### NativeBridge API Key

Both repos need the same NativeBridge API key:
- Add to both repos as: `NATIVEBRIDGE_API_KEY`

---

## 💡 Usage Examples

### For Developers

**Auto-trigger (Git Commit):**
```bash
git commit -m "Add feature - test on nativebridge"
git push

# Result: DEV builds → uploads → QA tests run automatically
```

**Manual trigger:**
1. Actions → "Build and Upload to NativeBridge"
2. Configure parameters
3. Run workflow

### For QA Team

**Independent testing:**
1. Get `app_id` from DEV team
2. QA repo → Actions → "Run QA Tests"
3. Fill parameters (app_id, device, etc.)
4. Run workflow

**Result:** Tests run without blocking on DEV!

---

## 🔄 Workflow Flow

```
┌─────────────────────────────────────┐
│  DEV Repository Workflow            │
│  (build-and-upload.yml)             │
│                                     │
│  1. Check Trigger                   │
│     ├─ Git push with "nativebridge"│
│     └─ Manual trigger               │
│                                     │
│  2. Build APK                       │
│     └─ Output: APK file             │
│                                     │
│  3. Upload to NativeBridge          │
│     └─ Output: app_id, nb_version   │
│                                     │
│  4. Trigger QA Repository           │
│     └─ API call to QA repo          │
└─────────────────────────────────────┘
                  │
                  │ Cross-repo trigger
                  │ (GitHub API)
                  ▼
┌─────────────────────────────────────┐
│  QA Repository Workflow             │
│  (qa-tests.yml)                     │
│                                     │
│  1. Validate Inputs                 │
│     └─ app_id, nb_version, device   │
│                                     │
│  2. Setup Environment               │
│     └─ Java 17, Maven               │
│                                     │
│  3. Run Appium Tests                │
│     └─ Sessionless mode             │
│                                     │
│  4. Generate Reports                │
│     └─ Logs, screenshots, summary   │
│                                     │
│  5. Send Notifications              │
│     └─ Slack, GitHub summary        │
└─────────────────────────────────────┘
```

---

## 🆚 Comparison: Two-Repo vs Single-Repo

| Feature | Two-Repo | Single-Repo |
|---------|----------|-------------|
| **Separation of Concerns** | ✅ Clean | ⚠️ Mixed |
| **Team Independence** | ✅ Full | ⚠️ Limited |
| **Setup Complexity** | ⚠️ More complex | ✅ Simple |
| **Access Control** | ✅ Granular | ⚠️ Shared |
| **QA Independence** | ✅ Full | ❌ Depends on DEV |
| **Scalability** | ✅ High | ⚠️ Medium |
| **Maintenance** | ⚠️ Two repos | ✅ One repo |

**When to Use:**
- **Small teams / Single app:** Single-repo
- **Large teams / Multiple apps:** Two-repo ✅

---

## 📚 Documentation

| Document | Description | Read Time |
|----------|-------------|-----------|
| **[QUICK_START.md](QUICK_START.md)** | Get started in 30 minutes | 5 min |
| **[TWO_REPO_SETUP_GUIDE.md](TWO_REPO_SETUP_GUIDE.md)** | Complete setup guide | 15 min |
| **[DEV_REPO/README.md](DEV_REPO/README.md)** | DEV repository usage | 10 min |
| **[QA_REPO/README.md](QA_REPO/README.md)** | QA repository usage | 10 min |

---

## 🔧 Customization

### Change Device Defaults

Edit DEV repo variables:
```
DEFAULT_DEVICE = Your device name
DEFAULT_REGION = Your region
```

### Add More Test Cases

In QA repo:
```bash
cd appium_tests/src/main/java
# Add new test classes
# Commit and push
```

### Change Workflow Names

Edit workflow files:
```yaml
name: Your Custom Name
```

---

## 🐛 Troubleshooting

### Common Issues

| Issue | Solution |
|-------|----------|
| "QA_REPO_PAT not configured" | Add PAT to DEV repo secrets |
| "Could not trigger QA workflow" | Check PAT has `workflow` scope |
| "APK not found" | Add APK to `builds/` folder |
| QA doesn't trigger | Verify variables: QA_REPO_OWNER, QA_REPO_NAME |
| Tests fail | Check NATIVEBRIDGE_API_KEY in QA repo |

**Detailed troubleshooting:** See [TWO_REPO_SETUP_GUIDE.md](TWO_REPO_SETUP_GUIDE.md)

---

## ✅ Pre-Setup Checklist

Before you start:

- [ ] Have NativeBridge account and API key
- [ ] Have GitHub account with repo creation access
- [ ] Have APK file ready (or Android project)
- [ ] Have Appium test code ready
- [ ] Understand Git and GitHub Actions basics
- [ ] 30-60 minutes available for setup

---

## 🎓 Learning Path

1. **Start here:** Read this README ✅
2. **Quick setup:** Follow [QUICK_START.md](QUICK_START.md)
3. **Understand details:** Read [TWO_REPO_SETUP_GUIDE.md](TWO_REPO_SETUP_GUIDE.md)
4. **DEV repo usage:** Read [DEV_REPO/README.md](DEV_REPO/README.md)
5. **QA repo usage:** Read [QA_REPO/README.md](QA_REPO/README.md)

---

## 🚀 Ready to Start?

Choose your path:

### Path 1: Quick Setup (Recommended)
→ Follow **[QUICK_START.md](QUICK_START.md)**
→ 30 minutes
→ Get running fast

### Path 2: Detailed Setup
→ Follow **[TWO_REPO_SETUP_GUIDE.md](TWO_REPO_SETUP_GUIDE.md)**
→ 1 hour
→ Understand everything

---

## 📞 Support

### Documentation
- Quick start: [QUICK_START.md](QUICK_START.md)
- Full guide: [TWO_REPO_SETUP_GUIDE.md](TWO_REPO_SETUP_GUIDE.md)
- DEV repo: [DEV_REPO/README.md](DEV_REPO/README.md)
- QA repo: [QA_REPO/README.md](QA_REPO/README.md)

### Troubleshooting
- Check workflow logs in Actions tab
- Review troubleshooting section in full guide
- Verify all secrets and variables are set
- Test each component individually

---

## 🎉 What You'll Have

After setup:

✅ DEV repo that builds and uploads automatically
✅ QA repo that runs tests automatically or on-demand
✅ Cross-repo integration that triggers QA from DEV
✅ Independent QA testing capability
✅ Complete test reports and artifacts
✅ Slack notifications (optional)
✅ Clean separation of concerns
✅ Scalable architecture

**Happy testing!** 🚀
