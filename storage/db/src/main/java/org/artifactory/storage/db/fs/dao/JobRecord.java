package org.artifactory.storage.db.fs.dao;


import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Yoaz Menda
 */
@Data
@AllArgsConstructor
public class JobRecord {
    private Long jobId;
    private String jobType;
    private String jobStatus;
    private long started;
    private long finished;
    private String additionalInfo;
}
