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

package org.artifactory.layout;

import org.artifactory.config.ConfigurationChangesInterceptor;
import org.artifactory.config.ConfigurationException;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.security.SecurityDescriptor;
import org.springframework.stereotype.Component;

/**
 * @author NadavY
 */
@Component
public class SecurityConfigConfigurationChangesInterceptor implements ConfigurationChangesInterceptor {

    @Override
    public void onBeforeSave(CentralConfigDescriptor newDescriptor) {
        SecurityDescriptor security = newDescriptor.getSecurity();
        if (!security.isBuildGlobalBasicReadAllowed() && security.isBuildGlobalBasicReadForAnonymous()) {
            throw new ConfigurationException(
                    "Anonymous basic read build permission is not allowed when global basic read build permission is disabled!");
        }
    }
}
