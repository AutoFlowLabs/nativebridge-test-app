# NativeBridge Appium Tests

This directory contains automated Appium tests for the NativeBridge Debug Application.

## Structure

```
appium_tests/
├── pom.xml                          # Maven configuration
├── src/main/java/
│   └── GenericAppLaunchTest.java   # Main test file
└── README.md                        # This file
```

## GenericAppLaunchTest.java

A comprehensive test that validates app launch and basic functionality using NativeBridge's zero-config mode.

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- Valid NativeBridge API key
- An active device session on NativeBridge

### Running Locally

1. **Update Configuration** in `GenericAppLaunchTest.java`:
   ```java
   private static final String API_KEY = "YOUR_API_KEY";
   private static final String DEVICE_SESSION_ID = "YOUR_SESSION_ID";
   ```

2. **Compile**:
   ```bash
   mvn clean compile
   ```

3. **Run**:
   ```bash
   mvn exec:java -Dexec.mainClass="GenericAppLaunchTest"
   ```

### CI/CD Usage

This test is automatically run in the GitHub Actions workflow `release-with-appium-test.yml`.

The workflow:
1. Creates a device session via NativeBridge API
2. Automatically updates the test with the session ID
3. Runs the test against the created session
4. Reports results in Slack and GitHub

### Test Coverage

The test performs the following validations:

1. ✅ **Verify Current Package** - Confirms app is running
2. ✅ **Verify Current Activity** - Validates launcher activity
3. ✅ **Get Page Source** - Retrieves UI hierarchy
4. ✅ **Get Screen Size** - Checks device dimensions
5. ✅ **Find Visible Elements** - Locates interactive UI elements
6. ✅ **Check App State** - Verifies app is in foreground
7. ✅ **Take Screenshot** - Captures screen image
8. ✅ **Press Back Button** - Tests navigation

### Features

- **Zero Configuration**: No need to specify appPackage or appActivity
- **Automatic App Launch**: Backend pre-launches the app before Appium connection
- **Generic Tests**: Works with any Android app
- **Detailed Logging**: Clear output showing each test step

### Dependencies

- Appium Java Client 8.6.0
- Selenium WebDriver 4.15.0
- SLF4J for logging

### Environment Variables (CI/CD)

The workflow uses these GitHub Secrets:
- `NATIVEBRIDGE_API_KEY` - Your NativeBridge API key
- `GITHUB_TOKEN` - Automatically provided by GitHub

### Troubleshooting

**Test fails with "Session not found"**:
- Verify the session ID is correct
- Check that the session is still active on NativeBridge dashboard

**Test fails with "Authentication failed"**:
- Verify your API key is valid
- Check that the API key has permissions for the device

**Test times out**:
- Session creation can take 2-5 minutes for real devices
- Increase timeout in workflow if needed

### Adding More Tests

To add additional tests:

1. Create a new method in `GenericAppLaunchTest.java`
2. Call it from the `runGenericTests()` method
3. Follow the existing test pattern for consistent output

Example:
```java
private static void test9_YourNewTest() throws Exception {
    System.out.println("\n🧪 Test 9: Your New Test");
    System.out.println("─────────────────────────────────────────────────────────────");

    try {
        // Your test logic here
        System.out.println("✅ Test passed!");
    } catch (Exception e) {
        System.out.println("⚠️  Error: " + e.getMessage());
    }
}
```

### Support

For issues or questions:
- Check the [NativeBridge Documentation](https://docs.nativebridge.io)
- View workflow logs in GitHub Actions
- Contact support at support@nativebridge.io
