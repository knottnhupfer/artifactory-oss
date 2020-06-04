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

package org.artifactory.maven;

import org.artifactory.api.maven.MavenMetadataPluginWorkItem;
import org.artifactory.api.maven.MavenMetadataService;
import org.artifactory.api.maven.MavenMetadataWorkItem;
import org.artifactory.descriptor.repo.RepoLayout;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.LocalRepo;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.spring.InternalContextHelper;
import org.artifactory.util.RepoLayoutUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * A service for calculating maven metadata.
 *
 * @author Yossi Shaul
 */
@Service
public class MavenMetadataServiceImpl implements MavenMetadataService {
    private static final Logger log = LoggerFactory.getLogger(MavenMetadataServiceImpl.class);
    @Autowired
    private InternalRepositoryService repoService;

    private static MavenMetadataService getTransactionalMe() {
        return InternalContextHelper.get().beanForType(MavenMetadataService.class);
    }

    @Override
    public void calculateMavenMetadataAsync(MavenMetadataWorkItem workItem) {
        calculateMavenMetadata(workItem);
    }

    @Override
    public void calculateMavenMetadata(MavenMetadataWorkItem workItem) {
        RepoPath baseFolderPath = workItem.getRepoPath();
        if (baseFolderPath == null) {
            log.debug("Couldn't find repo for null repo path.");
            return;
        }
        LocalRepo localRepo = repoService.localRepositoryByKey(baseFolderPath.getRepoKey());
        if (localRepo == null) {
            log.debug("Couldn't find local non-cache repository for path '{}'.", baseFolderPath);
            return;
        }
        log.debug("Calculate maven metadata on {}", baseFolderPath);
        RepoLayout repoLayout = localRepo.getDescriptor().getRepoLayout();
        RepoType type = localRepo.getDescriptor().getType();
        // Do not calculate maven metadata if type == null or type doesn't belong to the maven group (Maven, Ivy, Gradle) or repoLayout not equals MAVEN_2_DEFAULT
        if (type != null && !(type.isMavenGroup() || RepoLayoutUtils.MAVEN_2_DEFAULT.equals(repoLayout))) {
            log.debug("Skipping maven metadata calculation since repoType '{}' doesn't belong to " +
                    "neither Maven, Ivy, Gradle repositories types.", baseFolderPath.getRepoKey());
            return;
        }

        new MavenMetadataCalculator(baseFolderPath, workItem.isRecursive()).calculate();
        // Calculate maven plugins metadata asynchronously
        getTransactionalMe().calculateMavenPluginsMetadataAsync(new MavenMetadataPluginWorkItem(localRepo.getKey()));
    }

    // get all folders marked for maven metadata calculation and execute the metadata calculation
    @Override
    public void calculateMavenPluginsMetadataAsync(MavenMetadataPluginWorkItem workItem) {
        LocalRepo localRepo = localRepositoryByKeyFailIfNull(
                InternalRepoPathFactory.repoRootPath(workItem.getLocalRepo()));
        new MavenPluginsMetadataCalculator().calculate(localRepo);
    }

    private LocalRepo localRepositoryByKeyFailIfNull(RepoPath localRepoPath) {
        LocalRepo localRepo = repoService.localRepositoryByKey(localRepoPath.getRepoKey());
        if (localRepo == null) {
            throw new IllegalArgumentException("Couldn't find local non-cache repository for path " + localRepoPath);
        }
        return localRepo;
    }

}
