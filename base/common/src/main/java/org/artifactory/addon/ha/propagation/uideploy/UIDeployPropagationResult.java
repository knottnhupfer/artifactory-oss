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

package org.artifactory.addon.ha.propagation.uideploy;

import org.artifactory.addon.ha.propagation.PropagationResult;

import javax.annotation.Nullable;

/**
 * @author Dan Feldman
 * @author Yinon Avraham
 */
public class UIDeployPropagationResult implements PropagationResult<String> {

    private final int statusCode;
    private final String errorMessage;
    private final String content;

    public UIDeployPropagationResult(int statusCode, String content, String errorMessage) {
        this.statusCode = statusCode;
        this.errorMessage = errorMessage;
        this.content = content;
    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }

    @Nullable
    @Override
    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String getContent() {
        return content;
    }
}
