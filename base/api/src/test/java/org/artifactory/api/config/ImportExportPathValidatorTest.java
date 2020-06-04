package org.artifactory.api.config;

import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author Hezi Cohen
 */
public class ImportExportPathValidatorTest extends ArtifactoryHomeBoundTest {

    @DataProvider
    public Object[][] pathsToValidate() {
        return new Object[][]{
                {"\\\\127.0.0.1\\my_dir", false},
                {"${shared_drive}/my_dir", false},
                {"//my_dir", false},
                {"//shared_drive\\my_dir", false},
                {"/shared_drive\\my_dir", true},
                {"/shared_drive/my_dir", true},
                {"shared_drive\\my_dir", true},
                {"shared_drive/${another_dir}", true}
        };
    }

    @Test(dataProvider = "pathsToValidate")
    public void testIsValidPath(String pathToValidate, boolean isValid) {
        Assert.assertEquals(ImportExportPathValidator.isValidPath(pathToValidate), isValid);
    }
}
