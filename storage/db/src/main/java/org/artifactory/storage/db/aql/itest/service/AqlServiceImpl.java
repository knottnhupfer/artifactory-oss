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

import com.google.common.collect.Sets;
import org.artifactory.aql.AqlService;
import org.artifactory.aql.api.internal.AqlBase;
import org.artifactory.aql.model.AqlOperatorEnum;
import org.artifactory.aql.model.AqlPermissionProvider;
import org.artifactory.aql.model.AqlRepoProvider;
import org.artifactory.aql.model.DomainSensitiveField;
import org.artifactory.aql.result.AqlComposedResult;
import org.artifactory.aql.result.AqlEagerResult;
import org.artifactory.aql.result.AqlLazyResult;
import org.artifactory.aql.result.rows.AqlRowResult;
import org.artifactory.common.config.db.ArtifactoryDbProperties;
import org.artifactory.storage.db.aql.dao.AqlDao;
import org.artifactory.storage.db.aql.itest.service.decorator.*;
import org.artifactory.storage.db.aql.parser.AqlParser;
import org.artifactory.storage.db.aql.parser.ParserElementResultContainer;
import org.artifactory.storage.db.aql.sql.builder.query.aql.*;
import org.artifactory.storage.db.aql.sql.builder.query.sql.SqlQuery;
import org.artifactory.storage.db.aql.sql.builder.query.sql.SqlQueryBuilder;
import org.artifactory.storage.db.aql.sql.result.AqlComposedResultImpl;
import org.artifactory.storage.db.aql.sql.result.AqlEagerResultImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Set;

/**
 * Execute the Aql queries by processing the three Aql steps one after the other:
 * Step 1 Convert the AqlApi or the parser result into AqlQuery.
 * Step 2 Convert the AqlQuery into SqlQuery.
 * Step 3 Execute the SqlQuery and return the results.
 *
 * @author Gidi Shabat
 */
@Service
public class AqlServiceImpl implements AqlService {
    private static final Logger log = LoggerFactory.getLogger(AqlServiceImpl.class);

    @Autowired
    private AqlDao aqlDao;

    @Autowired
    private ArtifactoryDbProperties storageProperties;

    private AqlParser parser;
    private ParserToAqlAdapter parserToAqlAdapter;
    private AqlApiToAqlAdapter aqlApiToAqlAdapter;
    private SqlQueryBuilder sqlQueryBuilder;
    private AqlQueryOptimizer optimizer;
    private AqlQueryValidator validator;
    private AqlQueryDecorator externalDecorator; //Decorator used for user queries
    private AqlQueryDecorator internalDecorator; //Decorator used for internal api queries
    private AqlPermissionProvider permissionProvider = new AqlPermissionProviderImpl();
    private AqlRepoProvider repoProvider = new AqlRepoProviderImpl();

    @PostConstruct
    private void initDb(){
        // The parser is constructed by many internal elements therefore we create it once and then reuse it.
        // Please note that it doesn't really have state therefore we can use it simultaneously
        // TODO init the parser eagerly here not lazy
        parser = new AqlParser();
        parserToAqlAdapter = new ParserToAqlAdapter();
        sqlQueryBuilder = new SqlQueryBuilder();
        aqlApiToAqlAdapter = new AqlApiToAqlAdapter();
        optimizer = new AqlQueryOptimizer(storageProperties.getDbType());
        validator = new AqlQueryValidator();
        externalDecorator = new AqlQueryDecorator(new DefaultSortDecorator(), new TrashcanDecorator(), new SupportBundlesRepoDecorator(),
                new VirtualRepoCriteriaDecorator());
        internalDecorator = new AqlQueryDecorator(new TrashcanDecorator(),  new SupportBundlesRepoDecorator(), new VirtualRepoCriteriaDecorator());
    }

    /**
     * Converts the Json query into SQL query and executes the query eagerly
     */
    @Override
    public AqlEagerResult executeQueryEager(String query) {
        log.debug("Processing textual AqlApi query: {}", query);
        ParserElementResultContainer parserResult = parser.parse(query);
        return executeQueryEager(parserResult);
    }

    /**
     * Converts the Json query into SQL query and executes the query lazy
     */
    @Override
    public AqlLazyResult<AqlRowResult> executeQueryLazy(String query) {
        log.debug("Processing textual AqlApi query: {}", query);
        ParserElementResultContainer parserResult = parser.parse(query);
        return executeQueryLazy(parserResult);
    }

    /**
     * Converts the Json query into SQL query and executes the query lazy
     */
    @Override
    public <T extends AqlRowResult> AqlComposedResult executeQueryLazy(String originalMainQuery, AqlOperatorEnum operatorEnum,
                                                                       AqlBase<? extends AqlBase, T> extensionQuery) {
        log.debug("Processing textual AqlApi query {} and API AqlApi", originalMainQuery);
        ParserElementResultContainer parserResult = parser.parse(originalMainQuery);
        AqlQuery<AqlRowResult> originalMainAqlQuery = parserToAqlAdapter.toAqlModel(parserResult);
        AqlQuery<AqlRowResult> extensionAqlQuery = aqlApiToAqlAdapter.toAqlModel(extensionQuery);
        log.debug("Attempting to merge original query with the extension query");
        AqlQuery<AqlRowResult> mergedAqlQuery = merge(originalMainAqlQuery, extensionAqlQuery, operatorEnum);
        optimizer.optimize(mergedAqlQuery);
        validator.validate(mergedAqlQuery, permissionProvider);
        externalDecorator.decorate(mergedAqlQuery, new AqlQueryDecoratorContext(repoProvider, permissionProvider));
        log.trace("Successfully finished to convert the parser result into AqlApi query");
        return new AqlComposedResultImpl(getAqlQueryStreamResult(mergedAqlQuery), originalMainAqlQuery,
                extensionAqlQuery, mergedAqlQuery);
    }

    /**
     * Converts the API's AqlApi query into SQL query and executes the query eagerly
     */
    @Override
    public <T extends AqlRowResult> AqlEagerResult<T> executeQueryEager(AqlBase<? extends AqlBase, T> aql) {
        log.debug("Processing API AqlApi query");
        AqlQuery<T> aqlQuery = aqlApiToAqlAdapter.toAqlModel(aql);
        optimizer.optimize(aqlQuery);
        internalDecorator.decorate(aqlQuery, new AqlQueryDecoratorContext(repoProvider, permissionProvider));
        return getAqlQueryResult(aqlQuery);
    }

    @Override
    public <T extends AqlRowResult> AqlLazyResult<T> executeQueryLazy(AqlBase<? extends AqlBase, T> aql) {
        log.debug("Processing API AqlApi query");
        AqlQuery<T> aqlQuery = aqlApiToAqlAdapter.toAqlModel(aql);
        optimizer.optimize(aqlQuery);
        internalDecorator.decorate(aqlQuery, new AqlQueryDecoratorContext(repoProvider, permissionProvider));
        return getAqlQueryStreamResult(aqlQuery);
    }

    /**
     * Converts the parser elements into AqlApi query, convert the AqlApi query to sql and executes the query eagerly
     */
    private <T extends AqlRowResult> AqlEagerResult<T> executeQueryEager(ParserElementResultContainer parserResult) {
        log.trace("Converting the parser result into AqlApi query");
        AqlQuery<T> aqlQuery = parserToAqlAdapter.toAqlModel(parserResult);
        optimizer.optimize(aqlQuery);
        validator.validate(aqlQuery, permissionProvider);
        externalDecorator.decorate(aqlQuery, new AqlQueryDecoratorContext(repoProvider, permissionProvider));
        log.trace("Successfully finished to convert the parser result into AqlApi query");
        return getAqlQueryResult(aqlQuery);
    }

    /**
     * Converts the parser elements into AqlApi query and executes the query lazy
     */
    private <T extends AqlRowResult> AqlLazyResult<T> executeQueryLazy(ParserElementResultContainer parserResult) {
        log.trace("Converting the parser result into AqlApi query");
        AqlQuery<T> aqlQuery = parserToAqlAdapter.toAqlModel(parserResult);
        optimizer.optimize(aqlQuery);
        validator.validate(aqlQuery, permissionProvider);
        externalDecorator.decorate(aqlQuery, new AqlQueryDecoratorContext(repoProvider, permissionProvider));
        log.trace("Successfully finished to convert the parser result into AqlApi query");
        return getAqlQueryStreamResult(aqlQuery);
    }

    /**
     * Converts the AqlApi query into SQL query and executes the query eagerly
     */
    private <T extends AqlRowResult> AqlEagerResult<T> getAqlQueryResult(AqlQuery<T> aqlQuery) {
        log.trace("Converting the AqlApi query into SQL query: {}", aqlQuery);
        SqlQuery<T> sqlQuery = sqlQueryBuilder.buildQuery(aqlQuery);
        log.trace("Successfully finished to convert the parser result into the following SQL query '{}'", sqlQuery);
        log.trace("processing the following SQL query: {}", sqlQuery);
        AqlEagerResultImpl<T> aqlQueryResult = aqlDao.executeQueryEager(sqlQuery, repoProvider);
        log.debug("Successfully finished to process SQL query with the following size: {}", aqlQueryResult.getSize());
        return aqlQueryResult;
    }

    private <T extends AqlRowResult> AqlLazyResult<T> getAqlQueryStreamResult(AqlQuery<T> aqlQuery) {
        log.trace("Converting the AqlApi query into SQL query: {}", aqlQuery);
        SqlQuery<T> sqlQuery
                = sqlQueryBuilder.buildQuery(aqlQuery);
        log.trace("Successfully finished to convert the parser result into the following SQL query '{}'", sqlQuery);
        log.trace("processing the following SQL query: {}", sqlQuery);
        AqlLazyResult<T> aqlQueryStreamResult = aqlDao.executeQueryLazy(sqlQuery, permissionProvider, repoProvider);
        log.debug("Successfully finished to process SQL query (lazy)");
        return aqlQueryStreamResult;
    }

    private AqlQuery<AqlRowResult> merge(AqlQuery<AqlRowResult> originalMainQueryAqlQuery, AqlQuery<AqlRowResult> extensionAqlQuery, AqlOperatorEnum operatorEnum) {
        AqlQuery<AqlRowResult> merge = new AqlQuery<>();
        merge.setAction(originalMainQueryAqlQuery.getAction());
        merge.setDomain(originalMainQueryAqlQuery.getDomain());
        merge.setLimit(extensionAqlQuery.getLimit() != Long.MAX_VALUE ? extensionAqlQuery.getLimit() :
                originalMainQueryAqlQuery.getLimit());
        merge.setOffset(extensionAqlQuery.getOffset() != 0 ? extensionAqlQuery.getOffset() :
                originalMainQueryAqlQuery.getOffset());
        merge.setSort(extensionAqlQuery.getSort() != null ? extensionAqlQuery.getSort() :
                originalMainQueryAqlQuery.getSort());
        Set<DomainSensitiveField> mergedFields = Sets.newHashSet();
        mergedFields.addAll(originalMainQueryAqlQuery.getResultFields());
        mergedFields.addAll(extensionAqlQuery.getResultFields());
        merge.getResultFields().addAll(mergedFields);
        if (extensionAqlQuery.getAqlElements().isEmpty()) {
            merge.getAqlElements().addAll(originalMainQueryAqlQuery.getAqlElements());
        } else {
            merge.getAqlElements().add(AqlAdapter.open);
            merge.getAqlElements().addAll(originalMainQueryAqlQuery.getAqlElements());
            merge.getAqlElements().add(AqlAdapter.close);
            merge.getAqlElements().add(getOperator(operatorEnum));
            merge.getAqlElements().add(AqlAdapter.open);
            merge.getAqlElements().addAll(extensionAqlQuery.getAqlElements());
            merge.getAqlElements().add(AqlAdapter.close);
        }
        return merge;
    }

    private AqlQueryElement getOperator(AqlOperatorEnum operatorEnum) {
        switch (operatorEnum) {
            case or:
                return AqlAdapter.or;
            case and:
                return AqlAdapter.and;
            default:
                throw new UnsupportedOperationException("Aql merger support only And / or operations");
        }
    }
}