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

package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes;

import org.artifactory.api.repo.BaseBrowsableItem;
import org.artifactory.descriptor.repo.RepoType;

/**
 * @author Chen Keinan
 */
public class VirtualRemoteFileNode extends FileNode {

    private boolean cached;

    public VirtualRemoteFileNode(BaseBrowsableItem pathItem, String text, String repoType, RepoType repoPkgType,
            boolean isArchive) {
        super(pathItem.getRepoPath(), text, repoType, repoPkgType, isArchive);
        setLocal(false);
        cached = !pathItem.isRemote();
    }

    @Override
    public String getType() {
        return "virtualRemoteFile";
    }

    public boolean isCached() {
        return cached;
    }

    public void setCached(boolean cached) {
        this.cached = cached;
    }
}
