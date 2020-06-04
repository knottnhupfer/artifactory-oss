package org.artifactory.rest.common.service;

import org.artifactory.addon.AddonsManager;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.delegation.ContentSynchronisation;
import org.artifactory.descriptor.repo.HttpRepoDescriptor;
import org.artifactory.features.matrix.SmartRepoVersionFeatures;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Dan Feldman
 */
public class ResearchServiceImplTest {

    @Mock SmartRepoVersionFeatures versionFeatures;
    @Mock CentralConfigService configService;
    @Mock AddonsManager addonsManager;

    private ResearchServiceImpl researchService;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
         researchService = new ResearchServiceImpl();
        researchService.setSmartRepoVersionFeatures(versionFeatures);
        researchService.setConfigService(configService);
        researchService.setAddonsManager(addonsManager);
    }

    @Test
    public void syncPropsDisabledWhenNoContentSyncAndNoPropSync() {
        HttpRepoDescriptor descriptor = new HttpRepoDescriptor();
        descriptor.setSynchronizeProperties(false);
        descriptor.setContentSynchronisation(null);
        assertFalse(researchService.isRepoConfiguredToSyncProperties(descriptor));
    }

    @Test
    public void syncPropsDisabledWhenContentSyncPresentAndDisabledAndNoPropSync() {
        HttpRepoDescriptor descriptor = new HttpRepoDescriptor();
        descriptor.setSynchronizeProperties(false);
        ContentSynchronisation contentSynchronisation = new ContentSynchronisation();
        contentSynchronisation.setEnabled(false);
        descriptor.setContentSynchronisation(contentSynchronisation);
        assertFalse(researchService.isRepoConfiguredToSyncProperties(descriptor));
    }

    @Test
    public void syncPropsDisabledWhenContentSyncEnabledAndContentPropSyncDisabledAndPropSyncDisabled() {
        HttpRepoDescriptor descriptor = new HttpRepoDescriptor();
        descriptor.setSynchronizeProperties(false);
        ContentSynchronisation contentSynchronisation = new ContentSynchronisation();
        contentSynchronisation.setEnabled(true);
        contentSynchronisation.getProperties().setEnabled(false);
        descriptor.setContentSynchronisation(contentSynchronisation);
        assertFalse(researchService.isRepoConfiguredToSyncProperties(descriptor));
    }

    @Test
    public void syncPropsEnabledWhenNoContentSyncAndPropSyncEnabled() {
        HttpRepoDescriptor descriptor = new HttpRepoDescriptor();
        descriptor.setSynchronizeProperties(true);
        descriptor.setContentSynchronisation(null);
        assertTrue(researchService.isRepoConfiguredToSyncProperties(descriptor));
    }

    @Test
    public void syncPropsEnabledWhenContentSyncEnabledAndContentPropSyncEnabledAndPropSyncDisabled() {
        HttpRepoDescriptor descriptor = new HttpRepoDescriptor();
        descriptor.setSynchronizeProperties(false);
        ContentSynchronisation contentSynchronisation = new ContentSynchronisation();
        contentSynchronisation.setEnabled(true);
        contentSynchronisation.getProperties().setEnabled(true);
        descriptor.setContentSynchronisation(contentSynchronisation);
        assertTrue(researchService.isRepoConfiguredToSyncProperties(descriptor));
    }

}