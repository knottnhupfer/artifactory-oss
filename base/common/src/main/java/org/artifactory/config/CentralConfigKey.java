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

package org.artifactory.config;

import org.jfrog.common.config.diff.DiffUtils;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * @author Noam Shemesh
 */
public enum CentralConfigKey {
    none("-"), // Listen to none to prevent reloads
    proxies("proxies"),
    security("security"),
    securityPasswordSettings("security.passwordSettings"),
    securityAccessClientSettings("security.accessClientSettings"),
    securityCrowdSettings("security.crowdSettings"),
    securitySamlSettings("security.samlSettings"),
    remoteReplications("remoteReplications"),
    localReplications("localReplications"),
    replicationsConfig("replicationsConfig"),
    localRepositoriesMap("localRepositoriesMap"),
    virtualRepositoriesMap("virtualRepositoriesMap"),
    remoteRepositoriesMap("remoteRepositoriesMap"),
    releaseBundlesRepositoriesMap("releaseBundlesRepositoriesMap"),
    singleReplicationPerRepoMap("singleReplicationPerRepoMap"),
    distributionRepositoriesMap("distributionRepositoriesMap"),
    folderDownloadConfig("folderDownloadConfig"),
    propertySets("propertySets"),
    backups("backups"),
    indexer("indexer"),
    sumoLogicConfig("sumoLogicConfig"),
    urlBase("urlBase"),
    serverName("serverName"),
    cleanupConfig("cleanupConfig"),
    virtualCacheCleanupConfig("virtualCacheCleanupConfig"),
    gcConfig("gcConfig"),
    offlineMode("offlineMode"),
    xrayConfig("xrayConfig");

    private final String key;

    CentralConfigKey(@Nonnull String key) {
        this.key = Objects.requireNonNull(key, "Key must not be null").replaceAll("[.]", DiffUtils.DELIMITER);
    }

    public String getKey() {
        return key;
    }
}
