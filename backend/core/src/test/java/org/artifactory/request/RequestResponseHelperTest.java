package org.artifactory.request;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author Shay Bagants
 */
@Test
public class RequestResponseHelperTest {

    @Test(dataProvider = "provideUserNames")
    public void testIsXrayUser(String userName, boolean expectedResult) {
        boolean result = RequestResponseHelper.isXrayUser(userName);
        Assert.assertEquals(result, expectedResult);
    }

    @DataProvider
    public static Object[][] provideUserNames() {
        return new Object[][]{
                //agent, user, expectedResult
                {"token:jfxr@123", true},
                {"xray", true},
                {"token:jfrt@123", false},
                {"token:xyz@123", false},
                {"token:abc", false},
                {"xday", false},
                {"admin", false},
                {"system", false},
                {"me", false}
        };
    }
}