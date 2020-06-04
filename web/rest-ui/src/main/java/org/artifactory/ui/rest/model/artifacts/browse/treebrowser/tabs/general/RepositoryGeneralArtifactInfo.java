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

package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general;

import org.artifactory.descriptor.repo.RealRepoDescriptor;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.rest.common.util.JsonUtil;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.info.RepositoryInfo;
import org.codehaus.jackson.annotate.JsonTypeName;

/**
 * @author Chen Keinan
 */
@JsonTypeName("repository")
public class RepositoryGeneralArtifactInfo extends GeneralArtifactInfo {

    protected String blackedOutMessage;

    RepositoryGeneralArtifactInfo() {
    }

    @Override
    public void populateGeneralData() {
        RepoDescriptor descriptor = retrieveRepoService().repoDescriptorByKey(getRepoKey());
        setInfo(new RepositoryInfo(descriptor, retrieveRepoPath()));
        populateVirtualRepositories(descriptor);
        setRepositoryBlackedOut(descriptor);
    }

    private void setRepositoryBlackedOut(RepoDescriptor repoDescriptor) {
        if (repoDescriptor != null && repoDescriptor instanceof RealRepoDescriptor) {
            if (((RealRepoDescriptor) repoDescriptor).isBlackedOut()) {
                this.setBlackedOutMessage("This repository is blacked out, items can only be viewed but cannot be resolved or deployed.");
            }
        }
    }

    public String getBlackedOutMessage() {
        return blackedOutMessage;
    }

    public void setBlackedOutMessage(String blackedOutMessage) {
        this.blackedOutMessage = blackedOutMessage;
    }

    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
