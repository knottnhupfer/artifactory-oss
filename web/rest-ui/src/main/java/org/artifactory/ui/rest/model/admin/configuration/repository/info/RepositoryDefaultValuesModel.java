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

package org.artifactory.ui.rest.model.admin.configuration.repository.info;

import com.google.common.collect.Maps;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.distribution.DistributionAdvancedRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.local.LocalAdvancedRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.local.LocalBasicRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.remote.RemoteAdvancedRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.remote.RemoteBasicRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.remote.RemoteCacheRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.remote.RemoteNetworkRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.typespecific.*;
import org.artifactory.ui.rest.model.admin.configuration.repository.virtual.VirtualAdvancedRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.virtual.VirtualBasicRepositoryConfigModel;

import java.util.Map;

/**
 * @author Dan Feldman
 */
public class RepositoryDefaultValuesModel {

    private Map<String, RestModel> defaultModels = Maps.newHashMap();

    public RepositoryDefaultValuesModel() {
        // Construct models with default values
        defaultModels.put("localBasic", new LocalBasicRepositoryConfigModel());
        defaultModels.put("localAdvanced", new LocalAdvancedRepositoryConfigModel());
        defaultModels.put("remoteBasic", new RemoteBasicRepositoryConfigModel());
        defaultModels.put("remoteAdvanced", new RemoteAdvancedRepositoryConfigModel());
        defaultModels.put("virtualBasic", new VirtualBasicRepositoryConfigModel());
        defaultModels.put("virtualAdvanced", new VirtualAdvancedRepositoryConfigModel());
        defaultModels.put("network", new RemoteNetworkRepositoryConfigModel());
        defaultModels.put("cache", new RemoteCacheRepositoryConfigModel());
        defaultModels.put("bower", new BowerTypeSpecificConfigModel());
        defaultModels.put("cocoapods", new CocoaPodsTypeSpecificConfigModel());
        defaultModels.put("conan", new ConanTypeSpecificConfigModel());
        defaultModels.put("composer", new ComposerTypeSpecificConfigModel());
        defaultModels.put("gradle", new GradleTypeSpecificConfigModel());
        defaultModels.put("helm", new HelmTypeSpecificConfigModel());
        defaultModels.put("go", new GoTypeSpecificConfigModel());
        defaultModels.put("cran", new CranTypeSpecificConfigModel());
        defaultModels.put("conda", new CondaTypeSpecificConfigModel());
        defaultModels.put("ivy", new IvyTypeSpecificConfigModel());
        defaultModels.put("debian", new DebTypeSpecificConfigModel());
        defaultModels.put("distribution", new DistRepoTypeSpecificConfigModel());
        defaultModels.put("distributionAdvanced", new DistributionAdvancedRepositoryConfigModel());
        defaultModels.put("docker", new DockerTypeSpecificConfigModel());
        defaultModels.put("maven", new MavenTypeSpecificConfigModel());
        defaultModels.put("nuget", new NugetTypeSpecificConfigModel());
        defaultModels.put("npm", new NpmTypeSpecificConfigModel());
        defaultModels.put("p2", new P2TypeSpecificConfigModel());
        defaultModels.put("sbt", new SbtTypeSpecificConfigModel());
        defaultModels.put("vcs", new VcsTypeSpecificConfigModel());
        defaultModels.put("yum", new YumTypeSpecificConfigModel());
        defaultModels.put("pypi", new PypiTypeSpecificConfigModel());
        defaultModels.put("puppet", new PuppetTypeSpecificConfigModel());
        defaultModels.put("generic", new GenericTypeSpecificConfigModel());
        defaultModels.put("gems", new GemsTypeSpecificConfigModel());
        defaultModels.put("chef", new ChefTypeSpecificConfigModel());
    }

    public Map<String, RestModel> getDefaultModels() {
        return defaultModels;
    }
}
