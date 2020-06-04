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

package org.artifactory.ui.rest.service.builds.buildsinfo.tabs;

import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.ui.rest.model.builds.IssueModel;
import org.jfrog.build.api.Build;
import org.jfrog.build.api.Issues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class BuildIssuesService extends AbstractBuildService {

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        Build build = getBuild(request, response);
        if (build == null) {
            //Already logged
            return;
        }
        response.iModelList(populateIssues(build.getIssues()));
    }

    private List<IssueModel> populateIssues(Issues issues) {
        List<IssueModel> response;
        if (issues == null || issues.getAffectedIssues() == null) {
            response = new ArrayList<>();
        } else {
            response = issues.getAffectedIssues()
                    .stream()
                    .map(IssueModel::new)
                    .collect(Collectors.toList());
        }
        return response;
    }
}
