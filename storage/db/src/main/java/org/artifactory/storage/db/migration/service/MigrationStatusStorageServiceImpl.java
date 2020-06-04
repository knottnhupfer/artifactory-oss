package org.artifactory.storage.db.migration.service;

import org.artifactory.storage.StorageException;
import org.artifactory.storage.db.migration.dao.MigrationStatusDao;
import org.artifactory.storage.db.migration.entity.DbMigrationStatus;
import org.artifactory.storage.db.migration.model.MigrationInfoBlob;
import org.artifactory.storage.db.migration.model.MigrationStatus;
import org.artifactory.storage.db.migration.service.mapper.MigrationStatusMapper;
import org.jfrog.common.ClockUtils;
import org.jfrog.common.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Optional;

/**
 * @author Uriah Levy
 */
@Service
public class MigrationStatusStorageServiceImpl implements MigrationStatusStorageService {
    private static final Logger log = LoggerFactory.getLogger(MigrationStatusStorageServiceImpl.class);
    private MigrationStatusDao migrationStatusDao;

    private MigrationStatusMapper mapper;

    @Autowired
    public MigrationStatusStorageServiceImpl(MigrationStatusDao migrationStatusDao, MigrationStatusMapper mapper) {
        this.migrationStatusDao = migrationStatusDao;
        this.mapper = mapper;
    }

    @Override
    public MigrationStatus findMigrationById(String migrationId) {
        try {
            Optional<DbMigrationStatus> migration = migrationStatusDao.findMigrationById(migrationId);
            if (migration.isPresent()) {
                return mapper.dbMigrationStatusToMigrationStatus(migration.get());
            }
        } catch (SQLException e) {
            throw new StorageException("Unable to find migration by id: " + migrationId);
        }
        return null;
    }

    @Override
    public MigrationStatus findMigrationByIdWithInfoBlob(String migrationId,
            Class<? extends MigrationInfoBlob> infoBlobModel) {
        try {
            Optional<DbMigrationStatus> migration = migrationStatusDao.findMigrationById(migrationId);
            if (migration.isPresent()) {
                MigrationStatus migrationStatus = mapper.dbMigrationStatusToMigrationStatus(migration.get());
                if (migration.get().getMigrationInfoBlob() != null && migration.get().getMigrationInfoBlob().length != 0) {
                    migrationStatus.setMigrationInfoBlob(
                            JsonUtils.getInstance().readValue(migration.get().getMigrationInfoBlob(), infoBlobModel));
                }
                return migrationStatus;
            }
        } catch (SQLException e) {
            throw new StorageException("Unable to find migration by id: " + migrationId, e);
        }
        return null;
    }

    @Override
    public void insertMigration(MigrationStatus migrationStatus) {
        try {
            log.info("About to insert new migration status record '{}'", migrationStatus);
            migrationStatusDao.insertMigration(mapper.migrationStatusToDbMigrationStatus(migrationStatus));
        } catch (SQLException e) {
            if (findMigrationById(migrationStatus.getIdentifier()) != null) {
                log.info("Migration with ID '{}' already exists.", migrationStatus.getIdentifier());
                return;
            }
            throw new StorageException("Unable to insert new migration for: " + migrationStatus.getIdentifier());
        }
    }

    @Override
    public void insertMigrationWithInfoBlob(MigrationStatus migrationStatus) {
        try {
            log.info("Inserting new migration status record '{}'", migrationStatus);
            migrationStatusDao.insertMigration(mapper.migrationStatusToDbMigrationStatus(migrationStatus));
        } catch (SQLException e) {
            if (findMigrationById(migrationStatus.getIdentifier()) != null) {
                log.info("Migration with ID '{}' already exists.", migrationStatus.getIdentifier());
                return;
            }
            throw new StorageException("Unable to insert new migration for: " + migrationStatus.getIdentifier());
        }
    }

    @Override
    public void migrationDone(String migrationId) {
        log.info("Migration for '{}' has finished.", migrationId);
        try {
            migrationStatusDao.updateFinishTimeById(migrationId, ClockUtils.epochMillis());
        } catch (SQLException e) {
            throw new StorageException("Unable to mark migration as finished", e);
        }
    }
}
