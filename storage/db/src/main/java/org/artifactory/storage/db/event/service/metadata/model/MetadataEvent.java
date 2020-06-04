package org.artifactory.storage.db.event.service.metadata.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.artifactory.storage.event.EventType;
import org.jfrog.metadata.client.model.MetadataEntity;
import org.jfrog.metadata.client.model.event.MetadataEventEntity;

/**
 * @author Uriah Levy
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetadataEvent {
    private MetadataEventEntity metadataEventEntity;
    private EventType eventType;
    private long eventTime;
    private String path;

    private MetadataEntity metadataEntity;
}
