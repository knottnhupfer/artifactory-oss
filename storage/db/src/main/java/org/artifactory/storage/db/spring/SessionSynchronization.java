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

package org.artifactory.storage.db.spring;

import org.artifactory.storage.fs.session.StorageSession;
import org.artifactory.storage.fs.session.StorageSessionHolder;
import org.jfrog.common.TimeUnitFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;

/**
 * Callback for synchronization between Artifactory {@link org.artifactory.storage.fs.session.StorageSession} and the
 * JDBC transaction.
 *
 * @author Yossi Shaul
 */
public class SessionSynchronization extends TransactionSynchronizationAdapter {
    private static final Logger log = LoggerFactory.getLogger(SessionSynchronization.class);

    private final StorageSession session;
    private final String sessionName;
    private final long startTime = System.nanoTime();

    private boolean sessionActive = true;

    public SessionSynchronization(StorageSession session, String name) {
        this.session = session;
        if (name != null) {
            sessionName = name + ":" + session.getSessionId();
        } else {
            sessionName = "default:" + session.getSessionId();
        }
        StorageSessionHolder.setSession(session);
        log.debug("Session started: '{}'", sessionName);
    }

    @Override
    public void suspend() {
        log.debug("Session suspended: '{}'", sessionName);
        if (sessionActive) {
            StorageSessionHolder.removeSession();
        }
        sessionActive = false;
    }

    @Override
    public void resume() {
        log.debug("Session resume: '{}'", sessionName);
        if (sessionActive) {
            log.warn("TX-resume when session is already active: {}", sessionName);
        }
        StorageSessionHolder.setSession(session);
        sessionActive = true;
    }

    @Override
    public void beforeCommit(boolean readOnly) {
        if (!sessionActive) {
            log.warn("Session {} is not active and set to commit", sessionName);
            return;
        }

        //Save any pending changes (no need to test for rollback at this phase)
        log.debug("Session commit: '{}'", sessionName);
        session.beforeCommit();
    }

    @Override
    public void afterCompletion(int status) {
        if (sessionActive) {
            boolean success = status == TransactionSynchronization.STATUS_COMMITTED;
            // Commit the locks/discard changes on rollback
            try {
                session.afterCompletion(success);
            } finally {
                try {
                    session.releaseResources();
                } finally {
                    StorageSessionHolder.removeSession();
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Session completed: '{}' in {}",
                    sessionName, TimeUnitFormat.getTimeString(System.nanoTime() - startTime));
        }
    }
}
