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

import com.google.common.collect.Maps;
import org.artifactory.storage.db.aql.sql.builder.links.TableLink;
import org.artifactory.storage.db.aql.sql.model.SqlTableEnum;

import java.util.Collections;
import java.util.Map;

import static org.artifactory.aql.model.AqlTableFieldsEnum.*;
import static org.artifactory.storage.db.aql.sql.model.SqlTableEnum.*;

/**
 * The class contains the relations between the tables.
 * It is being used to find the shortest path between to tables to minimize the number of joins.
 *
 * @author Gidi Shabat
 */
public class AqlTableGraph {
    public static final Map<SqlTableEnum, TableLink> tablesLinksMap;

    static {
        //Create Tables and links
        TableLink nodesTable = new TableLink(nodes);
        TableLink statisticsTable = new TableLink(stats);
        TableLink statisticsRemoteTable = new TableLink(stats_remote);
        TableLink nodesProps = new TableLink(node_props);
        TableLink archiveNames = new TableLink(archive_names);
        TableLink archivePaths = new TableLink(archive_paths);
        TableLink indexedArchives = new TableLink(indexed_archives);
        TableLink indexedArchiveEntries = new TableLink(indexed_archives_entries);
        TableLink builds = new TableLink(SqlTableEnum.builds);
        TableLink buildsProps = new TableLink(build_props);
        TableLink buildsModules = new TableLink(build_modules);
        TableLink buildsPromotions = new TableLink(build_promotions);
        TableLink buildsArtifacts = new TableLink(build_artifacts);
        TableLink buildsDependencies = new TableLink(build_dependencies);
        TableLink buildModuleProperties = new TableLink(module_props);
        TableLink releaseBundles = new TableLink(artifact_bundles);
        TableLink releaseBundleFiles = new TableLink(bundle_files);
        nodesTable.addLink(node_id, nodesProps, node_id);
        nodesTable.addLink(sha1_actual, buildsArtifacts, sha1);
        nodesTable.addLink(sha1_actual, buildsDependencies, sha1);
        nodesTable.addLink(sha1_actual, indexedArchives, archive_sha1);
        nodesTable.addLink(node_id, statisticsTable, node_id);
        nodesTable.addLink(node_id, statisticsRemoteTable, node_id);
        statisticsTable.addLink(node_id, statisticsRemoteTable, node_id);
        indexedArchives.addLink(indexed_archives_id, indexedArchiveEntries, indexed_archives_id);
        indexedArchiveEntries.addLink(entry_name_id, archiveNames, name_id);
        indexedArchiveEntries.addLink(entry_path_id, archivePaths, path_id);
        buildsModules.addLink(module_id, buildsArtifacts, module_id);
        buildsModules.addLink(module_id, buildsDependencies, module_id);
        buildsModules.addLink(module_id, buildModuleProperties, module_id);
        buildsModules.addLink(build_id, builds, build_id);
        builds.addLink(build_id, buildsProps, build_id);
        nodesProps.addLink(node_id, nodesProps, node_id);
        buildsProps.addLink(build_id, buildsProps, build_id);
        buildModuleProperties.addLink(module_id, buildModuleProperties, module_id);
        builds.addLink(build_id, buildsPromotions, build_id);
        buildsPromotions.addLink(build_id, builds, build_id);
        releaseBundles.addLink(id, releaseBundleFiles, bundle_id);
        releaseBundleFiles.addLink(bundle_id, releaseBundles, id);
        releaseBundleFiles.addLink(node_id, nodesTable, node_id);
        nodesTable.addLink(node_id, releaseBundleFiles, node_id);


        //Fill the tables map
        Map<SqlTableEnum, TableLink> map = Maps.newHashMap();
        map.put(indexed_archives, indexedArchives);
        map.put(indexed_archives_entries, indexedArchiveEntries);
        map.put(archive_names, archiveNames);
        map.put(archive_paths, archivePaths);
        map.put(stats, statisticsTable);
        map.put(stats_remote, statisticsRemoteTable);
        map.put(nodes, nodesTable);
        map.put(node_props, nodesProps);
        map.put(build_dependencies, buildsDependencies);
        map.put(build_artifacts, buildsArtifacts);
        map.put(build_modules, buildsModules);
        map.put(module_props, buildModuleProperties);
        map.put(SqlTableEnum.builds, builds);
        map.put(build_props, buildsProps);
        map.put(build_promotions, buildsPromotions);
        map.put(artifact_bundles, releaseBundles);
        map.put(bundle_files, releaseBundleFiles);
        tablesLinksMap = Collections.unmodifiableMap(map);
    }
}
