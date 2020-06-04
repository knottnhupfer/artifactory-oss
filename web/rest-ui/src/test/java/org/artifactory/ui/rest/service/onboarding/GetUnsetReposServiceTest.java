package org.artifactory.ui.rest.service.onboarding;

import com.google.common.collect.Lists;
import org.artifactory.addon.ArtifactoryRunningMode;
import org.artifactory.addon.OssAddonsManager;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.ui.rest.model.onboarding.OnboardingRepoState;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author dudim
 */
public class GetUnsetReposServiceTest {

    @Test
    public void testGetSupportedRepoTypesMapJcrType() {
        OssAddonsManager addonsManagerSpy = Mockito.spy(new OssAddonsManager());
        GetUnsetReposService getUnsetReposService = new GetUnsetReposService(null, addonsManagerSpy);
        Mockito.doReturn(ArtifactoryRunningMode.JCR).when(addonsManagerSpy).getArtifactoryRunningMode();
        Map<RepoType, OnboardingRepoState> supportedRepoTypesMap = getUnsetReposService.getSupportedRepoTypesMap();
        Assert.assertEquals(supportedRepoTypesMap.get(RepoType.Generic), OnboardingRepoState.UNSET);
        Assert.assertEquals(supportedRepoTypesMap.get(RepoType.Docker), OnboardingRepoState.UNSET);
        Assert.assertEquals(supportedRepoTypesMap.get(RepoType.Helm), OnboardingRepoState.UNSET);
        List<RepoType> repoTypesList = new ArrayList<>(Arrays.asList(RepoType.values()));
        repoTypesList.removeAll(Lists.newArrayList(RepoType.Helm, RepoType.Docker, RepoType.Generic));
        repoTypesList.forEach(repoType -> Assert.assertNotEquals(repoType, OnboardingRepoState.UNSET));
    }

    @Test
    public void testGetSupportedRepoTypesMapConanType() {
        OssAddonsManager addonsManagerSpy = Mockito.spy(new OssAddonsManager());
        GetUnsetReposService getUnsetReposService = new GetUnsetReposService(null, addonsManagerSpy);
        Mockito.doReturn(ArtifactoryRunningMode.CONAN).when(addonsManagerSpy).getArtifactoryRunningMode();
        Map<RepoType, OnboardingRepoState> supportedRepoTypesMap = getUnsetReposService.getSupportedRepoTypesMap();
        Assert.assertEquals(supportedRepoTypesMap.get(RepoType.Conan), OnboardingRepoState.UNSET);
        List<RepoType> repoTypesList = new ArrayList<>(Arrays.asList(RepoType.values()));
        repoTypesList.removeAll(Lists.newArrayList(RepoType.Conan));
        repoTypesList.forEach(repoType -> Assert.assertNotEquals(repoType, OnboardingRepoState.UNSET));
    }
}