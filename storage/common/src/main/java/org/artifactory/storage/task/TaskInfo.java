package org.artifactory.storage.task;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Shay Bagants
 */
@Data
@AllArgsConstructor
public class TaskInfo {
    private String taskType;
    private String taskContext;
    private long created;
}
