package org.artifactory.addon;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

/**
 * @author Shay Bagants
 */
@Test
public class OssAddonsManagerTest {

    @Test(dataProvider = "provideAddonsState")
    public void testIsAddonSupported(boolean isConan, boolean isJcr) {
        OssAddonsManager ossAddonsManagerSpy = spy(new OssAddonsManager());
        doReturn(isConan).when(ossAddonsManagerSpy).isConanCEVersion();
        doReturn(isJcr).when(ossAddonsManagerSpy).isJcrVersion();

        Arrays.stream(AddonType.values())
                .filter(addon -> filterExpectedSupportedAddons(isConan, isJcr, addon))
                .forEach(addonType -> {
                    System.out.println("Conan: " + isConan + " Jcr: " + isJcr + " Testing addon: " + addonType);
                    boolean addonSupported = ossAddonsManagerSpy.isAddonSupported(addonType);
                    assertTrue(addonSupported);
                });

        Arrays.stream(AddonType.values())
                .filter(addon -> !filterExpectedSupportedAddons(isConan, isJcr, addon))
                .forEach(addonType -> {
                    System.out.println("Conan: " + isConan + " Jcr: " + isJcr + " Testing addon: " + addonType);
                    boolean addonSupported = ossAddonsManagerSpy.isAddonSupported(addonType);
                    assertFalse(addonSupported);
                });
    }

    private boolean filterExpectedSupportedAddons(boolean isConan, boolean isJcr, AddonType addon) {
        if (isConan) {
            return addon == AddonType.CONAN;
        } else if (isJcr) {
            return addon == AddonType.DOCKER || addon == AddonType.HELM || addon == AddonType.PROPERTIES
                    || addon == AddonType.SMART_REPO || addon == AddonType.S3 || addon == AddonType.DISTRIBUTION
                    || addon == AddonType.AQL || addon == AddonType.AOL;
        }
        return false;
    }

    @DataProvider
    public static Object[][] provideAddonsState() {
        return new Object[][]{
                // isConan, isJcr
                {true, false},
                {false, true},
                {false, false}
        };
    }
}