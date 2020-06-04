package org.artifactory.event.work;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.artifactory.event.priority.service.NodeEventPriorityService;
import org.artifactory.event.priority.service.model.PrioritizedNodeEvent;
import org.artifactory.event.provider.EventProvider;
import org.artifactory.storage.event.NodeEvent;
import org.jfrog.storage.common.ConflictGuard;
import org.jfrog.storage.common.ConflictsGuard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import static org.artifactory.event.priority.service.NodeEventPriorityServiceImpl.MAX_RETRIES;

class EventOperatorDispatcher extends Thread {
    private static final Logger log = LoggerFactory.getLogger(EventOperatorDispatcher.class);
    private NodeEventPriorityService nodeEventPriorityService;
    private EventProvider<? extends NodeEvent> eventProvider;
    private NodeEventOperator nodeEventOperator;
    private boolean active = true;
    private ConflictsGuard<String> conflictsGuard;
    private int maxPermits;
    private int maxBatchErrors;
    private final Semaphore semaphore;
    private final Object workMonitor = new Object();
    private AtomicInteger batchErrors = new AtomicInteger();
    private List<PrioritizedNodeEvent> currentEventBatch = new ArrayList<>();
    private ExecutorService executor;
    private final ReentrantLock errorThresholdLock = new ReentrantLock();

    EventOperatorDispatcher(NodeEventPriorityService nodeEventPriorityService,
                            EventProvider<? extends NodeEvent> eventProvider,
                            NodeEventOperator operator,
                            ConflictsGuard<String> conflictsGuard, int numOfThreads) {
        this.nodeEventPriorityService = nodeEventPriorityService;
        this.eventProvider = eventProvider;
        this.nodeEventOperator = operator;
        this.conflictsGuard = conflictsGuard;
        this.maxPermits = numOfThreads;
        this.maxBatchErrors = numOfThreads * (MAX_RETRIES - 1);
        this.semaphore = new Semaphore(numOfThreads);
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat(operator.getOperatorId() + "-sweeper-%s").setDaemon(true).build();
        executor = new ThreadPoolExecutor(numOfThreads, numOfThreads, 5L, TimeUnit.MINUTES, new LinkedBlockingQueue<>(),
                threadFactory);
    }

    @Override
    public void run() {
        try {
            dispatchLoop();
        } finally {
            stopSweepers();
        }
    }

    void batchError() {
        if (batchErrors.incrementAndGet() >= maxBatchErrors) {
            locallyExclusive(this::notifyOperatorOnErrorThreshold);
        }
    }


    void submitSweeper(List<PrioritizedNodeEvent> currentEventBatch) {
        log.debug("Submitting event sweeper with {} events", currentEventBatch.size());
        executor.submit(
                new NodeEventSweeper(nodeEventPriorityService, nodeEventOperator, this, currentEventBatch));
    }


    void runExclusive(String lockKey, Runnable task) {
        ConflictGuard lock = conflictsGuard.getLock(lockKey);
        boolean lockAcquired = false;
        try {
            lockAcquired = lock.tryToLock(0, TimeUnit.SECONDS);
            if (lockAcquired) {
                log.debug("Lock acquired on key {}", lockKey);
                task.run();
                return;
            }
            log.debug("key '{}' is already locked.", lockKey);
        } catch (InterruptedException e) {
            log.error("Node Event Manager dispatcher or worker thread interrupted.", e);
            Thread.currentThread().interrupt();
        } finally {
            releaseLock(lockKey, lock, lockAcquired);
        }
    }

    /**
     * Called by the sweepers once (and only once), when a sweeper finishes working.
     */
    void sweeperDone() {
        synchronized (workMonitor) {
            workMonitor.notifyAll();
        }
        semaphore.release();
    }

    void stopDispatcher() {
        active = false;
    }

    private void notifyOperatorOnErrorThreshold() {
        log.warn("Event error threshold reached for operator '{}'. Notifying operator.",
                nodeEventOperator.getOperatorId());
        nodeEventOperator.onErrorThresholdReached();
        batchErrors.set(0);
    }

    private void locallyExclusive(Runnable function) {
        try {
            boolean lockAcquired = errorThresholdLock.tryLock();
            if (lockAcquired) {
                log.debug("Local error-threshold exclusive lock acquired");
                function.run();
            }
        } finally {
            if (errorThresholdLock.isHeldByCurrentThread()) {
                errorThresholdLock.unlock();
                log.debug("Local error-threshold exclusive lock released");
            }
        }
    }

    private void dispatchLoop() {
        while (active) {
            try {
                if (nodeEventPriorityService.isDistributed()) {
                    work();
                } else {
                    // If the priority service is not distributed, the dispatcher should operate within a lock
                    runExclusive(getLockKeyForOperation("dispatcher"), this::work);
                }
            } finally {
                pause();
            }
        }
    }

    /**
     * Creates the prioritized list of events, and dispatches work sweepers in a semi back-pressured manner.
     */
    private void work() {
        synchronized (workMonitor) {
            while (active) {
                try {
                    if (nodeEventOperator.isOperatorEnabled()) {
                        createPrioritiesIfNeeded();
                        submitSweepers();
                    }
                    // Wait for workers to signal work done
                    workMonitor.wait(getInterval());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    log.warn("Caught exception during the '{}' dispatch loop. The dispatcher will continue execution. message : {}",
                            nodeEventOperator.getOperatorId(), e.getMessage());
                    log.debug("", e);
                    try {
                        workMonitor.wait(getInterval());
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }

    private void pause() {
        try {
            Thread.sleep(getOperatorInterval());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Dispatcher thread for '{}' interrupted", nodeEventOperator.getOperatorId());
        }
    }

    private int getNumberOfPendingEventsFromCurrentBatch() {
        return (int) currentEventBatch.stream()
                .filter(PrioritizedNodeEvent::isPending).count();
    }

    private long getInterval() {
        if (nodeEventOperator.isMigrating()) {
            return 1; // when migration is on, sleep you shall none!
        }
        return getOperatorInterval();
    }

    private long getOperatorInterval() {
        return TimeUnit.SECONDS.toMillis(nodeEventOperator.getDispatcherIntervalSecs());
    }

    private void submitSweepers() {
        int activeWorkers = maxPermits - semaphore.availablePermits();
        if (!hasPendingEvents()) {
            // refill
            currentEventBatch = nodeEventPriorityService
                    .getCurrentPriorityBatch(nodeEventOperator.getOperatorId());
        }
        while (getNumberOfPendingEventsFromCurrentBatch() > activeWorkers && semaphore.tryAcquire()) {
            submitSweeper(currentEventBatch);
            activeWorkers = maxPermits - semaphore.availablePermits();
        }
    }

    private boolean hasPendingEvents() {
        return getNumberOfPendingEventsFromCurrentBatch() > 0;
    }

    private void createPrioritiesIfNeeded() {
        if (semaphore.availablePermits() == maxPermits) {
            try {
                if (nodeEventPriorityService.priorityListIsEmpty(nodeEventOperator.getOperatorId())) {
                    runExclusive(getLockKeyForOperation("priorities"),
                            () -> doCreatePriorities(nodeEventOperator.getOperatorId()));
                }
            } catch (Exception e) {
                log.error("Caught exception while creating the prioritized event list.", e);
            }
        }
    }

    private void doCreatePriorities(String operatorId) {
        if (nodeEventPriorityService.priorityListIsEmpty(operatorId)) {
            nodeEventPriorityService.createAndSaveEventPrioritiesIfNeeded(nodeEventOperator, eventProvider);
            batchErrors = new AtomicInteger(); // reset error count
            currentEventBatch = new ArrayList<>(); // reset current batch
        }
    }

    private void releaseLock(String lockKey, ConflictGuard lock, boolean lockAcquired) {
        try {
            if (lockAcquired) {
                lock.unlock();
                log.debug("Lock on key '{}' released in finally block", lockKey);
            }
        } catch (Exception e) {
            log.error("Unable to release lock with key {}", lockKey, e);
        }
    }

    private String getLockKeyForOperation(String lockSuffix) {
        return nodeEventOperator.getOperatorId() + "-" + lockSuffix;
    }

    private void stopSweepers() {
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
            executor.shutdownNow();
        } catch (InterruptedException e) {
            log.error("Unable to stop dispatched sweepers", e);
            Thread.currentThread().interrupt();
        }
    }
}