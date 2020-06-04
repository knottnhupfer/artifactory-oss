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

package org.artifactory.api.rest.keys;

import lombok.ToString;
import org.artifactory.api.util.LongFromDateDeserializer;
import org.artifactory.api.util.LongToDateSerializer;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import static org.jfrog.common.ArgUtils.requireNonBlank;
import static org.jfrog.common.ArgUtils.requireSatisfies;

/**
 * @author Rotem Kfir
 */
@ToString
public class TrustedKeyResponse {

    @JsonProperty
    private String kid;

    @JsonProperty("key")
    private String trustedKey;

    @JsonProperty
    private String fingerprint;

    @JsonProperty
    private String alias;

    @JsonProperty("issued_on")
    @JsonSerialize(using = LongToDateSerializer.class)
    @JsonDeserialize(using = LongFromDateDeserializer.class)
    private Long issued;

    @JsonProperty("issued_by")
    private String issuedBy;

    @JsonProperty("valid_until")
    @JsonSerialize(using = LongToDateSerializer.class)
    @JsonDeserialize(using = LongFromDateDeserializer.class)
    private Long expiry;

    // Used for Json mapping
    public TrustedKeyResponse() {
    }

    public TrustedKeyResponse(String kid, String trustedKey, String fingerprint, String alias, Long issued,
            String issuedBy, Long expiry) {
        this.kid = requireNonBlank(kid, "key ID is required");
        this.trustedKey = requireNonBlank(trustedKey, "Trusted key is required");
        this.fingerprint = requireNonBlank(fingerprint, "Fingerprint is required");
        this.alias = alias;
        this.issued = requireSatisfies(issued, v -> (v == null || v > 0), "Issued time must be positive");
        this.issuedBy = issuedBy;
        this.expiry = requireSatisfies(expiry, v -> (v == null || v == 0 || (v > 0 && (issued == null || v > issued))), "Expiry should be after issue date");
    }

    public String getKid() {
        return kid;
    }

    public String getTrustedKey() {
        return trustedKey;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public String getAlias() {
        return alias;
    }

    public Long getIssued() {
        return issued;
    }

    public String getIssuedBy() {
        return issuedBy;
    }

    public Long getExpiry() {
        return expiry;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String kid;
        private String trustedKey;
        private String fingerprint;
        private String alias;
        private Long issued;
        private String issuedBy;
        private Long expiry;

        public Builder setKid(String kid) {
            this.kid = kid;
            return this;
        }

        public Builder setTrustedKey(String trustedKey) {
            this.trustedKey = trustedKey;
            return this;
        }

        public Builder setFingerprint(String fingerprint) {
            this.fingerprint = fingerprint;
            return this;
        }

        public Builder setAlias(String alias) {
            this.alias = alias;
            return this;
        }

        public Builder setIssued(Long issued) {
            this.issued = issued;
            return this;
        }

        public Builder setIssuedBy(String issuedBy) {
            this.issuedBy = issuedBy;
            return this;
        }

        public Builder setExpiry(Long expiry) {
            this.expiry = expiry;
            return this;
        }

        public TrustedKeyResponse build() {
            return new TrustedKeyResponse(kid, trustedKey, fingerprint, alias, issued, issuedBy, expiry);
        }
    }
}
