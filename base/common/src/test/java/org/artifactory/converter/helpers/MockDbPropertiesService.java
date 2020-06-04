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

package org.artifactory.converter.helpers;

import org.artifactory.common.config.db.ArtifactoryCommonDbPropertiesService;
import org.artifactory.common.storage.db.properties.DbVersionInfo;
import org.artifactory.version.ArtifactoryVersion;
import org.artifactory.version.ArtifactoryVersionProvider;

import java.util.Date;

/**
 * @author Gidi Shabat
 */
public class MockDbPropertiesService implements ArtifactoryCommonDbPropertiesService {

    private ArtifactoryVersion version;
    private long release;
    private DbVersionInfo versionInfo;

    public MockDbPropertiesService(ArtifactoryVersion version, long release) {
        this.version = version;
        this.release = release;
    }

    @Override
    public void updateDbVersionInfo(DbVersionInfo dbProperties) {
        this.versionInfo = dbProperties;
    }

    @Override
    public DbVersionInfo getDbVersionInfo() {
        if (version == null) {
            return null;
        } else {
            return new DbVersionInfo(new Date().getTime(), version.getVersion(), (int) version.getRevision(), release);
        }
    }

    @Override
    public boolean isDbPropertiesTableExists() {
        return version != null && ArtifactoryVersionProvider.v310.get().beforeOrEqual(version);
    }

    public boolean isUpdateDbPropertiesHasBeenCalled() {
        return versionInfo != null;
    }
}
