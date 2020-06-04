package org.artifactory.addon.web;

import com.google.common.collect.Lists;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.license.ArtifactoryBaseLicenseDetails;
import org.artifactory.api.context.ArtifactoryContextThreadBinder;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.spring.ArtifactoryApplicationContext;
import org.testng.Assert;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Omri Ziv
 */
public class WebAddonsImplTest {

    private WebAddonsImpl webAddons = new WebAddonsImpl();
    @Mock
    private ArtifactoryApplicationContext artifactoryContext;
    @Mock
    private AddonsManager addonsManager;

    @BeforeClass
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ArtifactoryContextThreadBinder.bind(artifactoryContext);
        when(ContextHelper.get().beanForType(AddonsManager.class)).thenReturn(addonsManager);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testValidateLicenseJCR() {
        webAddons.validateTargetHasDifferentLicenseKeyHash("JFrog Container Registry",
                Lists.newArrayList("docker", "helm", "generic"));
        Assert.fail();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testValidateLicenseOSS() {
        webAddons.validateTargetHasDifferentLicenseKeyHash("Artifactory OSS",
                Lists.newArrayList("addon1"));
        Assert.fail();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testValidateLicenseConan() {
        webAddons.validateTargetHasDifferentLicenseKeyHash("Artifactory Community Edition for C/C++",
                Lists.newArrayList("addon1"));
        Assert.fail();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testValidateLicenseBlank() {
        webAddons.validateTargetHasDifferentLicenseKeyHash(null,
                Lists.newArrayList("not-replication-error"));
        Assert.fail();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testValidateLicenseNullReplicationAddon() {
        webAddons.validateTargetHasDifferentLicenseKeyHash(null,
                Lists.newArrayList("replication"));
        Assert.fail();
    }

    @Test
    public void testValidateTrial() {
        when(addonsManager.isLicenseInstalled()).thenReturn(true);
        ArtifactoryBaseLicenseDetails artifactoryBaseLicenseDetailsMock = mock(ArtifactoryBaseLicenseDetails.class);
        when(addonsManager.getProAndAolLicenseDetails()).thenReturn(artifactoryBaseLicenseDetailsMock);
        when(artifactoryBaseLicenseDetailsMock.getType()).thenReturn("Trial");
        webAddons.validateTargetHasDifferentLicenseKeyHash("Trial",
                Lists.newArrayList("addon1"));
    }

    @Test
    public void testValidateDifferentLicense() {
        when(addonsManager.getLicenseKeyHash(false)).thenReturn("alicence");
        webAddons.validateTargetHasDifferentLicenseKeyHash("blicence",
                Lists.newArrayList("addon1"));

    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testValidateSameLicense() {
        when(addonsManager.getLicenseKeyHash(false)).thenReturn("alicence");
        webAddons.validateTargetHasDifferentLicenseKeyHash("alicence",
                Lists.newArrayList("addon1"));
        Assert.fail();
    }

}
