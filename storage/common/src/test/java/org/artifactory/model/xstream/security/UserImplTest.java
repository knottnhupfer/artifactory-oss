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

package org.artifactory.model.xstream.security;

import com.google.common.collect.ImmutableList;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ArtifactoryContextThreadBinder;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.security.SaltedPassword;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests the {@link UserImpl}
 *
 * @author Yossi Shaul
 */
@Test
public class UserImplTest {

    @Mock
    private ArtifactoryContext context;

    @Mock
    private UserGroupService userGroupService;

    @BeforeMethod
    private void initMocks() {
        MockitoAnnotations.initMocks(this);
        ArtifactoryContextThreadBinder.bind(context);
    }

    public void sameAuthContextForSameInstance() {
        UserImpl paul = new UserImpl("paul");
        paul.setPassword(new SaltedPassword("atreides", "000"));
        assertTrue(paul.hasSameAuthorizationContext(paul));
    }

    public void authContextDifferentIfPasswordIfDifferent() {
        UserImpl leto = new UserImpl("leto");
        leto.setPassword(new SaltedPassword("atreides", "000"));

        UserImpl letoWithNewPass = new UserImpl("leto");
        leto.setPassword(new SaltedPassword("tyrant", "000"));

        assertFalse(leto.hasSameAuthorizationContext(letoWithNewPass));
    }

    public void sameAuthContextUserWithNoPassword() {
        UserImpl paul = new UserImpl("paul");
        assertTrue(paul.hasSameAuthorizationContext(paul));
    }

    public void testIsGroupAdmin() {
        when(context.beanForType(UserGroupService.class)).thenReturn(userGroupService);
        when(userGroupService.getAllAdminGroupsNames()).thenReturn(ImmutableList.of("admins"));

        UserImpl paul = new UserImpl("adminUser");
        paul.addGroup("admins");
        assertTrue(paul.isEffectiveAdmin());

        paul.setAdmin(false);
        paul.setGroupAdmin(false);
        paul.removeGroup("admins");
        assertFalse(paul.isEffectiveAdmin());
    }

    public void testIsAdminMultipleAssociations() {
        UserImpl paul = new UserImpl("adminUser");
        paul.addGroup(new GroupImpl("admins", "", false, true));

        assertTrue(paul.isGroupAdmin());
        assertTrue(paul.isEffectiveAdmin());
        assertTrue(paul.isGroupAdminVerbatim());

        paul.addGroup(new GroupImpl("non-admins", "", false, false));
        // Still an admin
        assertTrue(paul.isGroupAdmin());
        assertTrue(paul.isEffectiveAdmin());
        assertTrue(paul.isGroupAdminVerbatim());
    }
}