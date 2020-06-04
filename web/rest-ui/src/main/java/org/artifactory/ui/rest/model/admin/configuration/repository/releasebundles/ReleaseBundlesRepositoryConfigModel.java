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

package org.artifactory.ui.rest.model.admin.configuration.repository.releasebundles;

import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.repo.RepoBaseDescriptor;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.ReverseProxyDescriptor;
import org.artifactory.descriptor.repo.releasebundle.ReleaseBundlesRepoDescriptor;
import org.artifactory.rest.common.util.JsonUtil;
import org.artifactory.ui.rest.model.admin.configuration.repository.GeneralRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.RepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.replication.local.LocalReplicationConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.typespecific.ReleaseBundlesRepoTypeSpecificConfigModel;
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
 * @author Nadav Yogev
 */
@JsonTypeName("releaseBundlesRepoConfig")
public class ReleaseBundlesRepositoryConfigModel
        implements RepositoryConfigModel<ReleaseBundlesBasicRepositoryConfigModel,
        ReleaseBundlesAdvancedRepositoryConfigModel, LocalReplicationConfigModel> {

    protected GeneralRepositoryConfigModel general;
    protected ReleaseBundlesBasicRepositoryConfigModel basic;
    protected ReleaseBundlesAdvancedRepositoryConfigModel advanced;
    private List<LocalReplicationConfigModel> replications = new ArrayList<>();
    private ReleaseBundlesRepoTypeSpecificConfigModel typeSpecific;

    @Override
    public GeneralRepositoryConfigModel getGeneral() {
        return general;
    }

    @Override
    public void setGeneral(GeneralRepositoryConfigModel general) {
        this.general = general;
    }

    @Override
    public ReleaseBundlesBasicRepositoryConfigModel getBasic() {
        return basic;
    }

    @Override
    public void setBasic(ReleaseBundlesBasicRepositoryConfigModel basic) {
        this.basic = basic;
    }

    @Override
    public ReleaseBundlesAdvancedRepositoryConfigModel getAdvanced() {
        return advanced;
    }

    @Override
    public void setAdvanced(ReleaseBundlesAdvancedRepositoryConfigModel advanced) {
        this.advanced = advanced;
    }

    @Override
    public List<LocalReplicationConfigModel> getReplications() {
        return replications;
    }

    @Override
    public void setReplications(List<LocalReplicationConfigModel> replication) {

    }

    @Override
    public ReleaseBundlesRepoTypeSpecificConfigModel getTypeSpecific() {
        return typeSpecific;
    }

    @Override
    public void setTypeSpecific(TypeSpecificConfigModel typeSpecific) {
        if (!(typeSpecific instanceof ReleaseBundlesRepoTypeSpecificConfigModel)) {
            throw new RuntimeException("Type specific configuration for Distribution repo must be Distribution type.");
        }
        this.typeSpecific = (ReleaseBundlesRepoTypeSpecificConfigModel) typeSpecific;
    }

    @Override
    public ReverseProxyDescriptor getReverseProxyDescriptor(RepoBaseDescriptor repoDescriptor,
            RepoConfigDescriptorBuilder builder) {
        return builder.buildReverseProxyDescriptor(this.advanced, repoDescriptor);
    }

    @Override
    public ReleaseBundlesRepoDescriptor toDescriptor(RepoConfigValidator validator, RepoConfigDescriptorBuilder builder,
            MutableCentralConfigDescriptor configDescriptor)
            throws RepoConfigException {
        validator.validateReleaseBundle(this);
        return builder.buildReleaseBundlesDescriptor(this);
    }

    @Override
    public ReleaseBundlesRepositoryConfigModel fromDescriptor(RepoConfigModelBuilder builder,
            RepoDescriptor descriptor) {
        builder.populateReleaseBundlesDescriptorValuesToModel((ReleaseBundlesRepoDescriptor) descriptor, this);
        return this;
    }

    @Override
    public MutableCentralConfigDescriptor createRepo(CreateRepoConfigHelper creator,
            MutableCentralConfigDescriptor configDescriptor) throws RepoConfigException {
        return creator.handleReleaseBundles(this, configDescriptor);
    }

    @Override
    public void updateRepo(UpdateRepoConfigHelper updater) throws RepoConfigException {
        updater.handleReleaseBundles(this);
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
