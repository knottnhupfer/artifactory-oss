package org.artifactory.storage.fs.service;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class XrayTask {
    /** The time the Xray event task was created (millis) */
    private final long created;
    /** The task details */
    private final String taskContext;

    public XrayTask(long created, String taskContext) {
        this.created = created;
        this.taskContext = taskContext;
    }
}
