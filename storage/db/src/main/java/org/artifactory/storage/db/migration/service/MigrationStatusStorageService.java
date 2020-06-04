package org.artifactory.storage.db.migration.service;

import org.artifactory.storage.db.migration.model.MigrationInfoBlob;
import org.artifactory.storage.db.migration.model.MigrationStatus;

/**
 * @author Uriah Levy
 * A bussiness service for managing long running migrations.
 */
public interface MigrationStatusStorageService {

    /**
     * Find a stored migration status by its ID
     * @param migrationId the ID of the migration
     * @return the Migration Status
     */
    MigrationStatus findMigrationById(String migrationId);

    /**
     * Find a stored migration status by its ID, including the migration information blob
     * @param migrationId the ID of the migration
     * @param infoBlobModel the model class of the migration information blob.
     * @return the Migration Status
     */
    MigrationStatus findMigrationByIdWithInfoBlob(String migrationId, Class<? extends MigrationInfoBlob> infoBlobModel);

    /**
     * Insert a new migration status
     * @param migrationStatus a migration status to insert
     */
    void insertMigration(MigrationStatus migrationStatus);

    /**
     * Insert a new migration status, with a migration information blob
     * @param migrationStatus a migration status to insert
     */
    void insertMigrationWithInfoBlob(MigrationStatus migrationStatus);

    /**
     * Mark an existing migration status as done
     *
     * @param migrationId the identifier of the migration status to update
     */
    void migrationDone(String migrationId);
}
