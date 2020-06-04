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
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.util.JsonUtil;
import org.artifactory.ui.rest.model.artifacts.search.versionsearch.result.VersionDockerNativeModel;

import java.util.Date;
import java.util.List;

/**
 * @author ortalh
 */
@Data
@NoArgsConstructor
public class VersionsDockerNativeModel implements RestModel {
    private String packageName;
    private Date lastModified;
    private List<VersionDockerNativeModel> versions;
    private long resultsCount;
    private String errorStatus;

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}

