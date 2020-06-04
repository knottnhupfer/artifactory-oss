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

package org.artifactory.ui.rest.service.setmeup;

import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.filteredresources.FilteredResourcesAddon;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.rest.common.service.RestResponse;
import org.slf4j.Logger;

import java.io.StringReader;

/**
 * @author Rotem Kfir
 */
public abstract class GetSettingSnippetService {

    protected abstract Logger getLog();

    /**
     * Replace place holders with values. If we couldn't retrieve the user's password we use the credentials that the user has manually inserted
     */
    String filterResource(RestResponse restResponse, FilteredResourcesAddon filteredResourcesWebAddon, String snippet, String password) {
        try {
            String filtered = filteredResourcesWebAddon.filterResource(null,
                    (org.artifactory.md.Properties) InfoFactoryHolder.get().createProperties(),
                    new StringReader(snippet));
            return addPasswordToSnippet(filtered, password);
        } catch (Exception e) {
            getLog().error("Unable to filter file: " + e.getMessage());
            restResponse.error(e.getMessage());
        }
        return snippet;
    }

    /**
     * If we couldn't retrieve the user's password we use the credentials that the user has manually inserted
     */
    String addPasswordToSnippet(String snippet, String password) {
        if (StringUtils.isBlank(password)) {
            return snippet;
        }
        return snippet.replace("*** Insert encrypted password here ***", password);
    }
}
