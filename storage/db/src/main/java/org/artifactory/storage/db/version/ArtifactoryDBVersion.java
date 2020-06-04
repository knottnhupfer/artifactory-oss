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

package org.artifactory.storage.db.version;

import com.google.common.collect.Lists;
import org.artifactory.storage.db.conversion.version.v202.V202ConversionPredicate;
import org.artifactory.storage.db.conversion.version.v203.V203ConversionPredicates.V550aConversionPredicate;
import org.artifactory.storage.db.conversion.version.v203.V203ConversionPredicates.V550bConversionPredicate;
import org.artifactory.storage.db.conversion.version.v203.V203ConversionPredicates.V550cConversionPredicate;
import org.artifactory.storage.db.conversion.version.v213.V213ConversionFailFunction;
import org.artifactory.storage.db.conversion.version.v213.V213ConversionPredicate;
import org.artifactory.storage.db.conversion.version.v215.V215ConversionFailFunction;
import org.artifactory.storage.db.conversion.version.v215.V215ConversionPredicate;
import org.artifactory.storage.db.conversion.version.v222.V222ConversionPredicate;
import org.artifactory.storage.db.util.JdbcHelper;
import org.artifactory.storage.db.version.converter.ConditionalDBSqlConverter;
import org.artifactory.storage.db.version.converter.DBConverter;
import org.artifactory.storage.db.version.converter.DBSqlConverter;
import org.artifactory.storage.db.version.converter.OptionalDBConverter;
import org.artifactory.version.ArtifactoryVersion;
import org.artifactory.version.ArtifactoryVersionProvider;
import org.jfrog.storage.DbType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * NOTE! The conversion logic for this enum is different from the one used by
 * {@link org.artifactory.version.ArtifactoryConfigVersion}: here the converters are *in line* with the version that
 * requires them.
 * Meaning for example, that passing into 4.4.1 (denoted v441) requires that you run conversion for db version 'v441'
 * and this db version is the one used until 4.14.0 (denoted v4140)
 * Artifactory DB version
 *
 * @author Gidi Shabat
 */
public enum ArtifactoryDBVersion {
    v100(ArtifactoryVersionProvider.v300.get()),
    v101(ArtifactoryVersionProvider.v310.get(), new DBSqlConverter("v310")),
    v102(ArtifactoryVersionProvider.v311.get(), new DBSqlConverter("v311")),
    v103(ArtifactoryVersionProvider.v410.get(), new DBSqlConverter("v410")),
    v104(ArtifactoryVersionProvider.v420.get(), new DBSqlConverter("v420")),
    v106(ArtifactoryVersionProvider.v432.get()),
    v107(ArtifactoryVersionProvider.v440.get(), new DBSqlConverter("v440")),
    v108(ArtifactoryVersionProvider.v441.get(), new DBSqlConverter("v441")),
    v109(ArtifactoryVersionProvider.v4142.get()),
    v200(ArtifactoryVersionProvider.v500beta1.get() , new DBSqlConverter("v500")),
    v201(ArtifactoryVersionProvider.v530.get(), new DBSqlConverter("v530")),
    v202(ArtifactoryVersionProvider.v531.get(), new ConditionalDBSqlConverter("v441", new V202ConversionPredicate())),
    v203(ArtifactoryVersionProvider.v550m001.get(), new ConditionalDBSqlConverter("v550",
            new V550aConversionPredicate()),
            new ConditionalDBSqlConverter("v550a", new V550aConversionPredicate()),
            new ConditionalDBSqlConverter("v550b", new V550bConversionPredicate()),
            new ConditionalDBSqlConverter("v550c", new V550cConversionPredicate())),
    v204(ArtifactoryVersionProvider.v570m001.get(), new DBSqlConverter("v570")),
    v205(ArtifactoryVersionProvider.v572.get(), new DBSqlConverter("v572")),
    v206(ArtifactoryVersionProvider.v580.get(), new DBSqlConverter("v580")),
    v207(ArtifactoryVersionProvider.v590m001.get(), new DBSqlConverter("v207_bundles"),
            new DBSqlConverter("v207_events")),
    v208(ArtifactoryVersionProvider.v5100m009.get(), new DBSqlConverter("v208_blob_infos")),
    v209(ArtifactoryVersionProvider.v600m023.get(), new DBSqlConverter("v209_bundle_files")),
    v210(ArtifactoryVersionProvider.v610m002.get(), new DBSqlConverter("v210_trusted_keys_mandatory")),
    v211(ArtifactoryVersionProvider.v620.get(), new DBSqlConverter("v211_server_id")),
    v212(ArtifactoryVersionProvider.v640m007.get(), new DBSqlConverter("v212_rb_repo")),
    v213(ArtifactoryVersionProvider.v650m001.get(),
            new OptionalDBConverter("v213_node_props_index", new V213ConversionPredicate(),
                    new V213ConversionFailFunction())),
    v214(ArtifactoryVersionProvider.v660m001.get(), new DBSqlConverter("v214_replication_errors"), new DBSqlConverter("v214_remove_unique_index")),
    v215(ArtifactoryVersionProvider.v682m001.get(), new OptionalDBConverter("v215_original_content_type", new V215ConversionPredicate(), new V215ConversionFailFunction())),
    v216(ArtifactoryVersionProvider.v690m001.get(), new DBSqlConverter("v216_cursor_priorities_migrations")),
    v217(ArtifactoryVersionProvider.v6110m001.get(), new DBSqlConverter("v217_create_jobs_table"), new DBSqlConverter("v217_set_default_storing_repo"), new DBSqlConverter("v217_update_artifact_bundles_indexes"), new DBSqlConverter("v217_remove_metadata_migration_status")),
    v218(ArtifactoryVersionProvider.v6120m079.get(), new DBSqlConverter("v218_add_bundle_files_idx")),
    v219(ArtifactoryVersionProvider.v6170m001.get(), new DBSqlConverter("v219_add_type_to_node_event_cursor")),
    v220(ArtifactoryVersionProvider.v618m001.get(), new DBSqlConverter("v220_change_tasks_context_size")),
    v222(ArtifactoryVersionProvider.v6200m003.get(), new ConditionalDBSqlConverter("v222_change_bundle_version_length", new V222ConversionPredicate())),
    ;

    private static final Logger log = LoggerFactory.getLogger(ArtifactoryDBVersion.class);

    private ArtifactoryVersion version;
    private final DBConverter[] converters;

    ArtifactoryDBVersion(ArtifactoryVersion version, DBConverter... converters) {
        this.version = version;
        this.converters = converters;
    }

    public static ArtifactoryDBVersion getLast() {
        ArtifactoryDBVersion[] versions = ArtifactoryDBVersion.values();
        return versions[versions.length - 1];
    }

    public static void convert(ArtifactoryVersion from, JdbcHelper jdbcHelper, DbType dbType) {
        // All converters of versions above me needs to be executed in sequence
        List<DBConverter> converters = Lists.newArrayList();
        for (ArtifactoryDBVersion dbVersion : ArtifactoryDBVersion.values()) {
            if (dbVersion.version.after(from)) {
                converters.addAll(Arrays.asList(dbVersion.getConverters()));
            }
        }

        if (converters.isEmpty()) {
            log.debug("No database converters found between version {} and {}", from, ArtifactoryVersion.getCurrent());
        } else {
            log.info("Starting database conversion from {}({}) to {}({})", from.getVersion(),from.getRevision(), ArtifactoryVersion.getCurrent().getVersion(), ArtifactoryVersion.getCurrent().getRevision());
            for (DBConverter converter : converters) {
                converter.convert(jdbcHelper, dbType);
            }
            log.info("Finished database conversion from {}({}) to {}({})", from.getVersion(), from.getRevision(), ArtifactoryVersion.getCurrent().getVersion(), ArtifactoryVersion.getCurrent().getRevision());
        }
    }

    public ArtifactoryVersion getVersion() {
        return version;
    }

    public DBConverter[] getConverters() {
        return converters;
    }

}
