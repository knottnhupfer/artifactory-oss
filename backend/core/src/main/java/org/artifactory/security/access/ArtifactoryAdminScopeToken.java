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

import java.util.regex.Pattern;

import static org.artifactory.security.access.AccessServiceConstants.APPLIED_PERMISSIONS;
import static org.artifactory.security.access.AccessServiceConstants.ARTIFACTORY_SERVICE_ID_REGEX;

/**
 * @author Yinon Avraham.
 */
public class ArtifactoryAdminScopeToken {

    private ArtifactoryAdminScopeToken() {
    }

    private static final String ADMIN_SUFFIX = "admin";
    static final Pattern V1_SCOPE_SERVICE_ID_ARTIFACTORY_ADMIN_PATTERN = Pattern.compile(ARTIFACTORY_SERVICE_ID_REGEX + ":" + ADMIN_SUFFIX);
    private static final Pattern V2_SCOPE_APPLIED_PERMISSIONS_ADMIN_PATTERN = Pattern.compile(APPLIED_PERMISSIONS + "/" + ADMIN_SUFFIX);

    /**
     * Check whether a scope token is a valid artifactory admin scope token
     * @param scopeToken the scope token to parse
     */
    public static boolean accepts(String scopeToken) {
        return scopeToken != null && (
                V1_SCOPE_SERVICE_ID_ARTIFACTORY_ADMIN_PATTERN.matcher(scopeToken).matches() ||
                V2_SCOPE_APPLIED_PERMISSIONS_ADMIN_PATTERN.matcher(scopeToken).matches()
        );
    }

    static boolean isAdminScopeOnService(String scopeToken, ServiceId serviceId) {
        if (V1_SCOPE_SERVICE_ID_ARTIFACTORY_ADMIN_PATTERN.matcher(scopeToken).matches()) {
            String v1AdminSuffix = ":" + ADMIN_SUFFIX;
            String serviceIdName = scopeToken.substring(0, scopeToken.length() - v1AdminSuffix.length());
            ServiceId serviceIdFromScope = ServiceId.fromFormattedName(serviceIdName);
            return serviceId.equals(serviceIdFromScope);
        } else {
            return V2_SCOPE_APPLIED_PERMISSIONS_ADMIN_PATTERN.matcher(scopeToken).matches();
        }
    }
}
