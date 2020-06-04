package org.artifactory.storage.db.migration.service.mapper;

import org.artifactory.storage.db.migration.entity.DbMigrationStatus;
import org.artifactory.storage.db.migration.model.MigrationStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @author Uriah Levy
 */
@Mapper(uses = MigrationInfoBlobTranslator.class, componentModel = "spring")
public interface MigrationStatusMapper {

    @Mapping(target = "migrationInfoBlob", qualifiedByName = {"MigrationInfoBlobTranslator", "InfoBlobModelToByteArray"})
    DbMigrationStatus migrationStatusToDbMigrationStatus(MigrationStatus migrationStatus);

    @Mapping(target = "migrationInfoBlob", ignore = true)
    MigrationStatus dbMigrationStatusToMigrationStatus(DbMigrationStatus dbMigrationStatus);
}
