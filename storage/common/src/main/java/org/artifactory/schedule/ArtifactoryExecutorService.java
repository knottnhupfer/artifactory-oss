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

import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.concurrent.ArtifactoryRunnable;
import org.artifactory.security.AuthenticationHelper;
import org.springframework.security.core.Authentication;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Noam Shemesh
 */
public class ArtifactoryExecutorService extends AbstractExecutorService {
    private final ExecutorService threadPool;
    private Authentication authentication;
    private ArtifactoryContext context;

    public ArtifactoryExecutorService() {
        this(Executors.newCachedThreadPool());
    }

    public ArtifactoryExecutorService(ExecutorService executorService) {
        this(AuthenticationHelper.getAuthentication(), ContextHelper.get(), executorService);
    }

    public ArtifactoryExecutorService(Authentication authentication, ArtifactoryContext context, ExecutorService executorService) {
        this.authentication = authentication;
        this.context = context;
        this.threadPool = executorService;
    }

    @Override
    public void shutdown() {
        threadPool.shutdown();
    }

    @Override
    @Nonnull
    public List<Runnable> shutdownNow() {
        return threadPool.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return threadPool.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return threadPool.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, @Nonnull TimeUnit unit) throws InterruptedException {
        return threadPool.awaitTermination(timeout, unit);
    }

    @Override
    public void execute(@Nonnull Runnable command) {
        threadPool.execute(new ArtifactoryRunnable(command, context, authentication));
    }

    public ArtifactoryExecutorService copyOfWithCurrentThreadLocals() {
        return new ArtifactoryExecutorService(this.threadPool);
    }
}
