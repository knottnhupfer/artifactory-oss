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

package org.artifactory.ui.rest.service.artifacts.deploy;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.artifactory.api.repo.exception.RepoRejectException;
import org.artifactory.exception.CancelException;

/**
 * @author Dan Feldman
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class DeployUtil {

    static String getDeployError(String fileName, String repoKey, Exception e) {
        String err = "Failed to deploy file: '" + fileName + "' to Repository: " + repoKey +
                ". Please check the log file for more details.";
        //Unfortunately Spring really messes up the exception that's thrown by the UploadService (was supposed to be
        //RepoRejectException with CancelException as the cause, instead we get RepoRejectException as the cause
        //And a concatenated string of both messages. the return code is also missing.
        if (e instanceof CancelException) {
            err = "Failed to deploy file: '" + fileName + "' to Repository: " + repoKey + ": " + e.getMessage();
        } else if (e instanceof RepoRejectException) {
            if (e.getMessage().contains(CancelException.class.getName())) {
                err = e.getMessage().replace(". " + CancelException.class.getName(), "");
            } else {
                err = e.getMessage();
            }
        }
        return err;
    }
}
