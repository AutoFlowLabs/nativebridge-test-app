# NativeBridge Appium Tests

Automated Appium tests for NativeBridge application using **TestNG** framework.

## 🚀 Quick Start

```bash
# Set environment variables
export NATIVEBRIDGE_API_KEY="your-api-key"
export APP_ID="HgWp"
export DEVICE_NAME="Samsung Galaxy S22 Ultra"
export REGION="ind"

# Run tests
cd appium_tests
mvn test -P sessionless
```

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- NativeBridge API key

## 📁 Project Structure

```
appium_tests/
├── src/
│   ├── main/java/                    # Legacy test classes (backward compatible)
│   └── test/
│       ├── java/tests/               # TestNG test classes
│       │   ├── BaseTest.java         # Base class for all tests
│       │   ├── AppLaunchTest.java    # App launch tests
│       │   └── DeviceInfoTest.java   # Device info tests
│       └── resources/                 # TestNG suite XML files
│           ├── testng-sessionless.xml
│           ├── testng-session.xml
│           ├── testng-all.xml
│           └── testng-smoke.xml
├── pom.xml                            # Maven config with TestNG
├── TESTNG_GUIDE.md                    # Detailed TestNG documentation
└── QUICK_COMMANDS.md                  # Command reference
```

## Two Testing Modes

### Mode 1: Session-Based Testing (GenericAppLaunchTest)

**Use when:** You have a pre-created device session

**Requires:**
- `NATIVEBRIDGE_API_KEY`
- `DEVICE_SESSION_ID`

**Behavior:**
- Connects to existing session
- App must be pre-installed
- Session is NOT deleted after test

**Configuration:**

Environment variables:
```bash
export NATIVEBRIDGE_API_KEY="Nb-..."
export DEVICE_SESSION_ID="dmz3"
```

Or update hardcoded values in `GenericAppLaunchTest.java` lines 43-49.

**Run:**
```bash
# Using default profile
mvn exec:java

# Or explicitly
mvn exec:java -P session
mvn exec:java -Dexec.mainClass="GenericAppLaunchTest"
```

---

### Mode 2: Sessionless Testing (SessionlessAppLaunchTest)

**Use when:** You want automatic session creation and cleanup

**Requires:**
- `NATIVEBRIDGE_API_KEY`
- `APP_ID` (from `/v1/application` API upload)
- `DEVICE_NAME` (exact model name like "Xiaomi Poco C75")
- `REGION` (optional, defaults to "ind")

**Behavior:**
- Auto-creates device session
- Auto-installs app
- Auto-launches app
- Auto-deletes session on completion
- Takes 2-5 minutes for session creation

**Configuration:**

Environment variables:
```bash
export NATIVEBRIDGE_API_KEY="Nb-..."
export APP_ID="HgWp"
export DEVICE_NAME="Xiaomi Poco C75"
export REGION="ind"
```

Or update hardcoded values in `SessionlessAppLaunchTest.java` lines 31-43.

**Run:**
```bash
# Using sessionless profile
mvn exec:java -P sessionless

# Or explicitly
mvn exec:java -Dexec.mainClass="SessionlessAppLaunchTest"
```

## 🎯 Running Tests (TestNG)

### Run Test Suites (Recommended)

```bash
# Sessionless mode - runs all sessionless tests
mvn test -P sessionless

# Session-based mode - runs all session tests
mvn test -P session

# Smoke tests only - quick validation
mvn test -P smoke -DtestMode=sessionless

# Regression tests - comprehensive testing
mvn test -P regression -DtestMode=sessionless

# All tests
mvn test -P all -DtestMode=sessionless
```

### Run Specific Tests

```bash
# Run single test class
mvn test -Dtest=AppLaunchTest -DtestMode=sessionless

# Run specific test method
mvn test -Dtest=AppLaunchTest#testVerifyAppPackage -DtestMode=sessionless

# Run by group
mvn test -Dgroups="smoke" -DtestMode=sessionless
```

### Legacy Method (Backward Compatible)

```bash
# Old-style main class execution still works
mvn exec:java -P sessionless    # Runs SessionlessAppLaunchTest
mvn exec:java -P session        # Runs GenericAppLaunchTest
```

## 📊 Test Reports

TestNG automatically generates HTML reports:

```bash
# After running tests, view reports
open target/surefire-reports/index.html

# Reports include:
# - Test execution summary
# - Pass/Fail status
# - Execution time
# - Test groups
# - Failed test details
```

## CI/CD Integration

### Sessionless Workflow (TestNG)
**File:** `.github/workflows/release-sessionless-appium-test.yml`

```yaml
- name: Run Sessionless Appium Test Suite
  env:
    NATIVEBRIDGE_API_KEY: ${{ secrets.NATIVEBRIDGE_API_KEY }}
    APP_ID: ${{ steps.upload.outputs.app_id }}
    DEVICE_NAME: "Samsung Galaxy S22 Ultra"
    REGION: "ind"
  run: |
    mvn test -P sessionless -DtestMode=sessionless
```

**Features:**
- Runs TestNG test suite
- Auto-creates and deletes sessions
- Generates test reports
- Supports multiple test classes
- Can run specific groups (smoke, regression)

## Troubleshooting

### Error: "DEVICE_SESSION_ID is not set"
You're running `GenericAppLaunchTest` but need `SessionlessAppLaunchTest`. Use:
```bash
mvn exec:java -P sessionless
```

### Error: "APP_ID is not set"
You're running `SessionlessAppLaunchTest` but need `GenericAppLaunchTest`. Use:
```bash
mvn exec:java -P session
```

### Error: "504 Gateway Time-out"
This means the HTTP client timed out before session creation completed.

**Cause:** Sessionless mode takes 2-5 minutes to create session, but default HTTP timeout is 60 seconds.

**Solution:** The test already configures 10-minute HTTP timeouts using `ClientConfig`:
```java
ClientConfig clientConfig = ClientConfig.defaultConfig()
    .connectionTimeout(Duration.ofMinutes(10))
    .readTimeout(Duration.ofMinutes(10));
```

If you still see this error:
1. Check that you're using the latest `SessionlessAppLaunchTest.java`
2. Verify the backend is not experiencing issues
3. Try a different device (device may be offline)

### Session Creation Takes Too Long
Sessionless mode requires 2-5 minutes to:
- Find available device
- Create session
- Wait for device boot
- Install app
- Launch app

This is normal. The test has 10-minute HTTP timeouts and 15-minute overall timeout.

## 📚 Documentation

- **[TESTNG_GUIDE.md](TESTNG_GUIDE.md)** - Comprehensive TestNG documentation
  - Creating new tests
  - Advanced features (data providers, dependencies, etc.)
  - Parallel execution
  - Custom reports

- **[QUICK_COMMANDS.md](QUICK_COMMANDS.md)** - Quick command reference
  - All Maven commands
  - Profile usage
  - Examples

## 🆕 What's New: TestNG Framework

### Benefits Over Old Approach

| Feature | Old (main classes) | New (TestNG) |
|---------|-------------------|--------------|
| **Organization** | Single main class | Multiple test classes |
| **Grouping** | None | smoke, regression |
| **Reports** | Console only | HTML + XML reports |
| **Parallel Execution** | No | Yes (with multiple devices) |
| **Test Dependencies** | No | Yes |
| **Assertions** | Manual | TestNG assertions |
| **CI/CD** | Basic | Advanced with suite files |
| **Flexibility** | Run all or nothing | Run specific tests/groups |

### Test Structure

```
✓ BaseTest.java               # Base class with setup/teardown
  ├── AppLaunchTest.java      # App launch validations
  └── DeviceInfoTest.java     # Device capability tests
```

### Running Options

```bash
# By suite XML
mvn test -P sessionless           # Runs testng-sessionless.xml

# By test class
mvn test -Dtest=AppLaunchTest

# By group
mvn test -Dgroups="smoke"

# Specific method
mvn test -Dtest=AppLaunchTest#testVerifyAppPackage
```

## Dependencies

- Appium Java Client 8.6.0
- Selenium WebDriver 4.15.0
- TestNG 7.8.0
- SLF4J 2.0.9

## Support

For issues or questions:
- Check the [NativeBridge Documentation](https://docs.nativebridge.io)
- View workflow logs in GitHub Actions
- Contact support at support@nativebridge.io
