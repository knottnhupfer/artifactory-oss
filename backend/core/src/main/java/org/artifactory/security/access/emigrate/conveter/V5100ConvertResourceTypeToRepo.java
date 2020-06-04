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

package org.artifactory.security.access.emigrate.conveter;

import org.apache.commons.lang.mutable.MutableInt;
import org.artifactory.security.ArtifactoryResourceType;
import org.artifactory.security.access.AccessService;
import org.artifactory.security.access.emigrate.AccessConverter;
import org.jfrog.access.common.ResourceType;
import org.jfrog.access.rest.permission.UpdatePermissionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Noam Shemesh
 */
@Component
public class V5100ConvertResourceTypeToRepo implements AccessConverter {
    private static final Logger log = LoggerFactory.getLogger(V5100ConvertResourceTypeToRepo.class);

    private final AccessService accessService;

    @Autowired
    public V5100ConvertResourceTypeToRepo(AccessService accessService) {
        this.accessService = accessService;
    }

    @Override
    public void convert() {
        log.info("Starting '5.10: Add resource type to permission target' Access Conversion");
        MutableInt count = new MutableInt();
        accessService.getAccessClient().permissions()
                .findPermissionsByServiceId(accessService.getArtifactoryServiceId())
                .getPermissions()
                .stream()
                .filter(permission -> ResourceType.SERVICE.equals(permission.getResourceType()))
                .forEach(permission -> {
                    accessService.getAccessClient().permissions()
                            .updatePermission((UpdatePermissionRequest)
                                    UpdatePermissionRequest.create()
                                            .name(permission.getName())
                                            .resourceType(ArtifactoryResourceType.REPO.getName()));
                    count.increment();
                });

        log.info("Finished '5.10: Add resource type to permission target' Access Conversion. Updated {} permission targets.", count);
    }
}
