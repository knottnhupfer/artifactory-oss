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

import org.artifactory.api.module.ModuleInfo;
import org.artifactory.api.module.ModuleInfoUtils;
import org.artifactory.api.module.regex.NamedMatcher;
import org.artifactory.api.module.regex.NamedPattern;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.configuration.layouts.LayoutConfigViewModel;
import org.artifactory.util.ExceptionUtils;
import org.artifactory.util.RepoLayoutUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author Lior Hasson
 */
@Component
public class TestArtPathService implements RestService<LayoutConfigViewModel> {
    private static final Logger log = LoggerFactory.getLogger(TestArtPathService.class);

    @Override
    public void execute(ArtifactoryRestRequest<LayoutConfigViewModel> request, RestResponse response) {
        LayoutConfigViewModel layout = request.getImodel();
        try {
            ModuleInfo moduleInfo = null;
            if (layout.isDistinctiveDescriptorPathPattern()) {
                String pathPattern = layout.getDescriptorPathPattern();
                String regExp = RepoLayoutUtils.generateRegExpFromPattern(layout, pathPattern, true);
                moduleInfo = ModuleInfoUtils.moduleInfoFromDescriptorPath(layout.getPathToTest(), layout);
                checkIfEmptyCapturingGroup(moduleInfo, regExp, layout.getPathToTest());
            }

            if ((moduleInfo == null) || !moduleInfo.isValid()) {
                String pathPattern = layout.getArtifactPathPattern();
                String regExp = RepoLayoutUtils.generateRegExpFromPattern(layout, pathPattern, true);
                moduleInfo = ModuleInfoUtils.moduleInfoFromArtifactPath(layout.getPathToTest(), layout);
                checkIfEmptyCapturingGroup(moduleInfo, regExp, layout.getPathToTest());
            }

            response.iModel(moduleInfo);

        } catch (Exception e) {
            String message = "Failed to test path: " + ExceptionUtils.getRootCause(e).getMessage();
            response.error(message);
            log.debug(message);
        }
    }

    private void checkIfEmptyCapturingGroup(ModuleInfo moduleInfo, String regExp, String pathToTest) throws Exception {
        if (!moduleInfo.isValid()) {
            // May be due to empty capturing blocks
            NamedPattern compileArtifactRegex = NamedPattern.compile(regExp);
            NamedMatcher matcher = compileArtifactRegex.matcher(pathToTest);
            if (matcher.regexpMatches() && !matcher.matches()) {
                throw new Exception("Non named capturing groups are not allowed! Use (?:XXX)");
            }
        }
    }
}
