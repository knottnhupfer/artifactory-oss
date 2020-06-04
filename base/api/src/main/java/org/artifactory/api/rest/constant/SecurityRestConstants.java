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

package org.artifactory.api.rest.constant;

/**
 * @author Noam Y. Tenne
 */
public interface SecurityRestConstants {
    String PATH_ROOT = "security";
    String PATH_ROOT_V2 = "v2/security"; // This is temporary for /v2 . Once we really start it need to extend it
    String ENTITY_KEY = "entityKey";
    String ENTITY_TYPE = "entityType";
    String PERMISSIONS_ROOT = "permissions";
    String PATH_PERMISSIONS = PERMISSIONS_ROOT + "/{" + ENTITY_KEY + ": .+}";
    String GROUPS = "groups";
    String USERS = "users";
    String PATH_USER_PERMISSION = PERMISSIONS_ROOT + "/" + USERS + "/{" + ENTITY_KEY + ": .+}";
    String PATH_GROUP_PERMISSION = PERMISSIONS_ROOT + "/" + GROUPS + "/{" + ENTITY_KEY + ": .+}";

    String MT_USER = RestConstants.MT_ARTIFACTORY_APP + PATH_ROOT + ".User+json";
    String MT_GROUP = RestConstants.MT_ARTIFACTORY_APP + PATH_ROOT + ".Group+json";
    String MT_PERMISSION_TARGET = RestConstants.MT_ARTIFACTORY_APP + PATH_ROOT + ".PermissionTarget+json";
    String MT_USERS = RestConstants.MT_ARTIFACTORY_APP + PATH_ROOT + ".Users+json";
    String MT_GROUPS = RestConstants.MT_ARTIFACTORY_APP + PATH_ROOT + ".Groups+json";
    String MT_PERMISSION_TARGETS = RestConstants.MT_ARTIFACTORY_APP + PATH_ROOT + ".PermissionTargets+json";

    String RETURN_URL_ACCESS_EXTENSION_PARAM = "return_url";
    String USR_EXTENSION_PARAM = "usr";
}
