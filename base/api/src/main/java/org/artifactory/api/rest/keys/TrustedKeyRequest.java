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

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.codehaus.jackson.annotate.JsonProperty;

import static org.jfrog.common.ArgUtils.requireNonBlank;

/**
 * @author Rotem Kfir
 */
@Data
@NoArgsConstructor
@ToString
public class TrustedKeyRequest {

    @JsonProperty("key")
    private String trustedKey;

    @JsonProperty("public_key")
    private String publicTrustedKey;

    @JsonProperty
    private String alias;

    public TrustedKeyRequest(String trustedKey, String alias) {
        this.trustedKey = requireNonBlank(trustedKey, "Trusted key is required");
        this.alias = alias;
    }

    public String getTrustedKey() {
        if (trustedKey != null) {
            return trustedKey;
        }
        return publicTrustedKey;
    }

    public String getAlias() {
        return alias;
    }

    public static Builder builder() {
        return new Builder();
    }

    @NoArgsConstructor
    public static class Builder {
        private String trustedKey;
        private String alias;

        public Builder setTrustedKey(String trustedKey) {
            this.trustedKey = trustedKey;
            return this;
        }

        public Builder setAlias(String alias) {
            this.alias = alias;
            return this;
        }

        public TrustedKeyRequest build() {
            return new TrustedKeyRequest(trustedKey, alias);
        }
    }
}
