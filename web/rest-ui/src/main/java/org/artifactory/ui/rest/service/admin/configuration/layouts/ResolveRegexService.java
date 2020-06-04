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

package org.artifactory.ui.rest.service.admin.configuration.layouts;

import org.apache.commons.lang.StringUtils;
import org.artifactory.descriptor.repo.RepoLayout;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.configuration.layouts.RegExResolverModel;
import org.artifactory.util.ExceptionUtils;
import org.artifactory.util.RepoLayoutUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author Lior Hasson
 */
@Component
public class ResolveRegexService implements RestService<RepoLayout> {
    private static final Logger log = LoggerFactory.getLogger(ResolveRegexService.class);

    @Override
    public void execute(ArtifactoryRestRequest<RepoLayout> request, RestResponse response) {
        RepoLayout layout = request.getImodel();

        try {
            String artifactRegEx = RepoLayoutUtils.generateRegExpFromPattern(layout, layout.getArtifactPathPattern());

            String descriptorRegEx = StringUtils.EMPTY;
            if (layout.isDistinctiveDescriptorPathPattern()) {
                descriptorRegEx = RepoLayoutUtils.generateRegExpFromPattern(layout, layout.getDescriptorPathPattern());
            }

            RegExResolverModel regExResolverModel = new RegExResolverModel(artifactRegEx, descriptorRegEx);
            response.iModel(regExResolverModel);
        }
        catch (Exception e) {
            String message = "Failed to resolve regular expression: " + ExceptionUtils.getRootCause(e).getMessage();
            response.error(message);
            log.debug(message);
        }
    }
}
