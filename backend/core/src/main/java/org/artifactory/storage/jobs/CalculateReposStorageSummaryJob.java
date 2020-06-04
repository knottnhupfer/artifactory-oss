package org.artifactory.storage.jobs;

import org.apache.commons.lang.StringUtils;
import org.artifactory.common.ConstantValues;
import org.artifactory.cron.CronUtils;
import org.artifactory.metrics.TimeRandomizer;
import org.artifactory.repo.service.ImportJob;
import org.artifactory.schedule.JobCommand;
import org.artifactory.schedule.StopCommand;
import org.artifactory.schedule.StopStrategy;
import org.artifactory.schedule.TaskUser;
import org.artifactory.schedule.quartz.QuartzCommand;
import org.artifactory.spring.InternalArtifactoryContext;
import org.artifactory.spring.InternalContextHelper;
import org.artifactory.storage.service.InternalStorageService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cron job that calculates and updates the repositories storage summary cache.
 *
 * @author Inbar Tal
 * @author Rotem Kfir
 */
@JobCommand(singleton = true, runOnlyOnPrimary = true, schedulerUser = TaskUser.SYSTEM, manualUser = TaskUser.SYSTEM,
        description = "Calculate Repositories Storage Summary Job",
        commandsToStop = {@StopCommand(command = ImportJob.class, strategy = StopStrategy.IMPOSSIBLE)})
public class CalculateReposStorageSummaryJob extends QuartzCommand {
    private static final Logger log = LoggerFactory.getLogger(CalculateReposStorageSummaryJob.class);

    @Override
    protected void onExecute(JobExecutionContext callbackContext) throws JobExecutionException {
        InternalArtifactoryContext context = InternalContextHelper.get();
        if (!context.isReady()) {
            log.debug("Skipping execution of '{}', server is not ready yet", CalculateReposStorageSummaryJob.class.getName());
            return;
        }
        InternalStorageService storageService = context.beanForType(InternalStorageService.class);
        storageService.calculateStorageSummary();
    }

    /**
     * To reduce load on AOLs we set random execution time
     *
     * @return Quartz scheduling expression
     */
    public static String buildRandomQuartzExp() {
        // Adding an option to override CRON expression
        String predefinedExp = ConstantValues.calculateStorageSummaryJobCron.getString();
        if (!StringUtils.isBlank(predefinedExp) && CronUtils.isValid(predefinedExp)) {
            return predefinedExp;
        }
        return String.format("0 %d * ? * *", TimeRandomizer.randomMinute());
    }
}
