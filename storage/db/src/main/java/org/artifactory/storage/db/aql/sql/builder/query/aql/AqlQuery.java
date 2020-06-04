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

package org.artifactory.storage.db.aql.sql.builder.query.aql;

import com.google.common.collect.Lists;
import org.artifactory.aql.action.AqlAction;
import org.artifactory.aql.model.AqlDomainEnum;
import org.artifactory.aql.model.DomainSensitiveField;
import org.artifactory.aql.result.rows.AqlRowResult;

import java.util.List;

/**
 * The class contains all the information that has been collected from the AqlApi or the Parser
 *
 * @author Gidi Shabat
 */
public class AqlQuery<T extends AqlRowResult> {

    private List<DomainSensitiveField> resultFields = Lists.newArrayList();
    private SortDetails sort;
    private List<AqlQueryElement> aqlElements = Lists.newArrayList();
    private AqlDomainEnum domain;
    private long limit = Long.MAX_VALUE;
    private long offset = 0;
    private AqlAction action;
    private boolean distinct = true;

    public List<DomainSensitiveField> getResultFields() {
        return resultFields;
    }

    public SortDetails getSort() {
        return sort;
    }

    public void setSort(SortDetails sort) {
        this.sort = sort;
    }

    public List<AqlQueryElement> getAqlElements() {
        return aqlElements;
    }

    public AqlDomainEnum getDomain() {
        return domain;
    }

    public void setDomain(AqlDomainEnum domain) {
        this.domain = domain;
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

    public AqlAction getAction() {
        return action;
    }

    public void setAction(AqlAction action) {
        this.action = action;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

}
