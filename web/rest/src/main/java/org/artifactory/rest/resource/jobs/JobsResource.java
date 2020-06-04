package org.artifactory.rest.resource.jobs;

import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.jobs.JobStatus;
import org.artifactory.addon.replication.ReplicationAddon;
import org.artifactory.addon.replication.ReplicationStrategy;
import org.artifactory.addon.replication.ReplicationType;
import org.artifactory.api.replication.ReplicationJobInfo;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.rest.exception.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.apache.http.HttpStatus.SC_OK;
import static org.artifactory.api.rest.constant.JobsConstants.*;
import static org.artifactory.common.ConstantValues.jobsTableTimeToLiveMillis;

/**
 * Resource that provides the ability to monitor jobs in Artifactory
 *
 * @author Yoaz Menda
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Path(JOBS)
@RolesAllowed(AuthorizationService.ROLE_ADMIN)
public class JobsResource {
    private static final Logger log = LoggerFactory.getLogger(JobsResource.class);

    private AddonsManager addonsManager;

    @Autowired
    public JobsResource(AddonsManager addonsManager) {
        this.addonsManager = addonsManager;
    }

    /**
     * @param startedAfter    - replications since this time - string in UTC format (i.e. "2014-12-12T10:39:40Z")
     * @param finishedBefore  - replications until this time - string in UTC format (i.e. "2014-12-12T10:39:40Z")
     * @param jobstatus       - jobstatus of the replication job instance (org.artifactory.addon.jobs.JobStatus: RUNNING, STOPPED, FINISHED)
     * @param replicationType - replication type (org.artifactory.addon.replication.core.job.ReplicationType: PUSH, PULL)
     * @param sourceRepoKey   - replication source repo key
     * @param targetURL       - url of the replication target
     * @return list of instances of active and past jobs of type REPLICATION optionally filtered by different params
     */
    @GET
    @Path(REPLICATION_JOBS)
    @Produces(MediaType.APPLICATION_JSON)
    public Response monitorReplications(
            @QueryParam(STARTED_AFTER) String startedAfter,
            @QueryParam(FINISHED_BEFORE) String finishedBefore,
            @QueryParam(JOB_STATUS) JobStatus jobstatus,
            @QueryParam(REPLICATION_TYPE) ReplicationType replicationType,
            @QueryParam(REPLICATION_STRATEGY) ReplicationStrategy replicationStrategy,
            @QueryParam(SOURCE_REPO) String sourceRepoKey,
            @QueryParam(TARGET_URL) String targetURL
    ) {
        log.debug("Replication jobs request");
        validateJobDates(startedAfter, finishedBefore);
        ReplicationAddon replicationAddon = addonsManager.addonByType(ReplicationAddon.class);
        List<ReplicationJobInfo> replicationJobs = replicationAddon
                .getReplicationJobs(startedAfter, finishedBefore, jobstatus, replicationType, replicationStrategy,
                        sourceRepoKey, targetURL);
        return Response.status(SC_OK).entity(replicationJobs).build();
    }

    private void validateJobDates(String startedAfter, String finishedBefore) {
        if (StringUtils.isBlank(startedAfter)) {
            String error = "'" + STARTED_AFTER + "' parameter is mandatory";
            log.debug("can't serve replication jobs query: {}",  error);
            throw new BadRequestException(error);
        }
        Date startedAfterDate;
        try {
            startedAfterDate = Date.from(Instant.parse(startedAfter));
        } catch (DateTimeParseException e) {
            String error = "Cannot parse the given '" + STARTED_AFTER + "' parameter";
            log.debug("can't serve replication jobs query: {}",  error);
            throw new BadRequestException(error);
        }
        long maxDaysAgoInMillis = jobsTableTimeToLiveMillis.getLong();
        long maxDaysAgo = maxDaysAgoInMillis / 1000L / 60L / 60L / 24L;
        if (startedAfterDate.before(new Date(Instant.now().minus(maxDaysAgo, DAYS).toEpochMilli()))) {
            String error = "'" + STARTED_AFTER + "' parameter must not be under " + maxDaysAgo + " days from now";
            log.debug("can't serve replication jobs query: {}",  error);
            throw new BadRequestException(error);
        }
        try {
            if (StringUtils.isNotBlank(finishedBefore)) {
                Date finishedBeforeDate = Date.from(Instant.parse(finishedBefore));
                if (startedAfterDate.after(finishedBeforeDate)) {
                    String error =
                            "'" + STARTED_AFTER + "' parameter must precede '" + FINISHED_BEFORE + "' parameter";
                    log.debug("can't serve replication jobs query: {}",  error);
                    throw new BadRequestException(error);
                }
            }
        } catch (DateTimeParseException e) {
            String error = "Cannot parse the given '" + FINISHED_BEFORE + "' parameter";
            log.debug("can't serve replication jobs query: {}",  error);
            throw new BadRequestException(error);
        }
    }
}
