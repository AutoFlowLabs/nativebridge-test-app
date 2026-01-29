# Pipelines Quick Reference

One-page reference for DEV and QA pipelines.

## 🚀 Quick Start

### For Developers

```bash
# Deploy and test on NativeBridge
git add .
git commit -m "feat: New feature - test on nativebridge"
git push

# Result: DEV Pipeline → Upload → QA Pipeline → Tests run automatically
```

### For QA Team

```
1. Go to Actions tab
2. Click "QA Pipeline - Run Appium Tests"
3. Fill: app_id, nb_version, device_name
4. Click "Run workflow"

Result: Tests run independently on specified device
```

---

## 📊 Pipeline Overview

| Pipeline | Triggers | Duration | Outputs |
|----------|----------|----------|---------|
| **DEV** | Git push (with "nativebridge") OR Manual | 8-12 min | `app_id`, `nb_version`, `magic_link` |
| **QA** | Auto (by DEV) OR Manual | 8-12 min | `test_result`, `test_duration`, logs |

---

## 🔀 Workflow Triggers

### DEV Pipeline Triggers

| Method | How | Upload Happens When |
|--------|-----|---------------------|
| **Git Push** | `git push origin main` | Commit message contains "nativebridge" |
| **Manual** | Actions → DEV Pipeline → Run workflow | Set `upload_to_nativebridge: true` |

### QA Pipeline Triggers

| Method | How | Requirements |
|--------|-----|--------------|
| **Auto** | DEV Pipeline success | DEV Pipeline uploads successfully |
| **Manual** | Actions → QA Pipeline → Run workflow | Need `app_id` and `nb_version` |

---

## 💡 Common Use Cases

### Use Case 1: Dev Deploys Feature (Auto)

```bash
git commit -m "Add login - test on nativebridge"
git push
```

**Result:** DEV builds → uploads → QA tests → Done in 15-20 min

### Use Case 2: QA Tests on Different Device

```
Actions → QA Pipeline
app_id: XyZ9
device_name: Google Pixel 7 Pro
→ Run workflow
```

**Result:** Tests run on new device in 8-12 min

### Use Case 3: Dev Uploads Without Testing

```
Actions → DEV Pipeline
upload_to_nativebridge: true
trigger_qa_pipeline: false  ← Disable auto-test
→ Run workflow
```

**Result:** Builds and uploads, QA can test later

---

## 📝 Required Parameters

### DEV Pipeline (Manual)

| Parameter | Required | Default | Description |
|-----------|----------|---------|-------------|
| `upload_to_nativebridge` | Yes | `true` | Upload to NativeBridge? |
| `trigger_qa_pipeline` | Yes | `true` | Auto-trigger QA? |
| `device_name` | No | `Samsung Galaxy S22 Ultra` | Device for QA |
| `region` | No | `ind` | Region |

### QA Pipeline (Manual)

| Parameter | Required | Default | Description |
|-----------|----------|---------|-------------|
| `app_id` | **Yes** | - | NativeBridge app ID |
| `nb_version` | **Yes** | - | App version |
| `device_name` | **Yes** | `Samsung Galaxy S22 Ultra` | Device to test |
| `region` | No | `ind` | Region |

---

## 🔑 Required Secrets

| Secret | Used By | Get From |
|--------|---------|----------|
| `NATIVEBRIDGE_API_KEY` | Both | NativeBridge dashboard |
| `SLACK_WEBHOOK_URL` | QA (optional) | Slack workspace settings |

**Set at:** Settings → Secrets and variables → Actions

---

## 📦 Pipeline Outputs

### DEV Pipeline

```yaml
app_id: "XyZ9"           # Use in QA Pipeline
nb_version: "1.2.3"      # Use in QA Pipeline
magic_link: "https://..." # Share for manual testing
```

### QA Pipeline

```yaml
test_result: "passed" or "failed"
test_duration: "5m 32s"
```

**Artifacts:**
- `test-logs-{app_id}-{run_id}` (30 days)
- `test-screenshots-{app_id}-{run_id}` (30 days)

---

## 🎯 Decision Matrix

### Should I Use DEV or QA Pipeline?

| Scenario | Use | Why |
|----------|-----|-----|
| I made code changes and want to test | **DEV** | Builds, uploads, and tests automatically |
| I want to test existing app on different device | **QA** | No build needed, just test |
| I want to upload without testing | **DEV** (manual) | Set `trigger_qa_pipeline: false` |
| I want to re-run tests without rebuild | **QA** | Saves time, uses existing app |
| I'm QA and dev deployed earlier | **QA** | Get app_id from dev, run independently |

---

## ⚡ Quick Commands

### Check if APK Exists

```bash
ls -lh builds/NativeBridge-Production.apk
```

### View Recent Commits (Check for "nativebridge")

```bash
git log --oneline -10
```

### Get app_id from Last DEV Run

1. Go to Actions
2. Click latest DEV Pipeline run
3. Check summary for `app_id`

### Run Tests Locally

```bash
cd appium_tests
export NATIVEBRIDGE_API_KEY="Nb-xxxx..."
export APP_ID="XyZ9"
export DEVICE_NAME="Samsung Galaxy S22 Ultra"
export REGION="ind"
mvn exec:java -P sessionless
```

---

## 🚨 Common Issues (Quick Fixes)

| Error | Quick Fix |
|-------|-----------|
| "NATIVEBRIDGE_API_KEY not configured" | Add secret in Settings → Secrets |
| "APK not found" | Add APK to `builds/NativeBridge-Production.apk` |
| "QA Pipeline not triggered" | Check workflow permissions: "Read and write" |
| "Device not available" | Check device name matches NativeBridge exactly |
| "Could not extract nb_version" | Okay - pipeline uses "latest" as fallback |
| Tests timeout | Normal for first run, subsequent runs faster |

---

## 📱 Device Names

Get exact names from NativeBridge dashboard. Common examples:

- `Samsung Galaxy S22 Ultra`
- `Google Pixel 7 Pro`
- `iPhone 14 Pro Max`
- `OnePlus 11`

**Important:** Must match exactly (case-sensitive)!

---

## 🔄 Typical Workflows

### Workflow A: Feature Development

```
1. Dev codes feature
2. Dev commits: "Add feature - test on nativebridge"
3. Dev pushes to main
4. DEV Pipeline auto-runs → uploads
5. QA Pipeline auto-runs → tests pass
6. Dev gets Slack notification
7. Feature verified! ✅
```

**Time:** ~15-20 minutes

### Workflow B: QA Testing Sprint

```
1. Dev uploads app earlier (app_id: XyZ9, version: 1.2.3)
2. QA tests on Device 1 (Samsung S22)
3. QA tests on Device 2 (Pixel 7) - parallel
4. QA tests on Device 3 (iPhone 14) - parallel
5. All results available in ~10 min
6. QA creates test report
```

**Time:** ~10 minutes (parallel execution)

### Workflow C: Hotfix Deployment

```
1. Dev fixes critical bug
2. Dev manually triggers DEV Pipeline
3. Sets upload=true, trigger_qa=true
4. Gets app_id from output
5. QA re-runs tests multiple times to verify
6. All tests pass
7. Hotfix verified! ✅
```

**Time:** ~20-30 minutes

---

## 📚 Documentation Files

| File | Purpose |
|------|---------|
| `DEV_QA_PIPELINES_GUIDE.md` | Complete guide (read first!) |
| `PIPELINES_SETUP_CHECKLIST.md` | Setup steps and verification |
| `PIPELINES_QUICK_REFERENCE.md` | This file (quick lookup) |

---

## 🎓 Tips & Best Practices

### For Devs

- ✅ Always include "nativebridge" when you want to test
- ✅ Use descriptive commit messages
- ✅ Check Actions tab after pushing
- ✅ Share `app_id` with QA team

### For QA

- ✅ Save `app_id` and `nb_version` for retesting
- ✅ Test on multiple devices in parallel
- ✅ Check test logs in artifacts for debugging
- ✅ Verify device names are exact matches

### For Both

- ✅ Monitor Slack for instant feedback
- ✅ Download artifacts for detailed analysis
- ✅ Use GitHub summaries for quick status
- ✅ Keep secrets up to date

---

## 🆘 Getting Help

1. Check this quick reference
2. Read `DEV_QA_PIPELINES_GUIDE.md`
3. View workflow logs in Actions tab
4. Download and check test logs artifact
5. Verify secrets are configured

---

## 📞 Quick Links

| Link | URL Pattern |
|------|-------------|
| Actions | `github.com/{org}/{repo}/actions` |
| Secrets | `github.com/{org}/{repo}/settings/secrets/actions` |
| Workflow Permissions | `github.com/{org}/{repo}/settings/actions` |
| NativeBridge Dashboard | `dashboard.nativebridge.io` |

---

## ✅ Checklist Before Running

### Before Running DEV Pipeline:

- [ ] APK exists at `builds/NativeBridge-Production.apk` OR build steps configured
- [ ] `NATIVEBRIDGE_API_KEY` secret is set
- [ ] Commit message has "nativebridge" (for git push trigger)

### Before Running QA Pipeline (Manual):

- [ ] Have valid `app_id` (from DEV Pipeline or NativeBridge)
- [ ] Have `nb_version` (from DEV Pipeline or NativeBridge)
- [ ] Device name matches NativeBridge dashboard exactly
- [ ] `NATIVEBRIDGE_API_KEY` secret is set

---

## 🎯 Success Indicators

### DEV Pipeline Success:

```
✅ Build Complete
✅ Upload Complete
✅ App ID: XyZ9
✅ NB Version: 1.2.3
✅ QA Pipeline Triggered
```

### QA Pipeline Success:

```
✅ Tests Passed
✅ Duration: 5m 32s
✅ Test logs uploaded
✅ Slack notification sent
```

---

**Last Updated:** 2025-01-29

**Version:** 1.0.0

**For detailed information, see:** `DEV_QA_PIPELINES_GUIDE.md`
