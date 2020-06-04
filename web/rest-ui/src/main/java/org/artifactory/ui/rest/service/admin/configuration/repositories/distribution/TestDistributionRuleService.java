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

package org.artifactory.ui.rest.service.admin.configuration.repositories.distribution;

import com.google.common.collect.Lists;
import org.apache.http.HttpStatus;
import org.artifactory.api.bintray.distribution.DistributionService;
import org.artifactory.api.bintray.distribution.reporting.DistributionReporter;
import org.artifactory.api.bintray.distribution.resolver.DistributionCoordinatesResolver;
import org.artifactory.api.bintray.distribution.resolver.DistributionRuleFilterType;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.common.StatusEntry;
import org.artifactory.descriptor.repo.RepoLayout;
import org.artifactory.md.Properties;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.configuration.repository.distribution.rule.DistributionCoordinatesModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.distribution.rule.DistributionRuleModel;
import org.artifactory.util.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Dan Feldman
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class TestDistributionRuleService implements RestService<DistributionRuleModel> {

    @Autowired
    private DistributionService distributionService;

    @Autowired
    private RepositoryService repoService;

    @Override
    public void execute(ArtifactoryRestRequest<DistributionRuleModel> request, RestResponse response) {
        DistributionRuleModel rule = request.getImodel();
        DistributionReporter status = new DistributionReporter(false);
        //Not really 'paths' - just one path in rule test
        List<RepoPath> paths = distributionService.inputPathsToRepoPaths(Lists.newArrayList(rule.getTestPath()), status);
        if (status.isError()) {
            response.error(status.getLastError().getMessage()).responseCode(status.getLastError().getStatusCode());
            return;
        } else if(CollectionUtils.isNullOrEmpty(paths)) {
            response.error("No valid path was given to test").responseCode(HttpStatus.SC_BAD_REQUEST);
            return;
        }
        RepoPath path = paths.get(0);
        Map<RepoPath, Properties> pathProps = distributionService.getPathInformation(paths, rule.getProductName(), status);
        if (paths.size() != 1 || status.hasErrors() || pathProps.size() != 1) {
            response.error("Invalid path given to test").responseCode(HttpStatus.SC_BAD_REQUEST);
            return;
        }

        RepoLayout layout = repoService.localRepoDescriptorByKey(path.getRepoKey()).getRepoLayout();
        DistributionCoordinatesResolver resolver = new DistributionCoordinatesResolver(rule, path,
                pathProps.values().iterator().next(), layout);

        distributionService.addCaptureGroupsToRuleResolverIfMatches(rule, DistributionRuleFilterType.repo,
                rule.getRepoFilter(), path.getRepoKey(), resolver, status);
        distributionService.addCaptureGroupsToRuleResolverIfMatches(rule, DistributionRuleFilterType.path,
                rule.getPathFilter(), path.getPath(), resolver, status);

        resolver.resolve(status);
        DistributionCoordinatesModel resolvedModel = new DistributionCoordinatesModel(resolver);
        resolvedModel.tokens = resolver.tokens;
        if (status.hasErrors()) {
            List<String> errors = status.getGeneralErrors().values().stream()
                    .map(StatusEntry::getMessage)
                    .collect(Collectors.toList());
            response.errors(errors).responseCode(HttpStatus.SC_BAD_REQUEST);
        }
        if (status.hasWarnings()) {
            response.warn(status.getLastWarning().getMessage()).responseCode(HttpStatus.SC_OK);
        }
        response.iModel(resolvedModel);
    }
}
