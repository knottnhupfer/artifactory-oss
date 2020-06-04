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

package org.artifactory.converter.postinit.v102;

import org.artifactory.common.ArtifactoryHome;
import org.artifactory.converter.helpers.ConvertersManagerTestBase;
import org.artifactory.converter.helpers.MockArtifactoryHome;
import org.artifactory.descriptor.config.CentralConfigDescriptorImpl;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.repo.HttpRepoDescriptor;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.test.TestUtils;
import org.artifactory.version.ArtifactoryVersionProvider;
import org.artifactory.version.CompoundVersionDetails;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

import static org.artifactory.converter.postinit.v102.RemoteRepoBypassHeadSystemPropsConverter.PROP_KEY;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Dan Feldman
 */
@Test
public class RemoteRepoBypassHeadSystemPropsConverterTest extends ConvertersManagerTestBase {

    private static final String REMOTE1 = "danf-remote";
    private static final String REMOTE2 = "ChilliAndRay-remote";
    private static final String NOT_IN_LIST_REMOTE = "not-in-list-remote";

    //This is how its defined in PostInitVersion
    private RemoteRepoBypassHeadSystemPropsConverter converter = new RemoteRepoBypassHeadSystemPropsConverter(
            ArtifactoryVersionProvider.v5412.get(), ArtifactoryVersionProvider.v640m007.get());
    private CompoundVersionDetails goodFrom = new CompoundVersionDetails(ArtifactoryVersionProvider.v5412.get(), null, 0L);
    private CompoundVersionDetails goodUntil = new CompoundVersionDetails(ArtifactoryVersionProvider.v640m007.get(),
            null, 0L);

    @BeforeClass
    public void setup() {
        System.clearProperty(PROP_KEY);
    }

    @AfterClass(alwaysRun = true)
    public void clear() {
        System.clearProperty(PROP_KEY);
        ArtifactoryHome.unbind();
    }

    public void testInvalidVersion() {
        CompoundVersionDetails badFrom = new CompoundVersionDetails(ArtifactoryVersionProvider.v640m007.get(), null, 0L);
        CompoundVersionDetails badUntil = new CompoundVersionDetails(ArtifactoryVersionProvider.v552.get(), null, 0L);

        assertFalse(converter.isInterested(badFrom, badUntil));
        assertFalse(converter.isInterested(goodFrom, badUntil));
        assertFalse(converter.isInterested(badFrom, goodUntil));
    }

    public void testValidVersionValidRepoList() throws IOException {
        System.setProperty(PROP_KEY, "my-repo, your-repo");
        initHome();
        assertTrue(converter.isInterested(goodFrom, goodUntil));
    }

    public void testValidVersionInvalidRepoList() throws IOException {
        System.clearProperty(PROP_KEY);
        initHome();
        assertFalse(converter.isInterested(goodFrom, goodUntil));
    }

    public void testConversion() throws IOException {
        System.setProperty(PROP_KEY, REMOTE1 + "," + REMOTE2);
        initHome();
        MutableCentralConfigDescriptor descriptor = new CentralConfigDescriptorImpl();

        descriptor.getRemoteRepositoriesMap().put(REMOTE1, createRemoteRepo(REMOTE1));
        descriptor.getRemoteRepositoriesMap().put(REMOTE2, createRemoteRepo(REMOTE2));
        descriptor.getRemoteRepositoriesMap().put(NOT_IN_LIST_REMOTE, createRemoteRepo(NOT_IN_LIST_REMOTE));

        assertFalse(getRemote(descriptor, REMOTE1).isBypassHeadRequests());
        assertFalse(getRemote(descriptor, REMOTE2).isBypassHeadRequests());
        assertFalse(getRemote(descriptor, NOT_IN_LIST_REMOTE).isBypassHeadRequests());

        converter.addRepoConfigWhereNeeded(descriptor);

        assertTrue(getRemote(descriptor, REMOTE1).isBypassHeadRequests());
        assertTrue(getRemote(descriptor, REMOTE2).isBypassHeadRequests());
        assertFalse(getRemote(descriptor, NOT_IN_LIST_REMOTE).isBypassHeadRequests());
    }

    private void initHome() throws IOException {
        File home = TestUtils.createTempDir(getClass());
        //Doesn't really matter
        createHomeEnvironment(home, ArtifactoryVersionProvider.v5412.get());
        ArtifactoryHome artifactoryHome = new MockArtifactoryHome(home);
        ArtifactoryHome.bind(artifactoryHome);
        artifactoryHome.initArtifactorySystemProperties();
    }

    private RemoteRepoDescriptor createRemoteRepo(String repoKey) {
        HttpRepoDescriptor repo = new HttpRepoDescriptor();
        repo.setKey(repoKey);
        return repo;
    }

    private RemoteRepoDescriptor getRemote(MutableCentralConfigDescriptor descriptor, String repoKey) {
        return descriptor.getRemoteRepositoriesMap().get(repoKey);
    }


}