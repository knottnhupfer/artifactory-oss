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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.artifactory.aql.util.AqlUtils.arrayOf;

/**
 * @author Gidi Shabat
 */
public enum AqlDomainEnum {

    items("item", arrayOf("items")),
    properties("property", arrayOf("properties")),
    statistics("stat", arrayOf("stats")),
    archives("archive", arrayOf("archives")),
    entries("entry", arrayOf("archive","entries")),
    builds("build", arrayOf("builds")),
    artifacts("artifact", arrayOf("artifacts")),
    dependencies("dependency", arrayOf("dependencies")),
    modules("module", arrayOf("modules")),
    buildProperties("property", arrayOf("build", "properties")),
    buildPromotions("promotion", arrayOf("build", "promotions")),
    moduleProperties("property", arrayOf("module","properties")),
    releaseBundles("release", arrayOf("releases")),
    releaseBundleFiles("release_artifact", arrayOf("release_artifacts")),
    ;

    public final String signature;
    public final String[] subDomains;

    AqlDomainEnum(String signature, String[] subDomains) {
        this.signature = signature;
        this.subDomains = subDomains;
    }

    public static AqlDomainEnum valueFromSubDomains(List<String> subDomains) {
        String[] externalSubDomain = subDomains.toArray(new String[subDomains.size()]);
        for (AqlDomainEnum aqlDomainEnum : values()) {
            if (Arrays.equals(aqlDomainEnum.subDomains, externalSubDomain)) {
                return aqlDomainEnum;
            }
        }
        return null;
    }

    public AqlFieldEnum[] getDefaultResultFields() {
        //Currently only physical fields can be default fields
        List<? extends AqlFieldEnum> fields = Stream.of(AqlPhysicalFieldEnum.getFieldsByDomain(this))
                .filter(AqlPhysicalFieldEnum::isDefaultResultField)
                .collect(Collectors.toList());
        return fields.toArray(new AqlFieldEnum[fields.size()]);
    }

    public AqlPhysicalFieldEnum[] getPhysicalFields() {
        return AqlPhysicalFieldEnum.getFieldsByDomain(this);
    }

    public AqlLogicalFieldEnum[] getLogicalFields() {
        return AqlLogicalFieldEnum.getFieldsByDomain(this);
    }

    public AqlFieldEnum[] getAllFields() {
        List<AqlFieldEnum> fields = Stream.concat(Stream.of(getPhysicalFields()), Stream.of(getLogicalFields()))
                .collect(Collectors.toList());
        return fields.toArray(new AqlFieldEnum[fields.size()]);
    }

    public AqlPhysicalFieldEnum resolvePhysicalField(String fieldSignature) {
        return Stream.of(getPhysicalFields())
                .filter(field -> field.getSignature().equals(fieldSignature))
                .findFirst()
                .orElse(null);
    }

    public AqlFieldEnum resolveField(String fieldSignature) {
        return Stream.concat(Stream.of(getPhysicalFields()), Stream.of(getLogicalFields()))
                .filter(field -> field.getSignature().equals(fieldSignature))
                .findFirst()
                .orElse(null);
    }
}