/*
 *
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2018 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.artifactory.ui.rest.service.artifacts.search.versionsearch;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.http.HttpStatus;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.xray.XrayAddon;
import org.artifactory.addon.xray.XrayArtifactSummary;
import org.artifactory.addon.xray.XrayArtifactsSummary;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ArtifactoryContextThreadBinder;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.aql.AqlService;
import org.artifactory.aql.api.internal.AqlBase;
import org.artifactory.aql.model.AqlComparatorEnum;
import org.artifactory.aql.model.AqlFieldEnum;
import org.artifactory.aql.model.AqlPhysicalFieldEnum;
import org.artifactory.aql.result.AqlEagerResult;
import org.artifactory.aql.result.rows.AqlBaseFullRowImpl;
import org.artifactory.aql.result.rows.AqlItem;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.property.ArtifactorySystemProperties;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.fs.FileInfo;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.ArtifactoryRestResponse;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.ui.rest.model.artifacts.search.PackageNativeXraySummaryModel;
import org.artifactory.ui.rest.model.artifacts.search.VersionsNativeModel;
import org.artifactory.ui.rest.model.artifacts.search.packagesearch.search.AqlUISearchModel;
import org.artifactory.ui.rest.model.artifacts.search.versionsearch.result.VersionNativeModel;
import org.artifactory.ui.rest.service.artifacts.search.packagesearch.util.NativeExtraInfoHelper;
import org.artifactory.ui.rest.service.artifacts.search.packagesearch.util.PackageNativeSearchHelper;
import org.jfrog.security.util.Pair;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.artifactory.ui.rest.service.artifacts.search.packagesearch.util.PackageNativeRestConstants.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

/**
 * @author Nadav Yogev
 */
public class VersionNativeListSearchServiceRequestTest {

    @Mock
    private AqlService aqlService;

    @Mock
    private AddonsManager addonsManager;

    @Mock
    private ArtifactoryContext artifactoryContext;

    @Mock
    private AuthorizationService authorizationService;

    @Mock
    private RepositoryService repositoryService;

    @Mock
    private XrayAddon xrayAddon;

    @Mock
    private
    NativeExtraInfoHelper nativeExtraInfoHelper;

    private VersionNativeListSearchService versionNativeDockerListSearchService;


    @BeforeClass
    public void init() {
        MockitoAnnotations.initMocks(this);
        when(artifactoryContext.getAuthorizationService()).thenReturn(authorizationService);
        when(authorizationService.canRead(any())).thenReturn(true);
        when(artifactoryContext.getRepositoryService()).thenReturn(repositoryService);
        when(repositoryService.distributionRepoDescriptorByKey(any())).thenReturn(null);
        addXraySummary();
        ArtifactoryContextThreadBinder.bind(artifactoryContext);

        AqlEagerResult result = mock(AqlEagerResult.class);
        List<AqlItem> results = Lists.newArrayList();
        results.add(createAqlItem("path", "name", new Date(2000)));
        results.add(createAqlItem("path2", "name2", new Date(1000)));
        when(result.getResults()).thenReturn(results);
        when(aqlService.executeQueryEager(any(AqlBase.class))).thenReturn(result);

        ArtifactoryHome home = mock(ArtifactoryHome.class);
        ArtifactorySystemProperties props = mock(ArtifactorySystemProperties.class);
        when(home.getArtifactoryProperties()).thenReturn(props);
        when(props.getLongProperty(ConstantValues.searchMaxResults)).thenReturn(500L);
        ArtifactoryHome.bind(home);


        versionNativeDockerListSearchService = new VersionNativeListSearchService(
                new PackageNativeSearchHelper(aqlService), addonsManager, repositoryService, nativeExtraInfoHelper);
    }

    @Test
    public void testWithoutXray() {
        Pair<ArtifactoryRestRequest, RestResponse> requestAndResponse = getRequestAndResponse(false);
        RestResponse response = requestAndResponse.getSecond();
        versionNativeDockerListSearchService.execute(requestAndResponse.getFirst(), response);
        assertEquals(response.getResponseCode(), HttpStatus.SC_OK);
        List<VersionNativeModel> results = ((VersionsNativeModel) response.getIModel()).getResults();
        assertEquals(results.size(), 1);
        assertEquals(results.get(0).getName(), "v1");
        assertEquals(results.get(0).getLatestPath(), "repo/path/name");
        assertEquals(results.get(0).getRepositories().size(), 1);
        assertTrue(results.get(0).getRepositories().contains("repo"));
        assertNull(results.get(0).getXrayViolations());
    }

    @Test
    public void testWithXray() {
        Pair<ArtifactoryRestRequest, RestResponse> requestAndResponse = getRequestAndResponse(true);
        RestResponse response = requestAndResponse.getSecond();
        versionNativeDockerListSearchService.execute(requestAndResponse.getFirst(), response);
        assertEquals(response.getResponseCode(), HttpStatus.SC_OK);
        List<VersionNativeModel> results = ((VersionsNativeModel) response.getIModel()).getResults();
        assertEquals(results.size(), 1);
        assertEquals(results.get(0).getName(), "v1");
        assertEquals(results.get(0).getLatestPath(), "repo/path/name");
        assertEquals(results.get(0).getRepositories().size(), 1);
        assertTrue(results.get(0).getRepositories().contains("repo"));
        PackageNativeXraySummaryModel xrayViolations = results.get(0).getXrayViolations();
        assertNotNull(xrayViolations);
        assertEquals(xrayViolations.getDetailsUrl(), "https://www.youtube.com/watch?v=dQw4w9WgXcQ");
        assertEquals(xrayViolations.getXrayStatus(), "CRITICAL");
        assertEquals(xrayViolations.getVersion(), "v1");
        assertEquals(xrayViolations.getViolations().size(), 1);
        Map<String, Integer> deMinimisViolations = xrayViolations.getViolations().get("De Minimis");
        assertEquals(deMinimisViolations.size(), 2);
        assertEquals(deMinimisViolations.get("Critical").intValue(), 2);
        assertEquals(deMinimisViolations.get("Minimal").intValue(), 3);
    }

    private Pair<ArtifactoryRestRequest, RestResponse> getRequestAndResponse(boolean withXray) {
        ArtifactoryRestRequest request = mock(ArtifactoryRestRequest.class);
        when(request.getPathParamByKey(TYPE)).thenReturn(RepoType.Npm.getType());
        when(request.getQueryParamByKey(SORT_BY)).thenReturn("lastModified");
        when(request.getQueryParamByKey(ORDER)).thenReturn("ASC");
        when(request.getQueryParamByKey(LIMIT)).thenReturn("100");
        when(request.getQueryParamByKey(WITH_XRAY)).thenReturn(String.valueOf(withXray));
        List<AqlUISearchModel> searches = Lists.newArrayList();
        searches.add(new AqlUISearchModel("npmName", AqlComparatorEnum.matches, Lists.newArrayList("pkg1")));
        when(request.getModels()).thenReturn(searches);
        RestResponse response = new ArtifactoryRestResponse();
        return new Pair<>(request, response);
    }

    private AqlBaseFullRowImpl createAqlItem(String path, String name, Date date) {
        HashMap<AqlFieldEnum, Object> itemMap = Maps.newHashMap();
        itemMap.put(AqlPhysicalFieldEnum.itemRepo, "repo");
        itemMap.put(AqlPhysicalFieldEnum.itemPath, path);
        itemMap.put(AqlPhysicalFieldEnum.itemName, name);
        itemMap.put(AqlPhysicalFieldEnum.itemModified, date);
        itemMap.put(AqlPhysicalFieldEnum.propertyKey, "npm.version");
        itemMap.put(AqlPhysicalFieldEnum.propertyValue, "v1");
        return new AqlBaseFullRowImpl(itemMap);
    }

    private void addXraySummary() {
        FileInfo fileInfo = mock(FileInfo.class);
        when(fileInfo.getSha2()).thenReturn("SHA256");
        when(repositoryService.getFileInfo(any())).thenReturn(fileInfo);
        when(addonsManager.addonByType(XrayAddon.class)).thenReturn(xrayAddon);
        when(xrayAddon.isXrayEnabled()).thenReturn(true);
        XrayArtifactsSummary xraySummary = mock(XrayArtifactsSummary.class);
        XrayArtifactSummary xrayArtifactSummary = mock(XrayArtifactSummary.class);
        when(xrayArtifactSummary.getStatus()).thenReturn("CRITICAL");
        when(xrayArtifactSummary.getDetailsUrl()).thenReturn("https://www.youtube.com/watch?v=dQw4w9WgXcQ");
        HashMap<String, Map<String, Integer>> violations = Maps.newHashMap();
        HashMap<String, Integer> severity = Maps.newHashMap();
        severity.put("Minimal", 3);
        severity.put("Critical", 2);
        violations.put("De Minimis", severity);
        when(xrayArtifactSummary.getViolations()).thenReturn(violations);
        when(xraySummary.getVersions()).thenReturn(Lists.newArrayList(xrayArtifactSummary));
        doReturn(xraySummary).when(xrayAddon)
                .getArtifactXraySummary(eq("SHA256"), anyList(), eq("v1"),
                        eq("npm"), eq(true));
    }


}