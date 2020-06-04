package org.artifactory.metadata.migration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.artifactory.storage.db.migration.model.MigrationInfoBlob;

import java.util.List;

/**
 * @author Uriah Levy
 */
@Data
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class MetadataMigrationInfoBlob implements MigrationInfoBlob {

    private long migrationTargetNodeId;
    private long currentEventLogTimestamp;
    private List<String> reposToMigrate;

    public MetadataMigrationInfoBlob(long migrationTargetNodeId, long currentEventLogTimestamp,
            List<String> reposToMigrate) {
        this.migrationTargetNodeId = migrationTargetNodeId;
        this.currentEventLogTimestamp = currentEventLogTimestamp;
        this.reposToMigrate = reposToMigrate;
    }

    @JsonIgnore
    @Override
    public Class getModel() {
        return this.getClass();
    }
}
