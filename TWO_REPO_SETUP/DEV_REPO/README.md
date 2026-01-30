# Developer Repository - Android App

This repository contains the Android application source code and CI/CD pipeline for building and uploading to NativeBridge.

## Repository Purpose

**Role:** Developer/Build Repository

**Responsibilities:**
- Build Android APK
- Upload APK to NativeBridge
- Trigger QA tests in separate repository

## Repository Structure

```
android-app-repo/
├── .github/
│   └── workflows/
│       └── build-and-upload.yml     # Main CI/CD pipeline
├── android/                          # Android app source code
│   ├── app/
│   ├── build.gradle
│   └── settings.gradle
├── builds/                           # Pre-built APKs (temporary)
│   └── NativeBridge-Production.apk
└── README.md
```

## Quick Start

### 1. Configure Secrets

Go to **Settings → Secrets and variables → Actions** and add:

| Secret | Description | Example |
|--------|-------------|---------|
| `NATIVEBRIDGE_API_KEY` | NativeBridge API key | `Nb-xxxx.yyyy-zzzz` |
| `QA_REPO_PAT` | GitHub PAT to trigger QA repo | `ghp_xxxxx...` |
| `SLACK_WEBHOOK_URL` | (Optional) Slack notifications | `https://hooks.slack.com/...` |

### 2. Configure Variables

Go to **Settings → Secrets and variables → Actions → Variables** and add:

| Variable | Description | Example |
|----------|-------------|---------|
| `QA_REPO_OWNER` | QA repository owner | `AutoFlowLabs` |
| `QA_REPO_NAME` | QA repository name | `qa-automation` |
| `DEFAULT_DEVICE` | Default test device | `Samsung Galaxy S22 Ultra` |
| `DEFAULT_REGION` | Default region | `ind` |

### 3. Trigger Build

**Method 1: Git Push (Automatic)**
```bash
git add .
git commit -m "New feature - test on nativebridge"
git push
```
✅ Triggers if commit contains "nativebridge"

**Method 2: Manual (GitHub UI)**
1. Go to Actions → "Build and Upload to NativeBridge"
2. Click "Run workflow"
3. Fill parameters
4. Click "Run workflow"

## Workflow Triggers

### Automatic (Git Push)

The workflow triggers on push to:
- `main` branch
- `develop` branch
- `staging` branch

**Upload happens only if:** Commit message contains "nativebridge" (case-insensitive)

**Examples:**
```bash
✅ git commit -m "Add login feature - test on nativebridge"
✅ git commit -m "Fix bug, deploy to NativeBridge"
❌ git commit -m "Update README"  # No upload
```

### Manual (GitHub UI)

Manually trigger with custom parameters:
- Upload to NativeBridge: yes/no
- Trigger QA tests: yes/no
- Device name: Customize test device
- Region: Customize region

## Workflow Parameters

When manually triggering:

| Parameter | Required | Default | Description |
|-----------|----------|---------|-------------|
| `upload_to_nativebridge` | Yes | `true` | Upload APK? |
| `trigger_qa_tests` | Yes | `true` | Trigger QA repo? |
| `device_name` | No | (from variable) | Test device |
| `region` | No | (from variable) | Region |

## Workflow Steps

```
1. Check Trigger
   ├─ Git push: Check commit message for "nativebridge"
   └─ Manual: Use input parameter

2. Build Application
   ├─ Setup Android SDK
   ├─ Build APK
   └─ Sign APK

3. Upload to NativeBridge (if enabled)
   ├─ Upload APK via API
   ├─ Get app_id
   ├─ Get nb_version
   └─ Get magic_link

4. Trigger QA Repository (if upload succeeded)
   ├─ Call QA repo's workflow
   └─ Pass: app_id, nb_version, device, region
```

## Outputs

The pipeline creates these outputs:

```yaml
app_id: "XyZ9"           # NativeBridge app ID
nb_version: "1.2.3"      # NativeBridge version
magic_link: "https://..." # Installation link
```

## Examples

### Example 1: Deploy Feature

```bash
git checkout -b feature/new-login
# ... make changes ...
git add .
git commit -m "feat: New login screen - test on nativebridge"
git push origin feature/new-login
```

**Result:**
- ✅ Pipeline builds APK
- ✅ Uploads to NativeBridge
- ✅ Triggers QA tests automatically
- ✅ Results in ~15-20 minutes

### Example 2: Build Without Testing

1. Actions → "Build and Upload to NativeBridge"
2. Set:
   - `upload_to_nativebridge: true`
   - `trigger_qa_tests: false` ← Disable QA
3. Run workflow

**Result:**
- ✅ Builds and uploads
- ❌ QA tests NOT triggered
- 📝 Get app_id for manual QA testing later

### Example 3: Just Build (No Upload)

```bash
git commit -m "Update documentation"  # No "nativebridge" keyword
git push
```

**Result:**
- ✅ Pipeline triggers
- ✅ Builds APK
- ❌ Doesn't upload to NativeBridge
- ❌ QA tests NOT triggered

## Artifacts

Pipeline creates these artifacts:

1. **Build APK** (`app-build-{sha}`)
   - Retention: 30 days
   - Contains: Built APK file

## Notifications

If `SLACK_WEBHOOK_URL` is configured:
- Notifies on upload success/failure
- Includes app_id, version, magic link
- Links to pipeline run

## Troubleshooting

### Build Fails

**Check:**
- Android SDK version compatibility
- Gradle dependencies
- Build configuration

### Upload Fails

**Check:**
- `NATIVEBRIDGE_API_KEY` secret is set correctly
- APK file exists after build
- NativeBridge API is accessible

### QA Trigger Fails

**Check:**
- `QA_REPO_PAT` secret has correct permissions
- `QA_REPO_OWNER` and `QA_REPO_NAME` variables are correct
- PAT has `repo` and `workflow` scopes

## Integration with QA Repository

This repository triggers the QA repository automatically:

```
DEV Repo (this repo)
  └─ Uploads APK to NativeBridge
      └─ Triggers workflow in QA Repo
          └─ QA Repo runs tests
```

**QA Repository:** `{QA_REPO_OWNER}/{QA_REPO_NAME}`

**Workflow Triggered:** `qa-tests.yml`

**Parameters Passed:**
- `app_id` - From NativeBridge upload
- `nb_version` - From NativeBridge upload
- `device_name` - From input or variable
- `region` - From input or variable
- `triggered_by` - Always `dev-repo`

## Best Practices

### For Developers

1. ✅ Always include "nativebridge" in commit message when you want to deploy
2. ✅ Check Actions tab after pushing
3. ✅ Share app_id with QA team
4. ✅ Monitor Slack notifications

### Commit Message Guidelines

Good examples:
```bash
✅ "feat: Add dark mode - test on nativebridge"
✅ "fix: Login bug - deploy to NativeBridge for QA"
✅ "refactor: Update API calls (nativebridge testing)"
```

Bad examples:
```bash
❌ "changes"  # Too vague
❌ "fix bug"  # No "nativebridge" keyword
❌ "test"     # Not descriptive
```

## Team Collaboration

### Developer Workflow

1. Developer commits with "nativebridge" keyword
2. Pipeline builds and uploads automatically
3. QA team gets notification
4. QA tests run automatically in QA repo
5. Results posted to Slack and GitHub

### QA Independence

QA team can also:
- Run tests manually in QA repo
- Test on different devices
- Re-run tests without rebuild
- See QA repo README for details

## Related Documentation

- QA Repository: See QA repo's README
- Two-Repo Setup Guide: `TWO_REPO_SETUP_GUIDE.md`
- Workflow Details: See `.github/workflows/build-and-upload.yml`

## Support

For issues:
1. Check workflow logs in Actions tab
2. Verify all secrets and variables are set
3. Check QA repo is accessible with PAT
4. Review `TWO_REPO_SETUP_GUIDE.md`
