# TestNG Test Framework Guide

## Overview

This project uses **TestNG** as the test framework for running Appium tests. TestNG provides powerful features like test suites, groups, priorities, parallel execution, and detailed reporting.

## Project Structure

```
appium_tests/
├── pom.xml                                    # Maven configuration with TestNG
├── src/
│   ├── main/java/                            # Legacy main classes (backward compatible)
│   │   ├── GenericAppLaunchTest.java         # Original session-based test
│   │   └── SessionlessAppLaunchTest.java     # Original sessionless test
│   └── test/
│       ├── java/tests/                       # TestNG test classes
│       │   ├── BaseTest.java                 # Base class for all tests
│       │   ├── AppLaunchTest.java            # App launch tests
│       │   └── DeviceInfoTest.java           # Device information tests
│       └── resources/                         # TestNG suite XML files
│           ├── testng-sessionless.xml        # Sessionless test suite
│           ├── testng-session.xml            # Session-based test suite
│           ├── testng-all.xml                # All tests
│           └── testng-smoke.xml              # Smoke tests only
└── test-output/                               # TestNG reports (generated)
```

## Running Tests

### Option 1: Run Test Suites (Recommended)

**Sessionless Mode:**
```bash
# Set environment variables
export NATIVEBRIDGE_API_KEY="your-api-key"
export APP_ID="HgWp"
export DEVICE_NAME="Samsung Galaxy S22 Ultra"
export REGION="ind"

# Run sessionless test suite
mvn test -P sessionless
```

**Session-Based Mode:**
```bash
# Set environment variables
export NATIVEBRIDGE_API_KEY="your-api-key"
export DEVICE_SESSION_ID="dmz3"

# Run session-based test suite
mvn test -P session
```

**All Tests:**
```bash
mvn test -P all -DtestMode=sessionless  # or -DtestMode=session
```

### Option 2: Run Specific Suite XML

```bash
# Run a specific test suite file
mvn test -DsuiteXmlFile=src/test/resources/testng-smoke.xml

# With test mode
mvn test -DsuiteXmlFile=src/test/resources/testng-sessionless.xml -DtestMode=sessionless
```

### Option 3: Run Tests by Groups

```bash
# Run only smoke tests
mvn test -P smoke -DtestMode=sessionless

# Run only regression tests
mvn test -P regression -DtestMode=sessionless
```

### Option 4: Run Specific Test Class

```bash
# Run a single test class
mvn test -Dtest=AppLaunchTest -DtestMode=sessionless

# Run multiple test classes
mvn test -Dtest=AppLaunchTest,DeviceInfoTest -DtestMode=sessionless
```

### Option 5: Run Specific Test Method

```bash
# Run a single test method
mvn test -Dtest=AppLaunchTest#testVerifyAppPackage -DtestMode=sessionless
```

## Maven Profiles

| Profile | Description | Suite File | Usage |
|---------|-------------|------------|-------|
| `sessionless` | Sessionless mode tests | `testng-sessionless.xml` | `mvn test -P sessionless` |
| `session` | Session-based mode tests | `testng-session.xml` | `mvn test -P session` |
| `all` | All tests | `testng-all.xml` | `mvn test -P all` |
| `smoke` | Smoke tests only (by group) | N/A | `mvn test -P smoke` |
| `regression` | Regression tests (by group) | N/A | `mvn test -P regression` |

## Test Groups

Tests are organized into groups using TestNG `@Test(groups = {...})` annotation:

- **smoke**: Quick tests for basic functionality (3-5 minutes)
- **regression**: Comprehensive tests for all functionality (10-15 minutes)

### Examples:

```bash
# Run all smoke tests in sessionless mode
mvn test -P smoke -DtestMode=sessionless

# Run all regression tests in session mode
mvn test -P regression -DtestMode=session
```

## TestNG Suite Files

### testng-sessionless.xml
Runs smoke and regression tests in sessionless mode (auto-creates and deletes sessions).

```xml
<suite name="NativeBridge Sessionless Test Suite">
    <parameter name="testMode" value="sessionless"/>
    <test name="Smoke Tests - Sessionless Mode">
        <groups>
            <run>
                <include name="smoke"/>
            </run>
        </groups>
        <classes>
            <class name="tests.AppLaunchTest"/>
        </classes>
    </test>
</suite>
```

### testng-session.xml
Runs smoke and regression tests in session-based mode (uses existing session).

### testng-all.xml
Runs all test classes without group filtering.

### testng-smoke.xml
Runs only smoke-tagged tests for quick validation.

## Creating New Tests

### Step 1: Create Test Class

Create a new test class in `src/test/java/tests/`:

```java
package tests;

import org.testng.Assert;
import org.testng.annotations.Test;

public class MyFeatureTest extends BaseTest {

    @Test(groups = {"smoke", "regression"}, priority = 1,
          description = "Test my feature")
    public void testMyFeature() {
        System.out.println("\n🧪 Test: My Feature");
        System.out.println("─────────────────────────────────────────────────────────────");

        // Your test logic here
        String result = driver.getCurrentPackage();

        Assert.assertNotNull(result);
        System.out.println("✅ Test Passed");
    }
}
```

### Step 2: Add to Suite XML

Add your test class to the appropriate suite file:

```xml
<classes>
    <class name="tests.AppLaunchTest"/>
    <class name="tests.DeviceInfoTest"/>
    <class name="tests.MyFeatureTest"/>  <!-- New test class -->
</classes>
```

### Step 3: Run Tests

```bash
mvn test -P sessionless
```

## TestNG Annotations

| Annotation | Purpose | Example |
|------------|---------|---------|
| `@Test` | Mark method as test | `@Test(description = "...")` |
| `@BeforeClass` | Run once before all tests in class | Setup driver config |
| `@BeforeMethod` | Run before each test method | Initialize driver |
| `@AfterMethod` | Run after each test method | Close driver |
| `@AfterClass` | Run once after all tests in class | Cleanup |
| `@Parameters` | Pass parameters from XML | `@Parameters({"browser"})` |

## Test Priorities

Use `priority` attribute to control test execution order:

```java
@Test(priority = 1)
public void testFirst() { }

@Test(priority = 2)
public void testSecond() { }

@Test(priority = 3)
public void testThird() { }
```

## Parallel Execution

TestNG supports parallel test execution. Update suite XML:

```xml
<suite name="Parallel Suite" parallel="tests" thread-count="3">
    <test name="Test 1">...</test>
    <test name="Test 2">...</test>
    <test name="Test 3">...</test>
</suite>
```

**Note:** For NativeBridge, parallel execution requires multiple devices/sessions.

## Test Reports

TestNG generates HTML reports automatically:

```bash
# Run tests
mvn test -P sessionless

# Reports location
appium_tests/target/surefire-reports/
├── index.html                    # Main report
├── testng-results.xml            # XML results
└── emailable-report.html         # Email-friendly report
```

Open reports:
```bash
# Mac
open target/surefire-reports/index.html

# Linux
xdg-open target/surefire-reports/index.html

# Windows
start target/surefire-reports/index.html
```

## CI/CD Integration

### GitHub Actions Example

```yaml
- name: Run Sessionless Appium Tests
  working-directory: appium_tests
  env:
    NATIVEBRIDGE_API_KEY: ${{ secrets.NATIVEBRIDGE_API_KEY }}
    APP_ID: ${{ steps.upload.outputs.app_id }}
    DEVICE_NAME: "Samsung Galaxy S22 Ultra"
    REGION: "ind"
  run: |
    mvn test -P sessionless -DtestMode=sessionless
```

## Advanced TestNG Features

### Data Providers

Run the same test with different data:

```java
@DataProvider(name = "devices")
public Object[][] deviceData() {
    return new Object[][]{
        {"Samsung Galaxy S22 Ultra", "ind"},
        {"Xiaomi Poco C75", "ind"},
    };
}

@Test(dataProvider = "devices")
public void testOnMultipleDevices(String deviceName, String region) {
    // Test logic
}
```

### Test Dependencies

Make tests depend on others:

```java
@Test(priority = 1)
public void login() {
    // Login logic
}

@Test(priority = 2, dependsOnMethods = {"login"})
public void accessProtectedPage() {
    // Requires login to complete
}
```

### Soft Assertions

Continue test even after assertion fails:

```java
import org.testng.asserts.SoftAssert;

@Test
public void testMultipleConditions() {
    SoftAssert softAssert = new SoftAssert();

    softAssert.assertTrue(condition1, "Condition 1 failed");
    softAssert.assertEquals(actual, expected, "Values don't match");
    softAssert.assertNotNull(object, "Object is null");

    softAssert.assertAll();  // Report all failures at end
}
```

## Troubleshooting

### Tests Not Running

**Issue:** `mvn test` doesn't run tests

**Solution:** Ensure you're using a profile:
```bash
mvn test -P sessionless
```

### Wrong Test Mode

**Issue:** Tests running in wrong mode (session vs sessionless)

**Solution:** Pass `-DtestMode` explicitly:
```bash
mvn test -P sessionless -DtestMode=sessionless
```

### Environment Variables Not Set

**Issue:** `Configuration errors detected`

**Solution:** Set required environment variables:
```bash
# Sessionless mode
export NATIVEBRIDGE_API_KEY="..."
export APP_ID="..."
export DEVICE_NAME="..."
export REGION="ind"

# Session mode
export NATIVEBRIDGE_API_KEY="..."
export DEVICE_SESSION_ID="..."
```

### TestNG Reports Not Generated

**Issue:** No reports in `target/surefire-reports/`

**Solution:** Tests may have compilation errors. Check:
```bash
mvn clean test-compile
mvn test -P sessionless
```

## Comparison: Old vs New Approach

| Feature | Old (main classes) | New (TestNG) |
|---------|-------------------|--------------|
| Run method | `mvn exec:java` | `mvn test` |
| Test organization | Single main class | Multiple test classes |
| Test grouping | None | smoke, regression |
| Test reports | Console only | HTML + XML reports |
| Parallel execution | No | Yes (with multiple devices) |
| Test dependencies | No | Yes |
| Data providers | No | Yes |
| Assertions | Manual | TestNG assertions |
| CI/CD integration | Basic | Advanced |

## Best Practices

1. **Use Groups**: Tag tests as `smoke` or `regression` for flexible execution
2. **Set Priorities**: Control test execution order when needed
3. **Write Descriptive Names**: Use meaningful test method and description names
4. **Use Assertions**: Always assert expected outcomes
5. **Clean Up**: Use `@AfterMethod` to ensure driver closes
6. **Keep Tests Independent**: Each test should work standalone
7. **Use Base Class**: Extend `BaseTest` for common functionality
8. **Update Suite XML**: Add new tests to appropriate suite files

## Next Steps

1. **Add More Tests**: Create new test classes in `src/test/java/tests/`
2. **Customize Suites**: Modify XML files to organize tests your way
3. **Enable Parallel Execution**: Run tests on multiple devices simultaneously
4. **Integrate with CI/CD**: Use in GitHub Actions or Jenkins
5. **Generate Custom Reports**: Use TestNG listeners for custom reporting

## Support

For issues or questions:
- Check TestNG documentation: https://testng.org/doc/documentation-main.html
- Review test reports in `target/surefire-reports/`
- Check Maven Surefire plugin: https://maven.apache.org/surefire/maven-surefire-plugin/
