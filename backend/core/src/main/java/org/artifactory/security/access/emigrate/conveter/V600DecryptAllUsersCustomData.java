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

import org.artifactory.api.security.SecurityService;
import org.artifactory.security.access.emigrate.AccessConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Updating all users with decrypted custom data, and let Access encrypt whatever is sensitive
 * @author Noam Shemesh
 */
@Component
public class V600DecryptAllUsersCustomData implements AccessConverter {

    private static final Logger log = LoggerFactory.getLogger(V600DecryptAllUsersCustomData.class);

    private SecurityService securityService;

    @Autowired
    public V600DecryptAllUsersCustomData(SecurityService securityService) {
        this.securityService = securityService;
    }

    @Override
    public void convert() {
        log.info("Start decrypting all users custom data");
        securityService.decryptAllUserProps();
        log.info("Finished decrypting all users custom data");
    }
}
