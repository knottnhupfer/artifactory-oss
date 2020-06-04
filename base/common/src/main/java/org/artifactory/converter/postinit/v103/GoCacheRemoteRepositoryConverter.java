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

package org.artifactory.converter.postinit.v103;

import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.SecurityService;
import org.artifactory.aql.AqlConverts;
import org.artifactory.aql.AqlService;
import org.artifactory.aql.api.domain.sensitive.AqlApiItem;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.converter.ConverterPreconditionException;
import org.artifactory.converter.postinit.PostInitConverter;
import org.artifactory.descriptor.repo.RepoBaseDescriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.repo.RepoPath;
import org.artifactory.version.ArtifactoryVersion;
import org.artifactory.version.ArtifactoryVersionProvider;
import org.artifactory.version.CompoundVersionDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.stream.Collectors;

import static org.artifactory.aql.api.internal.AqlBase.and;
import static org.artifactory.repo.RepoPath.REMOTE_CACHE_SUFFIX;

/**
 * Finds and delete the old goget.html files in all of Go (GitHub) remote repositories.
 *
 * @author Nadavy
 */
public class GoCacheRemoteRepositoryConverter extends PostInitConverter {
    private static final Logger log = LoggerFactory.getLogger(GoCacheRemoteRepositoryConverter.class);

    private static final String GO_GET_HTML = "goget.html";

    public GoCacheRemoteRepositoryConverter(ArtifactoryVersion from) {
        super(from, from);
    }

    @Override
    public void convert(CompoundVersionDetails source, CompoundVersionDetails target) {
        ContextHelper.get().beanForType(SecurityService.class).doAsSystem(this::convert);
    }

    private void convert() {
        log.info("Starting Go cache repositories converter");
        RepositoryService repositoryService = ContextHelper.get().beanForType(RepositoryService.class);
        log.info("Deleting deprecated cache repositories goget.html files");
        repositoryService.getRemoteRepoDescriptors().stream()
                .filter(this::isGoRepository)
                .map(RepoBaseDescriptor::getKey)
                .forEach(remoteRepoKey -> deleteGoGetFiles(remoteRepoKey, repositoryService));
    }

    private void deleteGoGetFiles(String remoteRepoKey, RepositoryService repositoryService) {
        AqlApiItem apiItem = AqlApiItem.create().filter(
                and(
                        AqlApiItem.repo().equal(remoteRepoKey + REMOTE_CACHE_SUFFIX),
                        AqlApiItem.name().equal(GO_GET_HTML)
                )
        );
        AqlService aqlService = ContextHelper.get().beanForType(AqlService.class);
        Set<RepoPath> toDelete = aqlService.executeQueryEager(apiItem).getResults().stream()
                .map(AqlConverts.toRepoPath)
                .collect(Collectors.toSet());
        log.info("Found {} goget.html file(s) in remote repository {}", toDelete.size(), remoteRepoKey);
        repositoryService.delete(toDelete);

    }

    private boolean isGoRepository(RepoBaseDescriptor repoDescriptor) {
        return RepoType.Go.equals(repoDescriptor.getType());
    }

    @Override
    public boolean isInterested(CompoundVersionDetails source, CompoundVersionDetails target) {
        return source.getVersion().before(ArtifactoryVersionProvider.v6100.get()) &&
                ArtifactoryVersionProvider.v6100.get().beforeOrEqual(target.getVersion());
    }

    @Override
    public void assertConversionPrecondition(ArtifactoryHome home, CompoundVersionDetails fromVersion,
            CompoundVersionDetails toVersion) throws ConverterPreconditionException {
        //noop
    }
}
