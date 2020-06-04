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

package org.artifactory.security.access;

import org.jfrog.access.common.ServiceId;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.artifactory.security.access.AccessServiceConstants.APPLIED_PERMISSIONS;
import static org.artifactory.security.access.AccessServiceConstants.ARTIFACTORY_SERVICE_TYPE;
import static org.testng.Assert.assertEquals;

/**
 * @author Yinon Avraham.
 */
public class ArtifactoryAdminScopeTokenTest {

    @Test(dataProvider = "provideScopes")
    public void testIsAdminScopeOnService(String scopeToken, ServiceId serviceId, boolean expected) {
        assertEquals(ArtifactoryAdminScopeToken.isAdminScopeOnService(scopeToken, serviceId), expected);
    }

    @DataProvider
    public static Object[][] provideScopes() {
        ServiceId artServiceId1 = ServiceId.generateUniqueId(ARTIFACTORY_SERVICE_TYPE);
        ServiceId artServiceId2 = ServiceId.generateUniqueId(ARTIFACTORY_SERVICE_TYPE);
        ServiceId artServiceId3 = ServiceId.generateUniqueId("jf-artifactory");
        return new Object[][]{
                // old token
                {artServiceId1 + ":admin", artServiceId1, true},
                {artServiceId2 + ":admin", artServiceId2, true},
                {artServiceId3 + ":admin", artServiceId3, true},
                // non admin
                {artServiceId3 + ":nonadmin", artServiceId3, false},
                // wrong service id
                {artServiceId1 + ":admin", artServiceId2, false},
                {artServiceId2 + ":admin", artServiceId3, false},
                {artServiceId3 + ":admin", artServiceId1, false},
                // applied-permissions/admin
                {APPLIED_PERMISSIONS + "/admin", artServiceId1, true},
                {APPLIED_PERMISSIONS + "/admin", artServiceId3, true},
                // wrong delimiter
                {APPLIED_PERMISSIONS + ":admin", artServiceId1, false},
                // applied permissions non admin
                {APPLIED_PERMISSIONS + "/shay", artServiceId1, false},
        };
    }

    @Test(dataProvider = "provideAccepts")
    public void testAccepts(String scopeToken, boolean expected) throws Exception {
        assertEquals(ArtifactoryAdminScopeToken.accepts(scopeToken), expected);
    }

    @DataProvider
    public static Object[][] provideAccepts() {
        ServiceId artServiceId1 = ServiceId.generateUniqueId(ARTIFACTORY_SERVICE_TYPE);
        return new Object[][]{
                { ServiceId.generateUniqueId("jf-artifactory") + ":admin", true },
                { artServiceId1 + ":admin", true },
                { artServiceId1 + ":admin ", false },
                { " " + artServiceId1 + ":admin", false },
                { artServiceId1 + ":admins", false },
                { artServiceId1 + ":", false },
                { artServiceId1 + "", false },
                { ServiceId.generateUniqueId("other-service-type") + ":admin", false },
                // applied permissions admin
                {APPLIED_PERMISSIONS + "/admin", true},
                // incorrect data after delimiter and incorrect delimiters
                {APPLIED_PERMISSIONS + "/admi", false},
                {APPLIED_PERMISSIONS + ":admin", false},
                {APPLIED_PERMISSIONS + "-admin", false},
                {APPLIED_PERMISSIONS + "/nonadmin", false},
                // invalid applied-permissions scope
                {APPLIED_PERMISSIONS.substring(0, APPLIED_PERMISSIONS.length() -1 ) + "/admin", false},
                {"something" + "/admin", false},
        };
    }
}