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

package org.artifactory.ui.rest.model.admin.configuration.repository.typespecific;

import org.artifactory.addon.AddonsManager;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.repo.config.RepoConfigDefaultValues.DefaultUrl;
import org.artifactory.rest.common.exception.RepoConfigException;
import org.artifactory.rest.common.model.RestModel;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;

/**
 * @author Dan Feldman
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "repoType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = BowerTypeSpecificConfigModel.class, name = "Bower"),
        @JsonSubTypes.Type(value = ChefTypeSpecificConfigModel.class, name = "Chef"),
        @JsonSubTypes.Type(value = CocoaPodsTypeSpecificConfigModel.class, name = "CocoaPods"),
        @JsonSubTypes.Type(value = ComposerTypeSpecificConfigModel.class, name = "Composer"),
        @JsonSubTypes.Type(value = ConanTypeSpecificConfigModel.class, name = "Conan"),
        @JsonSubTypes.Type(value = DebTypeSpecificConfigModel.class, name = "Debian"),
        @JsonSubTypes.Type(value = DistRepoTypeSpecificConfigModel.class, name = "Distribution"),
        @JsonSubTypes.Type(value = OpkgTypeSpecificConfigModel.class, name = "Opkg"),
        @JsonSubTypes.Type(value = DockerTypeSpecificConfigModel.class, name = "Docker"),
        @JsonSubTypes.Type(value = GemsTypeSpecificConfigModel.class, name = "Gems"),
        @JsonSubTypes.Type(value = GenericTypeSpecificConfigModel.class, name = "Generic"),
        @JsonSubTypes.Type(value = GitLfsTypeSpecificConfigModel.class, name = "GitLfs"),
        @JsonSubTypes.Type(value = GradleTypeSpecificConfigModel.class, name = "Gradle"),
        @JsonSubTypes.Type(value = HelmTypeSpecificConfigModel.class, name = "Helm"),
        @JsonSubTypes.Type(value = GoTypeSpecificConfigModel.class, name = "Go"),
        @JsonSubTypes.Type(value = CranTypeSpecificConfigModel.class, name = "CRAN"),
        @JsonSubTypes.Type(value = CondaTypeSpecificConfigModel.class, name = "Conda"),
        @JsonSubTypes.Type(value = IvyTypeSpecificConfigModel.class, name = "Ivy"),
        @JsonSubTypes.Type(value = MavenTypeSpecificConfigModel.class, name = "Maven"),
        @JsonSubTypes.Type(value = NpmTypeSpecificConfigModel.class, name = "Npm"),
        @JsonSubTypes.Type(value = NugetTypeSpecificConfigModel.class, name = "NuGet"),
        @JsonSubTypes.Type(value = P2TypeSpecificConfigModel.class, name = "P2"),
        @JsonSubTypes.Type(value = PypiTypeSpecificConfigModel.class, name = "Pypi"),
        @JsonSubTypes.Type(value = PuppetTypeSpecificConfigModel.class, name = "Puppet"),
        @JsonSubTypes.Type(value = ReleaseBundlesRepoTypeSpecificConfigModel.class, name = "ReleaseBundles"),
        @JsonSubTypes.Type(value = SbtTypeSpecificConfigModel.class, name = "SBT"),
        @JsonSubTypes.Type(value = VagrantTypeSpecificConfigModel.class, name = "Vagrant"),
        @JsonSubTypes.Type(value = VcsTypeSpecificConfigModel.class, name = "VCS"),
        @JsonSubTypes.Type(value = YumTypeSpecificConfigModel.class, name = "YUM"),
})
public interface TypeSpecificConfigModel extends RestModel {

    RepoType getRepoType();

    /**
     * This should retrieve the default remote url for each package type
     * For instance: Maven, Gradle, Ivy and SBT should return http://jcenter.bintray.com
     * <p>
     * Notice: method name corresponds to the JSON model field name and is used by the UI.
     */
    default String getUrl() {
        return DefaultUrl.urlByType(getRepoType());
    }

    /**
     * Handle configuration shared between local, remote and virtual repositories
     */
    default void validateSharedTypeSpecific() {
        // no implementation by default
    }

    /**
     * Handle configuration specific to local repositories
     */
    default void validateLocalTypeSpecific() throws RepoConfigException {
        // no implementation by default
    }

    /**
     * Handle configuration specific to remote repositories
     */
    default void validateRemoteTypeSpecific() throws RepoConfigException {
        // no implementation by default
    }

    /**
     * Handle configuration specific to virtual repositories
     */
    default void validateVirtualTypeSpecific(AddonsManager addonsManager) throws RepoConfigException {
        // no implementation by default
    }

    default void throwUnsupportedRemoteRepoType() throws RepoConfigException {
        throw new RepoConfigException("Package type " + getRepoType().name()
                + " is unsupported in remote repositories", SC_BAD_REQUEST);
    }

    default void throwUnsupportedVirtualRepoType() throws RepoConfigException {
        throw new RepoConfigException("Package type " + getRepoType().name()
                + " is unsupported in virtual repositories", SC_BAD_REQUEST);
    }
}
