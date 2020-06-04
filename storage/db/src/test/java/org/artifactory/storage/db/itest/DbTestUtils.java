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

package org.artifactory.storage.db.itest;

import com.google.common.collect.Lists;
import org.artifactory.storage.db.version.converter.DbSqlConverterUtil;
import org.jfrog.common.ResourceUtils;
import org.jfrog.security.util.Pair;
import org.jfrog.storage.DbType;
import org.jfrog.storage.JdbcHelper;
import org.jfrog.storage.util.DbStatementUtils;
import org.jfrog.storage.util.DbUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Collections;
import java.util.List;

import static org.jfrog.storage.util.DbUtils.*;

/**
 * A utility class for integration tests to clean and setup the database
 *
 * @author Yossi Shaul
 */
public class DbTestUtils {
    // BE CAREFUL do not have logger here. Tests not initialized correctly

    private static final String[] DB_META_TABLE_QUALIFIER = new String[]{"TABLE"};

    /**
     * A list of all the tables in the database
     */
    private static final List<String> tables = Collections.unmodifiableList(Lists.newArrayList(
            "db_properties", "artifactory_servers",
            "stats_remote", "stats", "watches", "bundle_blobs", "bundle_files", "artifact_bundles", "node_props",
            "node_meta_infos", "nodes", "node_events",
            "indexed_archives_entries", "archive_names", "archive_paths", "indexed_archives",
            "binary_blobs", "binaries",
            "aces", "acls", "users_groups", "groups", "user_props", "users",
            "permission_target_repos", "permission_targets",
            "configs", "tasks", "jobs",
            "module_props", "build_props", "build_jsons", "build_promotions",
            "build_dependencies", "build_artifacts", "build_modules", "builds",
            "unique_ids", "distributed_locks", "master_key_status", "trusted_keys", "blob_infos", "UI_SESSION_ATTRIBUTES",
            "UI_SESSION", "replication_errors", "node_event_cursor", "node_event_priorities", "migration_status"
    ));

    //Testing for table existence is not enough, some schema conversions change columns as well.
    //Ordered by most probable table to change
    private static final List<Pair<String,List<String>>> schema = Collections.unmodifiableList(Lists.newArrayList(
            new Pair<>("binaries", Lists.newArrayList("sha1", "md5", "bin_length", "sha256")),
            new Pair<>("nodes",
                    Lists.newArrayList("node_id", "node_type", "repo", "node_path", "node_name", "depth",
                            "created", "created_by", "modified", "modified_by", "updated", "bin_length", "sha1_actual",
                            "sha1_original", "md5_actual", "md5_original", "sha256", "repo_path_checksum")),
            new Pair<>("binary_blobs", Lists.newArrayList("sha1", "data")),
            new Pair<>("aces", Lists.newArrayList("ace_id", "acl_id", "mask", "user_id", "group_id")),
            new Pair<>("acls", Lists.newArrayList("acl_id", "perm_target_id", "modified", "modified_by")),
            new Pair<>("users_groups", Lists.newArrayList("user_id", "group_id", "realm")),
            new Pair<>("groups", Lists.newArrayList("group_id", "group_name", "description", "default_new_user",
                    "realm", "realm_attributes", "admin_privileges")),
            new Pair<>("users",
                    Lists.newArrayList("user_id", "username", "password", "salt", "email", "gen_password_key",
                            "admin", "enabled", "updatable_profile", "realm", "private_key", "public_key",
                            "last_login_time", "last_login_ip",
                            "last_access_time", "last_access_ip", "bintray_auth", "locked", "credentials_expired")),
            new Pair<>("build_dependencies", Lists.newArrayList("dependency_id", "module_id", "dependency_name_id",
                    "dependency_scopes", "dependency_type", "sha1", "md5", "sha256")),
            new Pair<>("build_artifacts", Lists.newArrayList("artifact_id", "module_id", "artifact_name",
                    "artifact_type", "sha1", "md5", "sha256")),
            new Pair<>("build_promotions", Lists.newArrayList("build_id", "created", "created_by", "status", "repo",
                    "promotion_comment", "ci_user")),
            new Pair<>("builds", Lists.newArrayList("build_id", "build_name", "build_number", "build_date",
                    "ci_url", "created", "created_by", "modified", "modified_by")),
            new Pair<>("build_modules", Lists.newArrayList("module_id", "build_id", "module_name_id")),
            new Pair<>("artifactory_servers",
                    Lists.newArrayList("server_id", "start_time", "context_url", "membership_port", "server_state",
                            "server_role", "last_heartbeat", "artifactory_version", "artifactory_revision",
                            "artifactory_release", "artifactory_running_mode", "license_hash")),
            new Pair<>("db_properties", Lists.newArrayList("installation_date", "artifactory_version", "artifactory_revision", "artifactory_release")),
            new Pair<>("stats_remote", Lists.newArrayList("node_id", "origin", "download_count", "last_downloaded", "last_downloaded_by", "path")),
            new Pair<>("stats", Lists.newArrayList("node_id", "download_count", "last_downloaded", "last_downloaded_by")),
            new Pair<>("watches", Lists.newArrayList("watch_id", "node_id", "username", "since")),
            new Pair<>("node_props", Lists.newArrayList("prop_id", "node_id", "prop_key", "prop_value")),
            new Pair<>("node_meta_infos", Lists.newArrayList("node_id", "props_modified", "props_modified_by")),
            new Pair<>("indexed_archives_entries", Lists.newArrayList("indexed_archives_id", "entry_path_id", "entry_name_id")),
            new Pair<>("archive_names", Lists.newArrayList("name_id", "entry_name")),
            new Pair<>("archive_paths", Lists.newArrayList("path_id", "entry_path")),
            new Pair<>("indexed_archives", Lists.newArrayList("archive_sha1", "indexed_archives_id")),
            new Pair<>("user_props", Lists.newArrayList("user_id", "prop_key", "prop_value")),
            new Pair<>("permission_target_repos", Lists.newArrayList("perm_target_id", "repo_key")),
            new Pair<>("permission_targets", Lists.newArrayList("perm_target_id", "perm_target_name", "includes", "excludes")),
            new Pair<>("configs", Lists.newArrayList("config_name", "last_modified", "data")),
            new Pair<>("tasks", Lists.newArrayList("task_type", "task_context", "created")),
            new Pair<>("jobs", Lists.newArrayList("job_id", "job_type", "job_status", "started", "finished", "additional_details")),
            new Pair<>("module_props", Lists.newArrayList("prop_id", "module_id", "prop_key", "prop_value")),
            new Pair<>("build_props", Lists.newArrayList("prop_id", "build_id", "prop_key", "prop_value")),
            new Pair<>("build_jsons", Lists.newArrayList("build_id", "build_info_json")),
            new Pair<>("unique_ids", Lists.newArrayList("index_type", "current_id")),
            new Pair<>("master_key_status", Lists.newArrayList("set_by_node_id", "kid", "expires")),
            new Pair<>("node_events", Lists.newArrayList("event_id", "timestamp", "event_type", "path")),
            new Pair<>("artifact_bundles", Lists.newArrayList("id", "name", "version", "status", "date_created", "signature", "type", "storing_repo")),
            new Pair<>("bundle_files", Lists.newArrayList("id", "node_id", "bundle_id", "repo_path")),
            new Pair<>("bundle_blobs", Lists.newArrayList("id", "data", "bundle_id")),
            new Pair<>("trusted_keys", Lists.newArrayList("trusted_key", "fingerprint", "alias", "issued", "issued_by", "expiry")),
            new Pair<>("blob_infos", Lists.newArrayList("checksum", "blob_info")),
            new Pair<>("UI_SESSION_ATTRIBUTES", Lists.newArrayList("SESSION_ID", "ATTRIBUTE_NAME", "ATTRIBUTE_BYTES")),
            new Pair<>("UI_SESSION", Lists.newArrayList("SESSION_ID", "CREATION_TIME", "LAST_ACCESS_TIME", "MAX_INACTIVE_INTERVAL", "PRINCIPAL_NAME")),
            new Pair<>("replication_errors", Lists.newArrayList("error_id", "error_time", "error_count", "last_retry_time", "event_id", "event_timestamp", "event_type", "event_path")),
            new Pair<>("node_event_cursor", Lists.newArrayList("operator_id", "event_marker")),
            new Pair<>("node_event_priorities", Lists.newArrayList("priority_id", "path", "type", "operator_id", "priority", "timestamp", "retry_count")),
            new Pair<>("migration_status", Lists.newArrayList("identifier", "started", "finished", "migration_info_blob"))
    ));

    /**
     * A list of all the access server tables inside the artifactory database (since access server is embedded)
     */
    private static final List<String> accessServerTables = Collections.unmodifiableList(Lists.newArrayList(
            "access_tokens", "access_users", "access_servers"
    ));

    static void refreshOrRecreateSchema(Logger log, Connection conn, DbType dbType) throws SQLException {
        // to improve test speed, re-create the schema only if there's a missing table
        boolean recreateSchema = isSchemaIncomplete(conn, dbType);
        if (recreateSchema) {
            log.info("Recreating test database schema for database: {}", dbType);
            dropAllExistingTables(conn, dbType);
            createSchema(conn, dbType);
        } else {
            log.info("Deleting database tables data from database: {}", dbType);
            deleteFromAllTables(conn);
            deleteFromAccessServerTables(conn);
        }
    }

    public static void dropAllExistingTables(Connection con, DbType dbType) throws SQLException {
        for (String table : tables) {
            if (tableExists(con, dbType, table)) {
                try (Statement statement = con.createStatement()) {
                    statement.execute("DROP TABLE " + table);
                }
            }
        }
    }

    private static void deleteFromAllTables(Connection con) throws SQLException {
        for (String table : tables) {
            try (Statement statement = con.createStatement()) {
                statement.execute("DELETE FROM " + table);
            }
        }
    }

    private static void deleteFromAccessServerTables(Connection con) {
        for (String table : accessServerTables) {
            try (Statement statement = con.createStatement()) {
                statement.execute("DELETE FROM " + table);
            } catch (SQLException e) {
                //System.out.println("Failed to delete from access server table '" + table + "', ignoring. (" + e + ")");
            }
        }
    }

    public static boolean tableExists(Connection con, DbType dbType, String tableName) throws SQLException {
        DatabaseMetaData metaData = con.getMetaData();
        tableName = normalizedName(tableName, metaData);
        try (ResultSet rs = metaData.getTables(getActiveCatalog(con, dbType), getActiveSchema(con, dbType), tableName, DB_META_TABLE_QUALIFIER)) {
            return rs.next();
        }
    }

    public static boolean isTableMissing(Connection con, DbType dbType) throws SQLException {
        for (String table : tables) {
            if (!tableExists(con, dbType, table)) {
                return true;
            }
        }
        return false;
    }

    public static boolean indexNotExists(JdbcHelper jdbcHelper, String tableName, String columnName, String indexName, DbType dbType) throws SQLException {
        return withConnection(jdbcHelper, conn -> !indexExists(jdbcHelper, conn, dbType, tableName, columnName, indexName, true));
    }

    /**
     * Tests the db schema for missing columns, enough that once is missing to return true.
     */
    private static boolean isSchemaIncomplete(Connection con, DbType dbType) throws SQLException {
        DatabaseMetaData metaData = con.getMetaData();
        for (Pair<String, List<String>> tableColumns : schema) {
            String table = tableColumns.getFirst();
            for (String column : tableColumns.getSecond()) {
                if (!DbUtils.columnExists(metaData, dbType, table, column)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean columnExists(Connection con, DbType dbType, String table, String column) throws SQLException {
        DatabaseMetaData metaData = con.getMetaData();
        String tableName = normalizedName(table, metaData);
        String columnName = normalizedName(column, metaData);
        try (ResultSet rs = metaData.getColumns(getActiveCatalog(con, dbType), getActiveSchema(con, dbType), tableName, columnName)) {
            return rs.next();
        }
    }

    public static int getColumnSize(Connection con, String tableName, String columnName) throws SQLException {
        DatabaseMetaData metaData = con.getMetaData();
        tableName = normalizedName(tableName, metaData);
        columnName = normalizedName(columnName, metaData);

        try (Statement statement = con.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * from " + tableName)) {
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            int columnCount = resultSetMetaData.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                if (resultSetMetaData.getColumnName(i).equals(columnName)) {
                    return resultSetMetaData.getColumnDisplaySize(i);
                }
            }
        }

        return -1;
    }

    // read ddl from file and execute
    private static void createSchema(Connection con, DbType dbType) throws SQLException {
        String dbConfigDir  = DbSqlConverterUtil.getDbTypeNameForSqlResources(dbType);
        try (InputStream schema = ResourceUtils.getResource("/" + dbConfigDir + "/" + dbConfigDir + ".sql")) {
            DbStatementUtils.executeSqlStream(con, schema);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
