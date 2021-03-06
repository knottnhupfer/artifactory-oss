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

package org.artifactory.model.xstream.keys;

import lombok.*;
import org.artifactory.keys.TrustedKey;

import java.util.Objects;

/**
 * @author Rotem Kfir
 */
@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(builderClassName = "Builder")
public class TrustedKeyImpl implements TrustedKey {
    @NonNull private String kid;
    @NonNull private String trustedKey;
    @NonNull private String fingerprint;
    private String alias;
    private Long issued;
    private String issuedBy;
    private Long expiry;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TrustedKeyImpl)) {
            return false;
        }
        TrustedKeyImpl that = (TrustedKeyImpl) o;
        return Objects.equals(kid, that.kid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kid);
    }
}
