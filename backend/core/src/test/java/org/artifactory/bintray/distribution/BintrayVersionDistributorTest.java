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
import org.artifactory.api.bintray.distribution.resolver.DistributionCoordinatesResolver;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.descriptor.repo.distribution.DistributionRepoDescriptor;
import org.artifactory.descriptor.repo.distribution.rule.DistributionCoordinates;
import org.artifactory.descriptor.repo.distribution.rule.DistributionRule;
import org.artifactory.md.Properties;
import org.artifactory.model.common.RepoPathImpl;
import org.artifactory.model.xstream.fs.PropertiesImpl;
import org.artifactory.repo.RepoPath;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.testng.annotations.Test;

import static org.artifactory.mime.DockerNaming.MANIFEST_FILENAME;
import static org.junit.Assert.assertEquals;

/**
 * @author Yuval Reches
 */
@Test
public class BintrayVersionDistributorTest extends ArtifactoryHomeBoundTest {

    private final String distRepo = "DistributionRepo";

    // RTFACT-12717
    public void debianCoordinates() {
        String debName = "damaged.deb";
        RepoPath debPath = new RepoPathImpl("debianTest", debName);
        String bintrayDebRepo = "deb";
        DistributionCoordinates originalCoordinates = new DistributionCoordinates(bintrayDebRepo, "pnp4::nagios", "0.6.19-1~debmon60+2", debName);
        DistributionRule rule = new DistributionRule("debRule", RepoType.Debian, null, null, originalCoordinates);
        Properties prop = new PropertiesImpl();

        DistributionCoordinatesResolver coordinates = new DistributionCoordinatesResolver(rule, debPath, prop, null);
        DistributionRepoDescriptor distRepoDescriptor = new DistributionRepoDescriptor();
        distRepoDescriptor.setKey(distRepo);
        BintrayVersionDistributor distributor = new BintrayVersionDistributor(null, null, null, distRepoDescriptor, null, null);

        RepoPath artifactDistPath = distributor.getArtifactDistPath(coordinates);
        assertEquals(artifactDistPath.getRepoKey(), distRepo);
        assertEquals(artifactDistPath.getPath(), bintrayDebRepo + "/" + debName);
    }

    // RTFACT-12717
    public void dockerCoordinates() {
        String bintrayRepo = "bintrayRepo";
        String dockerImageName = "ubuntu";
        String dockerTag = "latest";
        RepoPath manifestPath = new RepoPathImpl("dockerTest", MANIFEST_FILENAME);
        DistributionRepoDescriptor distRepoDescriptor = new DistributionRepoDescriptor();
        distRepoDescriptor.setKey(distRepo);

        // For the copy, Artifactory takes the original docker values to create the final path in the Artifactory dist repo
        DistributionCoordinates originalCoordinates = new DistributionCoordinates(bintrayRepo, "ubuntu::123", "latest::latest", MANIFEST_FILENAME);
        DistributionRule distRule = new DistributionRule("dockerRule", RepoType.Docker, null, null, originalCoordinates);

        PropertiesImpl manifestProps = new PropertiesImpl();
        manifestProps.put("docker.repoName", "ubuntu");
        manifestProps.put("docker.manifest", "latest");
        DistributionCoordinatesResolver resolver = new DistributionCoordinatesResolver(distRule, manifestPath, manifestProps, null);
        resolver.resolve(new DistributionReporter(false));
        BintrayVersionDistributor distributor = new BintrayVersionDistributor(null, null, null, distRepoDescriptor, null, null);

        RepoPath artifactDistPath = distributor.getDockerImageDistPath(resolver);
        assertEquals(artifactDistPath.getRepoKey(), distRepo);
        assertEquals(artifactDistPath.getPath(), bintrayRepo + "/" + dockerImageName + "/" + dockerTag);
    }
}