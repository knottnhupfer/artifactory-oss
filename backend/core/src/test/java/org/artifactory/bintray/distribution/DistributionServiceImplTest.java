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

package org.artifactory.bintray.distribution;

import org.artifactory.api.bintray.distribution.reporting.DistributionReporter;
import org.artifactory.api.context.ArtifactoryContextThreadBinder;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.aql.AqlService;
import org.artifactory.aql.action.AqlAction;
import org.artifactory.aql.api.AqlApiElement;
import org.artifactory.aql.api.domain.sensitive.AqlApiItem;
import org.artifactory.aql.api.internal.AqlBase;
import org.artifactory.aql.model.AqlFieldEnum;
import org.artifactory.aql.model.AqlItemTypeEnum;
import org.artifactory.aql.model.AqlPhysicalFieldEnum;
import org.artifactory.aql.model.DomainSensitiveField;
import org.artifactory.aql.result.AqlEagerResult;
import org.artifactory.aql.result.rows.AqlBaseFullRowImpl;
import org.artifactory.aql.result.rows.AqlItem;
import org.artifactory.aql.result.rows.AqlRowResult;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RepoLayoutBuilder;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.md.Properties;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.spring.ArtifactoryApplicationContext;
import org.mockito.ArgumentMatcher;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.*;

import static org.artifactory.mime.DockerNaming.MANIFEST_FILENAME;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * @author Uriah Levy
 */
public class DistributionServiceImplTest {
    private static final String DOCKER_REPO = "docker-local";
    private static final String BUSYBOX_LATEST = "busybox/latest";

    @Mock
    private InternalRepositoryService repositoryService;

    @Mock
    private AqlService aqlService;

    @Mock
    private ArtifactoryApplicationContext applicationContext;

    @Mock
    private AuthorizationService authorizationService;

    @BeforeClass
    public void doInit() {
        MockitoAnnotations.initMocks(this);
        ArtifactoryContextThreadBinder.bind(applicationContext);
        initStubs();
    }

    private void initStubs() {
        LocalRepoDescriptor dockerLocal = new LocalRepoDescriptor();
        dockerLocal.setType(RepoType.Docker);
        RepoLayoutBuilder layoutBuilder = new RepoLayoutBuilder();
        layoutBuilder.name("docker-default").
                artifactPathPattern("");
        dockerLocal.setRepoLayout(layoutBuilder.build());
        when(repositoryService.repoDescriptorByKey(DOCKER_REPO)).thenReturn(dockerLocal);
        ManifestAqlEagerResultStub manifestResults = new ManifestAqlEagerResultStub();
        PropsAqlEagerResultStub propsResults = new PropsAqlEagerResultStub();
        when(aqlService.executeQueryEager(ArgumentMatchers.argThat(new ManifestsAqlApiItem()))).thenReturn(manifestResults);
        when(aqlService.executeQueryEager(ArgumentMatchers.argThat(new PropsAqlApiItem()))).thenReturn(propsResults);
        when(applicationContext.getAuthorizationService()).thenReturn(authorizationService);
        when(authorizationService.canRead(any())).thenReturn(true);
        when(repositoryService.localOrCachedRepoDescriptorByKey(DOCKER_REPO)).thenReturn(dockerLocal);
    }

    @Test
    public void testGetPathInformation() {
        DistributionServiceImpl distributionService = new DistributionServiceImpl(repositoryService, aqlService);
        List<RepoPath> paths = new ArrayList<>();
        // given two searchable paths
        paths.add(RepoPathFactory.create(DOCKER_REPO, BUSYBOX_LATEST + "/**/*"));
        paths.add(RepoPathFactory.create(DOCKER_REPO, BUSYBOX_LATEST + "/*"));
        Map<RepoPath, Properties> pathInformation = distributionService.getPathInformation(paths, null, new DistributionReporter(false));
        RepoPath manifestPath = RepoPathFactory.create(DOCKER_REPO, BUSYBOX_LATEST + "/" + MANIFEST_FILENAME);
        // expect the service to find exactly one matching path
        Assert.assertEquals(1, pathInformation.size());
        Assert.assertNotNull(pathInformation.get(manifestPath));
    }

    @Test
    public void testGetPathInformationExplicitPath() {
        DistributionServiceImpl distributionService = new DistributionServiceImpl(repositoryService, aqlService);
        List<RepoPath> paths = new ArrayList<>();
        RepoPath manifestPath = RepoPathFactory.create(DOCKER_REPO, BUSYBOX_LATEST + "/" + MANIFEST_FILENAME);
        // given one explicit path
        paths.add(manifestPath);
        Map<RepoPath, Properties> pathInformation = distributionService.getPathInformation(paths, null, new DistributionReporter(false));
        // expect the service to find exactly one matching path
        Assert.assertEquals(1, pathInformation.size());
        Assert.assertNotNull(pathInformation.get(manifestPath));
    }


    /**
     * An ArgumentMatcher to match the first manifest.json query
     */
    class ManifestsAqlApiItem implements ArgumentMatcher<AqlApiItem> {

        @Override
        public boolean matches(AqlApiItem argument) {
            if (argument != null) {
                for (AqlApiElement aqlApiElement : argument.get()) {
                    if (aqlApiElement instanceof AqlBase.FilterApiElement) {
                        AqlApiElement filterElement = ((AqlBase.FilterApiElement) aqlApiElement).getFilter();
                        return filterElement.get().size() == 3;
                    }
                }
            }
            return false;
        }
    }

    /**
     * An ArgumentMatcher to match the second manifest.json query that includes item properties
     */
    class PropsAqlApiItem implements ArgumentMatcher<AqlApiItem> {

        @Override
        public boolean matches(AqlApiItem argument) {
            if (argument != null) {
                for (AqlApiElement aqlApiElement : argument.get()) {
                    if (aqlApiElement instanceof AqlBase.IncludeApiElement) {
                        List<DomainSensitiveField> includeElements = ((AqlBase.IncludeApiElement) aqlApiElement)
                                .getIncludeFields();
                        for (DomainSensitiveField field : includeElements) {
                            if (field.toString().endsWith("property.key")) {
                                return true;
                            }
                        }
                    }
                }
            }
            return false;
        }
    }

    class ManifestAqlEagerResultStub implements AqlEagerResult {

        @Override
        public int getSize() {
            return 0;
        }

        @Override
        public AqlRowResult getResult(int j) {
            return null;
        }

        @Override
        public List getResults() {
            List<AqlItemMock> results = new ArrayList<>();
            results.add(new AqlItemMock(DOCKER_REPO, BUSYBOX_LATEST, MANIFEST_FILENAME));
            return results;
        }

        @Override
        public AqlAction getAction() {
            return null;
        }
    }

    class PropsAqlEagerResultStub implements AqlEagerResult {


        PropsAqlEagerResultStub() {

        }

        @Override
        public int getSize() {
            return 0;
        }

        @Override
        public AqlRowResult getResult(int j) {
            return null;
        }

        @Override
        public List getResults() {
            List<AqlBaseFullRowImpl> results = new ArrayList<>();
            Map<AqlFieldEnum, Object> propFieldsMap = new HashMap<>();
            propFieldsMap.put(AqlPhysicalFieldEnum.itemRepo, "docker-local");
            propFieldsMap.put(AqlPhysicalFieldEnum.itemPath, BUSYBOX_LATEST);
            propFieldsMap.put(AqlPhysicalFieldEnum.itemName, MANIFEST_FILENAME);
            AqlBaseFullRowImpl row = new AqlBaseFullRowImpl(propFieldsMap);
            results.add(row);
            return results;
        }

        @Override
        public AqlAction getAction() {
            return null;
        }
    }

    class AqlItemMock implements AqlItem {

        private String repo;
        private String path;
        private String name;

        AqlItemMock(String repo, String path, String name) {
            this.repo = repo;
            this.path = path;
            this.name = name;
        }

        @Override
        public Date getCreated() {
            return null;
        }

        @Override
        public Date getModified() {
            return null;
        }

        @Override
        public Date getUpdated() {
            return null;
        }

        @Override
        public String getCreatedBy() {
            return null;
        }

        @Override
        public String getModifiedBy() {
            return null;
        }

        @Override
        public AqlItemTypeEnum getType() {
            return null;
        }

        @Override
        public String getRepo() {
            return repo;
        }

        @Override
        public String getPath() {
            return path;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public long getSize() {
            return 0;
        }

        @Override
        public int getDepth() {
            return 0;
        }

        @Override
        public Long getNodeId() {
            return null;
        }

        @Override
        public String getOriginalMd5() {
            return null;
        }

        @Override
        public String getActualMd5() {
            return null;
        }

        @Override
        public String getOriginalSha1() {
            return null;
        }

        @Override
        public String getActualSha1() {
            return null;
        }

        @Override
        public String getSha2() {
            return null;
        }

        @Override
        public String[] getVirtualRepos() {
            return new String[0];
        }
    }
}
