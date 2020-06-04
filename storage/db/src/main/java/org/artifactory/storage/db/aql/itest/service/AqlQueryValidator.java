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

package org.artifactory.storage.db.aql.itest.service;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.artifactory.aql.AqlException;
import org.artifactory.aql.model.*;
import org.artifactory.aql.result.rows.AqlRowResult;
import org.artifactory.storage.db.aql.sql.builder.query.aql.AqlQuery;
import org.artifactory.storage.db.aql.sql.builder.query.aql.AqlQueryElement;
import org.artifactory.storage.db.aql.sql.builder.query.aql.Criterion;
import org.artifactory.storage.db.aql.sql.builder.query.aql.SortDetails;
import org.artifactory.storage.db.aql.sql.builder.query.sql.AqlToSqlQueryBuilderException;
import org.artifactory.storage.db.aql.sql.builder.query.sql.SqlTable;
import org.artifactory.storage.db.aql.sql.model.SqlTableEnum;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.artifactory.aql.model.AqlPhysicalFieldEnum.*;

/**
 * @author Gidi Shabat
 */
public class AqlQueryValidator {

    public <T extends AqlRowResult> void validate(AqlQuery<T> aqlQuery, AqlPermissionProvider permissionProvider) {
        // Assert that all the sort fields are unique
        assertSortFieldsAreUnique(aqlQuery);
        // Assert that all the sort fields exist in the result fields
        assertSortFieldsInResult(aqlQuery);
        // Assert that the result fields contains the repo, path and name fields for permissions needs
        assertMinimalResultFields(aqlQuery, permissionProvider);
        // Assert that the if the result fields contain the virtual_repos field then its required fields are also included
        assertRequiredResultFieldsWhenVirtualReposIncluded(aqlQuery);
        // Block property result filter for OSS
        blockPropertyResultFilterForOSS(aqlQuery, permissionProvider);
        // Block sorting for OSS
        blockSortingForOSS(aqlQuery, permissionProvider);
        // Some actions are not supported on all domains
        assertActionSupportedForDomain(aqlQuery);
    }

    private <T extends AqlRowResult> void assertMinimalResultFields(AqlQuery<T> aqlQuery, AqlPermissionProvider permissionProvider) {
        if (permissionProvider.isAdmin()) {
            return;
        }
        if (!AqlDomainEnum.items.equals(aqlQuery.getDomain())) {
            return;
        }
        List<AqlPhysicalFieldEnum> resultFields = Lists.transform(aqlQuery.getResultFields(), toAqlPhysicalFieldEnumOrNull);
        List<AqlPhysicalFieldEnum> minimalResultFields = Lists.newArrayList(itemRepo, itemPath, itemName);
        for (AqlPhysicalFieldEnum fieldEnum : minimalResultFields) {
            if (!resultFields.contains(fieldEnum)) {
                throw new AqlToSqlQueryBuilderException(
                        "For permissions reasons AQL demands the following fields: repo, path and name.");
            }
        }
    }

    private <T extends AqlRowResult> void assertRequiredResultFieldsWhenVirtualReposIncluded(AqlQuery<T> aqlQuery) {
        if (!AqlDomainEnum.items.equals(aqlQuery.getDomain())) {
            return;
        }
        List<AqlFieldEnum> resultFields = aqlQuery.getResultFields().stream()
                .map(DomainSensitiveField::getField)
                .collect(Collectors.toList());
        if (resultFields.contains(AqlLogicalFieldEnum.itemVirtualRepos)) {
            List<AqlPhysicalFieldEnum> requiredResultFields = Lists.newArrayList(itemRepo, itemPath, itemName);
            for (AqlPhysicalFieldEnum fieldEnum : requiredResultFields) {
                if (!resultFields.contains(fieldEnum)) {
                    throw new AqlToSqlQueryBuilderException(
                            "When including virtual_repos field, the following fields are also required: repo, path and name.");
                }
            }
        }
    }

    private <T extends AqlRowResult> void assertSortFieldsAreUnique(AqlQuery<T> aqlQuery) {
        // Assert that all the sort fields are unique
        if (aqlQuery.getSort() != null && aqlQuery.getSort().getFields() != null) {
            List<AqlPhysicalFieldEnum> fields = aqlQuery.getSort().getFields();
            if (fields.stream().distinct().count() != fields.size()) {
                throw new AqlToSqlQueryBuilderException(
                        "Duplicate fields, all the fields in the sort section should be unique.");
            }
        }
    }

    private <T extends AqlRowResult> void assertSortFieldsInResult(AqlQuery<T> aqlQuery) {
        List<AqlPhysicalFieldEnum> resultFields = Lists.transform(aqlQuery.getResultFields(), toAqlPhysicalFieldEnumOrNull);
        if (aqlQuery.getSort() != null && aqlQuery.getSort().getFields() != null) {
            List<AqlPhysicalFieldEnum> sortFields = aqlQuery.getSort().getFields();
            for (AqlPhysicalFieldEnum sortField : sortFields) {
                if (!resultFields.contains(sortField)) {
                    throw new AqlToSqlQueryBuilderException(
                            "Only the result fields are allowed to use in the sort section.");
                }
            }
        }
    }

    private static final Function<DomainSensitiveField, AqlPhysicalFieldEnum> toAqlPhysicalFieldEnumOrNull =
            new Function<DomainSensitiveField, AqlPhysicalFieldEnum>() {
        private final AqlFieldEnumSwitch<AqlPhysicalFieldEnum> physicalFieldOrNull = new AqlFieldEnumSwitch<AqlPhysicalFieldEnum>() {
            @Override
            public AqlPhysicalFieldEnum caseOf(AqlLogicalFieldEnum fieldEnum) {
                return null;
            }
            @Override
            public AqlPhysicalFieldEnum caseOf(AqlPhysicalFieldEnum fieldEnum) {
                return fieldEnum;
            }
        };
        @Nullable
        @Override
        public AqlPhysicalFieldEnum apply(@Nullable DomainSensitiveField domainSensitiveField) {
            if (domainSensitiveField != null) {
                return domainSensitiveField.getField().doSwitch(physicalFieldOrNull);
            } else {
                return null;
            }
        }
    };

    private void blockSortingForOSS(AqlQuery aqlQuery, AqlPermissionProvider permissionProvider) {
        if (permissionProvider.isOss()) {
            SortDetails sort = aqlQuery.getSort();
            if (sort != null && sort.getFields() != null && sort.getFields().size() > 0) {
                throw new AqlException("Sorting is not supported by AQL in the open source version\n");
            }
        }
    }

    private <T extends AqlRowResult> void blockPropertyResultFilterForOSS(AqlQuery<T> aqlQuery, AqlPermissionProvider permissionProvider) {
        if (permissionProvider.isOss()) {
            List<AqlQueryElement> aqlElements = aqlQuery.getAqlElements();
            for (AqlQueryElement aqlElement : aqlElements) {
                if (aqlElement instanceof Criterion) {
                    SqlTable table1 = ((Criterion) aqlElement).getTable1();
                    if ((SqlTableEnum.node_props == table1.getTable() && table1.getId() < SqlTable.MINIMAL_DYNAMIC_TABLE_ID)||
                            (SqlTableEnum.build_props== table1.getTable() && table1.getId() < SqlTable.MINIMAL_DYNAMIC_TABLE_ID)||
                            (SqlTableEnum.module_props == table1.getTable() && table1.getId() < SqlTable.MINIMAL_DYNAMIC_TABLE_ID)) {
                        throw new AqlException(
                                "Filtering properties result is not supported by AQL in the open source version\n");
                    }
                }
            }
        }
    }

    private void assertActionSupportedForDomain(AqlQuery aqlQuery) {
        if (aqlQuery.getAction() == null) {
            throw new AqlException("No action was given in this query.");
        }
        if (!aqlQuery.getAction().supportsDomain(aqlQuery.getDomain())) {
            throw new AqlException("The action '" + aqlQuery.getAction().getName() + "' is unsupported for domain '" +
                    Arrays.toString(aqlQuery.getDomain().subDomains));
        }
    }
}
