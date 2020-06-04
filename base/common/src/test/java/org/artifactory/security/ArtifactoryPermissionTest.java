package org.artifactory.security;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Inbar Tal
 */

public class ArtifactoryPermissionTest {

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testFromDisplayNameInvalid() {
        ArtifactoryPermission.fromDisplayName("notValidAction");
    }

    @Test
    public void testFromDisplayNameValid() {
        Assert.assertEquals(ArtifactoryPermission.fromDisplayName("read"), ArtifactoryPermission.READ);
        Assert.assertEquals(ArtifactoryPermission.fromDisplayName("write"), ArtifactoryPermission.DEPLOY);
        Assert.assertEquals(ArtifactoryPermission.fromDisplayName("annotate"), ArtifactoryPermission.ANNOTATE);
        Assert.assertEquals(ArtifactoryPermission.fromDisplayName("delete"), ArtifactoryPermission.DELETE);
        Assert.assertEquals(ArtifactoryPermission.fromDisplayName("manage"), ArtifactoryPermission.MANAGE);
        Assert.assertEquals(ArtifactoryPermission.fromDisplayName("distribute"), ArtifactoryPermission.DISTRIBUTE);
        Assert.assertEquals(ArtifactoryPermission.fromDisplayName("managedXrayMeta"),
                ArtifactoryPermission.MANAGED_XRAY_META);
        Assert.assertEquals(ArtifactoryPermission.fromDisplayName("managedXrayWatches"),
                ArtifactoryPermission.MANAGED_XRAY_WATCHES);
        Assert.assertEquals(ArtifactoryPermission.fromDisplayName("managedXrayPolicies"),
                ArtifactoryPermission.MANAGED_POLICIES);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testFromStringInvalid() {
        ArtifactoryPermission.fromString("imInvalidString");
    }

    @Test
    public void testFromStringValid() {
        Assert.assertEquals(ArtifactoryPermission.fromString("r"), ArtifactoryPermission.READ);
        Assert.assertEquals(ArtifactoryPermission.fromString("w"), ArtifactoryPermission.DEPLOY);
        Assert.assertEquals(ArtifactoryPermission.fromString("n"), ArtifactoryPermission.ANNOTATE);
        Assert.assertEquals(ArtifactoryPermission.fromString("d"), ArtifactoryPermission.DELETE);
        Assert.assertEquals(ArtifactoryPermission.fromString("m"), ArtifactoryPermission.MANAGE);
        Assert.assertEquals(ArtifactoryPermission.fromString("x"), ArtifactoryPermission.DISTRIBUTE);
        Assert.assertEquals(ArtifactoryPermission.fromString("mxm"),ArtifactoryPermission.MANAGED_XRAY_META);
        Assert.assertEquals(ArtifactoryPermission.fromString("mxw"),ArtifactoryPermission.MANAGED_XRAY_WATCHES);
        Assert.assertEquals(ArtifactoryPermission.fromString("mxp"),ArtifactoryPermission.MANAGED_POLICIES);
    }
}
