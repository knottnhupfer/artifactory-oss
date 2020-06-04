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

package org.artifactory.rest.common.util.build.distribution;

import com.google.common.collect.ImmutableList;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.repo.RepoPath;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.artifactory.mime.DockerNaming.MANIFEST_FILENAME;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.*;

/**
 * @author Dan Feldman
 */
@Test
public class DockerBuildPathsDistributionFilterTest {

    private static final String DOCKER_LOCAL = "docker-local";
    private static final String GENERIC_LOCAL = "generic-local";
    private static final String MAVEN_LOCAL = "maven-local";

    private static final RepoPath DOCKER_MANIFEST_PATH = mockPath(DOCKER_LOCAL, "my-image/my-tag/2.0/" + MANIFEST_FILENAME);
    private static final RepoPath DOCKER_LAYER_PATH = mockPath(DOCKER_LOCAL, "my-image/my-tag/sha256__8ac48589692a53a9b8c2d1ceaa6b402665aa7fe667ba51ccc03002300856d8c7");
    private static final RepoPath GENERIC_PATH = mockPath(GENERIC_LOCAL, "path/to/arti.fact");
    private static final RepoPath MAVEN_PATH = mockPath(MAVEN_LOCAL, "my/awesome/1.0/myAwesome-1.0.pom");

    private static final List<RepoPath> DOCKER_REPO_PATHS = ImmutableList.of(
            DOCKER_MANIFEST_PATH,
            mockPath(DOCKER_LOCAL, "my-image/my-tag/sha256__8ac48589692a53a9b8c2d1ceaa6b402665aa7fe667ba51ccc03002300856d8c7"),
            mockPath(DOCKER_LOCAL, "my-image/my-tag/sha256__8ac48589692a53a9b8c2d1ceaa6b402665aa7fe667ba51ccc0300230085123cf"),
            mockPath(DOCKER_LOCAL, "my-image/my-tag/sha256__8ac48589692a53a9b8c2d1ceaa6b402665aa7fe667ba51ccc0300230085686fa")
    );

    private static final List<RepoPath> MIXED_REPO_PATHS = ImmutableList.of(DOCKER_MANIFEST_PATH, DOCKER_LAYER_PATH, GENERIC_PATH, MAVEN_PATH);

    @Mock
    private RepositoryService repoService;
    private DockerBuildPathsDistributionFilter filter;

    @BeforeClass
    public void setUp() {
        initMocks(this);
        filter = new DockerBuildPathsDistributionFilter();
        LocalRepoDescriptor dockerDescriptor = new LocalRepoDescriptor() {{
            setKey(DOCKER_LOCAL);
            setType(RepoType.Docker);
        }};
        LocalRepoDescriptor genericDescriptor = new LocalRepoDescriptor() {{
            setKey(GENERIC_LOCAL);
            setType(RepoType.Generic);
        }};
        LocalRepoDescriptor mavenDescriptor = new LocalRepoDescriptor() {{
            setKey(MAVEN_LOCAL);
            setType(RepoType.Maven);
        }};
        when(repoService.repoDescriptorByKey(DOCKER_LOCAL)).thenReturn(dockerDescriptor);
        when(repoService.repoDescriptorByKey(GENERIC_LOCAL)).thenReturn(genericDescriptor);
        when(repoService.repoDescriptorByKey(MAVEN_LOCAL)).thenReturn(mavenDescriptor);
    }

    public void testOnlyManifestIsFilteredInDockerRepo() {
        List<RepoPath> filteredPaths = DOCKER_REPO_PATHS.stream()
                .filter(path -> filter.filter(path, repoService))
                .collect(Collectors.toList());

        assertEquals(filteredPaths.size(), 1);
        assertEquals(filteredPaths.get(0).getRepoKey(), DOCKER_MANIFEST_PATH.getRepoKey());
        assertEquals(filteredPaths.get(0).getPath(), DOCKER_MANIFEST_PATH.getPath());
    }

    //Should keep non-docker files obviously
    public void testOnlyManifestIsFilteredInMixedRepos() {
        List<RepoPath> filteredPaths = MIXED_REPO_PATHS.stream()
                .filter(path -> filter.filter(path, repoService))
                .collect(Collectors.toList());

        assertEquals(filteredPaths.size(), 3);
        assertTrue(filteredPaths.stream().anyMatch(path ->
                DOCKER_MANIFEST_PATH.getRepoKey().equals(path.getRepoKey()) && DOCKER_MANIFEST_PATH.getPath().equals(path.getPath())));
        assertFalse(filteredPaths.stream().anyMatch(path ->
                DOCKER_LAYER_PATH.getRepoKey().equals(path.getRepoKey()) && DOCKER_LAYER_PATH.getPath().equals(path.getPath())));
        assertTrue(filteredPaths.stream().anyMatch(path ->
                MAVEN_PATH.getRepoKey().equals(path.getRepoKey()) && MAVEN_PATH.getPath().equals(path.getPath())));
        assertTrue(filteredPaths.stream().anyMatch(path ->
                GENERIC_PATH.getRepoKey().equals(path.getRepoKey()) && GENERIC_PATH.getPath().equals(path.getPath())));
    }

    //Missing dependencies to be able to call RepoPathFactory here so... yeah.
    private static RepoPath mockPath(String repoKey, String path) {
        RepoPath rpp = Mockito.mock(RepoPath.class);
        when(rpp.getRepoKey()).thenReturn(repoKey);
        when(rpp.getPath()).thenReturn(path);
        return rpp;
    }
}