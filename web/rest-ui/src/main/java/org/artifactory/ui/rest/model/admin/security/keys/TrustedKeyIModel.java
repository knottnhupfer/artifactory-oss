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

package org.artifactory.ui.rest.model.admin.security.keys;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.artifactory.rest.common.model.BaseModel;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * @author Inbar Tal
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrustedKeyIModel extends BaseModel {
    @JsonProperty
    private String kid;
    @JsonProperty("public_key")
    private String trustedKey;
    @JsonProperty
    private String alias;
    @JsonProperty
    private String fingerprint;
    @JsonProperty
    private Long issued;
    @JsonProperty
    private String issuedBy;
    @JsonProperty
    private Long expiry;
}
