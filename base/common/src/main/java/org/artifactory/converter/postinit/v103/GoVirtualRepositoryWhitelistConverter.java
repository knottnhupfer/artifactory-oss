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

import com.google.common.collect.ImmutableList;
import org.artifactory.addon.go.GoGitProvider;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.security.SecurityService;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.converter.ConverterPreconditionException;
import org.artifactory.converter.postinit.PostInitConverter;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.repo.RepoBaseDescriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.descriptor.repo.VirtualRepoDescriptor;
import org.artifactory.version.ArtifactoryVersion;
import org.artifactory.version.ArtifactoryVersionProvider;
import org.artifactory.version.CompoundVersionDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Add /golang.org/** as a whitelist pattern to all of the Go virtual repositories with default patterns
 *
 * @author Nadavy
 */
public class GoVirtualRepositoryWhitelistConverter extends PostInitConverter {
    private static final Logger log = LoggerFactory.getLogger(GoVirtualRepositoryWhitelistConverter.class);

    private static final String GO_LANG_PATTERN = "**/" + GoGitProvider.Golang.getPrefix() + "**";
    private static final String K8S_PATTERN = "**/" + GoGitProvider.K8s.getPrefix() + "**";
    private static final List OLD_VIRTUAL_PATTERN = ImmutableList.of(
            "**/" + GoGitProvider.Github.getPrefix() + "**",
            "**/" + GoGitProvider.GoGoogleSource.getPrefix() + "**",
            "**/" + GoGitProvider.Gopkgin.getPrefix() + "**");

    public GoVirtualRepositoryWhitelistConverter(ArtifactoryVersion from) {
        super(from, from);
    }

    @Override
    public void convert(CompoundVersionDetails source, CompoundVersionDetails target) {
        ContextHelper.get().beanForType(SecurityService.class).doAsSystem(this::convert);
    }

    private void convert() {
        log.info("Whitelisting golang.org and k8s.io to all Go virtual repositories with default patterns");
        CentralConfigService configService = ContextHelper.get().beanForType(CentralConfigService.class);
        MutableCentralConfigDescriptor descriptor = configService.getMutableDescriptor();
        descriptor.getVirtualRepositoriesMap().values().stream()
                .filter(this::isGoRepository)
                .forEach(this::addReposToWhitelist);
        configService.saveEditedDescriptorAndReload(descriptor);
    }

    private void addReposToWhitelist(VirtualRepoDescriptor virtualRepoDescriptor) {
        List<String> patterns = virtualRepoDescriptor.getExternalDependencies().getPatterns();
        if (OLD_VIRTUAL_PATTERN.equals(patterns)) {
            log.info("Adding golang.org/** and k8s.io/** patterns to Go repository {}", virtualRepoDescriptor.getKey());
            // add only if the patterns are default
            patterns.add(GO_LANG_PATTERN);
            patterns.add(K8S_PATTERN);
        }
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
