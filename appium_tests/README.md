# NativeBridge Appium Tests

Automated Appium tests for NativeBridge application.

## Test Files

- **GenericAppLaunchTest.java** - Session-based test (requires pre-created session)
- **SessionlessAppLaunchTest.java** - Sessionless test (auto-creates and deletes session)

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- NativeBridge API key

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

## Common Commands

### Compile
```bash
mvn clean compile
```

### Run Session-Based Test
```bash
# Set environment variables
export NATIVEBRIDGE_API_KEY="your-api-key"
export DEVICE_SESSION_ID="your-session-id"

# Run
mvn exec:java
# or
mvn exec:java -P session
```

### Run Sessionless Test
```bash
# Set environment variables
export NATIVEBRIDGE_API_KEY="your-api-key"
export APP_ID="HgWp"
export DEVICE_NAME="Xiaomi Poco C75"
export REGION="ind"

# Run
mvn exec:java -P sessionless
```

## CI/CD Integration

### Session-Based Workflow
**File:** `.github/workflows/release-with-appium-test.yml`

This workflow:
1. Uploads APK to NativeBridge
2. Creates a device session via `/v1/application/session`
3. Extracts session ID
4. Runs `GenericAppLaunchTest`
5. Session remains active (manual cleanup required)

### Sessionless Workflow
**File:** `.github/workflows/release-sessionless-appium-test.yml`

This workflow:
1. Uploads APK via `/v1/application`
2. Extracts app ID from response
3. Creates `SessionlessAppLaunchTest.java` dynamically
4. Runs sessionless test (auto-creates and deletes session)
5. No manual cleanup needed!

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

## Comparison Table

| Feature | Session-Based | Sessionless |
|---------|---------------|-------------|
| Test File | `GenericAppLaunchTest` | `SessionlessAppLaunchTest` |
| Profile | `-P session` (default) | `-P sessionless` |
| Requires | Session ID | App ID + Device Name |
| Session Creation | Manual (pre-created) | Automatic |
| Session Cleanup | Manual | Automatic |
| Setup Time | Instant | 2-5 minutes |
| Use Case | Quick tests, debugging | CI/CD, clean slate testing |

## Test Coverage

Both tests perform the following validations:

1. ✅ **Verify Current Package** - Confirms app is running
2. ✅ **Verify Current Activity** - Validates launcher activity
3. ✅ **Get Screen Size** - Checks device dimensions

## Project Structure

```
appium_tests/
├── pom.xml                             # Maven configuration with profiles
├── src/main/java/
│   ├── GenericAppLaunchTest.java      # Session-based test
│   └── SessionlessAppLaunchTest.java  # Sessionless test
└── README.md                           # This file
```

## Dependencies

- Appium Java Client 8.6.0
- Selenium WebDriver 4.15.0
- SLF4J for logging

## Support

For issues or questions:
- Check the [NativeBridge Documentation](https://docs.nativebridge.io)
- View workflow logs in GitHub Actions
- Contact support at support@nativebridge.io
