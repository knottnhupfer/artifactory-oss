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

package org.artifactory.aql.model;

import java.util.List;
import java.util.stream.Stream;

/**
 * @author Gidi Shabat
 */
public class DomainSensitiveField {

    private AqlFieldEnum field;
    private List<AqlDomainEnum> subDomains;

    public DomainSensitiveField(AqlFieldEnum field, List<AqlDomainEnum> domains) {
        this.field = field;
        subDomains = domains;
    }

    public AqlFieldEnum getField() {
        return field;
    }

    public void setField(AqlFieldEnum field) {
        this.field = field;
    }

    public List<AqlDomainEnum> getSubDomains() {
        return subDomains;
    }

    public void setSubDomains(List<AqlDomainEnum> subDomains) {
        this.subDomains = subDomains;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DomainSensitiveField that = (DomainSensitiveField) o;

        if (field != that.field) {
            return false;
        }
        return subDomains != null ? subDomains.equals(that.subDomains) : that.subDomains == null;
    }

    @Override
    public int hashCode() {
        int result = field != null ? field.hashCode() : 0;
        result = 31 * result + (subDomains != null ? subDomains.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        subDomains.forEach(domain ->
            Stream.of(domain.subDomains).forEach(subDomain ->
                    sb.append(domain.signature).append(".")
            )
        );
        sb.append(field.getSignature());
        return sb.toString();
    }
}
