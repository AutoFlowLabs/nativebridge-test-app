package tests;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * App Launch Tests
 *
 * Tests basic app launch and device connectivity
 */
public class AppLaunchTest extends BaseTest {

    @Test(groups = {"smoke", "regression"}, priority = 1,
          description = "Verify that app package can be retrieved")
    public void testVerifyAppPackage() {
        System.out.println("\n📦 Test: Verify App Package");
        System.out.println("─────────────────────────────────────────────────────────────");

        String currentPackage = driver.getCurrentPackage();
        System.out.println("✓ Current Package: " + currentPackage);

        Assert.assertNotNull(currentPackage, "App package should not be null");
        Assert.assertFalse(currentPackage.isEmpty(), "App package should not be empty");

        System.out.println("✅ Test Passed");
    }

    @Test(groups = {"smoke", "regression"}, priority = 2,
          description = "Verify that current activity can be retrieved")
    public void testVerifyCurrentActivity() {
        System.out.println("\n🎯 Test: Verify Current Activity");
        System.out.println("─────────────────────────────────────────────────────────────");

        String currentActivity = driver.currentActivity();
        System.out.println("✓ Current Activity: " + currentActivity);

        Assert.assertNotNull(currentActivity, "Current activity should not be null");
        Assert.assertFalse(currentActivity.isEmpty(), "Current activity should not be empty");

        System.out.println("✅ Test Passed");
    }

    @Test(groups = {"smoke", "regression"}, priority = 3,
          description = "Verify screen dimensions")
    public void testGetScreenSize() {
        System.out.println("\n📐 Test: Get Screen Size");
        System.out.println("─────────────────────────────────────────────────────────────");

        var size = driver.manage().window().getSize();
        System.out.println("✓ Screen: " + size.width + "x" + size.height);

        Assert.assertTrue(size.width > 0, "Screen width should be greater than 0");
        Assert.assertTrue(size.height > 0, "Screen height should be greater than 0");

        System.out.println("✅ Test Passed");
    }
}
