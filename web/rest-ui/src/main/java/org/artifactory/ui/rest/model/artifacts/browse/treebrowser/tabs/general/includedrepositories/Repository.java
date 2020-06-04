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

package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.includedrepositories;

import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.VirtualRepoDescriptor;

import static org.artifactory.repo.RepoDetailsType.*;

/**
 * @author Chen Keinan
 */
public class Repository {

    private String repoKey;
    private String linkUrl;
    private String type;

    public Repository() {
        // For Jackson
    }

    public Repository(RepoDescriptor repoDescriptor, String linkUrl) {
        this.repoKey = repoDescriptor.getKey();
        this.linkUrl = linkUrl;
        if (repoDescriptor instanceof VirtualRepoDescriptor) {
            this.type = VIRTUAL.typeNameLowercase();
        } else if (repoDescriptor instanceof RemoteRepoDescriptor) {
            this.type = REMOTE.typeNameLowercase();
        } else if (repoDescriptor instanceof LocalRepoDescriptor) {
            this.type = LOCAL.typeNameLowercase();
        }
    }

    public String getRepoKey() {
        return repoKey;
    }

    public void setRepoKey(String repoKey) {
        this.repoKey = repoKey;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
