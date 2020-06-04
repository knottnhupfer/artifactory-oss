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

package org.artifactory.ui.rest.model.artifacts.search;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.artifactory.addon.xray.XrayArtifactSummary;
import org.artifactory.addon.xray.XrayArtifactsSummary;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.util.JsonUtil;
import org.artifactory.util.CollectionUtils;

import java.util.Map;

/**
 * @author Nadav Yogev
 */
@Data
@NoArgsConstructor
public class PackageNativeXraySummaryModel implements RestModel {

    public PackageNativeXraySummaryModel(String version) {
        this.version = version;
    }

    private String version;
    private int totalDownloads;
    private String xrayStatus;
    Map<String, Map<String, Integer>> violations;
    private String detailsUrl;
    private String errorStatus;

    public String toString() {
        return JsonUtil.jsonToString(this);
    }

    public void addXraySummary(XrayArtifactsSummary artifactXraySummary) {
        if (CollectionUtils.notNullOrEmpty(artifactXraySummary.getVersions())) {
            XrayArtifactSummary xrayArtifactSummary = artifactXraySummary.getVersions().get(0);
            setXrayStatus(xrayArtifactSummary.getStatus());
            setViolations(xrayArtifactSummary.getViolations());
            setDetailsUrl(xrayArtifactSummary.getDetailsUrl());
            setErrorStatus(artifactXraySummary.getErrorStatus());
        }
    }
}
