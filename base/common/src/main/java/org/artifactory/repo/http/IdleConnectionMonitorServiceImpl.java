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

package org.artifactory.repo.http;

import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.artifactory.common.ConstantValues;
import org.jfrog.client.http.CloseableObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.annotation.PreDestroy;

/**
 * Service providing idle connections monitoring
 * for {@link PoolingHttpClientConnectionManager}
 *
 * @author Michael Pasternak
 */
@Lazy(true)
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Service
public class IdleConnectionMonitorServiceImpl implements IdleConnectionMonitorService, CloseableObserver {

    private static final Logger log = LoggerFactory.getLogger(IdleConnectionMonitorServiceImpl.class);
    private static int MONITOR_INTERVAL;

    private IdleConnectionMonitorThread idleConnectionMonitorThread;

    private ConnectionManagersHolder remoteRepoHttpConnMgrs;

    @Autowired
    public IdleConnectionMonitorServiceImpl(ConnectionManagersHolder remoteRepoHttpConnMgrs) {
        MONITOR_INTERVAL = ConstantValues.idleConnectionMonitorInterval.getInt() * 1000;
        this.remoteRepoHttpConnMgrs = remoteRepoHttpConnMgrs;
        if (!ConstantValues.disableIdleConnectionMonitoring.getBoolean() &&
                (idleConnectionMonitorThread == null || !idleConnectionMonitorThread.isAlive())) {
            createThread();
        }
    }

    /**
     * Creates thread
     */
    private void createThread() {
        if (!ConstantValues.disableIdleConnectionMonitoring.getBoolean()) {
            idleConnectionMonitorThread = new IdleConnectionMonitorThread(remoteRepoHttpConnMgrs);
            idleConnectionMonitorThread.setName("Idle Connection Monitor");
            idleConnectionMonitorThread.setDaemon(true);
            idleConnectionMonitorThread.start();
        }
    }

    /**
     * @return {@link Thread.State}
     */
    @Nullable
    @Override
    public Thread.State getStatus() {
        return idleConnectionMonitorThread != null ?
                idleConnectionMonitorThread.getState() : null;
    }

    /**
     * Stops idleConnection monitoring
     */
    @Override
    @PreDestroy
    public final void stop() {
        log.debug("Stopping IdleConnectionMonitorService");
        if (idleConnectionMonitorThread != null) {
            idleConnectionMonitorThread.shutdown();
        }
    }

    @Override
    public long getManagerSize() {
        return remoteRepoHttpConnMgrs.size();
    }

    /**
     * Adds {@link org.apache.http.impl.conn.PoolingHttpClientConnectionManager} to monitor
     *
     * @param key the owner of connectionManager
     * @param connectionManager {@link PoolingHttpClientConnectionManager}
     */
    @Override
    public final void add(String key, PoolingHttpClientConnectionManager connectionManager) {
        if (key != null && connectionManager != null) {
            log.debug("Performing add request for params owner: {}, connectionManager: {}",
                    key, connectionManager);
            remoteRepoHttpConnMgrs.put(key, connectionManager);
        } else {
            log.debug("Ignoring add request for params owner: {}, connectionManager: {}",
                    key, connectionManager);
        }
    }

    /**
     * Removes monitored {@link PoolingHttpClientConnectionManager}
     *
     * @param key the object that owns this PoolingHttpClientConnectionManager
     */
    @Override
    public final void remove(String key) {
        if (key != null) {
            log.debug("Performing remove request for owner: {}", key);
            remoteRepoHttpConnMgrs.remove(key);
        } else {
            log.debug("Ignoring remove request for undefined owner");
        }
    }

    /**
     * Invoked by observed objects on close() event {@see CloseableObserver}
     *
     * @param key the owner of observed event
     */
    @Override
    public void onObservedClose(String key) {
        remove(key);
    }

    /**
     * thread to monitor expired and idle connection , if found clear it and return it back to pool
     */
    private static class IdleConnectionMonitorThread extends Thread {

        private final ConnectionManagersHolder connMgrs;
        private volatile boolean shutdown;

        public IdleConnectionMonitorThread(ConnectionManagersHolder connMgrs) {
            super();
            this.connMgrs = connMgrs;
        }

        @Override
        public void run() {
            try {
                log.debug("Starting Idle Connection Monitor Thread ");
                synchronized (this) {
                    while (!shutdown) {
                        wait(MONITOR_INTERVAL);
                        if (connMgrs.size() > 0) {
                            for (PoolingHttpClientConnectionManager connPollMgr : connMgrs.values()) {
                                if (connPollMgr != null) {
                                    log.debug("Cleaning idle connections for ConnectionManager: {}", connPollMgr);
                                    connPollMgr.closeExpiredConnections();
                                }
                            }
                        }
                    }
                }
            } catch (InterruptedException ex) {
                log.debug("Terminating Idle Connection Monitor Thread ");
            }
        }

        public void shutdown() {
            log.debug("Shutdown Idle Connection Monitor Thread ");
            shutdown = true;
            synchronized (this) {
                notifyAll();
            }
        }
    }
}
