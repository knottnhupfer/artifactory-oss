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

package org.artifactory.converter.postinit.v102;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.security.SecurityService;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.converter.ConverterPreconditionException;
import org.artifactory.converter.postinit.PostInitConverter;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.version.ArtifactoryVersion;
import org.artifactory.version.ArtifactoryVersionProvider;
import org.artifactory.version.CompoundVersionDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.Map;

/**
 * This converter is created as part of RTFACT-17371 and is meant to ease the upgrade of users coming from 5.4.12
 * If version 6.4.0 and above is detected and {@link this#PROP_KEY} is present in system props
 *
 * @author Dan Feldman
 */
public class RemoteRepoBypassHeadSystemPropsConverter extends PostInitConverter {
    private static final Logger log = LoggerFactory.getLogger(RemoteRepoBypassHeadSystemPropsConverter.class);

    //This key was included in 5.4.12 but there's no reason to keep it around out of that version so its not in the consts.
    static final String PROP_KEY = "artifactory.downloads.shouldSkipHeadRepositoriesList";


    public RemoteRepoBypassHeadSystemPropsConverter(ArtifactoryVersion from, ArtifactoryVersion until) {
        super(from, until);
    }

    @Override
    public void convert(CompoundVersionDetails source, CompoundVersionDetails target) {
        if (!isInterested(source, target)) {
            log.debug("RemoteRepoBypassHeadSystemPropsConverter deemed unnecessary to run");
        }
        SecurityService securityService = ContextHelper.get().beanForType(SecurityService.class);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        try {
            securityService.authenticateAsSystem();
            convert();
        } finally {
            // Restore previous permissions
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
    }

    private void convert() {
        CentralConfigService configService = ContextHelper.get().beanForType(CentralConfigService.class);
        MutableCentralConfigDescriptor descriptor = configService.getMutableDescriptor();
        if (addRepoConfigWhereNeeded(descriptor)) {
            configService.saveEditedDescriptorAndReload(descriptor);
        }
    }

    /**
     * Mainly separated for the test.
     * Goes over the {@param repoList}, acquires the matching repo from {@param descriptor} and adds
     * the {@link RemoteRepoDescriptor#bypassHeadRequests} config.
     */
    boolean addRepoConfigWhereNeeded(MutableCentralConfigDescriptor descriptor) {
        MutableBoolean hadAnyChanges = new MutableBoolean(false);
        //Already passed isInterested so the list has content
        String[] bypassingRepoList = getHeadBypassingRemoteRepoList().split(",");
        Map<String, RemoteRepoDescriptor> remoteRepos = descriptor.getRemoteRepositoriesMap();
        Arrays.stream(bypassingRepoList)
                .filter(remoteRepos::containsKey)
                .map(remoteRepos::get)
                .filter(repo -> !repo.isBypassHeadRequests())
                .peek(repo -> log.info("Adding 'bypass head request' config to repo '{}'", repo.getKey()))
                .forEach(repo -> {
                    repo.setBypassHeadRequests(true);
                    hadAnyChanges.setTrue();
                });
        return hadAnyChanges.booleanValue();
    }



    @Override
    public boolean isInterested(CompoundVersionDetails source, CompoundVersionDetails target) {
        return source.getVersion().before(ArtifactoryVersionProvider.v640m007.get())
                && target.getVersion().afterOrEqual(ArtifactoryVersionProvider.v640m007.get())
                && !StringUtils.isBlank(getHeadBypassingRemoteRepoList());
    }

    /**
     * Since {@link this#PROP_KEY} is omitted from the  {@link org.artifactory.common.ConstantValues},
     * this method will return props that were put on the file but are not present in {@link org.artifactory.common.ConstantValues}
     */
    private String getHeadBypassingRemoteRepoList() {
        return ArtifactoryHome.get().getArtifactoryProperties().getProperty(PROP_KEY, null);
    }

    @Override
    public void assertConversionPrecondition(ArtifactoryHome home, CompoundVersionDetails fromVersion, CompoundVersionDetails toVersion) throws ConverterPreconditionException {
        //noop
    }
}
