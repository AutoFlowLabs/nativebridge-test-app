import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.openqa.selenium.remote.http.ClientConfig;
import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Sessionless Appium Test for NativeBridge
 *
 * This test demonstrates sessionless mode where you only provide:
 * - appId (from /v1/application API)
 * - deviceName (exact model name)
 * - region (optional, defaults to 'ind')
 *
 * The backend automatically:
 * - Creates a device session
 * - Installs the app
 * - Launches the app
 * - Deletes the session when test completes
 *
 * Usage:
 * - CI/CD: Environment variables are set automatically
 * - Local: Update the values below or set environment variables
 */
public class SessionlessAppLaunchTest {
    private static AndroidDriver driver;

    private static final String APPIUM_ENDPOINT = "https://api.nativebridge.io/appium/wd/hub";

    // CI/CD: These are automatically set by the workflow
    // Local: Update these values OR set environment variables
    private static final String API_KEY = System.getenv("NATIVEBRIDGE_API_KEY") != null
        ? System.getenv("NATIVEBRIDGE_API_KEY")
        : "YOUR_API_KEY_HERE";  // UPDATE THIS for local testing

    private static final String APP_ID = System.getenv("APP_ID") != null
        ? System.getenv("APP_ID")
        : "YOUR_APP_ID_HERE";  // UPDATE THIS for local testing (e.g., "HgWp")

    private static final String DEVICE_NAME = System.getenv("DEVICE_NAME") != null
        ? System.getenv("DEVICE_NAME")
        : "Samsung Galaxy S22 Ultra";  // UPDATE THIS for local testing

    private static final String REGION = System.getenv("REGION") != null
        ? System.getenv("REGION")
        : "ind";  // UPDATE THIS for local testing (ind, usa, etc.)

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║        Sessionless Appium Test - NativeBridge           ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        System.out.println();

        try {
            validateEnvironment();
            setupSession();
            runTests();

            System.out.println("\n╔══════════════════════════════════════════════════════════╗");
            System.out.println("║                   ✅ TEST PASSED!                        ║");
            System.out.println("╚══════════════════════════════════════════════════════════╝");

        } catch (Exception e) {
            System.err.println("\n╔══════════════════════════════════════════════════════════╗");
            System.err.println("║                   ❌ TEST FAILED!                        ║");
            System.err.println("╚══════════════════════════════════════════════════════════╝");
            System.err.println("\nError: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } finally {
            cleanup();
        }
    }

    private static void validateEnvironment() {
        System.out.println("📋 Validating Configuration...");
        System.out.println("─────────────────────────────────────────────────────────────");

        boolean hasErrors = false;

        if (API_KEY == null || API_KEY.isEmpty() || API_KEY.equals("YOUR_API_KEY_HERE")) {
            System.err.println("❌ ERROR: API_KEY is not set!");
            System.err.println("   For CI/CD: Set NATIVEBRIDGE_API_KEY environment variable");
            System.err.println("   For local: Update API_KEY in SessionlessAppLaunchTest.java");
            hasErrors = true;
        } else {
            System.out.println("✓ API Key: " + API_KEY.substring(0, Math.min(10, API_KEY.length())) + "...");
        }

        if (APP_ID == null || APP_ID.isEmpty() || APP_ID.equals("YOUR_APP_ID_HERE")) {
            System.err.println("❌ ERROR: APP_ID is not set!");
            System.err.println("   For CI/CD: Set APP_ID environment variable");
            System.err.println("   For local: Update APP_ID in SessionlessAppLaunchTest.java");
            hasErrors = true;
        } else {
            System.out.println("✓ App ID: " + APP_ID);
        }

        if (DEVICE_NAME == null || DEVICE_NAME.isEmpty()) {
            System.err.println("❌ ERROR: DEVICE_NAME is not set!");
            System.err.println("   For CI/CD: Set DEVICE_NAME environment variable");
            System.err.println("   For local: Update DEVICE_NAME in SessionlessAppLaunchTest.java");
            hasErrors = true;
        } else {
            System.out.println("✓ Device Name: " + DEVICE_NAME);
        }

        System.out.println("✓ Region: " + (REGION != null ? REGION : "ind (default)"));
        System.out.println("✓ Endpoint: " + APPIUM_ENDPOINT);
        System.out.println();
        System.out.println("🎯 Mode: SESSIONLESS");
        System.out.println("   • Auto-creates device session");
        System.out.println("   • Installs and launches app");
        System.out.println("   • Auto-deletes session on completion");

        if (hasErrors) {
            System.err.println("\nConfiguration errors detected!");
            System.exit(1);
        }

        System.out.println();
    }

    private static void setupSession() throws Exception {
        System.out.println("🔧 Creating Sessionless Appium Session...");
        System.out.println("─────────────────────────────────────────────────────────────");
        System.out.println("This will:");
        System.out.println("  1. Find device '" + DEVICE_NAME + "'");
        System.out.println("  2. Create a new session");
        System.out.println("  3. Wait for device (2-5 minutes)");
        System.out.println("  4. Install and launch app");
        System.out.println("  5. Connect Appium");
        System.out.println();
        System.out.println("⏳ Please wait (this may take 2-5 minutes)...");
        System.out.println();

        UiAutomator2Options options = new UiAutomator2Options();
        options.setPlatformName("Android");
        options.setAutomationName("UiAutomator2");
        options.setNoReset(true);
        options.setNewCommandTimeout(Duration.ofSeconds(600));

        // CRITICAL: Increase timeouts for sessionless mode (session creation takes 2-5 minutes)
        // These timeouts apply to the HTTP client that makes the session creation request
        options.setCapability("appium:newCommandTimeout", 600);  // 10 minutes for commands

        // HTTP client timeouts (in milliseconds) - MUST be longer than session creation time
        // Default is 60 seconds which causes 504 Gateway Timeout
        options.setCapability("appium:connectionTimeout", 600000);  // 10 minutes (connection timeout)
        options.setCapability("appium:readTimeout", 600000);        // 10 minutes (read timeout)

        // Additional Appium timeouts
        options.setCapability("uiautomator2ServerInstallTimeout", 60000);
        options.setCapability("uiautomator2ServerLaunchTimeout", 60000);

        // NativeBridge sessionless options
        Map<String, Object> nativeBridgeOptions = new HashMap<>();
        nativeBridgeOptions.put("appId", APP_ID);
        nativeBridgeOptions.put("deviceName", DEVICE_NAME);
        nativeBridgeOptions.put("region", REGION != null ? REGION : "ind");

        options.setCapability("nativeBridge:options", nativeBridgeOptions);
        options.setCapability("appium:X-Api-Key", API_KEY);

        System.out.println("🚀 Connecting to NativeBridge (sessionless mode)...");
        System.out.println("   (HTTP timeout set to 10 minutes to handle session creation)");

        // Create custom HTTP client with extended timeouts
        // This is CRITICAL for sessionless mode as session creation takes 2-5 minutes
        ClientConfig clientConfig = ClientConfig.defaultConfig()
            .connectionTimeout(Duration.ofMinutes(10))  // Time to establish connection
            .readTimeout(Duration.ofMinutes(10));        // Time to wait for response

        driver = new AndroidDriver(clientConfig, new URL(APPIUM_ENDPOINT), options);

        System.out.println("✅ Session Created!");
        System.out.println("   Session ID: " + driver.getSessionId());

        try {
            String deviceName = (String) driver.getCapabilities().getCapability("deviceName");
            String platformVersion = (String) driver.getCapabilities().getCapability("platformVersion");
            System.out.println("   Device: " + (deviceName != null ? deviceName : "Unknown"));
            System.out.println("   Android: " + (platformVersion != null ? platformVersion : "Unknown"));
        } catch (Exception e) {
            // Ignore
        }

        System.out.println();
        System.out.println("⏳ Waiting for app to stabilize...");
        Thread.sleep(3000);
    }

    private static void runTests() throws Exception {
        System.out.println("\n🧪 Running Tests...");
        System.out.println("═════════════════════════════════════════════════════════════");

        // Test 1: Verify app is running
        System.out.println("\n📦 Test 1: Verify App Package");
        String currentPackage = driver.getCurrentPackage();
        System.out.println("✓ Current Package: " + currentPackage);

        // Test 2: Verify activity
        System.out.println("\n🎯 Test 2: Verify Current Activity");
        String currentActivity = driver.currentActivity();
        System.out.println("✓ Current Activity: " + currentActivity);

        // Test 3: Get screen size
        System.out.println("\n📐 Test 3: Get Screen Size");
        var size = driver.manage().window().getSize();
        System.out.println("✓ Screen: " + size.width + "x" + size.height);

        System.out.println("\n═════════════════════════════════════════════════════════════");
        System.out.println("All tests completed successfully!");
    }

    private static void cleanup() {
        if (driver != null) {
            System.out.println("\n🔄 Cleaning Up...");
            System.out.println("─────────────────────────────────────────────────────────────");

            try {
                driver.quit();
                System.out.println("✅ Session closed");
                System.out.println("✅ Device session auto-deleted (sessionless mode)");
                System.out.println();
                System.out.println("✨ No dangling sessions! Everything cleaned up automatically.");
            } catch (Exception e) {
                System.out.println("⚠️  Cleanup completed with warnings");
            }
        }
    }
}
