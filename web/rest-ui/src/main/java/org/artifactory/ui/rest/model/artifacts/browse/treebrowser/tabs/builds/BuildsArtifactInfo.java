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

package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.builds;

import org.artifactory.addon.build.artifacts.ProducedBy;
import org.artifactory.addon.build.artifacts.UsedBy;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.util.JsonUtil;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.BaseArtifactInfo;

import java.util.List;

/**
 * @author Chen Keinan
 */
public class BuildsArtifactInfo extends BaseArtifactInfo implements RestModel {

    private List<ProducedBy> producedBy;
    private List<UsedBy> usedBy;

    public BuildsArtifactInfo() {
    }

    public BuildsArtifactInfo(List<ProducedBy> producedByRows,List<UsedBy> usedByRows) {
        this.producedBy = producedByRows;
        this.usedBy = usedByRows;
    }
    public List<ProducedBy> getProducedBy() {
            return producedBy;
        }

    public void setProducedBy(List<ProducedBy> producedBy) {
        this.producedBy = producedBy;
    }

    public List<UsedBy> getUsedBy() {
        return usedBy;
    }

    public void setUsedBy(List<UsedBy> usedBy) {
        this.usedBy = usedBy;
    }
    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }

}
