package org.artifactory.storage.db.fs.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.artifactory.addon.jobs.JobStatus;
import org.artifactory.addon.jobs.JobType;

import java.time.Instant;

/**
 * @author Yoaz Menda
 */
@Data
@AllArgsConstructor
public class JobEntity {
    private Long jobId;
    private JobType jobType;
    private JobStatus jobStatus;
    private Instant started;
    private Instant finished;
    private String additionalDetails;
}
