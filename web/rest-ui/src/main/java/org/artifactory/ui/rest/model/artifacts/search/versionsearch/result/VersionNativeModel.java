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

import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.artifactory.ui.rest.model.artifacts.search.PackageNativeXraySummaryModel;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * @author Inbar Tal
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VersionNativeModel {
    private static final Logger log = LoggerFactory.getLogger(VersionNativeModel.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private String name;
    private long lastModified;
    private int numOfRepos;
    private String latestPath;
    private Set<String> repositories = Sets.newHashSet();
    private PackageNativeXraySummaryModel xrayViolations;

    public VersionNativeModel(String name, long lastModified, int numOfRepos, String latestPath,
            Set<String> repositories) {
        this.name = name;
        this.lastModified = lastModified;
        this.numOfRepos = numOfRepos;
        this.latestPath = latestPath;
        this.repositories = repositories;
    }

    public void addRepoKey(String repoKey) {
        if (StringUtils.isNotEmpty(repoKey)) {
            this.repositories.add(repoKey);
            this.numOfRepos = this.repositories.size();
        }
    }
}
