# Quick Command Reference

## Setup

```bash
# Set environment variables for sessionless mode
export NATIVEBRIDGE_API_KEY="Nb-..."
export APP_ID="HgWp"
export DEVICE_NAME="Samsung Galaxy S22 Ultra"
export REGION="ind"

# OR for session-based mode
export NATIVEBRIDGE_API_KEY="Nb-..."
export DEVICE_SESSION_ID="dmz3"
```

## Run Tests

### Run Test Suites

```bash
# Sessionless mode (recommended for CI/CD)
mvn test -P sessionless

# Session-based mode (requires existing session)
mvn test -P session

# All tests
mvn test -P all -DtestMode=sessionless

# Smoke tests only (quick validation)
mvn test -P smoke -DtestMode=sessionless

# Regression tests
mvn test -P regression -DtestMode=sessionless
```

### Run Specific Tests

```bash
# Run single test class
mvn test -Dtest=AppLaunchTest -DtestMode=sessionless

# Run multiple test classes
mvn test -Dtest=AppLaunchTest,DeviceInfoTest -DtestMode=sessionless

# Run single test method
mvn test -Dtest=AppLaunchTest#testVerifyAppPackage -DtestMode=sessionless
```

### Run Specific Suite XML

```bash
# Run custom suite XML
mvn test -DsuiteXmlFile=src/test/resources/testng-smoke.xml -DtestMode=sessionless
```

## Build Commands

```bash
# Clean and compile
mvn clean compile

# Compile tests only
mvn test-compile

# Clean, compile, and run tests
mvn clean test -P sessionless
```

## Backward Compatibility (Old Method)

```bash
# Run old-style main class tests
mvn exec:java -P sessionless           # Runs SessionlessAppLaunchTest
mvn exec:java -P session               # Runs GenericAppLaunchTest
```

## View Reports

```bash
# After running tests, open HTML report
open target/surefire-reports/index.html

# Or
xdg-open target/surefire-reports/index.html    # Linux
start target/surefire-reports/index.html       # Windows
```

## Common CI/CD Commands

```bash
# Full CI/CD pipeline
mvn clean test-compile test -P sessionless -DtestMode=sessionless

# With specific suite
mvn clean test -DsuiteXmlFile=src/test/resources/testng-sessionless.xml

# Generate reports
mvn surefire-report:report
```

## Debugging

```bash
# Run with debug output
mvn test -P sessionless -X

# Run with verbose TestNG output
mvn test -P sessionless -Dtestng.verbose=10

# Skip tests (for build only)
mvn clean compile -DskipTests
```

## Example: Complete Test Run

```bash
# 1. Set environment variables
export NATIVEBRIDGE_API_KEY="Nb-abc123..."
export APP_ID="HgWp"
export DEVICE_NAME="Samsung Galaxy S22 Ultra"
export REGION="ind"

# 2. Navigate to test directory
cd /path/to/NativeBridge-Debug-Application/appium_tests

# 3. Clean and compile
mvn clean test-compile

# 4. Run tests
mvn test -P sessionless

# 5. View results
open target/surefire-reports/index.html
```

## Profiles Summary

| Profile | Command | Description |
|---------|---------|-------------|
| `sessionless` | `mvn test -P sessionless` | Run sessionless test suite |
| `session` | `mvn test -P session` | Run session-based test suite |
| `all` | `mvn test -P all` | Run all tests |
| `smoke` | `mvn test -P smoke` | Run smoke tests only |
| `regression` | `mvn test -P regression` | Run regression tests |

## Test Groups

```bash
# Include specific groups
mvn test -Dgroups="smoke"
mvn test -Dgroups="regression"
mvn test -Dgroups="smoke,regression"

# Exclude specific groups
mvn test -DexcludedGroups="slow"
```

## Clean Up

```bash
# Remove generated files
mvn clean

# Remove all build artifacts
rm -rf target/
rm -rf test-output/
```
