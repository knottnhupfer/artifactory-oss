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

package org.artifactory.schedule;

import org.artifactory.common.ConstantValues;
import org.artifactory.concurrent.ArtifactoryRunnable;
import org.artifactory.storage.spring.ArtifactoryStorageContext;
import org.artifactory.storage.spring.StorageContextHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.concurrent.*;

/**
 * @author Yossi Shaul
 * @author Fred Simon
 */
public class ArtifactoryConcurrentExecutor implements Executor {

    private static final Logger log = LoggerFactory.getLogger(ArtifactoryConcurrentExecutor.class);

    private final ArtifactoryStorageContext storageContext;
    private final ThreadPoolExecutor executor;

    ArtifactoryConcurrentExecutor() {
        this("art-exec-");
    }

    ArtifactoryConcurrentExecutor(String threadNamePrefix) {
        CustomizableThreadFactory threadFactory = new CustomizableThreadFactory(threadNamePrefix);
        threadFactory.setThreadPriority(Thread.NORM_PRIORITY);
        executor = new ThreadPoolExecutor(
                ConstantValues.asyncCorePoolSize.getInt(),
                ConstantValues.asyncCorePoolSize.getInt(),
                ConstantValues.asyncPoolTtlSecs.getInt(),
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(ConstantValues.asyncPoolMaxQueueSize.getInt()),
                threadFactory);
        executor.allowCoreThreadTimeOut(true);
        storageContext = StorageContextHelper.get();
    }

    @Override
    public void execute(Runnable task) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        task = new ArtifactoryRunnable(task, storageContext, authentication);
        try {
            executor.execute(task);
        } catch (RejectedExecutionException e) {
            log.warn("Task {} was rejected by scheduler: {}", task, e.getMessage());
            throw e;
        }
    }

    <T> Future<T> submit(Runnable task, T result) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        task = new ArtifactoryRunnable(task, storageContext, authentication);
        return executor.submit(task, result);
    }

    void shutdown() {
        executor.shutdown();
    }

    public int getActiveCount() {
        return executor.getActiveCount();
    }

    public long getCompletedTaskCount() {
        return executor.getCompletedTaskCount();
    }

    public int getCorePoolSize() {
        return executor.getCorePoolSize();
    }

    public int getLargestPoolSize() {
        return executor.getLargestPoolSize();
    }

    public int getMaximumPoolSize() {
        return executor.getMaximumPoolSize();
    }

    public long getTaskCount() {
        return executor.getTaskCount();
    }

    public void setCorePoolSize(int corePoolSize) {
        executor.setCorePoolSize(corePoolSize);
    }

    public void setMaximumPoolSize(int maximumPoolSize) {
        executor.setMaximumPoolSize(maximumPoolSize);
    }
}
