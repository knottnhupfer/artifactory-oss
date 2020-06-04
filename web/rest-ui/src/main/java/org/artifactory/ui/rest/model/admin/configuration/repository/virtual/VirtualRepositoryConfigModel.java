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

package org.artifactory.ui.rest.model.admin.configuration.repository.virtual;

import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.repo.RepoBaseDescriptor;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.ReverseProxyDescriptor;
import org.artifactory.descriptor.repo.VirtualRepoDescriptor;
import org.artifactory.rest.common.util.JsonUtil;
import org.artifactory.ui.rest.model.admin.configuration.repository.GeneralRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.RepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.replication.RepositoryReplicationConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.typespecific.TypeSpecificConfigModel;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.CreateRepoConfigHelper;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.UpdateRepoConfigHelper;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.builder.RepoConfigDescriptorBuilder;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.builder.RepoConfigModelBuilder;
import org.artifactory.rest.common.exception.RepoConfigException;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.validator.RepoConfigValidator;
import org.codehaus.jackson.annotate.JsonTypeName;

import java.util.List;

/**
 * @author Aviad Shikloshi
 * @author Dan Feldman
 */
@JsonTypeName("virtualRepoConfig")
public class VirtualRepositoryConfigModel implements RepositoryConfigModel<VirtualBasicRepositoryConfigModel,
        VirtualAdvancedRepositoryConfigModel, RepositoryReplicationConfigModel> {

    private GeneralRepositoryConfigModel general;
    private VirtualBasicRepositoryConfigModel basic;
    private VirtualAdvancedRepositoryConfigModel advanced;
    private TypeSpecificConfigModel typeSpecific;

    @Override
    public VirtualBasicRepositoryConfigModel getBasic() {
        return basic;
    }

    @Override
    public void setBasic(VirtualBasicRepositoryConfigModel basic) {
        this.basic = basic;
    }

    @Override
    public VirtualAdvancedRepositoryConfigModel getAdvanced() {
        return advanced;
    }

    @Override
    public void setAdvanced(VirtualAdvancedRepositoryConfigModel advanced) {
        this.advanced = advanced;
    }

    @Override
    public GeneralRepositoryConfigModel getGeneral() {
        return general;
    }

    @Override
    public void setGeneral(GeneralRepositoryConfigModel general) {
        this.general = general;
    }

    @Override
    public List<RepositoryReplicationConfigModel> getReplications() {
        return null;
    }

    @Override
    public void setReplications(List<RepositoryReplicationConfigModel> replication) {

    }

    @Override
    public ReverseProxyDescriptor getReverseProxyDescriptor(RepoBaseDescriptor repoDescriptor,
            RepoConfigDescriptorBuilder builder) {
        return builder.buildReverseProxyDescriptor(this.advanced, repoDescriptor);
    }

    @Override
    public TypeSpecificConfigModel getTypeSpecific() {
        return typeSpecific;
    }

    @Override
    public void setTypeSpecific(TypeSpecificConfigModel typeSpecific) {
        this.typeSpecific = typeSpecific;
    }

    @Override
    public VirtualRepoDescriptor toDescriptor(RepoConfigValidator validator, RepoConfigDescriptorBuilder builder,
            MutableCentralConfigDescriptor configDescriptor)
            throws RepoConfigException {
        validator.validateVirtual(this, configDescriptor);
        return builder.buildVirtualDescriptor(this, configDescriptor);
    }

    @Override
    public VirtualRepositoryConfigModel fromDescriptor(RepoConfigModelBuilder builder, RepoDescriptor descriptor) {
        builder.populateVirtualRepositoryConfigValuesToModel((VirtualRepoDescriptor) descriptor, this);
        return this;
    }

    @Override
    public MutableCentralConfigDescriptor createRepo(CreateRepoConfigHelper creator,
            MutableCentralConfigDescriptor configDescriptor) throws RepoConfigException {
        return creator.handleVirtual(this, configDescriptor);
    }

    @Override
    public void updateRepo(UpdateRepoConfigHelper updater) throws RepoConfigException {
        updater.handleVirtual(this);
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
