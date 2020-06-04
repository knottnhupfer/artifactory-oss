package org.artifactory.engine;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.CoreAddons;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.common.ConstantValues;
import org.artifactory.fs.RepoResource;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.jfrog.storage.common.StorageUnit;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@Test
public class DownloadRedirectHelperTest extends ArtifactoryHomeBoundTest {

    @Mock
    AddonsManager addonsManager;

    @Mock
    CentralConfigService centralConfig;

    @BeforeClass
    public void init() {
        MockitoAnnotations.initMocks(this);
        CoreAddons coreAddons = mock(CoreAddons.class);
        when(coreAddons.isAol()).thenReturn(true);
        when(addonsManager.addonByType(CoreAddons.class)).thenReturn(coreAddons);
    }

    public void testThresholdWithoutChangingConstantValue() {
        final DownloadRedirectHelper downloadRedirectHelper = new DownloadRedirectHelper(addonsManager, centralConfig);
        RepoResource resource = mock(RepoResource.class);
        when(resource.getRepoPath()).thenReturn((RepoPathFactory.create("foo", "bar")));

        when(resource.getSize()).thenReturn(200L * 1024L);
        assertTrue(downloadRedirectHelper.isArtifactGreaterEqualsRedirectThreshold(resource));

        when(resource.getSize()).thenReturn(10L);
        assertFalse(downloadRedirectHelper.isArtifactGreaterEqualsRedirectThreshold(resource));

        when(resource.getSize()).thenReturn((long) StorageUnit.MB.toBytes(2));
        assertTrue(downloadRedirectHelper.isArtifactGreaterEqualsRedirectThreshold(resource));


    }

    public void testThresholdSmallerThanConstantValue() {
        final DownloadRedirectHelper downloadRedirectHelper = new DownloadRedirectHelper(addonsManager, centralConfig);

        setStringSystemProperty(ConstantValues.cloudBinaryProviderRedirectThresholdInBytes, "100");
        RepoResource resource = mock(RepoResource.class);
        when(resource.getRepoPath()).thenReturn((RepoPathFactory.create("foo", "bar")));

        when(resource.getSize()).thenReturn((long) StorageUnit.MB.toBytes(1));
        assertTrue(downloadRedirectHelper.isArtifactGreaterEqualsRedirectThreshold(resource));

        when(resource.getSize()).thenReturn(50L);
        assertFalse(downloadRedirectHelper.isArtifactGreaterEqualsRedirectThreshold(resource));

    }

}