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

package org.artifactory.aql.result.rows;

import org.artifactory.aql.model.AqlItemTypeEnum;

import java.util.Date;

/**
 * @author Gidi Shabat
 */
public interface FullRow {
    Date getCreated();

    Date getModified();

    Date getUpdated();

    String getCreatedBy();

    String getModifiedBy();

    long getStatId();

    Date getDownloaded();

    int getDownloads();

    String getDownloadedBy();

    AqlItemTypeEnum getType();

    String getRepo();

    String getPath();

    String getName();

    long getSize();

    int getDepth();

    Long getNodeId();

    String getRepoPathChecksum();

    String getOriginalMd5();

    String getActualMd5();

    String getOriginalSha1();

    String getActualSha1();

    String getSha2();

    String getKey();

    String getValue();

    String getEntryName();

    String getEntryPath();

    String getBuildModuleName();

    Long getBuildModuleId();

    String getBuildDependencyName();

    String getBuildDependencyScope();

    String getBuildDependencyType();

    String getBuildDependencySha1();

    //String getBuildDependencySha2();

    String getBuildDependencyMd5();

    String getBuildArtifactName();

    String getBuildArtifactType();

    String getBuildArtifactSha1();

    //String getBuildArtifactSha2();

    String getBuildArtifactMd5();

    String getBuildPropKey();

    String getBuildPropValue();

    String getBuildUrl();

    String getBuildName();

    String getBuildNumber();

    Date getBuildStarted();

    Date getBuildCreated();

    String getBuildCreatedBy();

    Date getBuildModified();

    String getBuildModifiedBy();
}
