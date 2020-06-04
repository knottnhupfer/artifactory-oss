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

package org.artifactory.ui.rest.model.admin.security.accesstokens;

import org.artifactory.api.security.access.TokenInfo;
import org.artifactory.rest.common.model.BaseModel;
import org.artifactory.rest.common.util.RestUtils;

import static org.artifactory.api.security.access.UserTokenSpec.extractUsername;
import static org.artifactory.api.security.access.UserTokenSpec.isUserTokenSubject;

/**
 * @author Yinon Avraham.
 */
public class AccessTokenUIModel extends BaseModel {

    private String tokenId;
    private String issuer;
    private String subject;
    private String issuedAt; //ISO8601 date format
    private String expiry; //ISO8601 date format
    private boolean refreshable;

    public AccessTokenUIModel() {}

    public AccessTokenUIModel(TokenInfo tokenInfo) {
        this.tokenId = tokenInfo.getTokenId();
        this.issuer = tokenInfo.getIssuer();
        this.subject = isUserTokenSubject(tokenInfo.getSubject()) ?
                extractUsername(tokenInfo.getSubject()) :
                tokenInfo.getSubject();
        this.issuedAt = toIsoDate(tokenInfo.getIssuedAt());
        this.expiry = toIsoDate(tokenInfo.getExpiry());
        this.refreshable = tokenInfo.isRefreshable();
    }

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(String issuedAtIsoDate) {
        this.issuedAt = issuedAtIsoDate;
    }

    public String getExpiry() {
        return expiry;
    }

    public void setExpiry(String expiryIsoDate) {
        this.expiry = expiryIsoDate;
    }

    public boolean isRefreshable() {
        return refreshable;
    }

    public void setRefreshable(boolean refreshable) {
        this.refreshable = refreshable;
    }

    private static String toIsoDate(Long timeInMillis) {
        return timeInMillis == null ? null : RestUtils.toIsoDateString(timeInMillis);
    }
}
