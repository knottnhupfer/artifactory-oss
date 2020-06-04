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

package org.artifactory.rest.resource.token;

import org.artifactory.api.security.access.TokenInfo;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

/**
 * @author Yinon Avraham.
 */
public class TokenInfoModel {

    @JsonProperty("token_id")
    private String tokenId;
    @JsonProperty("issuer")
    private String issuer;
    @JsonProperty("subject")
    private String subject;
    @JsonProperty("issued_at")
    private long issuedAt;
    @JsonProperty("expiry")
    private Long expiry;
    @JsonProperty("refreshable")
    private boolean refreshable;

    public TokenInfoModel() { }

    public TokenInfoModel(@Nonnull TokenInfo tokenInfo) {
        this.tokenId = tokenInfo.getTokenId();
        this.issuer = tokenInfo.getIssuer();
        this.subject = tokenInfo.getSubject();
        this.issuedAt = toEpochInSecs(tokenInfo.getIssuedAt());
        this.expiry = toEpochInSecs(tokenInfo.getExpiry());
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

    public long getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(long issuedAt) {
        this.issuedAt = issuedAt;
    }

    public Long getExpiry() {
        return expiry;
    }

    public void setExpiry(Long expiry) {
        this.expiry = expiry;
    }

    public boolean isRefreshable() {
        return refreshable;
    }

    public void setRefreshable(boolean refreshable) {
        this.refreshable = refreshable;
    }

    private Long toEpochInSecs(Long epochInMillis) {
        return epochInMillis == null ? null : TimeUnit.MILLISECONDS.toSeconds(epochInMillis);
    }
}
