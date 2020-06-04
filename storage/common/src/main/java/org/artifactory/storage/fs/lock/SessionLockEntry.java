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

package org.artifactory.storage.fs.lock;

import org.artifactory.common.ConstantValues;
import org.artifactory.concurrent.LockingException;
import org.artifactory.repo.RepoPath;
import org.artifactory.storage.fs.MutableVfsItem;
import org.artifactory.storage.fs.lock.provider.JVMLockWrapper;
import org.artifactory.storage.fs.lock.provider.LockWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * A session bound lock entry for a single {@link org.artifactory.repo.RepoPath}.
 *
 * @author Yossi Shaul
 */
public class SessionLockEntry implements FsItemLockEntry {
    private static final Logger log = LoggerFactory.getLogger(SessionLockEntry.class);

    private final LockEntryId lockEntryId;
    private MutableVfsItem mutableItem;

    public SessionLockEntry(LockEntryId lockEntryId) {
        this.lockEntryId = lockEntryId;
    }

    @Override
    public MutableVfsItem getMutableFsItem() {
        return mutableItem;
    }

    @Override
    public void setWriteFsItem(MutableVfsItem mutableItem) {
        this.mutableItem = mutableItem;
    }

    @Override
    public void unlock() {
        releaseWriteLock();
    }

    @Override
    public void save() {
        log.trace("Saving lock entry {}", getRepoPath());
        if (!isWriteLockedByMe()) {
            String msg = "Cannot save item " + lockEntryId + " which not locked by me!";
            StringBuilder messageBuilder = new StringBuilder().append(msg);
            LockingDebugUtils.debugLocking(messageBuilder);
            throw new LockingException(msg);
        }
        if (mutableItem == null) {
            throw new IllegalStateException("Cannot save item " + lockEntryId + ". Mutable item is null.");
        }
        if (mutableItem.hasPendingChanges()) {
            log.debug("Saving item: {}", getRepoPath());
            mutableItem.save();
        } else {
            log.trace("Item {} has no pending changes", getRepoPath());
        }
    }

    public void acquireWriteLock() {
        log.trace("Acquiring WRITE lock on {}", lockEntryId);
        if (isWriteLockedByMe()) {
            // current thread already holds the write lock
            return;
        }

        acquire();
    }

    private void acquire() {
        LockWrapper lock = lockEntryId.getLock();
        try {
            boolean success = lock.tryLock(ConstantValues.locksTimeoutSecs.getLong(), TimeUnit.SECONDS);
            if (!success) {
                StringBuilder messageBuilder =
                        new StringBuilder().append("Lock on ").append(lockEntryId)
                                .append(" not acquired in ").append(ConstantValues.locksTimeoutSecs.getLong())
                                .append(" seconds. Lock info: ").append(lock).append(".");

                if (lock instanceof JVMLockWrapper && log.isDebugEnabled()) {
                    JVMLockWrapper cast = (JVMLockWrapper) lock;
                    Thread owner = cast.lockOwner();
                    StackTraceElement[] stackTrace = owner.getStackTrace();
                    log.debug("The lockEntryId {} requested by {} is acquirted by {}" , lockEntryId, Thread.currentThread().getName(), owner.getName());
                    for (StackTraceElement element : stackTrace) {
                        log.debug("\tat {}", element);
                    }
                }

                if (ConstantValues.locksDebugTimeouts.getBoolean()) {
                    LockingDebugUtils.debugLocking(lockEntryId, messageBuilder);
                }
                throw new LockingException(messageBuilder.toString());
            }
        } catch (InterruptedException e) {
            throw new LockingException("Lock on " + lockEntryId + " not acquired!", e);
        }
    }

    private void releaseWriteLock() {
        log.trace("Releasing WRITE lock on {}", lockEntryId);
        if (isWriteLockedByMe()) {
            try {
                if (mutableItem != null) {
                    mutableItem.releaseResources();
                    if (mutableItem.hasPendingChanges()) {
                        // local modification will be discarded
                        log.warn("Mutable item '{}' has local modifications that will be discarded.", mutableItem);
                    }
                }
            } catch (Throwable e) {
                log.error("Exception while releasing resources on " + lockEntryId + " due to " + e.getMessage(), e);
            } finally {
                mutableItem = null;
                lockEntryId.getLock().unlock();
            }
        }
    }

    public boolean isWriteLockedByMe() {
        return lockEntryId.getLock().isHeldByCurrentThread();
    }

    private RepoPath getRepoPath() {
        return lockEntryId.getRepoPath();
    }

    public boolean isDeleted() {
        return isWriteLockedByMe() && mutableItem.isDeleted();
    }

}
