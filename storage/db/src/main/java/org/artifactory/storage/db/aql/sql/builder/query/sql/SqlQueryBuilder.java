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

import com.google.common.collect.Maps;
import org.artifactory.aql.AqlException;
import org.artifactory.aql.model.AqlDomainEnum;
import org.artifactory.aql.result.rows.AqlRowResult;
import org.artifactory.storage.db.aql.sql.builder.query.aql.AqlQuery;
import org.artifactory.storage.db.aql.sql.builder.query.aql.AqlQueryElement;
import org.artifactory.storage.db.aql.sql.builder.query.aql.Criterion;
import org.artifactory.storage.db.aql.sql.builder.query.sql.type.*;
import org.artifactory.storage.db.util.querybuilder.ArtifactoryQueryWriter;
import org.jfrog.security.util.Pair;
import org.jfrog.storage.util.querybuilder.QueryWriter;

import java.util.List;
import java.util.Map;

/**
 * The Class converts AqlQuery into sql query
 * Basically the query is ANSI SQL except the limit and the offset
 *
 * @author Gidi Shabat
 */
public class SqlQueryBuilder {
    private Map<AqlDomainEnum, BasicSqlGenerator> sqlGeneratorMap;

    public SqlQueryBuilder() {
        sqlGeneratorMap = Maps.newHashMap();
        sqlGeneratorMap.put(AqlDomainEnum.items, new ArtifactsSqlGenerator());
        sqlGeneratorMap.put(AqlDomainEnum.properties, new PropertiesSqlGenerator());
        sqlGeneratorMap.put(AqlDomainEnum.archives, new ArchiveSqlGenerator());
        sqlGeneratorMap.put(AqlDomainEnum.entries, new ArchiveEntrySqlGenerator());
        sqlGeneratorMap.put(AqlDomainEnum.statistics, new StatisticsSqlGenerator());
        sqlGeneratorMap.put(AqlDomainEnum.artifacts, new BuildArtifactSqlGenerator());
        sqlGeneratorMap.put(AqlDomainEnum.dependencies, new BuildDependenciesSqlGenerator());
        sqlGeneratorMap.put(AqlDomainEnum.modules, new BuildModuleSqlGenerator());
        sqlGeneratorMap.put(AqlDomainEnum.moduleProperties, new BuildModulePropertySqlGenerator());
        sqlGeneratorMap.put(AqlDomainEnum.buildProperties, new BuildPropertySqlGenerator());
        sqlGeneratorMap.put(AqlDomainEnum.buildPromotions, new BuildsPromotionsSqlGeneratot());
        sqlGeneratorMap.put(AqlDomainEnum.builds, new BuildSqlGenerator());
        sqlGeneratorMap.put(AqlDomainEnum.releaseBundles, new ReleaseBundleSqlGenerator());
        sqlGeneratorMap.put(AqlDomainEnum.releaseBundleFiles, new ReleaseBundleFileSqlGenerator());
    }

    private static <T extends AqlRowResult> boolean isWhereClauseExist(AqlQuery<T> aqlQuery) {
        List<AqlQueryElement> elements = aqlQuery.getAqlElements();
        for (AqlQueryElement element : elements) {
            if (element instanceof Criterion) {
                return true;
            }
        }
        return false;
    }


    public <T extends AqlRowResult> SqlQuery<T> buildQuery(AqlQuery<T> aqlQuery) throws AqlException {
        QueryWriter queryWriter = new ArtifactoryQueryWriter();
        AqlDomainEnum domain = aqlQuery.getDomain();
        SqlQuery<T> sqlQuery = new SqlQuery<>(domain);
        BasicSqlGenerator generator = sqlGeneratorMap.get(domain);
        generateSqlQuery(aqlQuery, generator, sqlQuery, queryWriter);
        sqlQuery.setResultFields(aqlQuery.getResultFields());
        sqlQuery.setLimit(aqlQuery.getLimit());
        sqlQuery.setOffset(aqlQuery.getOffset());
        sqlQuery.setAction(aqlQuery.getAction());
        return sqlQuery;
    }

    private <T extends AqlRowResult> void generateSqlQuery(AqlQuery<T> aqlQuery, BasicSqlGenerator handler, SqlQuery<T> query, QueryWriter queryWriter) throws AqlException {
        // Generate the result part of the query
        queryWriter.select(handler.results(aqlQuery));
        if (aqlQuery.isDistinct()) {
            queryWriter.distinct();
        }
        // Generate the from part of the query
        queryWriter.from(handler.tables(aqlQuery));
        // Add where clause if needed
        boolean whereClause = SqlQueryBuilder.isWhereClauseExist(aqlQuery);
        if (whereClause) {
            Pair<String, List<Object>> filter = handler.conditions(aqlQuery);
            queryWriter.where(filter.getFirst());
            query.setParams(filter.getSecond());
        }
        // Generate the sort part of the query
        String sort = handler.sort(aqlQuery);
        if (sort != null) {
            queryWriter.orderBy(sort);
        }
        // Generate offset and limit
        long offset = aqlQuery.getOffset();
        long limit = aqlQuery.getLimit();
        queryWriter.offset(offset);
        queryWriter.limit(limit);
        query.setQuery(queryWriter.build());
    }
}
