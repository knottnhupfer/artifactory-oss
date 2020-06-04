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

package org.artifactory.rest.common.model.xray;

import org.artifactory.addon.xray.ArtifactXrayInfo;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;

/**
 * Converts internal artifact Xray model to REST model
 *
 * @author Yuval Reches
 */
public abstract class ArtifactXrayModelHelper {

    public static XrayArtifactInfo getXrayInfo(ArtifactXrayInfo artifactXrayInfo) {
        XrayArtifactInfo xrayArtifactInfo = new XrayArtifactInfo();
        // Updating the model
        String indexStatus = artifactXrayInfo.getIndexStatus();
        if (indexStatus == null) {
            return xrayArtifactInfo;
        }
        xrayArtifactInfo.setXrayIndexStatus(indexStatus);
        xrayArtifactInfo.setXrayBlocked(artifactXrayInfo.isBlocked());
        xrayArtifactInfo.setXrayBlockReason(artifactXrayInfo.getBlockReason());
        Long indexLastUpdated = artifactXrayInfo.getIndexLastUpdated();
        CentralConfigService centralConfig = ContextHelper.get().getCentralConfig();
        xrayArtifactInfo.setXrayIndexStatusLastUpdatedTimestamp(
                indexLastUpdated == null ? null : centralConfig.format(indexLastUpdated));
        xrayArtifactInfo.setDetailsUrl(artifactXrayInfo.getDetailsUrl());
        return xrayArtifactInfo;
    }

}
