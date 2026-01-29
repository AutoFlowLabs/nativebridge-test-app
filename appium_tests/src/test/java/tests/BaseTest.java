package tests;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.openqa.selenium.remote.http.ClientConfig;
import org.openqa.selenium.remote.http.HttpClient;
import org.testng.annotations.*;

import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Base Test Class for all NativeBridge Appium Tests
 *
 * Provides common setup and teardown methods
 * Handles driver initialization based on test mode (session-based or sessionless)
 */
public class BaseTest {
    protected AndroidDriver driver;

    // Appium endpoint
    protected static final String APPIUM_ENDPOINT = "https://api.nativebridge.io/appium/wd/hub";

    // Environment variables - set via Maven or CI/CD
    protected static final String API_KEY = System.getenv("NATIVEBRIDGE_API_KEY");
    protected static final String DEVICE_SESSION_ID = System.getenv("DEVICE_SESSION_ID");
    protected static final String APP_ID = System.getenv("APP_ID");
    protected static final String DEVICE_NAME = System.getenv("DEVICE_NAME");
    protected static final String REGION = System.getenv("REGION");

    /**
     * Get test mode from system property
     * Options: "session" or "sessionless"
     */
    protected String getTestMode() {
        return System.getProperty("testMode", "sessionless");
    }

    /**
     * Setup method - runs before each test class
     */
    @BeforeClass
    public void setupClass() {
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║     NativeBridge Appium Test - " + getClass().getSimpleName() + "     ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        System.out.println();
    }

    /**
     * Setup driver - runs before each test method
     */
    @BeforeMethod
    public void setupDriver() throws Exception {
        String testMode = getTestMode();

        System.out.println("📋 Initializing driver...");
        System.out.println("  Mode: " + testMode);
        System.out.println("  API Key: " + (API_KEY != null ? API_KEY.substring(0, Math.min(10, API_KEY.length())) + "..." : "NOT SET"));
        System.out.println();

        if ("sessionless".equals(testMode)) {
            setupSessionlessDriver();
        } else {
            setupSessionDriver();
        }
    }

    /**
     * Setup driver for sessionless mode
     */
    protected void setupSessionlessDriver() throws Exception {
        validateSessionlessConfig();

        System.out.println("🔧 Creating Sessionless Appium Session...");
        System.out.println("  App ID: " + APP_ID);
        System.out.println("  Device: " + DEVICE_NAME);
        System.out.println("  Region: " + (REGION != null ? REGION : "ind (default)"));
        System.out.println("⏳ Please wait (this may take 2-5 minutes)...");
        System.out.println();

        UiAutomator2Options options = new UiAutomator2Options();
        options.setPlatformName("Android");
        options.setAutomationName("UiAutomator2");
        options.setNoReset(true);
        options.setNewCommandTimeout(Duration.ofSeconds(600));

        // Extended timeouts for sessionless mode
        options.setCapability("appium:newCommandTimeout", 600);
        options.setCapability("appium:connectionTimeout", 600000);
        options.setCapability("appium:readTimeout", 600000);
        options.setCapability("uiautomator2ServerInstallTimeout", 60000);
        options.setCapability("uiautomator2ServerLaunchTimeout", 60000);

        // NativeBridge sessionless options
        Map<String, Object> nativeBridgeOptions = new HashMap<>();
        nativeBridgeOptions.put("appId", APP_ID);
        nativeBridgeOptions.put("deviceName", DEVICE_NAME);
        nativeBridgeOptions.put("region", REGION != null ? REGION : "ind");

        options.setCapability("nativeBridge:options", nativeBridgeOptions);
        options.setCapability("appium:X-Api-Key", API_KEY);

        // Create custom HTTP client factory with extended timeouts
        ClientConfig clientConfig = ClientConfig.defaultConfig()
            .connectionTimeout(Duration.ofMinutes(10))
            .readTimeout(Duration.ofMinutes(10));

        HttpClient.Factory clientFactory = HttpClient.Factory.createDefault().createClient(clientConfig);

        driver = new AndroidDriver(new URL(APPIUM_ENDPOINT), clientFactory, options);

        System.out.println("✅ Session Created!");
        System.out.println("   Session ID: " + driver.getSessionId());
        printDeviceInfo();
        System.out.println();
    }

    /**
     * Setup driver for session-based mode
     */
    protected void setupSessionDriver() throws Exception {
        validateSessionConfig();

        System.out.println("🔧 Connecting to Existing Session...");
        System.out.println("  Session ID: " + DEVICE_SESSION_ID);
        System.out.println();

        UiAutomator2Options options = new UiAutomator2Options();
        options.setPlatformName("Android");
        options.setAutomationName("UiAutomator2");
        options.setNoReset(true);
        options.setNewCommandTimeout(Duration.ofSeconds(300));

        options.setCapability("appium:X-Api-Key", API_KEY);
        options.setCapability("nativeBridge:sessionId", DEVICE_SESSION_ID);

        driver = new AndroidDriver(new URL(APPIUM_ENDPOINT), options);

        System.out.println("✅ Connected to Session!");
        System.out.println("   Session ID: " + driver.getSessionId());
        printDeviceInfo();
        System.out.println();
    }

    /**
     * Print device information
     */
    protected void printDeviceInfo() {
        try {
            String deviceName = (String) driver.getCapabilities().getCapability("deviceName");
            String platformVersion = (String) driver.getCapabilities().getCapability("platformVersion");
            System.out.println("   Device: " + (deviceName != null ? deviceName : "Unknown"));
            System.out.println("   Android: " + (platformVersion != null ? platformVersion : "Unknown"));
        } catch (Exception e) {
            // Ignore
        }
    }

    /**
     * Validate sessionless configuration
     */
    protected void validateSessionlessConfig() {
        boolean hasErrors = false;

        if (API_KEY == null || API_KEY.isEmpty()) {
            System.err.println("❌ ERROR: NATIVEBRIDGE_API_KEY not set!");
            hasErrors = true;
        }

        if (APP_ID == null || APP_ID.isEmpty()) {
            System.err.println("❌ ERROR: APP_ID not set!");
            hasErrors = true;
        }

        if (DEVICE_NAME == null || DEVICE_NAME.isEmpty()) {
            System.err.println("❌ ERROR: DEVICE_NAME not set!");
            hasErrors = true;
        }

        if (hasErrors) {
            throw new RuntimeException("Configuration errors detected. Please set required environment variables.");
        }
    }

    /**
     * Validate session-based configuration
     */
    protected void validateSessionConfig() {
        boolean hasErrors = false;

        if (API_KEY == null || API_KEY.isEmpty()) {
            System.err.println("❌ ERROR: NATIVEBRIDGE_API_KEY not set!");
            hasErrors = true;
        }

        if (DEVICE_SESSION_ID == null || DEVICE_SESSION_ID.isEmpty()) {
            System.err.println("❌ ERROR: DEVICE_SESSION_ID not set!");
            hasErrors = true;
        }

        if (hasErrors) {
            throw new RuntimeException("Configuration errors detected. Please set required environment variables.");
        }
    }

    /**
     * Teardown method - runs after each test method
     */
    @AfterMethod
    public void teardownDriver() {
        if (driver != null) {
            System.out.println("\n🔄 Cleaning Up...");
            try {
                driver.quit();
                System.out.println("✅ Session closed");

                if ("sessionless".equals(getTestMode())) {
                    System.out.println("✅ Device session auto-deleted (sessionless mode)");
                }
            } catch (Exception e) {
                System.out.println("⚠️  Cleanup completed with warnings: " + e.getMessage());
            }
        }
    }

    /**
     * Teardown method - runs after each test class
     */
    @AfterClass
    public void teardownClass() {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║              Test Class Completed                        ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        System.out.println();
    }
}
