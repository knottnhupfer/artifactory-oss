package org.artifactory.storage.db.migration.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Uriah Levy
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DbMigrationStatus {
    private String identifier;
    private long started;
    private long finished;
    private byte[] migrationInfoBlob;
}
