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

package org.artifactory.storage.jobs.migration.sha256;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang.StringUtils;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.exception.ItemNotFoundRuntimeException;
import org.artifactory.aql.api.domain.sensitive.AqlApiItem;
import org.artifactory.aql.result.AqlEagerResult;
import org.artifactory.aql.result.rows.AqlItem;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.fs.FileInfo;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.trash.TrashService;
import org.artifactory.schedule.JobCommand;
import org.artifactory.schedule.TaskUser;
import org.artifactory.storage.binstore.service.BinaryInfo;
import org.artifactory.storage.binstore.service.BinaryService;
import org.artifactory.storage.binstore.service.InternalBinaryService;
import org.artifactory.storage.db.InternalDbService;
import org.artifactory.storage.db.binstore.dao.BinariesDao;
import org.artifactory.storage.jobs.migration.MigrationJobBase;
import org.artifactory.version.ArtifactoryVersion;
import org.artifactory.version.ArtifactoryVersionProvider;
import org.jfrog.common.MultimapCollectors;
import org.jfrog.storage.binstore.common.ChecksumInputStream;
import org.jfrog.storage.binstore.exceptions.BinaryNotFoundException;
import org.jfrog.storage.binstore.ifc.model.BinaryElement;
import org.jfrog.storage.binstore.ifc.model.BinaryElementHeaders;
import org.jfrog.storage.binstore.utils.Checksum;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.artifactory.api.rest.constant.Sha256MigrationRestConstants.*;
import static org.artifactory.aql.api.internal.AqlBase.and;
import static org.artifactory.aql.api.internal.AqlBase.or;

/**
 * This job does the db migration to sha256 by calculating the sha2 for each binary that's missing it in the binaries
 * table once and then inserting values into the nodes table.
 *
 * This job is meant to run as long as there are rows in the nodes table that don't have sha2 values.
 * At the end it introduces a NOT NULL constraint on the binaries' table sha256 column.
 *
 * @author Dan Feldman
 */
@JobCommand(singleton = true,
        schedulerUser = TaskUser.SYSTEM,
        manualUser = TaskUser.SYSTEM,
        runOnlyOnPrimary = false,
        description = "SHA256 Migration Job")
public class Sha256MigrationJob extends MigrationJobBase implements Sha256MigrationJobDelegate {
    private static final Logger log = LoggerFactory.getLogger(Sha256MigrationJobDelegate.class);
    private static final Logger migrationLog = LoggerFactory.getLogger(Sha256MigrationJob.class); //this log is written to a separate appender to help keep track of the migration.

    private static final String JOB_NAME = "SHA256 Migration";
    private static final String V550_SHA2_DB_FINALIZE_CONVERSION = "v550d";
    private static final String WORK_QUEUE_CALLBACK_NAME = "updateSha2";
    private static final String ERR_BINARY_NOT_FOUND = "Failed to locate SHA1 '%s' in %s while trying to calculate " +
            "SHA2 for all artifacts that are linked to it, all related paths will be skipped";

    private String forceExecutionNodeId = ConstantValues.sha2MigrationJobForceRunOnNodeId.getString();
    private boolean jobEnabled = ConstantValues.sha2MigrationJobEnabled.getBoolean();
    private int nodesMissingSha2Count = 0;                                                // Total artifacts (in nodes table) missing a sha2 value, calculated once when job starts
    private int binariesMissingSha2Count = 0;                                             // Total rows in binaries table missing a sha2 value, calculated once when job starts
    private int totalBinariesDone = 0;                                                    // Total binary table rows updated this job
    private final AtomicInteger totalNodesDone = new AtomicInteger(0);                    // Total artifacts updated this job
    private final AtomicInteger currentBatchCount = new AtomicInteger(0);                 // Artifacts updated by current iteration of this job
    private final Map<RepoPath, String> fatalNodeErrors = new ConcurrentHashMap<>();      // Saves failures we cannot recover from
    private final Map<RepoPath, String> noneFatalNodeErrors = new ConcurrentHashMap<>();  // Saves failures that should not fail the migration
    private final Map<String, String> fatalSha1Errors = new HashMap<>();                  // Saves failures for sha1 values we couldn't make up for in the binaries table.

    @Override
    protected void initParams(JobExecutionContext callbackContext) {
        //Incoming from rest execution, else we default to system props if not present
        JobDataMap jobParams = callbackContext.getJobDetail().getJobDataMap();
        this.sleepInterval = (long) Optional.ofNullable(jobParams.get(SLEEP_INTERVAL_MILLIS)).orElse(sleepInterval);
        this.batchThreshold = (int) Optional.ofNullable(jobParams.get(BATCH_THRESHOLD)).orElse(batchThreshold);
        this.queryLimit = (int) Optional.ofNullable(jobParams.get(QUERY_LIMIT)).orElse(queryLimit);
        this.waitForClusterSleepInterval = (int) Optional.ofNullable(jobParams.get(WAIT_FOR_CLUSTERS_SLEEP_INTERVAL_MILLIS)).orElse(waitForClusterSleepInterval);
        this.jobEnabled = (boolean) Optional.ofNullable(jobParams.get(JOB_ENABLED)).orElse(jobEnabled);
        this.forceExecutionNodeId = (String) Optional.ofNullable(jobParams.get(FORCE_RUN_ON_NODE_ID)).orElse(forceExecutionNodeId);
    }

    @Override
    protected boolean jobEnabled() {
        return jobEnabled;
    }

    @Nullable
    @Override
    protected String forceExecutionNodeId(JobExecutionContext jobContext) {
        // Users can manipulate the node this task runs on, by default it will run on master only
        return forceExecutionNodeId;
    }

    @Override
    protected ArtifactoryVersion getMinimalVersion() {
        return ArtifactoryVersionProvider.v550m001.get();
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
    protected void logProgress() {
        if (totalNodesDone.get() > nodesMissingSha2Count) {
            migrationLog.info("SHA256 migration state: {} artifact calculation tasks were submitted (now in retry phase).", totalNodesDone.get());
        } else {
            migrationLog.info("SHA256 migration state: {}/{} artifacts (approx.) were handled.", totalNodesDone.get(), nodesMissingSha2Count);
        }
    }

    @Override
    protected boolean migrationLogic() {
        boolean tasksWereSubmitted = false;
        Multimap<String, RepoPath> sha1ToPaths = getMissingSha2Nodes();
        if (sha1ToPaths.size() > 0) {
            tasksWereSubmitted = true; //makes the outer loop keep going for the second check
        }
        for (String sha1 : sha1ToPaths.keySet()) {
            markTaskAsSubmitted(sha1);
            repoService().updateSha2(new ChecksumCalculationWorkItem(sha1, sha1ToPaths.get(sha1), this));
            waitBetweenBatchesIfNeeded();
        }
        return tasksWereSubmitted;
    }

    @Override
    protected void additionalSteps() {
        fillMissingBinariesTableRows();
    }

    /**
     * The init phase takes care of a few considerations:
     *
     * - First check is made to test whether this migration has already previously run
     * (determined by {@link InternalDbService#isSha256Ready()} - if yes we break.
     *
     * - Next check is to verify this is the only job running in the cluster. If due to some user error (i.e. executing
     * the migration request on 2 different nodes) the job is running in more than this node, then this node will fail the job.
     *
     * - Next the init phase tries to figure out if it should work by the following criteria:
     * -- nodes table has null values in the sha256 column
     * -- binaries table has null values in the sha256 column
     *
     * - Lastly if no null sha256 values are found the init phase also runs the finalizing conversion which sets the
     * sh256 column to 'not null', regardless of whether the job itself actually ran - for cases where perhaps some
     * other node completed the conversion but did not finalize.  The end goal is to have a sha256 column which is not
     * null, when possible, regardless of the job's state.
     */
    @Override
    protected boolean init() {
        if (dbService().verifySha256State()) {
            //Previous job has already marked completion, no need to check anything.
            if (jobEnabled()) {
                log.debug("SHA256 values already set by previous run of this job, aborting.");
                migrationLog.info("SHA256 values already set by previous run of this job, aborting.");
            }
            return false;
        }
        if (refreshCounters()) {
            if (nodesMissingSha2Count < 1 && binariesMissingSha2Count < 1) {
                //first check - no need to run
                migrationLog.info("No missing SHA256 values found in database, skipping SHA256 migration job.");
                finalizeMigrationIfNeeded();
                return false;
            } else if (!jobEnabled()) {
                log.info("SHA256 migration job (for existing artifacts) is disabled and will not run, there are " +
                        "{} artifacts without SHA256 values in the database.  Future versions of Artifactory may enforce " +
                        "this migration as a prerequisite for upgrades.", nodesMissingSha2Count + binariesMissingSha2Count);
                return false;
            } else {
                String msg = "{} artifacts and {} binary entries are missing SHA256 values - starting calculation job.";
                migrationLog.info(msg, nodesMissingSha2Count, binariesMissingSha2Count);
                log.info(msg, nodesMissingSha2Count, binariesMissingSha2Count);
                return true;
            }
        }
        return false;
    }

    private boolean refreshCounters() {
        try {
            //ok to get these via spring here, by definition this job starts only when the db conversion finished.
            nodesMissingSha2Count = nodesDao().getMissingSha2ArtifactCount();
            binariesMissingSha2Count = binariesDao().getSha1ForMissingSha2Count();
        } catch (SQLException e) {
            migrationLog.error("Can't get a count of artifacts and binaries missing sha2 value, migration will not run: ", e);
            return false;
        }
        return true;
    }

    /**
     * @return a map (sha1 -> path) of nodes missing a sha2 value.
     * AQL query is limited to 100 results.
     */
    private Multimap<String, RepoPath> getMissingSha2Nodes() {
        AqlEagerResult<AqlItem> noSha2 = queryDbForNextBatch();
        List<FileInfo> missingSha2Nodes = mapResultsToFileInfo(noSha2);
        markLastNodeId(missingSha2Nodes, migrationLog);
        //Map sha1 -> nodes: group same sha1 nodes together to minimize db queries later on when trying to figure sha2
        //Filter out files that had unrecoverable errors so that we don't loop on them indefinitely
        return missingSha2Nodes.stream()
                .filter(file -> !fatalNodeErrors.keySet().contains(file.getRepoPath())) //filter out non-recoverable errors
                .collect(MultimapCollectors.toMultimap(FileInfo::getSha1, FileInfo::getRepoPath));
    }

    /**
     * Gets the next {@link #queryLimit} results starting from the {@link #lastEntityId} returned by the last query.
     * This way we make sure to get all missing nodes while not getting duplicate results for nodes that failed
     * to update for any reason.
     */
    private AqlEagerResult<AqlItem> queryDbForNextBatch() {
        AqlApiItem query = AqlApiItem.create().filter(
                and(
                        or(
                                //must include trashcan like this because aql removes it by default.
                                AqlApiItem.repo().equal(TrashService.TRASH_KEY),
                                AqlApiItem.repo().matches("*")
                        ),
                        AqlApiItem.sha2().equal(null),
                        AqlApiItem.itemId().greater(lastEntityId)
                ))
                .addSortElement(AqlApiItem.itemId())
                .asc()
                .limit(queryLimit);
        return aqlService().executeQueryEager(query);
    }

    /**
     * At the end of the nodes table conversion, sha256 values might still be missing from the binaries table in cases
     * where deletes happened on an active instance during the job's execution.
     * New nodes are written with sha256 values so only 'old' deletions might have this ailment - meaning we only need
     * to correct this once.
     */
    private void fillMissingBinariesTableRows() {
        //Update the missing binaries count since the former phase probably have updated most of them.
        try {
            binariesMissingSha2Count = binariesDao().getSha1ForMissingSha2Count();
        } catch (SQLException e) {
            migrationLog.warn("Can't refresh the count of binaries that are missing sha2 values, relying on old count("
                    + binariesMissingSha2Count + ")", e);
        }
        //Db queries are offset by the number of fatal errors so we don't keep getting the same results
        if (binariesMissingSha2Count > 0) {
            migrationLog.info("SHA256 migration job now filling in for missing SHA256 values for binary entries. " +
                    "Found {} such entries", binariesMissingSha2Count);
        }
        int batchCounter = 0;
        List<String> sha1ForMissingSha2 = getSha1ForMissingSha2();
        while(sha1ForMissingSha2.size() > 0) {
            for (String sha1 : sha1ForMissingSha2) {
                pauseOrBreakIfNeeded();
                try {
                    if (batchCounter >= batchThreshold) {
                        migrationLog.info("SHA256 migration state: {}/{} binary entries were handled.", totalBinariesDone, binariesMissingSha2Count);
                        //sleepy time
                        Thread.sleep(sleepInterval);
                        batchCounter = 0;
                    }
                    batchCounter++;
                    getOrCalculateSha2(sha1);
                    totalBinariesDone++;
                } catch (InterruptedException e) {
                    migrationLog.warn("Binary entries loop interrupted during batch threshold sleep.");
                } catch (Sha256CalculationFatalException e) {
                    migrationLog.error("Error retrieving and updating SHA256 value for SHA1 '" + sha1 + "': ", e);
                    fatalSha1Errors.put(sha1, e.getMessage());
                }
            }
            sha1ForMissingSha2 = getSha1ForMissingSha2();
            //break condition just in case, can't really do retries in this phase.
            if (fatalSha1Errors.size() >= binariesMissingSha2Count - totalBinariesDone) {
                return;
            }
        }
    }

    private List<String> getSha1ForMissingSha2() {
        List<String> sha1ForMissingSha2 = Lists.newArrayList();
        try {
            sha1ForMissingSha2 = binariesDao().getSha1ForMissingSha2(queryLimit, fatalSha1Errors.size());
        } catch (SQLException e) {
            migrationLog.error("Failed to retrieve SHA1 entries with missing SHA256 values:", e);
        }
        return sha1ForMissingSha2;
    }

    @Override
    protected void retry() {
        migrationLog.warn("SHA256 migration job determined it's done going over all db rows but has also detected there" +
                " are still rows with null values in the binaries table - it will attempt a second pass over these values.");
        //retry the migration loop one more time to redo operations for non-fatal errors
        migrationLoop();
        waitForSubmittedTasks();
        if (!stateOk() || hasErrors()) {
            finishedWithErrors();
        } else {
            markCompletion();
        }
    }

    /**
     * This part is called only if the job ran and finished without errors, it runs a db conversion that puts a
     * NOT NULL constraint on the sha256 column of the binaries table.
     * This state is what we check on startup to know an instance is sha256-ready.
     * In new installations the sha256 column is created with the constraint.
     */
    @Override
    protected void markCompletion() {
        migrationLog.info("SHA256 migration job done, adding NOT NULL constraint to SHA256 column.");
        if (finalizeMigrationIfNeeded()) {
            triggerConfigReload();
            String finalizeMsg = "SHA256 migration job has finished successfully. {} artifacts and {} binary entry calculations were submitted" +
                    ((totalNodesDone.get() > nodesMissingSha2Count || totalBinariesDone > binariesMissingSha2Count) ? " (including retries)" : ".");
            migrationLog.info(finalizeMsg, totalNodesDone.get(), totalBinariesDone);
            log.info(finalizeMsg, totalNodesDone.get(), totalBinariesDone);
        } else {
            String err = "Failed to finalize the SHA256 migration process, re-running this job (by restarting this node) may be required.";
            log.error(err);
            migrationLog.error(err);
        }
    }

    @Override
    protected boolean stateOk() {
        refreshCounters();
        // Non fatal errors are ignored here, these shouldn't fail the migration.
        return nodesMissingSha2Count - noneFatalNodeErrors.size() < 1 && binariesMissingSha2Count < 1;
    }

    @Override
    protected boolean hasErrors() {
        return fatalNodeErrors.size() != 0 || fatalSha1Errors.size() != 0;
    }

    @Override
    protected void finishedWithErrors() {
        int fatalNodeErrors = this.fatalNodeErrors.size();
        int fatalSha1Errors = this.fatalSha1Errors.size();
        if (fatalNodeErrors == 0 && fatalSha1Errors == 0) {
            //some unspecified error... user needs to restart the migration or call us...
            String unspecified = "The SHA256 migration job has finished with unspecified errors which might be deduced " +
                    "from this run's log. To mitigate this it may be enough to re-run the migration (by restarting this node)," +
                    " if a second run fails please contact JFrog support with the migration log.";
            log.error(unspecified);
            migrationLog.error(unspecified);
            return;
        }
        //sizes here are an estimate only, meant more to make the user feel good with tasty numbers rather then anything else.
        String errMsg = "The SHA256 migration job has finished with {} errors (out of {} total artifacts and binary entries that " +
                        "needed conversion), it will continue to run on each restart until you either resolve the errors " +
                        "or disable this job (with system flag 'checksum.calculation.job.enabled'). Disabling this job " +
                        "leaves your instance in a vulnerable state and may prevent upgrading to future major versions.\n";
        log.error(errMsg, fatalNodeErrors + fatalSha1Errors, nodesMissingSha2Count + binariesMissingSha2Count);
        migrationLog.error(errMsg, fatalNodeErrors + fatalSha1Errors, nodesMissingSha2Count + binariesMissingSha2Count);
        StringBuilder listBuilder = new StringBuilder();
        if (fatalNodeErrors > 0) {
            listBuilder.append("Paths that failed conversion:\n-----------------------------\n");
            this.fatalNodeErrors.forEach((path, error) -> listBuilder.append(path).append(" --> ").append(error).append("\n"));
        }
        if (fatalSha1Errors > 0) {
            listBuilder.append("Binaries (SHA1 values) that failed conversion:\n----------------------------------------------\n");
            this.fatalSha1Errors.forEach((sha1, error) -> listBuilder.append(sha1).append(" --> ").append(error).append("\n"));
        }
        String list = listBuilder.toString();
        try {
            File dumpFile = new File(ArtifactoryHome.get().getEtcDir(),"sha2_migration_badFiles-" + System.currentTimeMillis() + ".list");
            FileUtils.write(dumpFile, list);
            log.error("The failed paths list has been written to {} for convenience.", dumpFile.getAbsolutePath());
            migrationLog.error("The failed paths list has been written to {} for convenience.", dumpFile.getAbsolutePath());
        } catch (IOException e) {
            String err = "Failed to write failed paths list to etc folder: ";
            log.warn(err +  e.getMessage());
            migrationLog.warn(err, e);
        }
        migrationLog.error("Failures: {}", list);
    }

    /**
     * Tests dbService (which verifies the sha256 column's null constraint) and runs the required conversion only if
     * it is needed (if the constraint is not yet set).
     */
    private boolean finalizeMigrationIfNeeded() {
        boolean ok;
        try {
            if (dbService().verifySha256State()) {
                migrationLog.info("Skipping SHA256 migration finalization ('v550d' db conversion) - database is already in correct state.");
                ok = true;
            } else {
                executeDbConversion(V550_SHA2_DB_FINALIZE_CONVERSION);
                ok = dbService().verifySha256State();
            }
        } catch (Exception e) {
            String err = "Failed to finalize SHA256 migration: ";
            log.error(err + e.getMessage());
            migrationLog.error(err, e);
            ok = false;
        }
        return ok;
    }

    // ----  This is the part that the repository service work queue method runs (Sha256MigrationJobDelegate) --- \\

    @Override
    public void markFatalErrorOnPaths(Collection<RepoPath> badPaths, String error) {
        badPaths.forEach(path -> fatalNodeErrors.put(path, error));
    }

    @Override
    public void markNonFatalErrorOnPaths(Collection<RepoPath> badPaths, String error) {
        badPaths.forEach(path -> noneFatalNodeErrors.put(path, error));
    }

    @Override
    public void incrementTotalDone() {
        totalNodesDone.incrementAndGet();
    }

    @Override
    public void incrementCurrentBatchCount() {
        currentBatchCount.incrementAndGet();
    }

    @Override
    public Logger log() {
        return migrationLog;
    }

    /**
     * Tries to retrieve the sha2 value matching this {@param sha1} from the binaries table, if it doesn't exist
     * the binary matching the sha1 value is retrieved from the binarystore and the sha2 is calculated from the
     * incoming stream.
     */
    @Override
    public String getOrCalculateSha2(String sha1) throws Sha256CalculationFatalException {
        String sha2 = getSha2FromDb(sha1);
        if (StringUtils.isNotBlank(sha2)) {
            //gotcha!
            migrationLog.info("SHA256 value '{}' for sha1 '{}' found from binary entries.", sha2, sha1);
        } else {
            migrationLog.info("No SHA256 value found in db for sha1 '{}' - running calculation from stream", sha1);
            sha2 = calculateSha2AndUpdateDb(sha1);
        }
        return sha2;
    }

    @Override
    public void handleExceptionDuringMigration(Collection<RepoPath> paths, Exception e) {
        if (e instanceof ItemNotFoundRuntimeException) {
            // Non fatal
            markNonFatalErrorOnPaths(paths, e.getMessage());
        } else {
            // Fatal (the migration will fail)
            markFatalErrorOnPaths(paths, e.getMessage());
        }
    }

    /**
     * Tries the binaries table for {@param sha1}
     */
    private String getSha2FromDb(String sha1) throws Sha256CalculationFatalException {
        BinaryInfo binary;
        try {
            binary = BinaryService().findBinary(ChecksumType.sha1, sha1);
        } catch (Exception e) {
            String msg = String.format(ERR_BINARY_NOT_FOUND, sha1, "binary entries") + ": " + e.getMessage();
            migrationLog.error(msg, e);
            throw new Sha256CalculationFatalException(msg, e);
        }
        if (binary == null) {
            String msg = String.format(ERR_BINARY_NOT_FOUND, sha1, "binary entries");
            migrationLog.warn(msg);
            throw new Sha256CalculationFatalException(msg);
        } else {
            return binary.getSha2();
        }
    }

    /**
     * Triggers retrieval and sha256 calculation of the binary matching {@param sha1}.
     * If ok, inserts the value into the binaries table.
     */
    private String calculateSha2AndUpdateDb(String sha1) throws Sha256CalculationFatalException {
        String sha2;
        sha2 = calculateSha2For(sha1);
        if (StringUtils.isNotBlank(sha2)) {
            migrationLog.info("Inserting sha256 value '{}' for sha1 value '{}' into binaries table", sha2, sha1);
            try {
                if (!binaryService().updateSha2ForSha1(sha1, sha2)) {
                    migrationLog.warn("No binary entries were updated for sha1 '{}' and new sha256 '{}'", sha1, sha2);
                }
            } catch (SQLException e) {
                migrationLog.error("Failed to update database with new sha256 '" + sha2 + "' for sha1 '" + sha1 + "': ", e);
                sha2 = null;
            }
        }
        return sha2;
    }

    /**
     * Retrieves the binary matching {@param sha1} from the binarystore and
     * reads it using a {@link ChecksumInputStream} in order to calculate its sha2 value.
     */
    private String calculateSha2For(String sha1) throws Sha256CalculationFatalException {
        BinaryElement element = binaryService().createBinaryElement(sha1, null, null, -1);
        //we don't want to cache random, probably less used artifacts in the filesystem just for the sake of this calculation
        element.addHeader(BinaryElementHeaders.SKIP_CACHE, "true");
        Checksum sha2 = new Checksum(ChecksumType.sha256.alg(), ChecksumType.sha256.length());
        migrationLog.info("Retrieving binary for sha1 '{}' from binary store", sha1);
        String sha2Value = null;
        try (InputStream in = binaryService().getBinary(element);
             ChecksumInputStream calcStream = new ChecksumInputStream(in, sha2)) {
            migrationLog.info("Calculating sha256 for sha1 '{}'", sha1);
            //waste the stream to have it calculate the checksum
            IOUtils.copy(calcStream, new NullOutputStream());
            calcStream.close();
            sha2Value = sha2.getChecksum();
        } catch (BinaryNotFoundException bnf) {
            String msg = String.format(ERR_BINARY_NOT_FOUND, sha1, "filestore") + ": " + bnf.getMessage();
            migrationLog.error(msg, bnf);
            throw new Sha256CalculationFatalException(msg, bnf);
        } catch (Exception e) {
            String msg = "Can't retrieve binary matching SHA1 '" + sha1 + "' from the binarystore: ";
            migrationLog.error(msg, e);
            throw new Sha256CalculationFatalException(msg + e.getMessage(), e);
        }
        migrationLog.info("sha256 value '{}' calculated from stream for sha1 '{}'", sha2Value, sha1);
        return sha2Value;
    }

    private BinaryService BinaryService() {
        return ContextHelper.get().beanForType(BinaryService.class);
    }

    private BinariesDao binariesDao() {
        return ContextHelper.get().beanForType(BinariesDao.class);
    }

    private InternalBinaryService binaryService() {
        return ContextHelper.get().beanForType(InternalBinaryService.class);
    }
}
