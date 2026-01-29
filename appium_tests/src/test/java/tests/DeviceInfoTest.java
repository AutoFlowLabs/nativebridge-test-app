package tests;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Device Information Tests
 *
 * Tests for retrieving device capabilities and information
 */
public class DeviceInfoTest extends BaseTest {

    @Test(groups = {"regression"}, priority = 1,
          description = "Verify device capabilities can be retrieved")
    public void testGetDeviceCapabilities() {
        System.out.println("\n📱 Test: Get Device Capabilities");
        System.out.println("─────────────────────────────────────────────────────────────");

        String platformName = (String) driver.getCapabilities().getCapability("platformName");
        String deviceName = (String) driver.getCapabilities().getCapability("deviceName");
        String platformVersion = (String) driver.getCapabilities().getCapability("platformVersion");

        System.out.println("✓ Platform: " + platformName);
        System.out.println("✓ Device: " + deviceName);
        System.out.println("✓ Version: " + platformVersion);

        Assert.assertNotNull(platformName, "Platform name should not be null");
        Assert.assertEquals(platformName, "Android", "Platform should be Android");

        System.out.println("✅ Test Passed");
    }

    @Test(groups = {"regression"}, priority = 2,
          description = "Verify device orientation can be retrieved")
    public void testGetDeviceOrientation() {
        System.out.println("\n📐 Test: Get Device Orientation");
        System.out.println("─────────────────────────────────────────────────────────────");

        var orientation = driver.getOrientation();
        System.out.println("✓ Orientation: " + orientation);

        Assert.assertNotNull(orientation, "Orientation should not be null");

        System.out.println("✅ Test Passed");
    }

    @Test(groups = {"regression"}, priority = 3,
          description = "Verify session ID is available")
    public void testGetSessionId() {
        System.out.println("\n🔑 Test: Get Session ID");
        System.out.println("─────────────────────────────────────────────────────────────");

        String sessionId = driver.getSessionId().toString();
        System.out.println("✓ Session ID: " + sessionId);

        Assert.assertNotNull(sessionId, "Session ID should not be null");
        Assert.assertFalse(sessionId.isEmpty(), "Session ID should not be empty");

        System.out.println("✅ Test Passed");
    }
}
