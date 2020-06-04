package org.artifactory.storage.db.migration.dao;

import org.apache.commons.lang3.tuple.Pair;
import org.artifactory.common.config.db.ArtifactoryDbProperties;
import org.artifactory.storage.db.migration.entity.DbMigrationStatus;
import org.jfrog.storage.JdbcHelper;
import org.jfrog.storage.SqlDaoHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * @author Uriah Levy
 */
@Repository
public class MigrationStatusDao {
    public static final String MIGRATION_STATUS_TABLE = "migration_status";
    private static final String IDENTIFIER_COLUMN = "identifier";
    private static final String STARTED_COLUMN = "started";
    private static final String FINISHED_COLUMN = "finished";
    private static final String MIGRATION_INFO_BLOB_COLUMN = "migration_info_blob";
    private static final String INSERT_MIGRATION_SQL =
            "INSERT INTO " + MIGRATION_STATUS_TABLE + " (identifier, started," +
                    " finished, migration_info_blob) VALUES (?, ?, ?, ?)";
    private final JdbcHelper jdbcHelper;
    private final SqlDaoHelper<DbMigrationStatus> daoHelper;

    @Autowired
    public MigrationStatusDao(JdbcHelper jdbcHelper, ArtifactoryDbProperties dbProperties) {
        this.jdbcHelper = jdbcHelper;
        this.daoHelper = new SqlDaoHelper<>(jdbcHelper, dbProperties.getDbType(),
                MIGRATION_STATUS_TABLE, this::getFromResultSet, 100);
    }

    private DbMigrationStatus getFromResultSet(ResultSet resultSet) throws SQLException {
        return new DbMigrationStatus(resultSet.getString(IDENTIFIER_COLUMN),
                resultSet.getLong(STARTED_COLUMN),
                resultSet.getLong(FINISHED_COLUMN),
                resultSet.getBytes(MIGRATION_INFO_BLOB_COLUMN));
    }

    public void insertMigration(DbMigrationStatus dbMigrationStatus) throws SQLException {
        byte[] migrationInfoBlob = dbMigrationStatus.getMigrationInfoBlob();
        jdbcHelper
                .executeUpdate(INSERT_MIGRATION_SQL, dbMigrationStatus.getIdentifier(),
                        dbMigrationStatus.getStarted(),
                        dbMigrationStatus.getFinished(), migrationInfoBlob != null ? migrationInfoBlob : new byte[]{});
    }

    public void updateFinishTimeById(String identifier, long finishTime) throws SQLException {
        daoHelper.updateByProperty(Pair.of(IDENTIFIER_COLUMN, identifier), Pair
                .of(FINISHED_COLUMN, finishTime));
    }

    public Optional<DbMigrationStatus> findMigrationById(String identifier) throws SQLException {
        return daoHelper.findFirstByProperty(IDENTIFIER_COLUMN, identifier);
    }
}
