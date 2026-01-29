# CI/CD Simplified - Single File Execution

## What Changed

The GitHub Actions workflow has been updated to run the **simple single Java file** instead of the TestNG framework.

### Before (TestNG Framework)
```bash
mvn test -P sessionless -DtestMode=sessionless
```
- Used TestNG XML suites
- Required BaseTest.java, AppLaunchTest.java, DeviceInfoTest.java
- More complex setup with @BeforeClass, @BeforeMethod annotations

### After (Single File Execution)
```bash
mvn exec:java -Dexec.mainClass="SessionlessAppLaunchTest"
```
- Uses single Java file: `SessionlessAppLaunchTest.java`
- Simple main() method execution
- No TestNG framework needed

## File Locations

### Test File
```
appium_tests/src/main/java/SessionlessAppLaunchTest.java
```

### Workflow File
```
.github/workflows/release-sessionless-appium-test.yml
```

## How It Works

### 1. Environment Variables (Set by CI/CD)
The workflow automatically sets these environment variables:
- `NATIVEBRIDGE_API_KEY` - From GitHub Secrets
- `APP_ID` - From upload step or manual input
- `DEVICE_NAME` - From workflow input
- `REGION` - From workflow input (defaults to 'ind')

### 2. Java File Reads Environment Variables
```java
private static final String API_KEY = System.getenv("NATIVEBRIDGE_API_KEY") != null
    ? System.getenv("NATIVEBRIDGE_API_KEY")
    : "YOUR_API_KEY_HERE";

private static final String APP_ID = System.getenv("APP_ID") != null
    ? System.getenv("APP_ID")
    : "YOUR_APP_ID_HERE";

private static final String DEVICE_NAME = System.getenv("DEVICE_NAME") != null
    ? System.getenv("DEVICE_NAME")
    : "Samsung Galaxy S22 Ultra";

private static final String REGION = System.getenv("REGION") != null
    ? System.getenv("REGION")
    : "ind";
```

### 3. CI/CD Steps
1. **Compile**: `mvn clean compile`
2. **Run Test**: `mvn exec:java -Dexec.mainClass="SessionlessAppLaunchTest"`
3. **Check Result**: Exit code 0 = PASSED, non-zero = FAILED

## Running Locally

### Option 1: With Environment Variables
```bash
cd appium_tests

# Set environment variables
export NATIVEBRIDGE_API_KEY="Nb-xxxx.yyyy-zzzz"
export APP_ID="HgWp"
export DEVICE_NAME="Samsung Galaxy S22 Ultra"
export REGION="ind"

# Compile and run
mvn clean compile
mvn exec:java -Dexec.mainClass="SessionlessAppLaunchTest"
```

### Option 2: Edit Java File Directly
1. Open `SessionlessAppLaunchTest.java`
2. Update these lines:
   ```java
   private static final String API_KEY = "Nb-xxxx.yyyy-zzzz";
   private static final String APP_ID = "HgWp";
   private static final String DEVICE_NAME = "Samsung Galaxy S22 Ultra";
   private static final String REGION = "ind";
   ```
3. Run:
   ```bash
   mvn clean compile
   mvn exec:java -Dexec.mainClass="SessionlessAppLaunchTest"
   ```

## Benefits of Single File Approach

✅ **Simple**: No complex TestNG framework
✅ **Fast**: Quick compilation and execution
✅ **Easy to Debug**: All code in one file with clear main() method
✅ **No Annotation Issues**: No @BeforeClass/@BeforeMethod execution problems
✅ **Backward Compatible**: Works exactly like before

## Test Execution Flow

```
1. Validate environment variables
   ├─ Check API_KEY is set
   ├─ Check APP_ID is set
   ├─ Check DEVICE_NAME is set
   └─ Check REGION is set

2. Setup Appium session (sessionless mode)
   ├─ Configure UiAutomator2Options
   ├─ Set NativeBridge options (appId, deviceName, region)
   ├─ Create AndroidDriver
   └─ Wait 5 seconds for app launch

3. Run tests
   ├─ Test 1: Verify app package
   ├─ Test 2: Verify current activity
   ├─ Test 3: Get page source
   ├─ Test 4: Get screen size
   ├─ Test 5: Find visible elements
   ├─ Test 6: Check app state
   ├─ Test 7: Take screenshot
   └─ Test 8: Press back button

4. Cleanup
   └─ Quit driver (auto-deletes session in sessionless mode)
```

## CI/CD Workflow Trigger

### Manual Trigger
Go to GitHub Actions → "Release with Sessionless Appium Test" → Run workflow

### Inputs
- **app_id** (optional): Skip upload, use existing app
- **version** (optional): APK version if uploading
- **device_name** (required): Exact device model name
- **region** (optional): Defaults to 'ind'

### Example
- app_id: `HgWp`
- device_name: `Samsung Galaxy S22 Ultra`
- region: `ind`

## Troubleshooting

### Test Fails with "API_KEY is not set"
- Check GitHub Secrets has `NATIVEBRIDGE_API_KEY`
- Verify workflow is passing it to environment

### Test Fails with "APP_ID is not set"
- If using manual app_id input, check it's provided
- If uploading APK, check upload step succeeded

### Compilation Fails
```bash
# Clean Maven cache and retry
mvn clean
mvn compile
```

### Test Times Out
- Sessionless mode takes 2-5 minutes for device session creation
- Nginx timeouts are configured to 600 seconds (10 minutes)
- CI/CD timeout is 15 minutes

## Success Indicators

✅ You should see this output:
```
╔══════════════════════════════════════════════════════════╗
║        Sessionless Appium Test - NativeBridge           ║
╚══════════════════════════════════════════════════════════╝

📋 Validating Configuration...
✓ API Key: Nb-xxxx...
✓ App ID: HgWp
✓ Device: Samsung Galaxy S22 Ultra
✓ Region: ind

🔧 Setting Up Appium Session...
🔌 Connecting to NativeBridge...
✅ Session Created!

🧪 Running Tests...
📦 Test 1: Verify Current Package
✅ Test Passed

...

╔══════════════════════════════════════════════════════════╗
║                   ✅ TEST PASSED!                        ║
╚══════════════════════════════════════════════════════════╝
```
