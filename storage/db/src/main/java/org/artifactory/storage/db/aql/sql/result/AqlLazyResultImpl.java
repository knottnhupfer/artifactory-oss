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
import org.artifactory.aql.model.AqlDomainEnum;
import org.artifactory.aql.model.AqlPermissionProvider;
import org.artifactory.aql.model.AqlRepoProvider;
import org.artifactory.aql.model.DomainSensitiveField;
import org.artifactory.aql.result.AqlLazyResult;
import org.artifactory.aql.result.rows.AqlRowResult;
import org.artifactory.aql.result.rows.populate.ResultPopulationContext;
import org.artifactory.storage.db.aql.sql.builder.query.sql.SqlQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * @author Gidi Shabat
 */
public class AqlLazyResultImpl<T extends AqlRowResult> implements AqlLazyResult<T> {
    private static final Logger log = LoggerFactory.getLogger(AqlLazyResultImpl.class);

    private final long limit;
    private final long offset;
    private final List<DomainSensitiveField> fields;
    private final ResultSet resultSet;
    private final AqlDomainEnum domain;
    private final AqlAction action;
    private final AqlPermissionProvider aqlPermissionProvider;
    private final AqlRepoProvider aqlRepoProvider;

    private AqlLazyResultImpl(ResultSet resultSet, List<DomainSensitiveField> fields, AqlDomainEnum domain, AqlAction action, AqlPermissionProvider aqlPermissionProvider, AqlRepoProvider aqlRepoProvider, long limit, long offset) {
        this.resultSet = resultSet;
        this.fields = fields;
        this.domain = domain;
        this.action = action;
        this.aqlPermissionProvider = aqlPermissionProvider;
        this.aqlRepoProvider = aqlRepoProvider;
        this.limit = limit;
        this.offset = offset;
    }

    public AqlLazyResultImpl(ResultSet resultSet, SqlQuery<T> sqlQuery, AqlPermissionProvider aqlPermissionProvider, AqlRepoProvider aqlRepoProvider) {
        this(resultSet, sqlQuery.getResultFields(), sqlQuery.getDomain(), sqlQuery.getAction(),
                aqlPermissionProvider, aqlRepoProvider, sqlQuery.getLimit(), sqlQuery.getOffset());
    }

    @Override
    public AqlPermissionProvider getPermissionProvider() {
        return aqlPermissionProvider;
    }

    @Override
    public AqlRepoProvider getRepoProvider() {
        return aqlRepoProvider;
    }

    @Override
    public List<DomainSensitiveField> getFields() {
        return fields;
    }

    @Override
    public ResultSet getResultSet() {
        return resultSet;
    }

    @Override
    public long getLimit() {
        return limit;
    }

    @Override
    public long getOffset() {
        return offset;
    }

    @Override
    public AqlDomainEnum getDomain() {
        return domain;
    }

    @Override
    public AqlAction getAction() {
        return action;
    }

    @Override
    public void close()  {
        try {
            if(resultSet != null) {
                resultSet.close();
            }
        } catch (SQLException e) {
            log.error("Failed to close AQL result: ", e);
        }
    }

    @Override
    public Stream<T> asStream(Consumer<Exception> onFinish) {
        ResultPopulationContext resultContext = new ResultPopulationContext(resultSet, fields, aqlRepoProvider);
        return new AqlStreamResultAdapter<T>(getResultSet(), resultContext, action).asStream(onFinish);
    }

}
