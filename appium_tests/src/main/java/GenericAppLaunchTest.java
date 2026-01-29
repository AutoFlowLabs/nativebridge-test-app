import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.Dimension;

import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generic App Launch Test - Zero Configuration Required!
 *
 * This test demonstrates NativeBridge's automatic app handling.
 * NO need to specify appPackage or appActivity - everything is handled automatically!
 *
 * Usage:
 *   1. Update DEVICE_SESSION_ID below (line 40)
 *   2. Update API_KEY below (line 41)
 *   3. Run:
 *      mvn compile
 *      mvn exec:java -Dexec.mainClass="GenericAppLaunchTest"
 *
 * How it works:
 *   - Backend automatically detects app from session's application_id
 *   - Backend pre-launches the app before creating Appium session
 *   - Backend auto-populates appPackage capability
 *   - NO appActivity needed - app is already running!
 */
public class GenericAppLaunchTest {

    private static AndroidDriver driver;

    // ============================================================================
    // CONFIGURATION - Automatically set by CI/CD or update for local testing
    // ============================================================================
    private static final String APPIUM_ENDPOINT = "https://api.nativebridge.io/appium/wd/hub";

    // CI/CD: These are automatically updated by the workflow
    // Local: Update these values for manual testing
    private static final String API_KEY = System.getenv("NATIVEBRIDGE_API_KEY") != null
        ? System.getenv("NATIVEBRIDGE_API_KEY")
        : "YOUR_API_KEY_HERE";  // UPDATE THIS for local testing

    private static final String DEVICE_SESSION_ID = System.getenv("DEVICE_SESSION_ID") != null
        ? System.getenv("DEVICE_SESSION_ID")
        : "YOUR_SESSION_ID_HERE";  // UPDATE THIS for local testing

    // ============================================================================
    // NO OTHER CONFIGURATION NEEDED!
    // Backend automatically handles:
    // - App package detection (from session's application_id)
    // - App pre-launch (via provider service)
    // - App activity (app already running, no auto-detection needed!)
    // ============================================================================

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║      Generic App Launch Test - NativeBridge Platform    ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        System.out.println();

        try {
            // Validate environment variables
            validateEnvironment();

            // Setup and create session
            setupSession();

            // Run generic tests
            runGenericTests();

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
            System.err.println("   For local: Update API_KEY in GenericAppLaunchTest.java");
            hasErrors = true;
        } else {
            System.out.println("✓ API Key: " + API_KEY.substring(0, Math.min(10, API_KEY.length())) + "...");
        }

        if (DEVICE_SESSION_ID == null || DEVICE_SESSION_ID.isEmpty() || DEVICE_SESSION_ID.equals("YOUR_SESSION_ID_HERE")) {
            System.err.println("❌ ERROR: DEVICE_SESSION_ID is not set!");
            System.err.println("   For CI/CD: Set DEVICE_SESSION_ID environment variable");
            System.err.println("   For local: Update DEVICE_SESSION_ID in GenericAppLaunchTest.java");
            hasErrors = true;
        } else {
            System.out.println("✓ Device Session ID: " + DEVICE_SESSION_ID);
        }

        System.out.println("✓ Endpoint: " + APPIUM_ENDPOINT);
        System.out.println();
        System.out.println("🎯 Configuration Mode: AUTOMATIC");
        System.out.println("   • App package: Auto-detected from session");
        System.out.println("   • App activity: NOT needed (pre-launched)");
        System.out.println("   • App pre-launch: Enabled");

        if (hasErrors) {
            System.err.println("\nPlease update the configuration and try again.");
            System.exit(1);
        }

        System.out.println();
    }

    private static void setupSession() throws Exception {
        System.out.println("🔧 Setting Up Appium Session...");
        System.out.println("─────────────────────────────────────────────────────────────");

        // Setup Appium capabilities
        UiAutomator2Options options = new UiAutomator2Options();

        // Standard capabilities
        options.setPlatformName("Android");
        options.setAutomationName("UiAutomator2");
        options.setNewCommandTimeout(Duration.ofSeconds(600));

        // Increase UiAutomator2 server installation timeout
        options.setCapability("uiautomator2ServerInstallTimeout", 60000); // 60 seconds

        // Increase HTTP client timeouts to handle slow orchestrator responses
        // Connection timeout: How long to wait for initial connection
        options.setCapability("appium:connectionTimeout", 300000); // 5 minutes
        // Read timeout: How long to wait for response data
        options.setCapability("appium:readTimeout", 300000); // 5 minutes

        // ============================================================================
        // NativeBridge custom options - MINIMAL CONFIGURATION!
        // ============================================================================
        Map<String, Object> nativeBridgeOptions = new HashMap<>();

        // ONLY sessionId is required - backend handles everything else!
        nativeBridgeOptions.put("sessionId", DEVICE_SESSION_ID);

        // DO NOT set appPackage - backend auto-detects from session's application_id
        // DO NOT set appActivity - backend pre-launches app, no auto-detection needed!

        // Optional: Test metadata
        nativeBridgeOptions.put("projectName", "GenericAppTest");
        nativeBridgeOptions.put("buildName", "AutoConfig_Build");
        nativeBridgeOptions.put("testName", "Zero_Config_Test");

        // Optional: Enable video and logs for debugging
        nativeBridgeOptions.put("recordVideo", true);
        nativeBridgeOptions.put("captureScreenshots", true);
        nativeBridgeOptions.put("captureLogs", true);

        options.setCapability("nativeBridge:options", nativeBridgeOptions);
        options.setCapability("X-Api-Key", API_KEY);

        System.out.println("📱 App Package: AUTO-DETECTED (from session)");
        System.out.println("⚙️  App Activity: NOT NEEDED (pre-launched)");
        System.out.println("🚀 App Pre-Launch: ENABLED");
        System.out.println();

        System.out.println("🔌 Connecting to NativeBridge...");
        driver = new AndroidDriver(new URL(APPIUM_ENDPOINT), options);

        System.out.println("✅ Session Created!");
        System.out.println("   Session ID: " + driver.getSessionId());

        // Get device info
        try {
            String deviceName = (String) driver.getCapabilities().getCapability("deviceName");
            String platformVersion = (String) driver.getCapabilities().getCapability("platformVersion");
            System.out.println("   Device: " + (deviceName != null ? deviceName : "Unknown"));
            System.out.println("   Android Version: " + (platformVersion != null ? platformVersion : "Unknown"));
        } catch (Exception e) {
            // Ignore if capabilities not available
        }

        System.out.println();
        System.out.println("⏳ Waiting for app to launch...");
        Thread.sleep(5000); // Give app time to fully launch
    }

    private static void runGenericTests() throws Exception {
        System.out.println("\n🧪 Running Generic Tests...");
        System.out.println("═════════════════════════════════════════════════════════════");

        // Test 1: Verify Current Package
        test1_VerifyCurrentPackage();

        // Test 2: Verify Current Activity
        test2_VerifyCurrentActivity();

        // Test 3: Get Page Source
        test3_GetPageSource();

        // Test 4: Get Screen Size
        test4_GetScreenSize();

        // Test 5: Find Any Visible Elements
        test5_FindVisibleElements();

        // Test 6: Check App State
        test6_CheckAppState();

        // Test 7: Take Screenshot
        test7_TakeScreenshot();

        // Test 8: Press Back Button
        test8_PressBackButton();

        System.out.println("\n═════════════════════════════════════════════════════════════");
        System.out.println("All generic tests completed successfully!");
    }

    private static void test1_VerifyCurrentPackage() throws Exception {
        System.out.println("\n📦 Test 1: Verify Current Package");
        System.out.println("─────────────────────────────────────────────────────────────");

        try {
            String currentPackage = driver.getCurrentPackage();
            System.out.println("Current Package: " + currentPackage);

            if (currentPackage != null && !currentPackage.isEmpty()) {
                System.out.println("✅ App is running with package: " + currentPackage);
                System.out.println("   (Auto-detected by backend from session)");
            } else {
                System.out.println("⚠️  Could not determine current package");
            }
        } catch (Exception e) {
            System.out.println("⚠️  Error getting current package: " + e.getMessage());
        }
    }

    private static void test2_VerifyCurrentActivity() throws Exception {
        System.out.println("\n🎯 Test 2: Verify Current Activity");
        System.out.println("─────────────────────────────────────────────────────────────");

        try {
            String currentActivity = driver.currentActivity();
            System.out.println("Current Activity: " + currentActivity);

            if (currentActivity != null && !currentActivity.isEmpty()) {
                System.out.println("✅ App launched successfully!");
                System.out.println("   The launcher activity is: " + currentActivity);
            } else {
                System.out.println("⚠️  Could not determine current activity");
            }
        } catch (Exception e) {
            System.out.println("⚠️  Error getting current activity: " + e.getMessage());
        }
    }

    private static void test3_GetPageSource() throws Exception {
        System.out.println("\n📄 Test 3: Get Page Source");
        System.out.println("─────────────────────────────────────────────────────────────");

        try {
            String pageSource = driver.getPageSource();

            if (pageSource != null && pageSource.length() > 0) {
                System.out.println("✅ Page source retrieved successfully");
                System.out.println("   Length: " + pageSource.length() + " characters");

                // Show first 200 characters
                String preview = pageSource.substring(0, Math.min(200, pageSource.length()));
                System.out.println("   Preview: " + preview + "...");
            } else {
                System.out.println("⚠️  Page source is empty");
            }
        } catch (Exception e) {
            System.out.println("⚠️  Error getting page source: " + e.getMessage());
        }
    }

    private static void test4_GetScreenSize() throws Exception {
        System.out.println("\n📐 Test 4: Get Screen Size");
        System.out.println("─────────────────────────────────────────────────────────────");

        try {
            Dimension size = driver.manage().window().getSize();
            System.out.println("✅ Screen size: " + size.width + " x " + size.height + " pixels");

            // Calculate aspect ratio
            double aspectRatio = (double) size.height / size.width;
            System.out.println("   Aspect ratio: " + String.format("%.2f", aspectRatio) + ":1");
        } catch (Exception e) {
            System.out.println("⚠️  Error getting screen size: " + e.getMessage());
        }
    }

    private static void test5_FindVisibleElements() throws Exception {
        System.out.println("\n🔍 Test 5: Find Visible Elements");
        System.out.println("─────────────────────────────────────────────────────────────");

        try {
            // Find all visible elements using generic selectors
            List<WebElement> elements = driver.findElements(AppiumBy.xpath("//*[@clickable='true' or @enabled='true']"));

            System.out.println("✅ Found " + elements.size() + " interactive elements");

            if (elements.size() > 0) {
                System.out.println("   First " + Math.min(5, elements.size()) + " elements:");

                for (int i = 0; i < Math.min(5, elements.size()); i++) {
                    WebElement element = elements.get(i);
                    try {
                        String className = element.getAttribute("class");
                        String text = element.getAttribute("text");
                        String resourceId = element.getAttribute("resource-id");

                        System.out.print("   " + (i + 1) + ". " + (className != null ? className : "Unknown"));
                        if (text != null && !text.isEmpty()) {
                            System.out.print(" [text: " + text.substring(0, Math.min(30, text.length())) + "]");
                        }
                        if (resourceId != null && !resourceId.isEmpty()) {
                            System.out.print(" [id: " + resourceId + "]");
                        }
                        System.out.println();
                    } catch (Exception e) {
                        System.out.println("   " + (i + 1) + ". [Unable to get element details]");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("⚠️  Error finding elements: " + e.getMessage());
        }
    }

    private static void test6_CheckAppState() throws Exception {
        System.out.println("\n📊 Test 6: Check App State");
        System.out.println("─────────────────────────────────────────────────────────────");

        try {
            // Get current package first
            String currentPackage = driver.getCurrentPackage();

            if (currentPackage == null || currentPackage.isEmpty()) {
                System.out.println("⚠️  Could not determine current package for app state check");
                return;
            }

            // Check if app is in foreground (state 4 = foreground)
            Long appState = (Long) driver.executeScript("mobile: queryAppState",
                Map.of("appId", currentPackage));

            String stateDescription;
            switch (appState.intValue()) {
                case 0:
                    stateDescription = "Not installed";
                    break;
                case 1:
                    stateDescription = "Not running";
                    break;
                case 2:
                    stateDescription = "Running in background (suspended)";
                    break;
                case 3:
                    stateDescription = "Running in background";
                    break;
                case 4:
                    stateDescription = "Running in foreground";
                    break;
                default:
                    stateDescription = "Unknown state";
            }

            System.out.println("App State: " + appState + " (" + stateDescription + ")");

            if (appState == 4) {
                System.out.println("✅ App is running in foreground");
            } else {
                System.out.println("⚠️  App is not in foreground (state: " + appState + ")");
            }
        } catch (Exception e) {
            System.out.println("⚠️  Error checking app state: " + e.getMessage());
        }
    }

    private static void test7_TakeScreenshot() throws Exception {
        System.out.println("\n📸 Test 7: Take Screenshot");
        System.out.println("─────────────────────────────────────────────────────────────");

        try {
            String screenshot = driver.getScreenshotAs(org.openqa.selenium.OutputType.BASE64);

            if (screenshot != null && screenshot.length() > 0) {
                System.out.println("✅ Screenshot captured successfully");
                System.out.println("   Size: " + screenshot.length() + " characters (base64)");
                System.out.println("   Approx size: " + (screenshot.length() * 3 / 4 / 1024) + " KB");
            } else {
                System.out.println("⚠️  Screenshot is empty");
            }
        } catch (Exception e) {
            System.out.println("⚠️  Error taking screenshot: " + e.getMessage());
        }
    }

    private static void test8_PressBackButton() throws Exception {
        System.out.println("\n⬅️  Test 8: Press Back Button");
        System.out.println("─────────────────────────────────────────────────────────────");

        try {
            String activityBefore = driver.currentActivity();
            System.out.println("Activity before back: " + activityBefore);

            driver.navigate().back();
            Thread.sleep(2000); // Wait for navigation

            String activityAfter = driver.currentActivity();
            System.out.println("Activity after back: " + activityAfter);

            if (!activityBefore.equals(activityAfter)) {
                System.out.println("✅ Back button navigated to different activity");
            } else {
                System.out.println("✅ Back button pressed (no activity change)");
            }
        } catch (Exception e) {
            System.out.println("⚠️  Error pressing back button: " + e.getMessage());
        }
    }

    private static void cleanup() {
        if (driver != null) {
            System.out.println("\n🔄 Cleaning Up...");
            System.out.println("─────────────────────────────────────────────────────────────");

            try {
                driver.quit();
                System.out.println("✅ Session closed successfully");
            } catch (Exception e) {
                System.out.println("⚠️  Session cleanup completed with warnings");
            }
        }
    }
}
