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

package org.artifactory.security.props.auth.model;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * @author Chen Keinan
 */
public class OauthErrorModel implements OauthModel {

    private int statusCode;
    private OauthErrorEnum internalErrorMsg;
    private String message;
    @JsonProperty("documentation_url")
    private String documentationUrl;

    public OauthErrorModel() {
    }

    public OauthErrorModel(int statusCode, OauthErrorEnum internalErrorMsg) {
        this.statusCode = statusCode;
        this.internalErrorMsg = internalErrorMsg;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public OauthErrorEnum getInternalErrorMsg() {
        return internalErrorMsg;
    }

    public void setInternalErrorMsg(OauthErrorEnum internalErrorMsg) {
        this.internalErrorMsg = internalErrorMsg;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDocumentationUrl() {
        return documentationUrl;
    }

    public void setDocumentationUrl(String documentationUrl) {
        this.documentationUrl = documentationUrl;
    }
}
