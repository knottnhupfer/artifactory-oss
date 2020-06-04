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

package org.artifactory.converter.postinit.v101;

import org.artifactory.converter.helpers.ConvertersManagerTestBase;
import org.artifactory.version.ArtifactoryVersionProvider;
import org.artifactory.version.CompoundVersionDetails;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author Tomer Mayost
 * @author Dan Feldman
 */
@Test
public class ConanPathConverterTest extends ConvertersManagerTestBase {

    //This is how its defined in PostInitVersion
    private ConanRepoPathConverter converter = new ConanRepoPathConverter(ArtifactoryVersionProvider.v4150.get(), ArtifactoryVersionProvider.v560m001.get());
    private CompoundVersionDetails goodFrom = new CompoundVersionDetails(ArtifactoryVersionProvider.v4161.get(), null, 0L);
    private CompoundVersionDetails goodUntil = new CompoundVersionDetails(ArtifactoryVersionProvider.v552.get(), null, 0L);

    public void tesConversion() {
        assertEquals(converter.fixConanPath("pkg/version/user/channel"), "user/pkg/version/channel");
        assertEquals(converter.fixConanPath("pkg/version/user/channel/a/b/c"), "user/pkg/version/channel/a/b/c");
    }

    public void testFailedConversion() {
        assertNull(converter.fixConanPath("path/too/short/"), null);
    }

    public void testValidVersion() {
        //Actually goodUntil is not relevant for converters using the default isInterested logic
        assertTrue(converter.isInterested(goodFrom, goodUntil));
    }

    public void testInvalidVersion() {
        CompoundVersionDetails badFrom = new CompoundVersionDetails(ArtifactoryVersionProvider.v4140.get(), null, 0L);
        CompoundVersionDetails badUntil = new CompoundVersionDetails(ArtifactoryVersionProvider.v560.get(), null, 0L);
        assertFalse(converter.isInterested(badFrom, badUntil));
        assertFalse(converter.isInterested(badFrom, goodUntil));
    }
}
