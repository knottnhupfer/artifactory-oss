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

package org.artifactory.ui.rest.service.artifacts.search.versionsearch;

import com.google.common.collect.Lists;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.build.BuildRun;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.fs.FileInfo;
import org.artifactory.fs.ItemInfo;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.storage.build.service.BuildSearchCriteria;
import org.artifactory.storage.build.service.BuildStoreService;
import org.artifactory.ui.rest.model.artifacts.search.versionsearch.result.BuildNativeModel;
import org.artifactory.ui.rest.model.artifacts.search.versionsearch.result.BuildsNativeModel;
import org.artifactory.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.artifactory.ui.rest.service.artifacts.search.packagesearch.util.PackageNativeRestConstants.PATH;

/**
 * @author Inbar Tal
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class VersionNativeGetBuildsService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(VersionNativeGetBuildsService.class);

    private RepositoryService repositoryService;
    private BuildStoreService buildStoreService;
    private AuthorizationService authorizationService;

    @Autowired
    public VersionNativeGetBuildsService(RepositoryService repositoryService,
            BuildStoreService buildStoreService, AuthorizationService authorizationService) {
        this.repositoryService = repositoryService;
        this.buildStoreService = buildStoreService;
        this.authorizationService = authorizationService;
    }

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String path = request.getQueryParamByKey(PATH);

        if (!hasReadPermissions(path)) {
            response.iModel(new BuildsNativeModel());
            return;
        }
        String sha1 = getSha1(path);
        log.debug("Searching builds for sha1: {}", sha1);
        Set<BuildRun> buildsForChecksum = buildStoreService
                .findBuildsForChecksum(BuildSearchCriteria.IN_BOTH, ChecksumType.sha1, sha1);

        if (CollectionUtils.isNullOrEmpty(buildsForChecksum)) {
            response.iModel(new BuildsNativeModel());
        }

        List<BuildNativeModel> buildNativeModels = Lists.newArrayList();
        buildsForChecksum.forEach(
                build -> buildNativeModels
                        .add(new BuildNativeModel(build.getName(), build.getNumber(), build.getStartedDate().getTime())
                        ));

        log.debug("Found {} builds for sha1 {}", buildNativeModels.size(), sha1);
        response.iModel(new BuildsNativeModel(sortResults(buildNativeModels)));
    }

    private boolean hasReadPermissions(String path) {
        return authorizationService.canRead(RepoPathFactory.create(path));
    }

    private String getSha1(String path) {
        RepoPath repoPath = RepoPathFactory.create(path);
        ItemInfo fileInfo = repositoryService.getItemInfo(repoPath);
        return ((FileInfo) fileInfo).getChecksumsInfo().getSha1();
    }

    private List<BuildNativeModel> sortResults(List<BuildNativeModel> builds) {
        Comparator<BuildNativeModel> comparator = Comparator.comparing(BuildNativeModel::getName)
                .thenComparing(BuildNativeModel::getNumber);
        return builds.stream().sorted(comparator).collect(Collectors.toList());
    }
}
