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

package org.artifactory.addon.license;

import org.artifactory.common.ArtifactoryHome;

/**
 * Helps to manage and transfer the servers members verification result
 *
 * @author gidis
 */
public enum VerificationResult {
    valid, noLicense, invalidKey, error, duplicateServerIds, duplicateLicense, converting, notSameVersion,
    runningModeConflict, haConfiguredNotHaLicense;

    public boolean isValid() {
        return this == valid;
    }

    public String showMassage() {
        switch (this) {
            case valid:
                return "Valid.";
            case invalidKey:
                return "Invalid Artifactory license";
            case duplicateServerIds:
                return "Stopping Artifactory since duplicate node ids have been found in registry. " +
                        "If you restarted this server, make sure to wait at least 30 seconds before re-activating it.";
            case duplicateLicense:
                return "Duplicate license found.";
            case runningModeConflict:
                if (ArtifactoryHome.get().isHaConfigured()) {
                    return "Stopping Artifactory since the local server is running as HA " +
                            "but found none HA server in registry.";
                } else {
                    return "Stopping Artifactory since the local server is running as PRO/OSS " +
                            "but found other servers in registry.";
                }
            case converting:
                return "Stopping Artifactory start up ,another server running converting process.";
            case notSameVersion:
                return "Stopping Artifactory start up ,another server with different version has been found.";
            case noLicense:
                if (ArtifactoryHome.get().isHaConfigured()) {
                    return "No license found. Make sure that you have an available valid license in the pool";
                } else {
                    return "Changing Artifactory mode to offline since no license is installed and other servers have " +
                            "been found in registry. Try to install HA license and then restart the server.";
                }
            case haConfiguredNotHaLicense:
                return "Changing Artifactory mode to offline since the server is configured as HA but the license " +
                        "is either not exist or not HA License.";
            case error:
            default:
                return "Error occurred during license verification/installation.";
        }
    }

    public static VerificationResult and(VerificationResult... values) {
        for (VerificationResult value : values) {
            if (valid != value) {
                return value;
            }
        }
        return valid;
    }

    public static VerificationResult or(VerificationResult... values) {
        VerificationResult result = valid;
        for (VerificationResult value : values) {
            if (valid == value) {
                return valid;
            } else {
                result = value;
            }
        }
        return result;
    }
}
