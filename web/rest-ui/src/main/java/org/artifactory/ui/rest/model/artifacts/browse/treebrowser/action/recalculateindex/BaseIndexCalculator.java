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

package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.action.recalculateindex;

import org.artifactory.addon.AddonsManager;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;



/**
 * @author Chen Keinan
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({@JsonSubTypes.Type(value = GemsIndexCalculator.class, name = "Gems"),
        @JsonSubTypes.Type(value = NpmIndexCalculator.class, name = "Npm"),
        @JsonSubTypes.Type(value = DebianIndexCalculator.class, name = "Debian"),
        @JsonSubTypes.Type(value = OpkgIndexCalculator.class, name = "Opkg"),
        @JsonSubTypes.Type(value = YumIndexCalculator.class, name = "YUM"),
        @JsonSubTypes.Type(value = NuGetIndexCalculator.class, name = "NuGet"),
        @JsonSubTypes.Type(value = PypiIndexCalculator.class, name = "Pypi"),
        @JsonSubTypes.Type(value = BowerIndexCalculator.class, name = "Bower"),
        @JsonSubTypes.Type(value = PodsIndexCalculator.class, name = "CocoaPods"),
        @JsonSubTypes.Type(value = ComposerIndexCalculator.class, name = "Composer"),
        @JsonSubTypes.Type(value = ChefIndexCalculator.class, name = "Chef"),
        @JsonSubTypes.Type(value = PuppetIndexCalculator.class, name = "Puppet"),
        @JsonSubTypes.Type(value = HelmIndexCalculator.class, name = "Helm"),
        @JsonSubTypes.Type(value = CranIndexCalculator.class, name = "CRAN"),
        @JsonSubTypes.Type(value = CondaIndexCalculator.class, name = "Conda"),
        @JsonSubTypes.Type(value = ConanIndexCalculator.class, name = "Conan")
}
)
public abstract class BaseIndexCalculator {

    protected AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
    protected RepositoryService repositoryService = ContextHelper.get().getRepositoryService();

    private String repoKey;

    public String getRepoKey() {
        return repoKey;
    }

    public void setRepoKey(String repoKey) {
        this.repoKey = repoKey;
    }

    public abstract void calculateIndex() throws Exception;

    boolean assertRepoType(String repoKey, RepoType repoType) {
        RepoDescriptor repoDescriptor = repositoryService.localOrCachedRepoDescriptorByKey(repoKey);
        return ((repoDescriptor != null) && repoType.equals(repoDescriptor.getType()));
    }
}
