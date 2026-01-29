# NativeBridge Appium Test CI/CD Guide

## Overview

This guide explains how to use the automated Appium testing in the CI/CD pipeline. The workflow uploads your APK, creates a NativeBridge session, and automatically runs Appium tests on it.

## What's Included

### 1. Test Files (`appium_tests/`)
```
appium_tests/
├── pom.xml                          # Maven configuration
├── src/main/java/
│   └── GenericAppLaunchTest.java   # Automated test
├── .gitignore                       # Git ignore rules
└── README.md                        # Test documentation
```

### 2. CI/CD Workflow
`.github/workflows/release-with-appium-test.yml`

## How It Works

### Workflow Steps

```
1. Upload APK & Create Session
   ├─ Upload NativeBridge-Production.apk
   ├─ Call NativeBridge API
   └─ Get Session ID

2. Run Appium Test
   ├─ Checkout repository
   ├─ Set up Java 17
   ├─ Update test with Session ID
   ├─ Compile test
   ├─ Run mvn exec:java
   └─ Upload test logs

3. Send Notifications
   ├─ Slack notification with test results
   └─ GitHub release with test status

4. Create GitHub Release
   ├─ Attach APK
   ├─ Attach test logs
   └─ Show test results
```

## Usage

### Method 1: Manual Trigger (Recommended for Testing)

1. Go to GitHub Actions
2. Select "Release with Appium Test" workflow
3. Click "Run workflow"
4. Fill in the form:
   ```
   Version: 1.0.0-test
   Device ID: 67a642531a4aa535498192f8
   Session validity: 300
   Build beta: unchecked
   Run Appium test: ✓ checked
   ```
5. Click "Run workflow"

### Method 2: Git Tag (Recommended for Production)

```bash
# Create a release tag with session configuration
git tag -a v1.5.0 -m "Release 1.5.0
[NB_SESSION_ENABLED]
[NB_DEVICE_IDS:67a642531a4aa535498192f8]
[NB_SESSION_VALIDITY:300]
"

# Push the tag
git push origin v1.5.0
```

The workflow will automatically:
1. ✅ Upload the APK
2. ✅ Create a device session
3. ✅ Run Appium tests
4. ✅ Send Slack notification
5. ✅ Create GitHub release

## Test Configuration

### Dynamic Configuration (Automated)

The workflow automatically updates the test with:
- **Session ID**: From the API response
- **API Key**: From GitHub secrets

No manual configuration needed! ✅

### Static Configuration (In the Java file)

Located in `GenericAppLaunchTest.java`:
```java
private static final String APPIUM_ENDPOINT = "https://api.nativebridge.io/appium/wd/hub";
private static final String API_KEY = "...";  // Updated by workflow
private static final String DEVICE_SESSION_ID = "...";  // Updated by workflow
```

## Required GitHub Secrets

Make sure these are configured in your repository settings:

### `NATIVEBRIDGE_API_KEY`
Your NativeBridge API key.

**How to set:**
1. Go to Repository → Settings → Secrets and variables → Actions
2. Click "New repository secret"
3. Name: `NATIVEBRIDGE_API_KEY`
4. Value: Your API key (e.g., `Nb-vODg.846d68e7-...`)

### `SLACK_WEBHOOK_URL` (Optional)
For Slack notifications.

### `GITHUB_TOKEN`
Automatically provided by GitHub. No action needed. ✅

## Test Output

### Successful Test
```
╔══════════════════════════════════════════════════════════╗
║      Generic App Launch Test - NativeBridge Platform    ║
╚══════════════════════════════════════════════════════════╝

📋 Validating Configuration...
─────────────────────────────────────────────────────────────
✓ API Key: Nb-vODg.84...
✓ Device Session ID: bQDh
✓ Endpoint: https://api.nativebridge.io/appium/wd/hub

🧪 Running Generic Tests...
═════════════════════════════════════════════════════════════

📦 Test 1: Verify Current Package
─────────────────────────────────────────────────────────────
Current Package: com.nativebridge.debug
✅ App is running with package: com.nativebridge.debug

...

╔══════════════════════════════════════════════════════════╗
║                   ✅ TEST PASSED!                        ║
╚══════════════════════════════════════════════════════════╝
```

### Failed Test
```
╔══════════════════════════════════════════════════════════╗
║                   ❌ TEST FAILED!                        ║
╚══════════════════════════════════════════════════════════╝

Error: Session not found: abc123
```

## Test Coverage

The `GenericAppLaunchTest` performs these validations:

| Test | Description | What It Checks |
|------|-------------|----------------|
| 1️⃣ Verify Current Package | Gets app package name | App is installed and running |
| 2️⃣ Verify Current Activity | Gets current activity | Launcher activity is active |
| 3️⃣ Get Page Source | Retrieves UI XML | UI is accessible |
| 4️⃣ Get Screen Size | Gets device dimensions | Screen info is available |
| 5️⃣ Find Visible Elements | Locates UI elements | Interactive elements exist |
| 6️⃣ Check App State | Verifies app state | App is in foreground (state 4) |
| 7️⃣ Take Screenshot | Captures screen | Screenshot API works |
| 8️⃣ Press Back Button | Tests navigation | Back navigation works |

## Artifacts

After the workflow completes, you can download:

### 1. Test Logs
- **Name**: `appium-test-logs-{version}`
- **Location**: GitHub Actions → Workflow run → Artifacts
- **Contents**: Complete test output including all test steps

### 2. APK
- **Name**: `NativeBridge-Android-v{version}`
- **Location**: GitHub Actions → Workflow run → Artifacts
- **Contents**: Production APK file

### 3. GitHub Release (Tag Triggers Only)
- APK file
- Test logs
- Test results summary
- Session links

## Slack Notifications

If `SLACK_WEBHOOK_URL` is configured, you'll receive notifications with:

```
🚀 Production Release & Test Complete

App Version: 1.5.0
Session ID: bQDh
Appium Test: ✅ Passed

Session URL: [Launch Session Now]

Triggered by GitHub Actions | [View Workflow]
```

Test status is color-coded:
- 🟢 Green: Test passed
- 🔴 Red: Test failed
- ⚪ Gray: Test skipped

## Troubleshooting

### Test Not Running

**Symptom**: Workflow completes but test job is skipped

**Solutions**:
1. Check that session was created successfully in job 1
2. For manual triggers, ensure "Run Appium test" is checked
3. For tag triggers, tests always run (no option to skip)

### Test Fails with "Session not found"

**Symptom**: Test fails immediately with session error

**Solutions**:
1. Check that the session ID from job 1 is correct
2. Verify session is still active (not expired)
3. Check API key has access to the session

### Test Fails with "Authentication failed"

**Symptom**: Test fails with 401 error

**Solutions**:
1. Verify `NATIVEBRIDGE_API_KEY` secret is set correctly
2. Check API key hasn't expired
3. Ensure API key has permissions for Appium access

### Test Timeout

**Symptom**: Test fails after 10 minutes

**Solutions**:
1. Increase `timeout-minutes` in workflow (currently 10)
2. Check device session is responding
3. Review test logs for hanging operations

### Maven Compilation Errors

**Symptom**: Workflow fails during "Compile Test" step

**Solutions**:
1. Check `pom.xml` dependencies are correct
2. Verify Java version is 17 or higher
3. Clear Maven cache and retry

## Advanced Configuration

### Changing Timeout

Edit `.github/workflows/release-with-appium-test.yml`:

```yaml
- name: Run Appium Test
  timeout-minutes: 10  # Change this value
```

### Adding More Tests

1. Edit `appium_tests/src/main/java/GenericAppLaunchTest.java`
2. Add new test method:
   ```java
   private static void test9_MyNewTest() throws Exception {
       System.out.println("\n🧪 Test 9: My New Test");
       // Your test code
   }
   ```
3. Call it from `runGenericTests()`:
   ```java
   test9_MyNewTest();
   ```
4. Commit and push

### Running Multiple Test Files

Currently, the workflow runs only `GenericAppLaunchTest.java`. To run multiple tests:

1. Create additional test files in `appium_tests/src/main/java/`
2. Update workflow to run each test:
   ```yaml
   - name: Run Test 1
     run: mvn exec:java -Dexec.mainClass="Test1"

   - name: Run Test 2
     run: mvn exec:java -Dexec.mainClass="Test2"
   ```

### Beta Testing

The workflow supports beta builds. To enable:

**Manual Trigger**:
- Check "Also build beta variant?"

**Tag Trigger**:
```bash
git tag -a v1.5.0 -m "Release 1.5.0
[NB_SESSION_ENABLED]
[NB_DEVICE_IDS:67a642531a4aa535498192f8]
[NB_BETA_ENABLED]
[NB_BETA_DEVICE_IDS:67a642531a4aa535498192f9]
"
```

**Note**: Currently, tests only run on the production build, not beta.

## Local Testing

You can run the test locally before committing:

```bash
cd NativeBridge-Debug-Application/appium_tests

# 1. Update GenericAppLaunchTest.java with your values
vim src/main/java/GenericAppLaunchTest.java

# 2. Compile
mvn clean compile

# 3. Run
mvn exec:java -Dexec.mainClass="GenericAppLaunchTest"
```

## Best Practices

### 1. Always Run Tests in CI/CD
Enable Appium tests for every release to catch issues early.

### 2. Review Test Logs
Even if tests pass, review logs for warnings or unexpected behavior.

### 3. Keep Tests Generic
The current test works with any Android app. Avoid app-specific assertions unless necessary.

### 4. Monitor Session Duration
Sessions have timeouts. Ensure tests complete before session expires.

### 5. Use Appropriate Devices
Choose devices that match your target audience for realistic results.

## Support

### Documentation
- [NativeBridge Docs](https://docs.nativebridge.io)
- [Appium Docs](https://appium.io/docs/)

### Debugging
- Check workflow logs in GitHub Actions
- Download test logs from Artifacts
- View session details in NativeBridge dashboard

### Contact
- Email: support@nativebridge.io
- GitHub Issues: [Create an issue](https://github.com/AutoFlowLabs/NativeBridge-Debug-Application/issues)

---

**Last Updated**: January 29, 2026
**Version**: 1.0
**Workflow**: `release-with-appium-test.yml`
