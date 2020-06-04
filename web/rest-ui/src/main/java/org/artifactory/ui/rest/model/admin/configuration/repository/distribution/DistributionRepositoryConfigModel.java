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

package org.artifactory.ui.rest.model.admin.configuration.repository.distribution;

import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.repo.RepoBaseDescriptor;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.ReverseProxyDescriptor;
import org.artifactory.descriptor.repo.distribution.DistributionRepoDescriptor;
import org.artifactory.rest.common.util.JsonUtil;
import org.artifactory.ui.rest.model.admin.configuration.repository.GeneralRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.RepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.replication.local.LocalReplicationConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.typespecific.DistRepoTypeSpecificConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.typespecific.TypeSpecificConfigModel;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.CreateRepoConfigHelper;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.UpdateRepoConfigHelper;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.builder.RepoConfigDescriptorBuilder;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.builder.RepoConfigModelBuilder;
import org.artifactory.rest.common.exception.RepoConfigException;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.validator.RepoConfigValidator;
import org.codehaus.jackson.annotate.JsonTypeName;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Dan Feldman
 */
@JsonTypeName("distributionRepoConfig")
public class DistributionRepositoryConfigModel implements RepositoryConfigModel<DistributionBasicRepositoryConfigModel,
        DistributionAdvancedRepositoryConfigModel, LocalReplicationConfigModel> {

    protected GeneralRepositoryConfigModel general;
    protected DistributionBasicRepositoryConfigModel basic;
    protected DistributionAdvancedRepositoryConfigModel advanced;
    private List<LocalReplicationConfigModel> replications = new ArrayList<>();
    private DistRepoTypeSpecificConfigModel typeSpecific;

    @Override
    public GeneralRepositoryConfigModel getGeneral() {
        return general;
    }

    @Override
    public void setGeneral(GeneralRepositoryConfigModel general) {
        this.general = general;
    }

    @Override
    public DistributionBasicRepositoryConfigModel getBasic() {
        return basic;
    }

    @Override
    public void setBasic(DistributionBasicRepositoryConfigModel basic) {
        this.basic = basic;
    }

    @Override
    public DistributionAdvancedRepositoryConfigModel getAdvanced() {
        return advanced;
    }

    @Override
    public void setAdvanced(DistributionAdvancedRepositoryConfigModel advanced) {
        this.advanced = advanced;
    }

    @Override
    public List<LocalReplicationConfigModel> getReplications() {
        return replications;
    }

    @Override
    public void setReplications(List<LocalReplicationConfigModel> replication) {
        //noop
    }

    @Override
    public DistRepoTypeSpecificConfigModel getTypeSpecific() {
        return typeSpecific;
    }

    @Override
    public void setTypeSpecific(TypeSpecificConfigModel typeSpecific) {
        if (!(typeSpecific instanceof DistRepoTypeSpecificConfigModel)) {
            throw new IllegalArgumentException("Type specific configuration for Distribution repo must be Distribution type.");
        }
        this.typeSpecific = (DistRepoTypeSpecificConfigModel) typeSpecific;
    }

    @Override
    public ReverseProxyDescriptor getReverseProxyDescriptor(RepoBaseDescriptor repoDescriptor,
            RepoConfigDescriptorBuilder builder) {
        return builder.buildReverseProxyDescriptor(this.advanced, repoDescriptor);
    }

    @Override
    public DistributionRepoDescriptor toDescriptor(RepoConfigValidator validator, RepoConfigDescriptorBuilder builder,
            MutableCentralConfigDescriptor configDescriptor)
            throws RepoConfigException {
        validator.validateDistribution(this);
        return builder.buildDistributionDescriptor(this);
    }

    @Override
    public DistributionRepositoryConfigModel fromDescriptor(RepoConfigModelBuilder builder, RepoDescriptor descriptor) {
        builder.populateDistributionDescriptorValuesToModel((DistributionRepoDescriptor) descriptor, this);
        return this;
    }

    @Override
    public MutableCentralConfigDescriptor createRepo(CreateRepoConfigHelper creator,
            MutableCentralConfigDescriptor configDescriptor) throws RepoConfigException {
        return creator.handleDistribution(this, configDescriptor);
    }

    @Override
    public void updateRepo(UpdateRepoConfigHelper updater) throws RepoConfigException {
        updater.handleDistribution(this);
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
