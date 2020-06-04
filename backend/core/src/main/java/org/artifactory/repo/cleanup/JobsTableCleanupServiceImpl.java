package org.artifactory.repo.cleanup;

import org.apache.commons.lang.StringUtils;
import org.artifactory.common.ConstantValues;
import org.artifactory.config.CentralConfigKey;
import org.artifactory.cron.CronUtils;
import org.artifactory.metrics.TimeRandomizer;
import org.artifactory.schedule.TaskBase;
import org.artifactory.schedule.TaskService;
import org.artifactory.schedule.TaskUtils;
import org.artifactory.spring.Reloadable;
import org.artifactory.storage.db.fs.service.JobsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * @author Yoaz Menda
 */
@Service
@Reloadable(beanClass = JobsTableCleanupService.class,
        initAfter = {TaskService.class},
        listenOn = CentralConfigKey.none)
public class JobsTableCleanupServiceImpl implements JobsTableCleanupService {
    private static final Logger log = LoggerFactory.getLogger(IntegrationCleanupServiceImpl.class);


    private JobsService jobsService;

    private TaskService taskService;

    @Autowired
    public JobsTableCleanupServiceImpl(JobsService jobsService, TaskService taskService) {
        this.jobsService = jobsService;
        this.taskService = taskService;
    }

    @Override
    public void clean() {
        try {
            log.info("cleaning jobs table");
            long ttlInMillis = ConstantValues.jobsTableTimeToLiveMillis.getLong();
            long deleteBefore = Instant.now().minusMillis(ttlInMillis).toEpochMilli();
            int deleted = jobsService.deleteJobsStartedBefore(deleteBefore);
            log.info("cleaned up {} old job records", deleted);
        } catch (Exception e) {
            log.error("An error occurred during jobs table cleanup", e);
        }
    }

    @Override
    public void init() {
        String cronExp = getCronExp();
        TaskBase integrationCleanupTask = TaskUtils.createCronTask(JobsTableCleanupJob.class, cronExp);
        taskService.startTask(integrationCleanupTask, false);
    }

    public static String getCronExp() {
        String predefinedExp = ConstantValues.jobsTableCleanupCron.getString();
        if (!StringUtils.isBlank(predefinedExp)) {
            if (CronUtils.isValid(predefinedExp)) {
                log.info("Using predefined cron {} for jobs table cleanup", predefinedExp);
                return predefinedExp;
            }
            log.error("Given cron expression for jobs table cleanup is not valid: {}", predefinedExp);
        }
        String cron = String.format(
                "0 %d %d ? * *",
                TimeRandomizer.randomMinute(),
                TimeRandomizer.randomHourAtNight()
        );
        log.info("Using generated cron {} for jobs table cleanup", cron);
        return cron;
    }
}
