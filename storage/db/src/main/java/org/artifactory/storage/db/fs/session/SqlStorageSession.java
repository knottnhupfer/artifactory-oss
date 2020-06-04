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

package org.artifactory.storage.db.fs.session;

import com.google.common.collect.Maps;
import org.artifactory.repo.RepoPath;
import org.artifactory.storage.fs.lock.FsItemLockEntry;
import org.artifactory.storage.fs.lock.LockEntryId;
import org.artifactory.storage.fs.lock.SessionLockEntry;
import org.artifactory.storage.fs.session.StorageSession;
import org.artifactory.storage.tx.SessionResource;
import org.artifactory.storage.tx.SessionResourceManager;
import org.artifactory.storage.tx.SessionResourceManagerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a thread bound storage session in a context of a transaction.
 *
 * @author Yossi Shaul
 */
public class SqlStorageSession implements StorageSession {
    private static final Logger log = LoggerFactory.getLogger(SqlStorageSession.class);

    private final UUID sessionId = UUID.randomUUID();
    private final Map<RepoPath, SessionLockEntry> locks = Maps.newLinkedHashMap();
    private SessionResourceManager sessionResourceManager = new SessionResourceManagerImpl();

    @Override
    public UUID getSessionId() {
        return sessionId;
    }

    @Override
    public void save() {
        log.trace("Save called on session {}", sessionId);
        if (locks.size() == 0) {
            log.trace("Save called on session {} with no locked items", sessionId);
            return;
        }

        log.debug("Save called on session {} with {} locked items", sessionId, locks.size());

        Iterator<Map.Entry<RepoPath, SessionLockEntry>> locksIter = locks.entrySet().iterator();
        while (locksIter.hasNext()) {
            SessionLockEntry lockEntry = locksIter.next().getValue();
            if (lockEntry.isWriteLockedByMe()) {
                lockEntry.save();
                if (lockEntry.isDeleted()) {
                    // deleted items are removed immediately from the session to support simpler override during move/copy
                    lockEntry.unlock();
                    locksIter.remove();
                }
            }
        }
    }

    @Override
    @Nonnull
    public FsItemLockEntry writeLock(LockEntryId lockEntryId) {
        log.trace("Acquiring write lock on {} in session {}", lockEntryId, sessionId);
        SessionLockEntry sessionLockEntry = getOrCreateSessionLockEntry(lockEntryId);
        sessionLockEntry.acquireWriteLock();
        return sessionLockEntry;
    }

    @Override
    public boolean removeLockEntry(RepoPath repoPath) {
        SessionLockEntry lockEntry = locks.remove(repoPath);
        if (lockEntry == null) {
            log.debug("Removing lock entry {} in {} but not locked by me!", repoPath, sessionId);
            return false;
        } else {
            log.trace("Removed lock entry {} in {}", repoPath, sessionId);
            lockEntry.unlock();
            return true;
        }
    }

    @Override
    @Nullable
    public SessionLockEntry getLockEntry(RepoPath repoPath) {
        return locks.get(repoPath);
    }

    private SessionLockEntry getOrCreateSessionLockEntry(LockEntryId lockEntryId) {
        RepoPath repoPath = lockEntryId.getRepoPath();
        SessionLockEntry sessionLockEntry = getLockEntry(repoPath);
        if (sessionLockEntry == null) {
            log.trace("Creating new SLE for {} in {}", repoPath, sessionId);
            sessionLockEntry = new SessionLockEntry(lockEntryId);
            locks.put(repoPath, sessionLockEntry);
        } else {
            log.trace("Reusing existing SLE for {} in {}", repoPath, sessionId);
        }
        return sessionLockEntry;
    }

    @Override
    public <T extends SessionResource> T getOrCreateResource(Class<T> resourceClass) {
        return getSessionResourceManager().getOrCreateResource(resourceClass);
    }

    @Override
    public void beforeCommit() {
        log.trace("before commit called on session {}", sessionId);
        save();
        sessionResourceManager.beforeCommit();
    }

    @Override
    public void afterCompletion(boolean success) {
        log.trace("After completion called on session {}", sessionId);
        sessionResourceManager.afterCompletion(success);
    }

    @Override
    public void releaseResources() {
        log.trace("Release resources called on session {}", sessionId);
        try {
            Collection<SessionLockEntry> lockEntries = locks.values();
            for (SessionLockEntry lockEntry : lockEntries) {
                lockEntry.unlock();
            }
        } finally {
            locks.clear();
        }
    }

    SessionResourceManager getSessionResourceManager() {
        return sessionResourceManager;
    }
}
