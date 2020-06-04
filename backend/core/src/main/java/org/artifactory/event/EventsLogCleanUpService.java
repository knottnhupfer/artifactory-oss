package org.artifactory.event;

import org.artifactory.api.context.ContextHelper;
import org.artifactory.common.ConstantValues;
import org.artifactory.schedule.*;
import org.artifactory.schedule.quartz.QuartzCommand;
import org.artifactory.spring.ContextCreationListener;
import org.artifactory.storage.db.event.entity.NodeEventCursorType;
import org.artifactory.storage.db.event.model.NodeEventCursor;
import org.artifactory.storage.db.event.service.NodeEventCursorService;
import org.artifactory.storage.event.EventsService;
import org.joda.time.DateTime;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.concurrent.TimeUnit.HOURS;

@Service
public class EventsLogCleanUpService implements ContextCreationListener {

    private static final Logger log = LoggerFactory.getLogger(EventsLogCleanUpService.class);
    public static final String DELETE_MARKER = "DELETE_MARKER";

    private final TaskService taskService;

    private final EventsService eventsService;

    private final NodeEventCursorService nodeEventCursorService;

    @Autowired
    public EventsLogCleanUpService(TaskService taskService, EventsService eventsService, NodeEventCursorService nodeEventCursorService) {
        this.taskService = taskService;
        this.eventsService = eventsService;
        this.nodeEventCursorService = nodeEventCursorService;
    }

    @Override
    public void onContextCreated() {
        TaskBase repeatingTask = TaskUtils.createRepeatingTask(EventsLogCleanUpTask.class,
                HOURS.toMillis(ConstantValues.eventsLogCleanupTaskPeriodHours.getLong()),
                10000,
                "Cleanup old events from events log");
        taskService.startTask(repeatingTask, false);
    }

    public void cleanup()  {
        log.info("Starting cleanup of old events from event log");
        if (eventsService.getEventsCount() == 0) {
            log.info("No events to cleanup in events log");
            return;
        }
        long eventsDeleteInterval = HOURS.toMillis(ConstantValues.eventsCleanupIntervalHours.getLong());
        final long maxBoundary = getMaxBoundary();
        long deleteFrom = eventsService.getFirstEventTimestamp();
        if (deleteFrom == 0) {
            log.warn("First event is in time 0");
            return;
        }
        log.debug("Delete from {} to {}", deleteFrom, maxBoundary);
        while (deleteFrom < maxBoundary) {
            final long deleteTo = min(deleteFrom + eventsDeleteInterval, maxBoundary);
            log.trace("Deleting events from {} to {}", deleteFrom, deleteTo);
            eventsService.deleteRange(deleteFrom, deleteTo);
            nodeEventCursorService.updateOrInsertCursor(new NodeEventCursor(DELETE_MARKER, deleteTo, NodeEventCursorType.DELETE_MARKER));
            deleteFrom = deleteTo;
            sleep();
        }
        log.info("Cleanup of old events from event log finished");
    }

    private long getMaxBoundary() {
        long maxBoundaryFromConfig = new DateTime()
                .minusDays(ConstantValues.ageOfEventsLogEntriesToDiscardDays.getInt()).getMillis();
        long lastTimestampFromMarker = nodeEventCursorService.oldestTimestampForEventLogOperator();
        return max(maxBoundaryFromConfig, lastTimestampFromMarker);
    }

    private void sleep() {
        try {
            Thread.sleep(ConstantValues.eventsCleanupIterationSleepTimeMillis.getLong());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EventsCleanupException("Error while cleaning up events log", e);
        }
    }

    @JobCommand(singleton = true,
            schedulerUser = TaskUser.SYSTEM,
            manualUser = TaskUser.SYSTEM,
            description = "Cleanup old events from events log")
    public static class EventsLogCleanUpTask extends QuartzCommand {

        @Override
        protected void onExecute(JobExecutionContext callbackContext) throws JobExecutionException {
            ContextHelper.get().beanForType(EventsLogCleanUpService.class).cleanup();
        }
    }
}
