# QA Repository - Test Automation

This repository contains Appium test automation for NativeBridge and CI/CD pipeline for running tests.

## Repository Purpose

**Role:** QA/Test Automation Repository

**Responsibilities:**
- Run Appium tests on NativeBridge
- Can be triggered by DEV repository OR run independently
- Generate test reports and artifacts

## Repository Structure

```
qa-automation-repo/
├── .github/
│   └── workflows/
│       └── qa-tests.yml              # QA test pipeline
├── appium_tests/
│   ├── src/
│   │   └── main/
│   │       └── java/
│   │           └── SessionlessAppLaunchTest.java
│   ├── pom.xml
│   └── README.md
├── test-reports/                     # Test reports (gitignored)
├── screenshots/                      # Test screenshots (gitignored)
└── README.md
```

## Quick Start

### 1. Configure Secrets

Go to **Settings → Secrets and variables → Actions** and add:

| Secret | Description | Example |
|--------|-------------|---------|
| `NATIVEBRIDGE_API_KEY` | NativeBridge API key | `Nb-xxxx.yyyy-zzzz` |
| `SLACK_WEBHOOK_URL` | (Optional) Slack notifications | `https://hooks.slack.com/...` |

**Note:** You don't need `QA_REPO_PAT` here - that's only in the DEV repo!

### 2. Run Tests

**Method 1: Automatic (Triggered by DEV Repo)**

When developers push code with "nativebridge" keyword, this repository's tests automatically run.

✅ **You don't need to do anything!** Just wait for notification.

**Method 2: Manual (Independent QA Testing)**

1. Go to **Actions** → **"Run QA Tests"**
2. Click **"Run workflow"**
3. Fill parameters:
   - `app_id`: Get from DEV team or NativeBridge dashboard
   - `nb_version`: App version
   - `device_name`: Device to test on
   - `region`: Region (optional)
4. Click **"Run workflow"**

## Workflow Triggers

### Automatic (Called by DEV Repository)

When DEV repository successfully uploads an app:
```
DEV Repo → Uploads APK → Triggers this workflow
```

**Parameters passed automatically:**
- `app_id` - NativeBridge app ID
- `nb_version` - App version
- `device_name` - Test device
- `region` - Region
- `triggered_by` - Set to `dev-repo`

### Manual (QA Team)

QA team can run tests independently:

1. Get `app_id` from:
   - DEV team
   - NativeBridge dashboard
   - Previous test runs

2. Trigger workflow with custom parameters

**Use cases:**
- Re-run tests after bug fix
- Test on different devices
- Test specific version
- Regression testing

## Workflow Parameters

| Parameter | Required | Default | Description |
|-----------|----------|---------|-------------|
| `app_id` | **Yes** | - | NativeBridge app ID (e.g., `XyZ9`) |
| `nb_version` | **Yes** | - | App version (e.g., `1.2.3`) |
| `device_name` | **Yes** | `Samsung Galaxy S22 Ultra` | Device to test |
| `region` | No | `ind` | Region (ind, usa, etc.) |
| `triggered_by` | No | `manual` | Trigger source |

## Workflow Steps

```
1. Validate Inputs
   └─ Ensure all required parameters are provided

2. Setup Test Environment
   ├─ Checkout repository
   ├─ Setup Java 17
   ├─ Cache Maven dependencies
   └─ Compile test code

3. Run Appium Tests
   ├─ Set environment variables
   ├─ Execute SessionlessAppLaunchTest
   ├─ Capture logs
   └─ Take screenshots

4. Generate Test Report
   ├─ Create detailed summary
   ├─ Upload logs
   └─ Upload screenshots

5. Send Notifications (optional)
   └─ Slack notification with results
```

## Test Execution

The tests run in **sessionless mode**, which means:

✅ **Auto-creates device session** - No manual setup needed
✅ **Auto-installs app** - App installed automatically
✅ **Runs tests** - Your test cases execute
✅ **Auto-cleanup** - Session deleted automatically

**Timeline:** ~8-12 minutes per test run

## Test Results

### GitHub Summary

Each test run creates a detailed summary with:
- ✅/❌ Test result (passed/failed)
- Duration
- Device and configuration used
- Link to download logs and screenshots

### Artifacts

Every test run uploads:

1. **Test Logs** (`test-logs-{app_id}-{run_id}`)
   - Complete console output
   - Appium driver logs
   - Test execution details
   - Retention: 30 days

2. **Screenshots** (`test-screenshots-{app_id}-{run_id}`)
   - Screenshots captured during tests
   - Retention: 30 days

### Slack Notifications (Optional)

If configured, Slack receives:
- Test result (✅ passed / ❌ failed)
- Test duration
- Device used
- App ID and version
- Link to full report

## Examples

### Example 1: QA Tests After DEV Deploy

**Scenario:** Developer pushed code, DEV repo uploaded app

**What happens:**
1. DEV repo finishes upload → `app_id: XyZ9`, `version: 1.2.3`
2. DEV repo triggers this repository
3. This workflow starts automatically
4. Tests run on Samsung Galaxy S22 Ultra
5. Results posted to Slack and GitHub

**Timeline:** Starts immediately after DEV upload

**QA Action:** None needed - just monitor results

### Example 2: QA Tests Different Device

**Scenario:** QA wants to test same app on Pixel 7 Pro

**Steps:**
1. Get `app_id: XyZ9` and `version: 1.2.3` from earlier run
2. Actions → "Run QA Tests"
3. Fill:
   ```
   app_id: XyZ9
   nb_version: 1.2.3
   device_name: Google Pixel 7 Pro
   region: usa
   triggered_by: manual
   ```
4. Run workflow

**Result:** Tests run on Pixel 7 Pro in ~10 minutes

### Example 3: Regression Testing

**Scenario:** Bug was fixed, QA wants to re-test

**Steps:**
1. Use same `app_id` from original upload
2. Run workflow manually
3. Compare results with previous run

### Example 4: Multi-Device Testing

**Scenario:** QA wants to test on 3 devices simultaneously

**Steps:**
Run workflow 3 times in parallel:

**Run 1:** Samsung Galaxy S22 Ultra
**Run 2:** Google Pixel 7 Pro
**Run 3:** iPhone 14 Pro Max

All run in parallel, results available in ~10 minutes!

## Integration with DEV Repository

This repository is triggered by the DEV repository:

```
┌────────────────────────────────────┐
│     DEV Repository                 │
│  (android-app-repo)                │
│                                    │
│  1. Build APK                      │
│  2. Upload to NativeBridge         │
│     └─ Get app_id, nb_version      │
│  3. Trigger QA Repository ─────────┼──┐
└────────────────────────────────────┘  │
                                        │
                                        │ (Cross-repo trigger)
                                        │
                                        ▼
┌────────────────────────────────────┐
│     QA Repository (THIS REPO)      │
│  (qa-automation-repo)              │
│                                    │
│  1. Receive: app_id, nb_version    │
│  2. Setup test environment         │
│  3. Run Appium tests               │
│  4. Generate reports               │
│  5. Send notifications             │
└────────────────────────────────────┘
```

**DEV Repository:** `{DEV_REPO_OWNER}/{DEV_REPO_NAME}`

**This Repository:** Independent test automation repo

## Test Development

### Adding New Tests

1. Create new test class in `appium_tests/src/main/java/`
2. Follow pattern of `SessionlessAppLaunchTest.java`
3. Commit and push
4. Tests automatically available in next run

### Test Structure

```java
public class MyNewTest {
    private static AndroidDriver driver;

    // Read environment variables
    private static final String API_KEY = System.getenv("NATIVEBRIDGE_API_KEY");
    private static final String APP_ID = System.getenv("APP_ID");
    private static final String DEVICE_NAME = System.getenv("DEVICE_NAME");

    public static void main(String[] args) {
        // Setup driver
        // Run tests
        // Cleanup
    }
}
```

### Running Tests Locally

```bash
cd appium_tests

# Set environment variables
export NATIVEBRIDGE_API_KEY="Nb-xxxx..."
export APP_ID="XyZ9"
export DEVICE_NAME="Samsung Galaxy S22 Ultra"
export REGION="ind"

# Compile and run
mvn clean compile
mvn exec:java -P sessionless
```

## Best Practices

### For QA Team

1. ✅ **Keep app_id and nb_version handy** for independent testing
2. ✅ **Test on multiple devices** using parallel runs
3. ✅ **Download test logs** when debugging failures
4. ✅ **Verify device names** match NativeBridge dashboard exactly
5. ✅ **Use meaningful test names** for easy identification

### Device Names

Get exact names from NativeBridge dashboard:
- ✅ `Samsung Galaxy S22 Ultra` (correct)
- ❌ `Samsung S22` (wrong - will fail)
- ❌ `Galaxy S22 Ultra` (wrong - will fail)

**Important:** Device names are case-sensitive and must match exactly!

### Test Reports

- Download artifacts from Actions tab
- Test logs: Complete execution details
- Screenshots: Visual evidence of test state
- Both retained for 30 days

## Troubleshooting

### Tests Fail: "device not available"

**Cause:** Device name is incorrect or device is offline

**Solution:**
1. Check exact device name in NativeBridge dashboard
2. Ensure device is online in the region
3. Verify spelling and case are exact

### Tests Fail: "API key not configured"

**Cause:** `NATIVEBRIDGE_API_KEY` secret not set

**Solution:**
1. Settings → Secrets → Actions
2. Add `NATIVEBRIDGE_API_KEY`
3. Use your NativeBridge API key

### Tests Timeout

**Cause:** Sessionless mode takes time for session creation

**Solution:**
- Normal for first test on a device
- Subsequent tests are faster
- Current timeout: 15 minutes (should be sufficient)

### Cannot Find app_id

**Cause:** Need app_id for manual testing

**Solution:**
Get from:
1. DEV team (they have it from upload)
2. NativeBridge dashboard
3. Previous test run summaries

### Compilation Fails

**Cause:** Maven dependencies issue

**Solution:**
- Java 17 is required
- Maven dependencies cached
- Check pom.xml is valid

## Team Collaboration

### DEV → QA Flow

1. DEV commits with "nativebridge"
2. DEV pipeline uploads app
3. QA pipeline triggers automatically
4. QA monitors results
5. QA can re-run on different devices

### QA Independence

QA team is **not blocked** by DEV:
- Can run tests anytime
- Just needs app_id and version
- Full control over device selection
- Can run parallel tests

### Communication

**DEV should share:**
- app_id after upload
- nb_version
- What changed in this build

**QA should share:**
- Test results
- Which devices tested
- Any failures found

## Related Documentation

- DEV Repository: See DEV repo's README
- Two-Repo Setup Guide: `TWO_REPO_SETUP_GUIDE.md`
- Test Framework: See `appium_tests/README.md`

## Support

For issues:
1. Check workflow logs in Actions tab
2. Download and review test logs artifact
3. Verify NATIVEBRIDGE_API_KEY secret is set
4. Ensure device name matches NativeBridge exactly
5. Review `TWO_REPO_SETUP_GUIDE.md`

## Metrics

Track these metrics for QA quality:
- Test pass rate
- Average test duration
- Devices tested
- Tests per week
- Time to detect issues

View all in Actions tab history!
