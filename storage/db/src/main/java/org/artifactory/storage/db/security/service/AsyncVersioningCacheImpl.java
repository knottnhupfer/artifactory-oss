package org.artifactory.storage.db.security.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/**
 * Similar to {@link VersioningCacheImpl}, but loading the cache asynchronously after the first time.
 *
 * @author Shay Bagants
 * @author Gidi Shabat
 */
public class AsyncVersioningCacheImpl<T extends BasicCacheModel> extends VersioningCacheImpl<T> {
    private static final Logger log = LoggerFactory.getLogger(AsyncVersioningCacheImpl.class);

    private boolean active = true;
    private final Object lock = new Object();
    private String name;

    public AsyncVersioningCacheImpl(long timeout, Callable<T> cacheLoader, long threadWaitingTime,
            long threadWaitingOnErrorMillis, String name) {
        super(timeout, cacheLoader);
        this.name = name;
        log.debug("Constructing async version cache for {}", name);
        Thread thread = new Thread(() -> doJob(threadWaitingTime, threadWaitingOnErrorMillis));
        thread.start();
    }

    @Override
    public T get() {
        if (hasNewVersion()) {
            if (cache != null) {
                synchronized (lock) {
                    try {
                        log.debug("Notify cache population thread to trigger cache update for {}.", name);
                        lock.notifyAll();
                    } catch (Exception e) {
                        log.warn("Error occurred while waking up cache population thread. {}", e.getMessage());
                        log.debug("Error occurred while waking up cache population thread. ", e);
                    }
                }
            } else {
                log.debug("Loading cache synchronously at the first time.");
                super.get();
            }
        }
        return cache;
    }

    @Override
    public int promoteVersion() {
        synchronized (lock) {
            lock.notifyAll();
        }
        return super.promoteVersion();
    }

    @Override
    public void destroy() {
        active = false;
    }

    private void doJob(long waitOnLockMillis, long waitOnLockAfterErrorMillis) {
        while (active) {
            try {
                // first cache population is done synchronously
                if (cache != null) {
                    while (hasNewVersion()) {
                        log.debug("Has new version. Updating {} cache.", name);
                        super.get();
                    }
                }
                lockAndWait(waitOnLockMillis);
            } catch (Exception e) {
                log.error("Error occurred on cache population thread. {}", e.getMessage());
                log.debug("Error occurred on cache population thread.", e);
                lockAndWait(waitOnLockAfterErrorMillis);
            }
        }
    }

    private void lockAndWait(long maxMillisToWait) {
        synchronized (lock) {
            try {
                log.debug("waiting on lock for {}", name);
                lock.wait(maxMillisToWait);
            } catch (Exception e) {
                log.warn("Error occurred while waiting on lock. {}", e.getMessage());
                log.debug("Error occurred while waiting on lock.", e);
            }
        }
    }
}
