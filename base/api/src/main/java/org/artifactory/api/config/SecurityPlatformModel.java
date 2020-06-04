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

package org.artifactory.api.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.artifactory.descriptor.security.SecurityDescriptor;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * The model for platform's security configuration
 *
 * @author Rotem Kfir
 */
@Data
@NoArgsConstructor
public class SecurityPlatformModel {

    @JsonProperty("anonymous_access_enabled")
    private boolean anonAccessEnabled;

    @JsonProperty("hide_unauthorized_resources")
    private boolean hideUnauthorizedResources;


    public SecurityPlatformModel(SecurityDescriptor descriptor) {
        this.anonAccessEnabled = descriptor.isAnonAccessEnabled();
        this.hideUnauthorizedResources = descriptor.isHideUnauthorizedResources();
    }
}
