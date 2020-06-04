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

package org.artifactory.ui.rest.service.artifacts.search.packagesearch.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Lior Gur
 */
public class PackageNativeModelHandlersFactory {

    private static final Logger log = LoggerFactory.getLogger(PackageNativeModelHandlersFactory.class);

    public static PackageNativeModelHandler getModelHandler(String repoType) {
        switch(repoType) {
            case "npm":
                return new NpmNativeModelHandler();
            default:
                log.error("Model handler was not found for this repository type: {}", repoType);
                throw new RuntimeException("Model handler was not found for this repository type: " + repoType);
        }
    }
}
