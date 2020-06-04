package org.artifactory.storage.db.event.entity;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jfrog.common.mapper.Validatable;

import static org.jfrog.common.ArgUtils.requireNonBlank;
import static org.jfrog.common.ArgUtils.requireNonNegative;
import static org.jfrog.common.ArgUtils.requirePositive;

/**
 * @author Uriah Levy
 */
@Data
@NoArgsConstructor
@Builder(toBuilder = true, builderClassName = "Builder")
public class StoragePrioritizedNodeEvent implements Validatable {
    long priorityId;
    String path;
    String type;
    String operatorId;
    int priority;
    long timestamp;
    int retryCount;

    public StoragePrioritizedNodeEvent(long priorityId, String path, String type, String operatorId, int priority, long timestamp,
            int retryCount) {
        validate(priorityId, path, type, operatorId, priority, timestamp, retryCount);
    }

    @Override
    public void validate() {
        validate(priorityId, path, type, operatorId, priority, timestamp, retryCount);
    }

    public void validate(long priorityId, String path, String type, String operationId, int priority, long timestamp,
            int retryCount) {
        this.priorityId = requireNonNegative(priorityId, "Priority ID  is required");
        this.path = requireNonBlank(path, "Event Path is required");
        this.type = requireNonBlank(type, "Event Type is required");
        this.operatorId = requireNonBlank(operationId, "Event Operator ID is required");
        this.priority = requirePositive(priority, "Event Priority is required, and needs to be positive");
        this.timestamp = requireNonNegative(timestamp, "Event Timestamp is required");
        this.retryCount = requireNonNegative(retryCount, "Retry Count is required");
    }
}
