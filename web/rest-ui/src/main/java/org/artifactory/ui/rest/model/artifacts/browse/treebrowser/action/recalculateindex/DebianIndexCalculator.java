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

import org.artifactory.addon.debian.DebianAddon;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.descriptor.repo.RepoBaseDescriptor;
import org.codehaus.jackson.annotate.JsonTypeName;

/**
 * @author Chen Keinan
 */
@JsonTypeName("Debian")
public class DebianIndexCalculator extends BaseIndexCalculator {

    @Override
    public void calculateIndex() throws Exception {
        DebianAddon debianAddon = addonsManager.addonByType(DebianAddon.class);
        RepoBaseDescriptor descriptor = ContextHelper.get().beanForType(RepositoryService.class).localRepoDescriptorByKey(getRepoKey());
        if (descriptor == null) {
            descriptor = ContextHelper.get().beanForType(RepositoryService.class).virtualRepoDescriptorByKey(getRepoKey());
        }
        if (debianAddon != null) {
            debianAddon.recalculateAll(descriptor, null, true);
        }
    }
}
