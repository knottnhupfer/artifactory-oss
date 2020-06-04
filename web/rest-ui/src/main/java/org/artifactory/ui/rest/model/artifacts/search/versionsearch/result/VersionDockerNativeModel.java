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

package org.artifactory.ui.rest.model.artifacts.search.versionsearch.result;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.artifactory.ui.rest.model.artifacts.search.PackageNativeXraySummaryModel;

import java.util.Date;

/**
 * @author ortalh
 */
@Data
@NoArgsConstructor
public class VersionDockerNativeModel {
    private String name;
    private String packageId;
    private String repoKey;
    private Date lastModified;
    private PackageNativeXraySummaryModel xrayViolations;
    private long size;
    private String errorStatus;

    public VersionDockerNativeModel(String name, String packageId, String repoKey, Date lastModified) {
        this.name = name;
        this.packageId = packageId;
        this.repoKey = repoKey;
        this.lastModified = lastModified;
    }
}