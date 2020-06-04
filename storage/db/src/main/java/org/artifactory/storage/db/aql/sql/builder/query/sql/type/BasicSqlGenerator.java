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

package org.artifactory.storage.db.aql.sql.builder.query.sql.type;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.artifactory.aql.AqlException;
import org.artifactory.aql.model.*;
import org.artifactory.aql.result.rows.AqlRowResult;
import org.artifactory.storage.db.aql.sql.builder.links.TableLink;
import org.artifactory.storage.db.aql.sql.builder.links.TableLinkBrowser;
import org.artifactory.storage.db.aql.sql.builder.links.TableLinkRelation;
import org.artifactory.storage.db.aql.sql.builder.query.aql.*;
import org.artifactory.storage.db.aql.sql.builder.query.sql.SqlTable;
import org.artifactory.storage.db.aql.sql.model.AqlFieldExtensionEnum;
import org.artifactory.storage.db.aql.sql.model.SqlTableEnum;
import org.jfrog.security.util.Pair;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;

import static org.artifactory.storage.db.aql.sql.builder.query.sql.type.AqlTableGraph.tablesLinksMap;
import static org.artifactory.storage.db.aql.sql.model.AqlFieldExtensionEnum.getExtensionFor;

/**
 * This is actually the class that contains all the code that converts the AqlQuery to sqlQuery.
 *
 * @author Gidi Shabat
 */
public abstract class BasicSqlGenerator {
    public final Map<SqlTableEnum, Map<SqlTableEnum, List<TableLinkRelation>>> tableRouteMap;

    private static final Function<DomainSensitiveField, DomainSensitiveTable> toTables = new Function<DomainSensitiveField, DomainSensitiveTable>() {
        @Nullable
        @Override
        public DomainSensitiveTable apply(@Nullable DomainSensitiveField input) {
            if (input == null) {
                return null;
            }
            return input.getField().doSwitch(new AqlFieldEnumSwitch<DomainSensitiveTable>() {
                @Override
                public DomainSensitiveTable caseOf(AqlLogicalFieldEnum fieldEnum) {
                    return null;
                }
                @Override
                public DomainSensitiveTable caseOf(AqlPhysicalFieldEnum fieldEnum) {
                    AqlFieldExtensionEnum extension = getExtensionFor(fieldEnum);
                    List<SqlTableEnum> tables = generateTableListFromSubDomainAndField(input.getSubDomains());
                    SqlTable table = tablesLinksMap.get(extension.table).getTable();
                    return new DomainSensitiveTable(table, tables);
                }
            });
        }
    };
    private static final Function<AqlQueryElement, DomainSensitiveTable> firstTableFromCriteria = new Function<AqlQueryElement, DomainSensitiveTable>() {
        @Nullable
        @Override
        public DomainSensitiveTable apply(@Nullable AqlQueryElement input) {
            SqlTable table = input != null ? ((Criterion) input).getTable1() : null;
            if (table != null) {
                List<SqlTableEnum> tables = generateTableListFromSubDomainAndField(((Criterion) input).getSubDomains());
                return new DomainSensitiveTable(table, tables);
            }
            return null;
        }
    };
    private static final Predicate<DomainSensitiveTable> notNull = input -> input != null;
    private static final Predicate<AqlQueryElement> criteriasOnly = input -> input instanceof Criterion;
    private static final Function<DomainSensitiveTable, SqlTableEnum> toTableEnum = new Function<DomainSensitiveTable, SqlTableEnum>() {
        @Nullable
        @Override
        public SqlTableEnum apply(@Nullable DomainSensitiveTable input) {
            return input != null ? input.getTable().getTable() : null;
        }
    };

    /**
     * The constructor scans the table schema and creates a map that contains the shortest route between two tables
     */
    protected BasicSqlGenerator() {
        Map<SqlTableEnum, Map<SqlTableEnum, List<TableLinkRelation>>> routeMap = Maps.newHashMap();
        for (TableLink from : tablesLinksMap.values()) {
            for (TableLink to : tablesLinksMap.values()) {
                List<TableLinkRelation> route = findShortestPathBetween(from, to);
                Map<SqlTableEnum, List<TableLinkRelation>> toRouteMap = routeMap.get(from.getTableEnum());
                if (toRouteMap == null) {
                    toRouteMap = Maps.newHashMap();
                    routeMap.put(from.getTableEnum(), toRouteMap);
                }
                toRouteMap.put(to.getTableEnum(), route);
            }
        }
        tableRouteMap = routeMap;
    }

    /**
     * The method generates the result part of the SQL query
     */
    public String results(AqlQuery aqlQuery) {
        StringBuilder result = new StringBuilder();
        result.append(" ");
        Iterator<DomainSensitiveField> iterator = aqlQuery.getResultFields().iterator();
        boolean[] first = new boolean[] {true};
        while (iterator.hasNext()) {
            DomainSensitiveField nextField = iterator.next();
            nextField.getField().doSwitch(new AqlFieldEnumSwitch<Void>() {
                @Override
                public Void caseOf(AqlLogicalFieldEnum fieldEnum) {
                    return null;
                }
                @Override
                public Void caseOf(AqlPhysicalFieldEnum fieldEnum) {
                    if (!first[0]) {
                        result.append(",");
                    } else {
                        first[0] = false;
                    }
                    AqlFieldExtensionEnum next = getExtensionFor(fieldEnum);
                    SqlTable table = tablesLinksMap.get(next.table).getTable();
                    result.append(table.getAlias()).append(next.tableField);
                    result.append(" as ").append(fieldEnum.name());
                    return null;
                }
            });
        }
        result.append(" ");
        return result.toString();
    }

    /**
     * This is one of the most important and complicated parts in the Aql mechanism
     * Its task is to create the tables declaration part in the SQL query
     * the method does this with the help "sub domains" : Each field in the result fields and in the criteria
     * contains a list of domain that represent the route to the main domain, so basically, in order to bind one field
     * to the other we can trace the sub domains and bind each field to the "Main Table"
     * The problem with tracing the sub domain is that there is no injective match between the tables and the domains
     * therefore we use the tablesLinksMap that contain the shortest route between two tables and help us to ensure
     * that in "threaded form" we will bind all the tables needed from the
     * "Field table" to the "Main table"
     *
     * @param aqlQuery
     * @return
     */
    public String tables(AqlQuery aqlQuery) {
        Set<SqlTable> usedTables = Sets.newHashSet();
        StringBuilder join = new StringBuilder();
        join.append(" ");
        // Get all Result tables
        Iterable<DomainSensitiveTable> resultTables = Iterables.transform(aqlQuery.getResultFields(), toTables);
        // Find all the criterias
        Iterable<AqlQueryElement> filter = Iterables.filter(aqlQuery.getAqlElements(), criteriasOnly);
        // Get the tables from the criterias
        Iterable<DomainSensitiveTable> criteriasTables = Iterables.transform(filter, firstTableFromCriteria);
        // Concatenate the resultTables and the criteriasTables
        Iterable<DomainSensitiveTable> allTables = Iterables.concat(resultTables, criteriasTables);
        // Resolve  Join type (inner join or left outer join) for better performance
        AqlJoinTypeEnum joinTypeEnum = resolveJoinType(allTables);
        // Clean null tables if exists
        allTables = Iterables.filter(allTables, notNull);
        SqlTableEnum[] mainTables = getMainTables();
        Map<Pair<SqlTable, AqlTableFieldsEnum>, Pair<String, String>> overrideJoins = Maps.newHashMap();
        if (mainTables.length > 1) {
            joinMainTables(mainTables, join, usedTables, overrideJoins);
        } else {
            SqlTable mainTable = tablesLinksMap.get(mainTables[0]).getTable();
            // Add the single main table as first table
            appendFirstTable(mainTable, join, usedTables);
        }
        for (DomainSensitiveTable table : allTables) {
            TableLink to;
            // Resolve the first table : which is always the "Main Table"
            SqlTableEnum fromTableEnum = table.getTables().get(0);
            // Find the route to the target ("to") table and add a join for each table in the route
            TableLink from = tablesLinksMap.get(fromTableEnum);
            for (int i = 1; i < table.getTables().size(); i++) {
                SqlTableEnum toTableEnum = table.getTables().get(i);
                to = tablesLinksMap.get(toTableEnum);
                List<TableLinkRelation> relations = tableRouteMap.get(from.getTableEnum()).get(to.getTableEnum());
                generateJoinTables(relations, usedTables, join, joinTypeEnum, overrideJoins);
                from = to;
            }
            // Finally add a join to the field table
            to = tablesLinksMap.get(table.getTable().getTable());
            List<TableLinkRelation> relations = tableRouteMap.get(from.getTableEnum()).get(to.getTableEnum());
            generateJoinTables(relations, usedTables, join, joinTypeEnum, overrideJoins, table.getTable());
        }
        return join.toString()+ " ";
    }

    private void joinMainTables(SqlTableEnum[] mainTables, StringBuilder joinBuilder, Set<SqlTable> usedTables,
            Map<Pair<SqlTable, AqlTableFieldsEnum>, Pair<String, String>> overrideJoins) {
        Map<SqlTableEnum, AqlTableFieldsEnum> tableIdField = Maps.newHashMap();
        /* a union for example:
        (select node_id as id from stats
         union
         select node_id as id from stats_remote) mainTableIds
         */
        String unionAlias = "mainTableIds";
        String unionIdAlias = "id";
        joinBuilder.append("(");
        for (int i = 0; i < mainTables.length; i++) {
            SqlTableEnum table = mainTables[i];
            if (i > 0) {
                joinBuilder.append("union ");
            }
            AqlTableFieldsEnum idField = Stream.of(table.getFields())
                    .filter(field -> field.getExtendedField().isId())
                    .findFirst()
                    .get().tableField;
            tableIdField.put(table, idField);
            joinBuilder.append("select ").append(idField).append(" as ").append(unionIdAlias).append(" from ").append(table.name()).append(" ");
        }
        joinBuilder.append(") ").append(unionAlias);
        /* join main tables with the ids union. for example:
        (select ... id from ... union ...) mainTableIds
        left outer join stats s on s.node_id = mainTableIds.id
        left outer join stats_remote sr on sr.node_id = mainTableIds.id
         */
        for (SqlTableEnum mainTable : mainTables) {
            SqlTable toTable = tablesLinksMap.get(mainTable).getTable();
            AqlTableFieldsEnum tableJoinField = tableIdField.get(toTable.getTable());
            String unionIdFieldPrefix = unionAlias + ".";
            appendTableJoin(toTable, tableJoinField, unionIdFieldPrefix, unionIdAlias, joinBuilder, AqlJoinTypeEnum.leftOuterJoin);
            overrideJoins.put(new Pair<>(toTable, tableJoinField), new Pair<>(unionIdFieldPrefix, unionIdAlias));
            usedTables.add(toTable);
        }
    }

    /**
     * The method create the where part of the SQL query.
     * It actually scan all the criterias and Parenthesis elements in the AQL Query
     * and transform does elements into SQL syntax.
     *
     * @param aqlQuery
     * @return
     * @throws AqlException
     */
    public <T extends AqlRowResult> Pair<String, List<Object>> conditions(AqlQuery<T> aqlQuery) throws AqlException {
        StringBuilder condition = new StringBuilder();
        List<Object> params = Lists.newArrayList();
        List<AqlQueryElement> aqlElements = aqlQuery.getAqlElements();
        for (AqlQueryElement aqlQueryElement : aqlElements) {
            if (aqlQueryElement instanceof ComplexPropertyCriterion || aqlQueryElement instanceof SimpleCriterion
                    || aqlQueryElement instanceof SimplePropertyCriterion) {
                Criterion criterion = (Criterion) aqlQueryElement;
                condition.append(criterion.toSql(params));
            }
            if (aqlQueryElement instanceof OperatorQueryElement) {
                AqlOperatorEnum operatorEnum = ((OperatorQueryElement) aqlQueryElement).getOperatorEnum();
                condition.append(" ").append(operatorEnum.name());
            }
            if (aqlQueryElement instanceof OpenParenthesisAqlElement) {
                condition.append("(");
            }
            if (aqlQueryElement instanceof CloseParenthesisAqlElement) {
                condition.append(")");
            }
        }
        return new Pair(condition.toString()+" ", params);
    }

    private List<TableLinkRelation> findShortestPathBetween(TableLink from, TableLink to) {
        List<TableLinkRelation> relations = TableLinkBrowser.create().findPathTo(from, to, getExclude());
        if (relations == null) {
            ArrayList<TableLink> excludes = Lists.newArrayList();
            relations = TableLinkBrowser.create().findPathTo(from, to, excludes);
        }
        relations = overrideRoute(relations);
        return relations;
    }

    protected abstract List<TableLink> getExclude();

    protected List<TableLinkRelation> overrideRoute(List<TableLinkRelation> route) {
        return route;
    }

    protected void generateJoinTables(List<TableLinkRelation> relations, Set<SqlTable> usedTables, StringBuilder join,
            AqlJoinTypeEnum joinTypeEnum, Map<Pair<SqlTable, AqlTableFieldsEnum>, Pair<String, String>> overrideJoins) {
        if (relations == null) {
            return;
        }
        for (TableLinkRelation relation : relations) {
            AqlTableFieldsEnum fromField = relation.getFromField();
            SqlTable fromTable = relation.getFromTable().getTable();
            AqlTableFieldsEnum toField = relation.getToFiled();
            SqlTable toTable = relation.getToTable().getTable();
            joinTable(toTable, toField, fromTable, fromField, usedTables, join, joinTypeEnum, overrideJoins);
        }
    }

    protected void generateJoinTables(List<TableLinkRelation> relations, Set<SqlTable> usedTables, StringBuilder join,
            AqlJoinTypeEnum joinTypeEnum, Map<Pair<SqlTable, AqlTableFieldsEnum>, Pair<String, String>> overrideJoins,
            SqlTable sqlTable) {
        if (relations == null) {
            return;
        }
        for (TableLinkRelation relation : relations) {
            AqlTableFieldsEnum fromField = relation.getFromField();
            SqlTable fromTable = relation.getFromTable().getTable();
            AqlTableFieldsEnum toFiled = relation.getToFiled();
            SqlTable toTable = relation.getToTable().getTable();
            toTable = toTable.getTable() == sqlTable.getTable() ? sqlTable : toTable;
            joinTable(toTable, toFiled, fromTable, fromField, usedTables, join, joinTypeEnum, overrideJoins);
        }
    }

    protected void joinTable(SqlTable table, AqlTableFieldsEnum tableJoinField, SqlTable onTable,
            AqlTableFieldsEnum onJoinField,
            Set<SqlTable> declaredTables, StringBuilder join, AqlJoinTypeEnum joinTypeEnum,
            Map<Pair<SqlTable, AqlTableFieldsEnum>, Pair<String, String>> overrideJoins) {
        if (!declaredTables.contains(table)) {
            Pair<String, String> overrideJoin = overrideJoins.get(new Pair<>(onTable, onJoinField));
            String onTablePrefix = overrideJoin == null ? onTable.getAlias() : overrideJoin.getFirst();
            String onField = overrideJoin == null ? onJoinField.name() : overrideJoin.getSecond();
            appendTableJoin(table, tableJoinField, onTablePrefix, onField, join, joinTypeEnum);
            declaredTables.add(table);
        }
    }

    private void appendFirstTable(SqlTable table, StringBuilder joinBuilder, Set<SqlTable> declaredTables) {
        joinBuilder.append(table.getTableName()).append(" ").append(table.getAliasDeclaration());
        declaredTables.add(table);
    }

    protected void appendTableJoin(SqlTable table, AqlTableFieldsEnum tableJoinField, String onTablePrefix, String onJoinField,
            StringBuilder joinBuilder, AqlJoinTypeEnum joinTypeEnum) {
        joinBuilder.append(" ").append(joinTypeEnum.signature).append(" ")
                .append(table.getTableName()).append(" ").append(table.getAliasDeclaration())
                .append(" on ").append(table.getAlias()).append(tableJoinField)
                .append(" = ").append(onTablePrefix).append(onJoinField);
    }

    public String sort(AqlQuery aqlQuery) {
        SortDetails sortDetails = aqlQuery.getSort();
        if (sortDetails == null || sortDetails.getFields().size() == 0) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        List<AqlPhysicalFieldEnum> fields = sortDetails.getFields();
        Iterator<AqlPhysicalFieldEnum> iterator = fields.iterator();
        while (iterator.hasNext()) {
            AqlPhysicalFieldEnum sortField = iterator.next();
            stringBuilder.append(sortField.name());
            stringBuilder.append(" ").append(sortDetails.getSortType().getSqlName());
            if (iterator.hasNext()) {
                stringBuilder.append(",");
            }else {
                stringBuilder.append(" ");
            }
        }
        return stringBuilder.toString();
    }

    /**
     * Query performance optimisation:
     * In case of single table join such as multiple properties table join
     * without the usage of any other table we can use inner join for better performance.
     *
     * @param allTables
     * @return
     */
    private AqlJoinTypeEnum resolveJoinType(Iterable<DomainSensitiveTable> allTables) {
        Iterable<SqlTableEnum> tables = Iterables.transform(allTables, toTableEnum);
        HashSet<SqlTableEnum> tableEnums = Sets.newHashSet();
        for (SqlTableEnum table : tables) {
            if (table != null) {
                tableEnums.add(table);
            }
        }
        if (tableEnums.size() == 1) {
            return AqlJoinTypeEnum.innerJoin;
        } else {
            return AqlJoinTypeEnum.leftOuterJoin;
        }
    }

    protected abstract SqlTableEnum[] getMainTables();

    private static List<SqlTableEnum> generateTableListFromSubDomainAndField(List<AqlDomainEnum> subDomains) {
        List<SqlTableEnum> result = Lists.newArrayList();
        if (subDomains.size() > 1) {
            for (int i = 0; i < subDomains.size() - 1; i++) {
                result.add(domainToTable(subDomains.get(i)));
            }
        } else {
            result.add(domainToTable(subDomains.get(0)));
        }
        return result;
    }

    private static SqlTableEnum domainToTable(AqlDomainEnum domainEnum) {
        switch (domainEnum) {
            case archives:
                return SqlTableEnum.indexed_archives;
            case entries:
                return SqlTableEnum.archive_names;
            case items:
                return SqlTableEnum.nodes;
            case properties:
                return SqlTableEnum.node_props;
            case statistics:
                return SqlTableEnum.stats;
            case builds:
                return SqlTableEnum.builds;
            case buildProperties:
                return SqlTableEnum.build_props;
            case artifacts:
                return SqlTableEnum.build_artifacts;
            case dependencies:
                return SqlTableEnum.build_dependencies;
            case modules:
                return SqlTableEnum.build_modules;
            case moduleProperties:
                return SqlTableEnum.module_props;
            case buildPromotions:
                return SqlTableEnum.build_promotions;
            case releaseBundles:
                return SqlTableEnum.artifact_bundles;
            case releaseBundleFiles:
                return SqlTableEnum.bundle_files;
        }
        return null;
    }
}
