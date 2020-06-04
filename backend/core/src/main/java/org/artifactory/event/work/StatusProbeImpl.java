package org.artifactory.event.work;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import static java.lang.Thread.currentThread;
import static java.lang.Thread.sleep;

/**
 * @author Uriah Levy
 */
public class StatusProbeImpl implements StatusProbe {
    private static final Logger log = LoggerFactory.getLogger(StatusProbeImpl.class);

    private long intervalTimeMillis = TimeUnit.MINUTES.toMillis(2);
    private int maxAttempts = 11_000;
    private ReentrantLock probingLock = new ReentrantLock();

    @Override
    public void startProbing(ExecutorService executor, Supplier<Boolean> probeFunction, Runnable onSuccess) {
        executor.submit(() -> locallyExclusive(() -> {
            boolean probeSuccess = false;
            int currentAttempt = 0;
            while (!probeSuccess && currentAttempt < maxAttempts) { // 2m * 11_000 = ~one week
                try {
                    probeSuccess = probeFunction.get();
                    currentAttempt++;
                    if (!probeSuccess) {
                        pause();
                    }
                } catch (Exception e) {
                    currentAttempt++;
                    pause();
                }
            }
            // Success
            onSuccess.run();
        }));
    }

    @Override
    public boolean isProbing() {
        return probingLock.isLocked();
    }

    private void pause() {
        try {
            log.debug("Probing function indicated failure. Waiting for {} seconds before retrying...",
                    intervalTimeMillis);
            sleep(intervalTimeMillis);
        } catch (InterruptedException e) {
            log.warn("Probing function interrupted", e);
            currentThread().interrupt();
            throw new IllegalStateException("Probing function interrupted.", e);
        }
    }

    private void locallyExclusive(Runnable function) {
        try {
            boolean lockAcquired = probingLock.tryLock();
            if (lockAcquired) {
                log.debug("Local probing exclusive lock acquired");
                function.run();
            }
        } finally {
            if (probingLock.isHeldByCurrentThread()) {
                probingLock.unlock();
                log.debug("Local probing exclusive lock released");
            }
        }
    }
}
