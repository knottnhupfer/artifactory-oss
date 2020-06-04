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
 * @author Chenk Keinan
 */
public class AuthenticationModel implements OauthModel {

    private String token;
    @JsonProperty("expires_in")
    private Integer expiresIn;
    @JsonProperty("issued_at")
    private String issuedAt;
    // FIXME (by RotemK): issuedAt is not always populated with the real timestamp, and also it is not really used in the http response...

    public AuthenticationModel() {
    }

    public AuthenticationModel(String token, String issuedAt) {
        this(token, issuedAt, 3600);
    }

    public AuthenticationModel(String token, String issuedAt, Integer expiresIn) {
        this.token = token;
        this.issuedAt = issuedAt;
        this.expiresIn = expiresIn;
    }


    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Integer getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Integer expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(String issuedAt) {
        this.issuedAt = issuedAt;
    }
}
