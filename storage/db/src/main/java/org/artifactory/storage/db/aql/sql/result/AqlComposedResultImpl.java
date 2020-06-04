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

package org.artifactory.storage.db.aql.sql.result;

import org.artifactory.aql.action.AqlAction;
import org.artifactory.aql.model.*;
import org.artifactory.aql.result.AqlComposedResult;
import org.artifactory.aql.result.AqlLazyResult;
import org.artifactory.aql.result.rows.AqlRowResult;
import org.artifactory.storage.db.aql.sql.builder.query.aql.AqlQuery;

import java.sql.ResultSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Wrapper for AqlLazyResult including the original Aql query, the extension query and the merged query.
 * This class is used when merging two aql queries. When we merge two queries, during the result processing, we might
 * need some data from the original/extension queries, this object can provide this data.
 *
 * @author Shay Bagants
 */
public class AqlComposedResultImpl implements AqlComposedResult {
    private AqlLazyResult<AqlRowResult> aqlLazyResult;
    private AqlQuery originalMainQueryAqlQuery;
    private AqlQuery extensionAqlQuery;
    private AqlQuery merge;

    public AqlComposedResultImpl(AqlLazyResult<AqlRowResult> aqlLazyResult,
                                 AqlQuery originalMainQueryAqlQuery,
                                 AqlQuery extentionAqlQuery,
                                 AqlQuery merge) {
        this.aqlLazyResult = aqlLazyResult;
        this.originalMainQueryAqlQuery = originalMainQueryAqlQuery;
        this.extensionAqlQuery = extentionAqlQuery;
        this.merge = merge;
    }

    @Override
    public AqlPermissionProvider getPermissionProvider() {
        return aqlLazyResult.getPermissionProvider();
    }

    @Override
    public AqlRepoProvider getRepoProvider() {
        return aqlLazyResult.getRepoProvider();
    }

    @Override
    public List<DomainSensitiveField> getFields() {
        return aqlLazyResult.getFields();
    }

    @Override
    public ResultSet getResultSet() {
        return aqlLazyResult.getResultSet();
    }

    @Override
    public long getLimit() {
        return aqlLazyResult.getLimit();
    }

    @Override
    public long getOffset() {
        return aqlLazyResult.getOffset();
    }

    @Override
    public AqlDomainEnum getDomain() {
        return aqlLazyResult.getDomain();
    }

    @Override
    public AqlAction getAction() {
        return aqlLazyResult.getAction();
    }

    @Override
    public void close() throws Exception {
        aqlLazyResult.close();
    }

    public AqlQuery getOriginalMainQueryAqlQuery() {
        return originalMainQueryAqlQuery;
    }

    public AqlQuery getExtensionAqlQuery() {
        return extensionAqlQuery;
    }

    public AqlQuery getMerge() {
        return merge;
    }

    @Override
    public List<AqlFieldEnum> getOriginalFields() {
        List<DomainSensitiveField> resultFields = originalMainQueryAqlQuery.getResultFields();
        return resultFields.stream().map(DomainSensitiveField::getField).collect(Collectors.toList());
    }

    @Override
    public Stream<AqlRowResult> asStream(Consumer<Exception> onFinish) {
        return aqlLazyResult.asStream(onFinish);
    }
}
