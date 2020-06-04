package org.artifactory.storage.jobs.migration.buildinfo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.artifactory.api.build.BuildService;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.repo.exception.RepoRejectException;
import org.artifactory.aql.api.domain.sensitive.AqlApiItem;
import org.artifactory.aql.result.AqlLazyResult;
import org.artifactory.aql.result.rows.AqlItem;
import org.artifactory.aql.result.rows.AqlLazyObjectResultStreamer;
import org.artifactory.aql.result.rows.FileInfoItemRow;
import org.artifactory.build.InternalBuildService;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.repo.RepoPath;
import org.artifactory.sapi.interceptor.context.DeleteContext;
import org.artifactory.schedule.JobCommand;
import org.artifactory.schedule.TaskUser;
import org.artifactory.storage.StorageException;
import org.artifactory.storage.db.build.dao.BuildsDao;
import org.artifactory.storage.db.build.entity.BuildEntity;
import org.artifactory.storage.jobs.migration.EndlessLoopPreventionException;
import org.artifactory.storage.jobs.migration.MigrationJobBase;
import org.artifactory.version.ArtifactoryVersion;
import org.artifactory.version.ArtifactoryVersionProvider;
import org.iostreams.streams.in.StringInputStream;
import org.jfrog.build.api.Build;
import org.jfrog.common.JsonUtils;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.artifactory.aql.api.domain.sensitive.AqlApiItem.*;
import static org.artifactory.aql.api.internal.AqlBase.and;
import static org.artifactory.build.BuildInfoUtils.formatBuildTime;
import static org.artifactory.build.BuildInfoUtils.parseBuildTime;
import static org.artifactory.build.BuildServiceUtils.getBuildJsonPathInRepo;

/**
 * Migration of Builds JSON from DB table to repo {@link InternalBuildService#getBuildInfoRepoKey()}.
 * If a foreign key called build_jsons_builds_fk set on the build_jsons table exists, this migration will run.
 *
 * We fetch {@link #queryLimit} build ids from build_jsons table at a time.
 * We select each json from the db table and try to retrieve the Build Info JSON matching this {@param buildId}
 * from the Build Info repository.
 *
 * if it doesn't exist, the Build info json value is written into the proper coordinates in the repo
 * {@link InternalBuildService#getBuildInfoRepoKey()}/<buildName>/<buildNumber>-<buildStarted>.json
 *
 * Job is completed once we iterated the entire table and has no errors.
 * Then the table is dropped from db.
 *
 * A config descriptor save is made in order to trigger a state reload to {@link InternalBuildService#isBuildInfoReady()}
 * Then all other active nodes are notified and reload the state as well (state is checked upon every config reload)
 *
 * State being True will:
 * - All build .jsons will be written to repo only.
 *
 * (While state is False we save each .json file to both repo AND build_jsons table in order to maintain legacy code)
 *
 * @author Yuval Reches
 */
@JobCommand(singleton = true,
        manualUser = TaskUser.SYSTEM,
        description = "Builds JSON migration Job")
public class BuildInfoMigrationJob extends MigrationJobBase implements BuildInfoMigrationJobDelegate {
    private static final Logger log = LoggerFactory.getLogger(BuildInfoMigrationJobDelegate.class);
    //this log is written to a separate appender to help keep track of the migration.
    private static final Logger migrationLog = LoggerFactory.getLogger(BuildInfoMigrationJob.class);

    private static final String JOB_NAME = "BuildInfo Migration";
    private static final String WORK_QUEUE_CALLBACK_NAME = "migrateBuildInfo";
    private static final String V660_BUILD_INFO_DB_FINALIZE_CONVERSION = "v660";

    private int repoMissingBuildJsonCount = 0;                                        // Total jsons in build_jsons table, calculated once when job starts
    private final AtomicInteger totalJsonsDone = new AtomicInteger(0);      // Total json artifacts created by this job
    private final AtomicInteger currentBatchCount = new AtomicInteger(0);   // Artifacts updated by current iteration of this job
    private final List<String> fatalBuildErrors = new ArrayList<>();  // Saves failures we cannot recover from

    @Override
    protected boolean jobEnabled() {
        return ConstantValues.buildInfoMigrationJobEnabled.getBoolean();
    }

    @Override
    protected ArtifactoryVersion getMinimalVersion() {
        return ArtifactoryVersionProvider.v660m001.get();
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
        migrationLog.info("Build Info migration state: {}/{} (approx.) json entries were handled.", totalJsonsDone.get(),
                repoMissingBuildJsonCount);
    }

    @Override
    protected void initParams(JobExecutionContext callbackContext) {
        // Nothing to do here, params are set by parent class
    }

    @Override
    protected void runMigration() {
        migrationLoop();
        additionalSteps();
        waitForSubmittedTasks();
        if (hasErrors()) {
            // We had errors, retry the entire op another time.
            retry();
        } else { //run verification on counters before final db conversion
            markCompletion();
        }
    }

    @Override
    protected boolean migrationLogic() {
        boolean tasksWereSubmitted = false;
        List<Long> buildJsonIds = getBuildJsonEntriesIds();
        if (buildJsonIds == null) {
            // Means error occurred and we stop the migration. Reset the last id in order to stop the loop
            lastEntityId = -1;
            fatalBuildErrors.add("Can't get list of build ids");
            return false;
        }
        if (CollectionUtils.isNotEmpty(buildJsonIds)) {
            tasksWereSubmitted = true; //makes the outer loop keep going for the second check
        }
        buildJsonIds.forEach(buildId -> {
            markTaskAsSubmitted(Long.toString(buildId));
            pauseOrBreakIfNeeded();
            buildService().migrateBuildInfo(new BuildInfoCalculationWorkItem(buildId, this));
            waitBetweenBatchesIfNeeded();
        });
        return tasksWereSubmitted;
    }

    @Override
    protected void additionalSteps() {
        lookForRepoTrashAndDeleteIt();
    }

    /**
     * The init phase takes care of a few considerations:
     *
     * - First check is to verify this is the only job running in the cluster.
     * If the job is running in more than this node, then this node will fail the job.
     * (Verified at {@link MigrationJobBase#onExecute(JobExecutionContext)})
     *
     * - Next check is to verify that all nodes were upgraded to {@link ArtifactoryVersionProvider#v660}.
     * Otherwise we sleep for {@link ConstantValues#migrationJobWaitForClusterSleepIntervalMillis} until upgrade it made
     * (Verified at {@link MigrationJobBase#onExecute(JobExecutionContext)})
     *
     * - Next check is made to test whether this migration has already previously run and finished
     * (determined by {@link InternalBuildService#verifyBuildInfoState()} - if yes we break.
     *
     * Otherwise we query for the number of records in build_jsons table and start the migration.
     */
    @Override
    protected boolean init() {
        if (buildService().verifyBuildInfoState()) {
            //Previous job has already marked completion, no need to check anything.
            migrationLog.info("Build info migration already set by previous run of this job, aborting.");
            return false;
        }
        if (!refreshCounter()) {
            migrationLog.info("No jsons found in db, or table doesn't exist - migration will not run, attempting finalization.");
            finalizeMigrationIfNeeded();
            return false;
        }
        if (!jobEnabled()) {
            log.info("Build Info migration job (for existing builds) is disabled and will not run, there are " +
                    "{} Build Info records in the database that should be migrated.", repoMissingBuildJsonCount);
            return false;
        }
        String msg = "Build info records are missing in Build info repo - starting calculation job.";
        migrationLog.info(msg);
        log.info(msg);
        return true;
    }

    private boolean refreshCounter() {
        try {
            //ok to get these via spring here, by definition this job starts only when the db conversion finished.
            repoMissingBuildJsonCount = buildsDao().getNumberOfBuildJsons();
        } catch (SQLException e) {
            migrationLog.error("Can't get a count of .json entries in build_jsons table", e);
            return false;
        }
        return repoMissingBuildJsonCount > 0;
    }

    /**
     * @return A list of build ids.
     * Query is limited to whatever is in the const.
     */
    private List<Long> getBuildJsonEntriesIds() {
        List<Long> buildIds = queryDbForNextBatch();
        if (buildIds == null) {
            // Means the db query failed and we wish to stop
            return null;
        }
        markLastNodeId(buildIds);
        return buildIds;
    }

    /**
     * Gets the next {@link #queryLimit} results starting from the {@link #lastEntityId} returned by the last query.
     * This way we make sure to get all missing jsons while not getting duplicate results for jsons that failed
     * to update for any reason.
     */
    private List<Long> queryDbForNextBatch() {
        try {
            //ok to get these via spring here, by definition this job starts only when the db conversion finished.
            return buildsDao().getBuildIdsStartingAt(lastEntityId, queryLimit);
        } catch (SQLException e) {
            migrationLog.error("Can't get a list of entries in build_jsons table, migration will not run: ", e);
            return null;
        }
    }

    /**
     * {@link #lastEntityId} is maintained to offset each db query so that we don't reiterate on already-failed nodes.
     */
    private void markLastNodeId(List<Long> buildIds) {
        long lastId = buildIds.stream()
                .max(Comparator.naturalOrder())
                .orElse(-1L);
        markLastNodeWithEndlessLoopPrevention(lastId);
        migrationLog.debug("Marked last build_id = {}", lastEntityId);
    }

    private void markLastNodeWithEndlessLoopPrevention(long lastId) {
        if (lastId == lastEntityId && lastEntityId == endlessLoopPreventionEntryId) {
            //oops!
            String err = jobName() + ": Fatal error: endless loop detection triggered, can't advance past build_id '" + lastId + "'";
            fatalBuildErrors.add(err);
            migrationLog.error(err);
            throw new EndlessLoopPreventionException(err);
        }
         else if (lastId == lastEntityId) {
            //Endless loop detection takes 3 turns to stop the loop, if in 3 con
            migrationLog.warn("Loop prevention mechanism detected 2 iterations over the same build_id, next identical iteration will fail.");
            endlessLoopPreventionEntryId = lastEntityId;
            lastEntityId = lastId;
        } else {
            endlessLoopPreventionEntryId = 0;
            lastEntityId = lastId;
        }
    }

    /**
     * Goes over all json files in repo.
     * Per every file --> look for the build id in DB.
     * If the build id is not in db --> delete file from repo silently (without triggering the build delete).
     */
    private void lookForRepoTrashAndDeleteIt() {
        // Query for all repo .json files and their matching build id
        FileInfoItemRow row;
        AqlLazyObjectResultStreamer<FileInfoItemRow> streamer = getRepoJsonFilesWithBuildId();
        while ((row = streamer.getRow()) != null) {
            pauseOrBreakIfNeeded();
            RepoPath repoPath = row.toFileInfo().getRepoPath();
            if (convertRepoPathToBuildId(repoPath) == 0) {
                deleteRepoBuildJsonSilently(repoPath);
            }
        }
    }

    /**
     * Query for all .json files under Build Info repo.
     * Mapped into a map of (RepoPath, BuildId)
     */
    private AqlLazyObjectResultStreamer<FileInfoItemRow> getRepoJsonFilesWithBuildId() {
        AqlApiItem query = createWithEmptyResults().filter(
                and(
                        repo().equal(buildService().getBuildInfoRepoKey()),
                        name().matches("*.json")
                )
        ).include(name(), itemId(), repo(), path());
        AqlLazyResult<AqlItem> aqlItemAqlLazyResult = aqlService().executeQueryLazy(query);
        return new AqlLazyObjectResultStreamer<>(aqlItemAqlLazyResult,
                FileInfoItemRow.class);
    }

    /**
     * @return 0L in case build id not found in DB
     */
    private long convertRepoPathToBuildId(RepoPath buildJsonPath) {
        try {
            // Get build coordinate according to layout
            Build buildFromFile = buildService().getBuildModelFromFile(buildJsonPath);
            // Get the build id in DB
            return getBuildIdFromBuildModel(buildFromFile);
        } catch (IllegalArgumentException | IllegalStateException | SQLException e) {
            migrationLog.error("Failed to find Build id for build '" + buildJsonPath + "'", e);
            handleExceptionDuringMigration(0L, e);
            return 0L;
        }
    }

    private long getBuildIdFromBuildModel(Build build) throws SQLException {
        try {
            return buildsDao().findBuildId(build.getName(), build.getNumber(), parseBuildTime(build.getStarted()));
        } catch (SQLException e) {
            migrationLog.warn("Could not get build id by name, number and started date", e);
            throw e;
        }
    }

    private void deleteRepoBuildJsonSilently(RepoPath jsonPath) {
        BasicStatusHolder statusHolder = repoService()
                .undeploy(jsonPath, true, new DeleteContext(jsonPath).avoidBuildDeleteInterceptor());
        if (statusHolder.isError()) {
            migrationLog.error("Failed to delete Build from repo at '{}' {}" , jsonPath,
                    statusHolder.getLastError().getException());
            fatalBuildErrors.add(statusHolder.getLastError().getMessage());
        }
    }

    @Override
    protected void retry() {
        migrationLog
                .warn("Build Info migration job determined it's done going over all db rows but has also detected there" +
                        " are still build ids not migrated into Build Info repo - it will attempt a second pass over these values.");
        // Resetting the errors and progress bar
        fatalBuildErrors.clear();
        totalJsonsDone.getAndSet(0);
        //retry the migration loop one more time to redo operations for non-fatal errors
        migrationLoop();
        additionalSteps();
        waitForSubmittedTasks();
        if (hasErrors()) {
            finishedWithErrors();
        } else {
            markCompletion();
        }
    }

    /**
     * This part is called only if the job ran and finished without errors, it triggers the
     * {@link #V660_BUILD_INFO_DB_FINALIZE_CONVERSION} db conversion which drops the foreign key 'build_jsons_builds_fk'
     * from the build_jsons table.
     *
     * This state is what we check on startup to know an instance is build-info-ready.
     * In new installations the db is created without the build_jsons table.
     *
     * We also set the config to be saved in order to trigger propagation.
     * That way other nodes will be notified of the new state
     */
    @Override
    protected void markCompletion() {
        migrationLog.info("Build Info migration job has finished, going into finalization step.");
        if (finalizeMigrationIfNeeded()) {
            String finalizeMsg =
                    "Build Info migration job has finished successfully. {} build json entries were converted to artifacts" +
                            ((totalJsonsDone.get() > repoMissingBuildJsonCount) ? " (including retries)" : ".");
            migrationLog.info(finalizeMsg, totalJsonsDone.get());
            log.info(finalizeMsg, totalJsonsDone.get());
        } else {
            String err = "Failed to finalize the Build Info migration process, re-running this job (by restarting this node) may be required.";
            log.error(err);
            migrationLog.error(err);
        }
    }

    @Override
    protected boolean stateOk() {
        return !hasErrors();
    }

    @Override
    protected boolean hasErrors() {
        return CollectionUtils.isNotEmpty(fatalBuildErrors);
    }

    @Override
    protected void finishedWithErrors() {
        int fatalNodeErrors = this.fatalBuildErrors.size();
        if (fatalNodeErrors == 0) {
            //some unspecified error... user needs to restart the migration or call us...
            String unspecified =
                    "The Build Info migration job has finished with unspecified errors which might be deduced " +
                            "from this run's log. To mitigate this it may be enough to re-run the migration (by restarting this node)," +
                            " if a second run fails please contact JFrog support with the migration log.";
            log.error(unspecified);
            migrationLog.error(unspecified);
            return;
        }
        //sizes here are an estimate only, meant more to make the user feel good with tasty numbers rather then anything else.
        String errMsg =
                "The Build Info migration job has finished with {} errors (out of {} total Build Info entries that " +
                        "needed conversion), it will continue to run on each restart until you resolve the errors.\n";
        log.error(errMsg, fatalNodeErrors, repoMissingBuildJsonCount);
        migrationLog.error(errMsg, fatalNodeErrors, repoMissingBuildJsonCount);
        StringBuilder listBuilder = new StringBuilder();
        listBuilder.append("Build Ids that failed conversion:\n-----------------------------\n");
        this.fatalBuildErrors
                .forEach(error -> listBuilder.append(error).append("\n"));
        String list = listBuilder.toString();
        try {
            File dumpFile = new File(ArtifactoryHome.get().getEtcDir(),
                    "buildIfo_migration_badBuildIds-" + System.currentTimeMillis() + ".list");
            FileUtils.write(dumpFile, list);
            String logMsg = "The failed builds ids list has been written to {} for convenience.";
            log.error(logMsg, dumpFile.getAbsolutePath());
            migrationLog.error(logMsg, dumpFile.getAbsolutePath());
        } catch (IOException e) {
            String err = "Failed to write failed builds ids list to etc folder: ";
            log.warn("{} {}", err, e.getMessage());
            migrationLog.warn(err, e);
        }
        migrationLog.error("Failures: {}", list);
    }

    /**
     * Tests {@link BuildService#isBuildInfoReady()} (which verifies if the {@link #V660_BUILD_INFO_DB_FINALIZE_CONVERSION}
     * was already run), and runs the required conversion only if it is needed (if the table is still present and the
     * build_jsons_builds_fk foreign key still exists).
     */
    private boolean finalizeMigrationIfNeeded() {
        boolean ok;
        try {
            if (buildService().verifyBuildInfoState()) {
                migrationLog.info("Skipping Build Info migration finalization ('v660' db conversion) - database is already in correct state.");
                ok = true;
            } else {
                executeDbConversion(V660_BUILD_INFO_DB_FINALIZE_CONVERSION);
                triggerConfigReload();
                ok = buildService().verifyBuildInfoState();
            }
        } catch (Exception e) {
            String err = "Failed to finalize Build Info migration: ";
            log.error("{} {}", err, e.getMessage());
            migrationLog.error(err, e);
            ok = false;
        }
        return ok;
    }

    // ----  This is the part that the build service work queue method runs (BuildInfoMigrationJobDelegate) --- \\

    @Override
    public void incrementTotalDone() {
        totalJsonsDone.incrementAndGet();
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
     * Tries to retrieve the Build Info JSON matching this {@param buildId} from the Build Info repository.
     * if it doesn't exist, the Build info json value is retrieved from db,
     * then written into the proper coordinates in the repo.
     */
    @Override
    public void migrateBuildJsonToRepo(long buildId) throws BuildInfoCalculationFatalException {
        BuildEntity buildInfo = getBuildInfoFromFromDb(buildId);
        if (buildInfo == null) {
            migrationLog.info("Json file for Build ID '{}' doesn't exist in db, nothing to migrate", buildId);
            return;
        }
        RepoPath buildJsonPathInRepo = getBuildJsonPathInRepo(buildInfo.getBuildName(), buildInfo.getBuildNumber(),
                formatBuildTime(buildInfo.getBuildDate()), buildService().getBuildInfoRepoKey());
        // Validate repo json existence
        if (repositoryService().exists(buildJsonPathInRepo)) {
            migrationLog.info("Json file for build ID '{}' is already in repo at path '{}', nothing to migrate", buildId, buildJsonPathInRepo);
            return;
        }
        // Get build json
        String build = getBuildJsonFromFromDb(buildId);
        if (build == null) {
            migrationLog.info("Build Info '{}' json record doesn't exist in db, nothing to migrate", buildId);
            return;
        }
        build = ConstantValues.buildInfoMigrationFixProperties.getBoolean() ?
                BuildMigrationUtils.fixBuildProperties(build, buildInfo, migrationLog) :
                build;
        // Create json file in a new shiny repo (interceptor will be triggered but ignored as the build exists in db)
        createBuildJsonInRepo(build, buildJsonPathInRepo);
    }

    private void createBuildJsonInRepo(String build, RepoPath targetBuildJson) throws BuildInfoCalculationFatalException {
        migrationLog.info("Creating build json at final location '{}'", targetBuildJson);
        try (StringInputStream stringInputStream = new StringInputStream(build)) {
            repoService().saveFileInternal(targetBuildJson, stringInputStream);
        } catch (RepoRejectException | IOException | StorageException e) {
            String msg = "Failed to save Build Info json at '" + targetBuildJson + "'";
            migrationLog.error(msg, e);
            throw new BuildInfoCalculationFatalException(msg, e);
        }
    }

    /**
     * Retrieves Build Info matching {@param buildId}.
     */
    private BuildEntity getBuildInfoFromFromDb(long buildId) throws BuildInfoCalculationFatalException {
        migrationLog.debug("Querying for Build Info basic details of build '{}'", buildId);
            try {
                return buildsDao().getBuild(buildId);
            } catch (SQLException e) {
                String msg = "Failed to read Build Info from database for build '" + buildId + "'";
                migrationLog.error(msg, e);
                throw new BuildInfoCalculationFatalException(msg, e);
            }
    }

    @Override
    public void handleExceptionDuringMigration(long buildId, Exception e) {
        String message = buildId == 0L ? e.getMessage() : "id: " + buildId + " error: " + e.getMessage();
        fatalBuildErrors.add(message);
    }

    /**
     * Retrieves Build Info JSON matching {@param buildId}.
     *
     * NOTE: the query must be into {@link String}, as parsing it into {@link Build} will mix the order of entities,
     * which can result in build interceptor trying to re-add the build into the DB.
     */
    private String getBuildJsonFromFromDb(long buildId) throws BuildInfoCalculationFatalException {
        migrationLog.debug("Querying for Build Info json build '{}'", buildId);
        try {
            return buildsDao().getJsonBuild(buildId, String.class);
        } catch (SQLException e) {
            String msg = "Failed to read Build json from database for build '" + buildId + "'";
            migrationLog.error(msg, e);
            throw new BuildInfoCalculationFatalException(msg, e);
        }
    }

    private RepositoryService repositoryService() {
        return ContextHelper.get().beanForType(RepositoryService.class);
    }

    private InternalBuildService buildService() {
        return ContextHelper.get().beanForType(InternalBuildService.class);
    }

    private BuildsDao buildsDao() {
        return ContextHelper.get().beanForType(BuildsDao.class);
    }
}