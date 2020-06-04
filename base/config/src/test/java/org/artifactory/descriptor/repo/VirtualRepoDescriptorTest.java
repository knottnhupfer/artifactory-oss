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

package org.artifactory.descriptor.repo;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Tests the VirtualRepoDescriptor.
 *
 * @author Yossi Shaul
 */
@Test
public class VirtualRepoDescriptorTest {

    public void defaultConstructor() {
        VirtualRepoDescriptor virtualRepo = new VirtualRepoDescriptor();
        assertNull(virtualRepo.getKey());
        assertNull(virtualRepo.getRepoLayout());
        assertNull(virtualRepo.getKeyPair());
        assertTrue(virtualRepo.getRepositories().isEmpty());
        assertNotEquals(virtualRepo.getType(), RepoType.NuGet, "NuGet should be off by default.");
        assertEquals(virtualRepo.getDockerApiVersion(), DockerApiVersion.V2, "Docker API version should be V2 by default.");
    }
}
