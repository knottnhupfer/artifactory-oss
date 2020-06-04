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

package org.artifactory.storage.db.aql.sql.builder.query.sql;

import com.google.common.collect.Lists;
import org.artifactory.aql.action.AqlAction;
import org.artifactory.aql.model.AqlDomainEnum;
import org.artifactory.aql.model.DomainSensitiveField;
import org.artifactory.aql.result.rows.AqlRowResult;

import java.util.List;


/**
 * The class represent Sql query, it also contains some simple convenient methods that helps to build the query
 *
 * @author Gidi Shabat
 */
public class SqlQuery<T extends AqlRowResult> {

    private String query = "";
    private List<Object> params = Lists.newArrayList();
    private List<DomainSensitiveField> resultFields;
    private long limit;
    private long offset;
    private AqlDomainEnum domain;
    private AqlAction action;

    SqlQuery(AqlDomainEnum domain) {
        this.domain = domain;
    }

    public Object[] getQueryParams() {
        Object[] objects = new Object[params.size()];
        return params.toArray(objects);
    }

    public String getQueryString() {
        return query;
    }

    @Override
    public String toString() {
        return "SqlQuery{" +
                "query='" + query + '\'' +
                ", params=" + params +
                '}';
    }

    public List<DomainSensitiveField> getResultFields() {
        return resultFields;
    }

    public void setResultFields(List<DomainSensitiveField> resultFields) {
        this.resultFields = resultFields;
    }

    public long getLimit() {
        return limit;
    }

    public void setLimit(long limit) {
        this.limit = limit;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public AqlDomainEnum getDomain() {
        return domain;
    }

    public void setParams(List<Object> params) {
        this.params = params;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public AqlAction getAction() {
        return action;
    }

    public void setAction(AqlAction action) {
        this.action = action;
    }
}
