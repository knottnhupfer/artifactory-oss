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

package org.artifactory.ui.rest.model.admin.configuration.repository;

import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.repo.RepoBaseDescriptor;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.ReverseProxyDescriptor;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.distribution.DistributionRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.local.LocalRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.releasebundles.ReleaseBundlesRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.remote.RemoteRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.replication.RepositoryReplicationConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.typespecific.TypeSpecificConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.virtual.VirtualRepositoryConfigModel;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.CreateRepoConfigHelper;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.UpdateRepoConfigHelper;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.builder.RepoConfigDescriptorBuilder;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.builder.RepoConfigModelBuilder;
import org.artifactory.rest.common.exception.RepoConfigException;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.validator.RepoConfigValidator;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

import java.io.IOException;
import java.util.List;

/**
 * @author Dan Feldman
 * @author Aviad Shikloshi
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = LocalRepositoryConfigModel.class, name = "localRepoConfig"),
        @JsonSubTypes.Type(value = RemoteRepositoryConfigModel.class, name = "remoteRepoConfig"),
        @JsonSubTypes.Type(value = VirtualRepositoryConfigModel.class, name = "virtualRepoConfig"),
        @JsonSubTypes.Type(value = DistributionRepositoryConfigModel.class, name = "distributionRepoConfig"),
        @JsonSubTypes.Type(value = ReleaseBundlesRepositoryConfigModel.class, name = "releaseBundlesRepoConfig")
})
public interface RepositoryConfigModel<B extends BasicRepositoryConfigModel, A extends AdvancedRepositoryConfigModel,
        R extends RepositoryReplicationConfigModel> extends RestModel {

    GeneralRepositoryConfigModel getGeneral();

    void setGeneral(GeneralRepositoryConfigModel general);

    B getBasic();

    void setBasic(B basic);

    A getAdvanced();

    void setAdvanced(A advanced);

    List<R> getReplications();

    void setReplications(List<R> replication);

    TypeSpecificConfigModel getTypeSpecific();

    void setTypeSpecific(TypeSpecificConfigModel typeSpecific);


    ReverseProxyDescriptor getReverseProxyDescriptor(RepoBaseDescriptor repoDescriptor,
                              RepoConfigDescriptorBuilder builder);

    /**
     * Uses the builder to produce a descriptor from this model
     */
    @JsonIgnore
    RepoDescriptor toDescriptor(RepoConfigValidator validator, RepoConfigDescriptorBuilder builder,
            MutableCentralConfigDescriptor configDescriptor)
            throws RepoConfigException;

    @JsonIgnore
    RepositoryConfigModel fromDescriptor(RepoConfigModelBuilder builder, RepoDescriptor descriptor);

    @JsonIgnore
    MutableCentralConfigDescriptor createRepo(CreateRepoConfigHelper creator, MutableCentralConfigDescriptor configDescriptor) throws RepoConfigException;

    @JsonIgnore
    void updateRepo(UpdateRepoConfigHelper updater) throws IOException, RepoConfigException;
}
