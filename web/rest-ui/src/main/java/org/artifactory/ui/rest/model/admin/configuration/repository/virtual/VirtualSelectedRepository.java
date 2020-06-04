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

import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.descriptor.repo.RepoDescriptor;

import static org.artifactory.repo.RepoDetailsType.*;

/**
 * @author Shay Yaakov
 */
public class VirtualSelectedRepository {

    private String repoName;
    private String type; // local/remote/virtual

    public VirtualSelectedRepository() {
    }

    public VirtualSelectedRepository(RepoDescriptor descriptor) {
        this.repoName = descriptor.getKey();
        if (descriptor instanceof LocalRepoDescriptor) {
            this.type = LOCAL.typeNameLowercase();
        } else if (descriptor instanceof RemoteRepoDescriptor) {
            this.type = REMOTE.typeNameLowercase();
        } else {
            this.type = VIRTUAL.typeNameLowercase();
        }
    }

    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
