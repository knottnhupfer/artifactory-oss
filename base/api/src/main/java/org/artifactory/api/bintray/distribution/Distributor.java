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

package org.artifactory.api.bintray.distribution;

import com.jfrog.bintray.client.api.handle.VersionHandle;
import org.artifactory.api.bintray.distribution.reporting.DistributionReporter;
import org.artifactory.api.repo.Async;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.descriptor.repo.distribution.DistributionRepoDescriptor;

/**
 * Responsible for all Distribution actions performed against Bintray
 *
 * @author Dan Feldman
 */
public interface Distributor {

    /**
     * Distributes a build or a set of path to Bintray based on parameters passed in {@param distribution} using
     * the Distribution repo as given in {@link Distribution#targetRepo}
     */
    DistributionReporter distribute(Distribution distribution);

    @Async
    DistributionReporter distributeInternal(Distribution distribution);

    /**
     * Executes sign and publish actions for {@param bintrayVersion} based on parameters in {@param distribution} and
     * {@param repoDescriptor}.  {@param versionFilesSize} is used as a factor for sign request timeout.
     * Some types cannot be signed and\or published in Bintray - {@param type} is used to determine what actions are possible
     */
    @Async
    void signAndPublish(Distribution distribution, VersionHandle bintrayVersion, int versionFilesSize, RepoType type,
            DistributionRepoDescriptor repoDescriptor);
}
