package org.artifactory.event.priority.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.storage.event.EventType;
import org.artifactory.storage.event.NodeEvent;
import org.jfrog.common.mapper.Validatable;

/**
 * @author Uriah Levy
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PrioritizedNodeEvent implements Validatable {
    private long priorityId;
    private String path;
    private EventType type;
    private String operatorId;
    private int priority;
    private long timestamp;
    private int retryCount;
    private EventStatus status = EventStatus.PENDING;

    public PrioritizedNodeEvent(NodeEvent nodeEvent, String operatorId) {
        this.path = nodeEvent.getPath();
        this.type = nodeEvent.getType();
        this.timestamp = nodeEvent.getTimestamp();
        this.operatorId = operatorId;
    }

    public RepoPath getRepoPath() {
        return RepoPathFactory.create(path);
    }

    public void processed() {
        this.status = EventStatus.PROCESSED;
    }

    public boolean isPending() {
        return this.status == EventStatus.PENDING;
    }

    @Override
    public void validate() {
        // noop
    }

    public enum EventStatus {
        PENDING,
        PROCESSED
    }
}
