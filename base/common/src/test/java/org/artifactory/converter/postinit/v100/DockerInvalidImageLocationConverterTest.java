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

package org.artifactory.converter.postinit.v100;

import org.artifactory.converter.helpers.ConvertersManagerTestBase;
import org.artifactory.version.ArtifactoryVersionProvider;
import org.artifactory.version.CompoundVersionDetails;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Just tests the {@link DockerInvalidImageLocationConverter#isInterested} part since there's also an Itest for the
 * functionality of this converter.
 *
 * @author Nadav Yogev
 * @author Dan Feldman
 */
@Test
public class DockerInvalidImageLocationConverterTest extends ConvertersManagerTestBase {

    //This is how its defined in PostInitVersion
    private DockerInvalidImageLocationConverter converter = new DockerInvalidImageLocationConverter(
            ArtifactoryVersionProvider.v4110.get(), ArtifactoryVersionProvider.v4121.get());
    private CompoundVersionDetails goodFrom = new CompoundVersionDetails(ArtifactoryVersionProvider.v4111.get(), null, 0L);
    private CompoundVersionDetails goodUntil = new CompoundVersionDetails(ArtifactoryVersionProvider.v4120.get(), null, 0L);

    public void testValidVersion() {
        //Actually goodUntil is not relevant for converters using the default isInterested logic
        assertTrue(converter.isInterested(goodFrom, goodUntil));
    }

    public void testInvalidVersion() {
        CompoundVersionDetails badFrom = new CompoundVersionDetails(ArtifactoryVersionProvider.v410.get(), null, 0L);
        CompoundVersionDetails badUntil = new CompoundVersionDetails(ArtifactoryVersionProvider.v4122.get(), null, 0L);

        assertFalse(converter.isInterested(badFrom, badUntil));
        assertFalse(converter.isInterested(badFrom, goodUntil));
    }
}