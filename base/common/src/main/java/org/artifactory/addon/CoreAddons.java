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

package org.artifactory.addon;

import org.artifactory.api.repo.DockerRepositoryAction;
import org.artifactory.security.UserInfo;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Core services addons interface.
 *
 * @author Yossi Shaul
 */
public interface CoreAddons extends Addon {
    String SUPER_USER_NAME = "super";

    /**
     * @return True if creation of new admin accounts is allowed.
     */
    boolean isCreateDefaultAdminAccountAllowed();

    /**
     * Check if a certain user is the AOL administrator, by checking that via the AOL dashboard.
     *
     * @param userInfo The user to check if its deletion is allowed.
     * @return True if the user is the AOL admin user
     */
    boolean isAolAdmin(UserInfo userInfo);

    /**
     * @return True if the AOL addon is activated
     */
    boolean isAol();

    boolean isDashboardUser();

    /**
     * @return Returns email addresses of Artifactory administrators to send error notification to.
     */
    @Nonnull
    List<String> getUsersForBackupNotifications();

    void validateTargetHasDifferentLicenseKeyHash(String targetLicenseHash, List<String> addons);

    /**
     * Validates that given licenseHash is different from license installed on this instance,
     * unless artifactoryId and current instance artifactoryId are equal (e.g same Artifactory)
     *
     * @param licenseHash license to check
     * @param artifactoryId artifactory id of the checked license
     */
    boolean validateTargetHasDifferentLicense(String licenseHash, String artifactoryId);

    void validateMultiPushReplicationSupportedForTargetLicense(String targetLicenseKey,
            boolean isMultiPushConfigureForThisRepo, String targetUrl);

    String getBuildNum();

    /**
     * @return Artifactory version string for list browsing
     */
    String getListBrowsingVersion();

    String getArtifactoryServerName();

    /**
     * Get the Artifactory URL for mail operations (e.g. send emails containing the Artifactory URL) including the
     * 'webapp/' suffix. Has two different implementations, see impl fo further information.
     */
    String getMailConfigArtifactoryUrl();

    void sendDockerRepoEvent(String repoName, String dockerApiVersion, DockerRepositoryAction action);
}