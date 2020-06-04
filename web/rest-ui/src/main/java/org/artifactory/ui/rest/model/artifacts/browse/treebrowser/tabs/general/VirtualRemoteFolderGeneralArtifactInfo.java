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

import org.artifactory.api.repo.VirtualRepoItem;
import org.artifactory.rest.common.util.JsonUtil;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.info.FolderInfo;

/**
 * @author Chen Keinan
 */
public class VirtualRemoteFolderGeneralArtifactInfo extends GeneralArtifactInfo {

    public VirtualRemoteFolderGeneralArtifactInfo() {
    }

    @Override
    public void populateGeneralData(VirtualRepoItem item) {
        setInfo(new FolderInfo(retrieveRepoPath(), false));
    }

    @Override
    public void populateGeneralData() {
        setInfo(new FolderInfo(retrieveRepoPath(), false));
    }


    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
