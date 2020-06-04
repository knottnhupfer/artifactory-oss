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

package org.artifactory.security.access.emigrate;

import org.artifactory.environment.converter.local.version.v6.V600AddAccessDecryptAllUsersMarkerFile;
import org.artifactory.security.access.emigrate.conveter.AccessSecurityEmigratorImpl;
import org.artifactory.security.access.emigrate.conveter.V5100ConvertResourceTypeToRepo;
import org.artifactory.security.access.emigrate.conveter.V600DecryptAllUsersCustomData;
import org.artifactory.security.access.emigrate.conveter.V6600CreateDefaultBuildAcl;
import org.artifactory.version.ArtifactoryVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Noam Shemesh
 */
@Component
public class AccessConverters {
    public static final ArtifactoryVersion DECRYPT_USERS_VERSION = V600AddAccessDecryptAllUsersMarkerFile.DECRYPT_USERS_VERSION;

    private final AccessSecurityEmigratorImpl accessSecurityEmigrator;
    private final V5100ConvertResourceTypeToRepo resourceTypeConverter;
    private final V600DecryptAllUsersCustomData decryptAllUsersCustomData;
    private final V6600CreateDefaultBuildAcl createDefaultBuildAcl;

    @Autowired
    public AccessConverters(AccessSecurityEmigratorImpl accessSecurityEmigrator,
            V5100ConvertResourceTypeToRepo resourceTypeConverter,
            V600DecryptAllUsersCustomData decryptAllUsersCustomData,
            V6600CreateDefaultBuildAcl createDefaultBuildAcl) {
        this.accessSecurityEmigrator = accessSecurityEmigrator;
        this.resourceTypeConverter = resourceTypeConverter;
        this.decryptAllUsersCustomData = decryptAllUsersCustomData;
        this.createDefaultBuildAcl = createDefaultBuildAcl;
    }

    public AccessConverter getSecurityEmigrator() {
        return accessSecurityEmigrator;
    }

    public AccessConverter getResourceTypeConverter() {
        return resourceTypeConverter;
    }

    public AccessConverter getUserCustomDataDecryptionConverter() {
        return decryptAllUsersCustomData;
    }

    public AccessConverter getDefaultBuildAclConverter() {
        return createDefaultBuildAcl;
    }
}
