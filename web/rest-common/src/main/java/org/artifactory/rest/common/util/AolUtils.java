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

package org.artifactory.rest.common.util;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.CoreAddons;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.DockerRepositoryAction;
import org.artifactory.common.ConstantValues;
import org.artifactory.rest.exception.ForbiddenWebAppException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Lior Azar
 */
public class AolUtils {
    private static final Logger log = LoggerFactory.getLogger(AolUtils.class);

    public static void assertNotAol(String functionName) {
        if (ContextHelper.get().beanForType(AddonsManager.class).addonByType(CoreAddons.class).isAol()) {
            log.warn("{} is not supported when running on the cloud", functionName);
            throw new ForbiddenWebAppException("Function is not supported when running on the cloud");
        }
    }

    public static void sendDockerRepoEvent(String repoKey, String version, DockerRepositoryAction action) {
        CoreAddons coreAddons = ContextHelper.get().beanForType(AddonsManager.class).addonByType(CoreAddons.class);
        if(coreAddons.isAol() && !ConstantValues.dev.getBoolean()){
               coreAddons.sendDockerRepoEvent(repoKey,version, action);
        }
    }

}
