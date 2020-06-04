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

package org.artifactory.converter.postinit;

import org.artifactory.common.ArtifactoryHome;
import org.artifactory.converter.postinit.v100.DockerInvalidImageLocationConverter;
import org.artifactory.converter.postinit.v101.ConanRepoPathConverter;
import org.artifactory.converter.postinit.v102.RemoteRepoBypassHeadSystemPropsConverter;
import org.artifactory.converter.postinit.v103.GoCacheRemoteRepositoryConverter;
import org.artifactory.converter.postinit.v103.GoVirtualRepositoryWhitelistConverter;
import org.artifactory.version.ArtifactoryVersionProvider;
import org.artifactory.version.CompoundVersionDetails;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Converters that will run post-init (but after all conversions)
 *
 * @author Nadav Yogev
 */
public enum PostInitVersions {
    v100(new DockerInvalidImageLocationConverter(ArtifactoryVersionProvider.v4110.get(), ArtifactoryVersionProvider.v4121.get())),
    v101(new ConanRepoPathConverter(ArtifactoryVersionProvider.v4150.get(), ArtifactoryVersionProvider.v560m001.get())),
    //This converter is not range-bound, it responds to any version above 6.4.0 (including)
    v102(new RemoteRepoBypassHeadSystemPropsConverter(ArtifactoryVersionProvider.v640m007.get(), ArtifactoryVersionProvider.v640m007.get())),
    v103(new GoCacheRemoteRepositoryConverter(ArtifactoryVersionProvider.v6100.get()),
            new GoVirtualRepositoryWhitelistConverter(ArtifactoryVersionProvider.v6100.get()));

    private final PostInitConverter[] converters;

    /**
     * @param converters A list of converters to use to move from <b>this</b> config version to the <b>next</b> config
     *                   version
     */
    PostInitVersions(PostInitConverter... converters) {
        this.converters = converters;
    }

    public static PostInitVersions getCurrent() {
        PostInitVersions[] versions = PostInitVersions.values();
        return versions[versions.length - 1];
    }

    public void convert(CompoundVersionDetails from, CompoundVersionDetails until) {
        Arrays.stream(PostInitVersions.values())
                .flatMap(version -> Stream.of(version.getConverters()))
                .filter(converter -> converter.isInterested(from, until))
                .forEach(converter -> converter.convert(from, until));
    }

    /**
     * Assert that all the post init converters that should run have all their preconditions met before running them
     */
    public void assertPreConditions(ArtifactoryHome home, CompoundVersionDetails from, CompoundVersionDetails until) {
        Arrays.stream(PostInitVersions.values())
                .flatMap(version -> Stream.of(version.getConverters()))
                .filter(converter -> converter.isInterested(from, until))
                .forEach(converter -> converter.assertConversionPrecondition(home, from, until));
    }

    public PostInitConverter[] getConverters() {
        return converters;
    }
}
