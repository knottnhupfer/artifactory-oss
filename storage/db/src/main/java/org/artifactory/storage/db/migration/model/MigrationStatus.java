package org.artifactory.storage.db.migration.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Uriah Levy
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MigrationStatus {
    private String identifier;
    private long started;
    private long finished;
    private MigrationInfoBlob migrationInfoBlob;

    public boolean isFinished() {
        return this.finished != 0;
    }
}
