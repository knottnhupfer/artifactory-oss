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

package org.artifactory.security;

import org.testng.annotations.Test;

import static org.testng.Assert.assertNotEquals;

/**
 * @author Tamir Hadad
 */
public class UsersSecurityEntityListItemTest {
    @Test
    public void testEqualHashCode() throws Exception {
        UsersSecurityEntityListItem usersSecurityEntityListItem1 = new UsersSecurityEntityListItem("user1",
                "user/user1",
                "internal");
        UsersSecurityEntityListItem usersSecurityEntityListItem2 = new UsersSecurityEntityListItem("user2",
                "user/user2",
                "internal");
        assertNotEquals(usersSecurityEntityListItem1, usersSecurityEntityListItem2);
    }
}