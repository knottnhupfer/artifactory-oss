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
 * Enum that represent license validation result for adding license operation
 *
 * @author Shay Bagants
 */
public enum AddRemoveLicenseVerificationResult {
    valid,
    invalidKey,
    invalidHaKey,
    licenseExists,
    licenseInUse,
    notForThisVersion,
    error,
    //bintray activation statuses
    invalidActivationKey,
    licenseIsBlacklisted,
    licenseInUseByAnotherServiceId,
    licenseDoesNotExist,
    licenseValidationCommunicationIssue;

    public boolean isValid() {
        return this == valid;
    }

    //TODO [by shayb]: replace with constructor with private field for the message + change the showmessage to getMessage
    public String showMassage() {
        switch (this) {
            case valid:
                return "OK.";
            case invalidKey:
                return "Invalid Artifactory license.";
            case invalidHaKey:
                return "Invalid Artifactory license for High Availability environment.";
            case licenseExists:
                if (ArtifactoryHome.get().isHaConfigured()) {
                    return "The license already exists in the pool.";
                } else {
                    return "License already exists.";
                }
            case licenseInUse:
                return "Artifactory could not find an available license to replace the selected license with. " +
                        "To delete a license, you can either shut the node down or add a new license and retry.";
            case licenseIsBlacklisted:
                return "The license provided has been blacklisted and is no longer valid for use";
            case licenseInUseByAnotherServiceId:
                return "The license provided is already in use by another Artifactory instance";
            case licenseDoesNotExist:
                return "The license provided doesn't exist. Please check the license provided";
            case licenseValidationCommunicationIssue:
                return "Unable to validate license due to a communication error. See log for further details";
            default:
                return "Error occurred during license verification.";
        }
    }
}
