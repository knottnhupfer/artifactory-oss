/*
 *
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2018 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.artifactory.storage.jobs.migration.pathchecksum;

import org.artifactory.aql.api.domain.sensitive.AqlApiItem;
import org.artifactory.aql.result.AqlEagerResult;
import org.artifactory.aql.result.rows.AqlItem;
import org.artifactory.common.ConstantValues;
import org.artifactory.fs.FileInfo;
import org.artifactory.fs.ItemInfo;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.trash.TrashService;
import org.artifactory.schedule.JobCommand;
import org.artifactory.schedule.TaskUser;
import org.artifactory.storage.jobs.migration.MigrationJobBase;
import org.artifactory.version.ArtifactoryVersion;
import org.artifactory.version.ArtifactoryVersionProvider;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.artifactory.aql.api.internal.AqlBase.and;
import static org.artifactory.aql.api.internal.AqlBase.or;

/**
 * This job does the db migration to repoPath checksum by calculating the sha1 for each repoPath
 *
 * This job is meant to run as long as there are rows in the nodes table that don't have repoPath checksum values -
 * once done it also modifies the database by adding unique index to the repo_path_checksum field.
 *
 * @author Gidi Shabat
 */
@JobCommand(singleton = true,
        schedulerUser = TaskUser.SYSTEM,
        manualUser = TaskUser.SYSTEM,
        runOnlyOnPrimary = true,
        description = "Path Checksum Migration Job")
public class RepoPathChecksumMigrationJob extends MigrationJobBase implements RepoPathChecksumMigrationJobDelegate {
    private static final Logger log = LoggerFactory.getLogger(RepoPathChecksumMigrationJobDelegate.class);
    private static final Logger migrationLog = LoggerFactory.getLogger(RepoPathChecksumMigrationJob.class);  //this log is written to a separate appender to help keep track of the migration.

    private static final String WORK_QUEUE_CALLBACK_NAME = "updateRepoPathChecksum";
    private static final String JOB_NAME = "Path Checksum Migration";
    private static final String V550_PATH_CHECKSUM_DB_FINALIZE_CONVERSION = "v550e";

    private int missingRepoPathChecksumCount = 0;
    private final AtomicInteger totalDone = new AtomicInteger(0);           // Total artifacts updated this job
    private final AtomicInteger currentBatchCount = new AtomicInteger(0);   // Artifacts updated by current iteration of this job

    @Nullable
    @Override
    protected String forceExecutionNodeId(JobExecutionContext jobContext) {
        // Users can manipulate the node this task runs on, by default it will run on master only
        return ConstantValues.pathChecksumMigrationJobForceRunOnNodeId.getString();
    }

    @Override
    protected void initParams(JobExecutionContext callbackContext) {
        //no implementation
    }

    @Override
    protected ArtifactoryVersion getMinimalVersion() {
        return ArtifactoryVersionProvider.v550m001.get();
    }

    @Override
    protected boolean jobEnabled() {
        return ConstantValues.pathChecksumMigrationJobEnabled.getBoolean();
    }

    @Override
    protected String jobName() {
        return JOB_NAME;
    }

    @Override
    protected AtomicInteger currentBatchCount() {
        return currentBatchCount;
    }

    @Override
    protected String workQueueCallbackName() {
        return WORK_QUEUE_CALLBACK_NAME;
    }

    @Override
    protected boolean init() {
        if (dbService().isUniqueRepoPathChecksumReady()) {
            //Previous job has already marked completion, no need to check anything.
            migrationLog.debug("Path checksums already calculated by previous run of this job, aborting.");
            log.debug("Path checksums already calculated by previous run of this job, aborting.");
            return false;
        }
        if (refreshCounter()) {
            if (missingRepoPathChecksumCount < 1) {
                //first check - no need to run
                if (jobEnabled()) {
                    migrationLog.info("No missing repoPathChecksum values found in database, skipping Path Checksum migration job.");
                    log.info("No missing repoPathChecksum values found in database, skipping Path Checksum migration job.");
                }
                finalizeMigrationIfNeeded();
                return false;
            } else if (!jobEnabled()) {
                String warnMsg = "Path Checksum calculation job (for existing artifacts) has been disabled and will not run, " +
                                "there are still {} artifacts without path checksum values in the database. Future version of " +
                                "Artifactory may enforce this conversion as a prerequisite for upgrades.";
                log.warn(warnMsg, missingRepoPathChecksumCount);
                migrationLog.warn(warnMsg, missingRepoPathChecksumCount);
                return false;
            } else {
                String startMsg = "{} artifacts are missing repoPathChecksum values - starting calculation job.";
                migrationLog.info(startMsg, missingRepoPathChecksumCount);
                log.info(startMsg, missingRepoPathChecksumCount);
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean migrationLogic() {
        boolean tasksWereSubmitted = false;
        List<RepoPath> repoPaths = getMissingRepoPathChecksumNodes();
        if (!repoPaths.isEmpty()) {
            tasksWereSubmitted = true; //makes the outer loop keep going for the second check
        }
        for (RepoPath repoPath : repoPaths) {
            repoService().updateRepoPathChecksum(new RepoPathChecksumCalculationWorkItem(repoPath, this));
            waitBetweenBatchesIfNeeded();
        }
        return tasksWereSubmitted;
    }

    private boolean refreshCounter() {
        try {
            missingRepoPathChecksumCount = nodesDao().getMissingRepoPathChecksumArtifactCount();
        } catch (SQLException e) {
            migrationLog.error("Can't get a count of artifacts missing repoPathChecksum value, migration will not run: ", e);
            return false;
        }
        return true;
    }

    /**
     * @return a map (sha1 -> path) of nodes missing a repoPath checksum value.
     * AQL query is limited to 100 results.
     */
    private List<RepoPath> getMissingRepoPathChecksumNodes() {
        AqlEagerResult<AqlItem> noRepoPathChecksum = queryDbForNextBatch();
        List<FileInfo> missingRepoPathChecksumNodes = mapResultsToFileInfo(noRepoPathChecksum);
        markLastNodeId(missingRepoPathChecksumNodes);
        return missingRepoPathChecksumNodes.stream()
                .map(ItemInfo::getRepoPath)
                .collect(Collectors.toList());
    }

    /**
     * Gets the next {@link #queryLimit} results starting from the {@link #lastEntityId} returned by the last query.
     * This way we make sure to get all missing nodes while not getting duplicate results for nodes that failed
     * to update for any reason.
     */
    private AqlEagerResult<AqlItem> queryDbForNextBatch() {
        AqlApiItem query = AqlApiItem.create().filter(
                and(
                        //must include trashcan like this because aql removes it by default.
                        or(
                                AqlApiItem.type().equal("any"),
                                AqlApiItem.repo().equal(TrashService.TRASH_KEY),
                                AqlApiItem.repo().matches("*")
                        ),
                        AqlApiItem.repoPathChecksum().equal(null),
                        AqlApiItem.itemId().greaterEquals(lastEntityId)
                ))
                .addSortElement(AqlApiItem.itemId())
                .asc()
                .limit(queryLimit);
        return aqlService().executeQueryEager(query);
    }

    /**
     * {@link #lastEntityId} is maintained to offset each db query so that we don't reiterate on already-failed nodes.
     */
    private void markLastNodeId(List<FileInfo> missingRepoPathChecksumNodes) {
        lastEntityId = missingRepoPathChecksumNodes.stream()
                .map(ItemInfo::getId)
                .max(Comparator.naturalOrder())
                .orElse(-1L);
        migrationLog.debug("Marked last node id = {}", lastEntityId);
    }

    /**
     * This part is called only if the job ran and finished without errors, it runs a db conversion that puts a
     * NOT NULL constraint on the repo_path_checksum column of the node table.
     * This state is what we check on startup to know an instance is repoPathChecksum-ready.
     * In new installations the repoPathChecksum column is created with the constraint.
     */
    @Override
    protected void markCompletion() {
        migrationLog.debug("Path checksum migration job done, adding NOT NULL constraint to repo_path_checksum column.");
        finalizeMigrationIfNeeded();
        migrationLog.info("RepoPath checksum migration job has finished successfully. {} nodes were updated with " +
                        "repo_path_checksum values.", totalDone);
    }

    /**
     * Tests dbService (which verifies the repo_path_checksum column's null constraint) and runs the required conversion only if
     * it is needed (if the constraint is not yet set).
     */
    private void finalizeMigrationIfNeeded() {
        try {
            if (!dbService().verifyUniqueRepoPathChecksumState()) {
                executeDbConversion(V550_PATH_CHECKSUM_DB_FINALIZE_CONVERSION);
                dbService().verifyUniqueRepoPathChecksumState();
            }
        } catch (Exception e) {
            migrationLog.error("Failed to finalize path checksum migration: ", e);
        }
    }

    @Override
    protected void additionalSteps() {
        //nop
    }

    @Override
    protected void retry() {
        //noop
    }

    @Override
    protected boolean hasErrors() {
        return totalDone.get() < missingRepoPathChecksumCount;
    }

    @Override
    protected void finishedWithErrors() {
        migrationLog.error("Path checksum migration job finished with errors ({} paths were calculated out of {} required) - " +
                        "check the log for further details.", totalDone.get(), missingRepoPathChecksumCount);
    }

    @Override
    protected boolean stateOk() {
        refreshCounter();
        return missingRepoPathChecksumCount < 1;
    }

    @Override
    public void incrementTotalDone() {
        totalDone.incrementAndGet();
    }

    @Override
    public void incrementCurrentBatchCount() {
        currentBatchCount.incrementAndGet();
    }

    @Override
    public Logger log() {
        return migrationLog;
    }

    @Override
    protected void logProgress() {
        if (totalDone.get() > missingRepoPathChecksumCount) {
            migrationLog.info("Path checksum migration state: {} path calculation tasks were submitted (now in retry phase).", totalDone.get());
        } else {
            migrationLog.info("Path checksum migration state: {}/{} artifacts were handled.", totalDone.get(), missingRepoPathChecksumCount);
        }
    }
}
