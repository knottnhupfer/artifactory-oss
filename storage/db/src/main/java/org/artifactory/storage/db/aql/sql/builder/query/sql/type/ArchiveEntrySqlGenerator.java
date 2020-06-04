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

import com.google.common.collect.Lists;
import org.artifactory.aql.model.AqlTableFieldsEnum;
import org.artifactory.storage.db.aql.sql.builder.links.TableLink;
import org.artifactory.storage.db.aql.sql.builder.links.TableLinkRelation;
import org.artifactory.storage.db.aql.sql.model.SqlTableEnum;

import java.util.List;

import static org.artifactory.aql.util.AqlUtils.arrayOf;
import static org.artifactory.storage.db.aql.sql.builder.query.sql.type.AqlTableGraph.tablesLinksMap;

/**
 * @author Gidi Shabat
 */
public class ArchiveEntrySqlGenerator  extends BasicSqlGenerator {

    private TableLink indexedArchiveEntries;
    private TableLinkRelation nameToIndex;
    private TableLinkRelation pathToIndex;
    private TableLinkRelation indexToPath;
    private TableLinkRelation indexToName;
    private TableLinkRelation indexToIndexArchive;
    private TableLinkRelation indexArchiveToIndex;
    private List<TableLinkRelation> nameToIndexToPath;
    private List<TableLinkRelation> pathToIndexToName;


    @Override
    protected List<TableLink> getExclude() {
        return Lists.newArrayList(tablesLinksMap.get(SqlTableEnum.build_dependencies));
    }

    @Override
    protected List<TableLinkRelation> overrideRoute(List<TableLinkRelation> route) {
        init();
        if (nameToIndexToPath.equals(route)) {
            return Lists.newArrayList(nameToIndex, indexToIndexArchive, indexArchiveToIndex, indexToPath);
        }
        if (pathToIndexToName.equals(route)) {
            return Lists.newArrayList(pathToIndex, indexToIndexArchive, indexArchiveToIndex, indexToName);
        }
        return route;
    }

    private void init() {
        if (indexedArchiveEntries == null) {
            TableLink archivePaths = AqlTableGraph.tablesLinksMap.get(SqlTableEnum.archive_paths);
            TableLink archiveNames = AqlTableGraph.tablesLinksMap.get(SqlTableEnum.archive_names);
            TableLink indexedArchives = AqlTableGraph.tablesLinksMap.get(SqlTableEnum.indexed_archives);
            indexedArchiveEntries = AqlTableGraph.tablesLinksMap.get(SqlTableEnum.indexed_archives_entries);
            indexToPath = new TableLinkRelation(indexedArchiveEntries, AqlTableFieldsEnum.entry_path_id,
                    archivePaths, AqlTableFieldsEnum.path_id);
            nameToIndex = new TableLinkRelation(archiveNames, AqlTableFieldsEnum.name_id,
                    indexedArchiveEntries, AqlTableFieldsEnum.entry_name_id);
            nameToIndexToPath = Lists.newArrayList(nameToIndex, indexToPath);
            indexToName = new TableLinkRelation(indexedArchiveEntries, AqlTableFieldsEnum.entry_name_id,
                    archiveNames, AqlTableFieldsEnum.name_id);
            pathToIndex = new TableLinkRelation(archivePaths, AqlTableFieldsEnum.path_id,
                    indexedArchiveEntries, AqlTableFieldsEnum.entry_path_id);
            pathToIndexToName = Lists.newArrayList(pathToIndex, indexToName);
            indexArchiveToIndex = new TableLinkRelation(indexedArchives, AqlTableFieldsEnum.indexed_archives_id,
                    indexedArchiveEntries, AqlTableFieldsEnum.indexed_archives_id);
            indexToIndexArchive = new TableLinkRelation(indexedArchiveEntries, AqlTableFieldsEnum.indexed_archives_id,
                    indexedArchives, AqlTableFieldsEnum.indexed_archives_id);
        }
    }

    @Override
    protected SqlTableEnum[] getMainTables() {
        return arrayOf(SqlTableEnum.archive_names);
    }

}
