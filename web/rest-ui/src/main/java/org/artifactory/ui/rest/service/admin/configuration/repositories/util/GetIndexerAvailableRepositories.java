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

package org.artifactory.ui.rest.service.admin.configuration.repositories.util;

import org.apache.commons.lang.StringUtils;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.RepoLayout;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.function.Predicate;


/**
 * Retrieves the available repositories for maven indexer
 *
 * @author Shay Yaakov
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetIndexerAvailableRepositories extends GetAvailableRepositories {

    @Override
    protected Predicate<RepoDescriptor> getFilter(ArtifactoryRestRequest request, String type) {
        String layout = request.getQueryParamByKey("layout");
        return repo -> filterByType(RepoType.valueOf(type), repo) && filterByLayout(layout, repo);
    }

    private boolean filterByType(RepoType type, RepoDescriptor repo) {
        return type.isMavenGroup() ? repo.getType().isMavenGroup() : repo.getType().equals(type);
    }

    private boolean filterByLayout(String layout, RepoDescriptor repo) {
        RepoLayout repoLayout = repo.getRepoLayout();
        return repo.getRepoLayout() == null || StringUtils.isNotBlank(layout) && layout.equals(repoLayout.getName());
    }
}