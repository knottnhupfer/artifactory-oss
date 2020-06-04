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

package org.artifactory.addon.security;

import org.artifactory.addon.LockingProvider;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.jfrog.storage.common.ConflictGuard;
import org.jfrog.storage.common.ConflictsGuard;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Nadav Yogev
 */
public class JvmConflictGuardProviderTest extends ArtifactoryHomeBoundTest {

    @Test
    public void testConflictGuard() throws InterruptedException {
        LockingProvider conflictGuardProvider = new JvmConflictGuardProvider();
        ConflictGuard guard1 = conflictGuardProvider.getConflictGuard("type");
        boolean locked = guard1.tryToLock(5, TimeUnit.SECONDS);
        assertTrue(locked);
        assertTrue(guard1.isLocked());
        // another guard with the same name should be locked
        ConflictGuard guard2 = conflictGuardProvider.getConflictGuard("type");
        assertTrue(guard2.isLocked());
        // another guard with the different name should not be locked
        ConflictGuard guard3 = conflictGuardProvider.getConflictGuard("otherType");
        assertFalse(guard3.isLocked());
        // unlocked and test is unlocked
        guard1.unlock();
        assertFalse(guard1.isLocked());
        assertFalse(guard2.isLocked());
    }

    @Test
    public void testConflictsGuard() throws InterruptedException {
        LockingProvider conflictGuardProvider = new JvmConflictGuardProvider();
        ConflictsGuard<String> conflictsGuard1 = conflictGuardProvider.getConflictsGuard("type");
        ConflictGuard guard1TypeA = conflictsGuard1.getLock("typeA.lock");
        ConflictGuard guard1TypeB = conflictsGuard1.getLock("typeB.lock");
        // lock A, expect the A guard to be lock
        boolean locked = guard1TypeA.tryToLock(5, TimeUnit.SECONDS);
        assertTrue(locked);
        assertTrue(guard1TypeA.isLocked());
        assertTrue(conflictsGuard1.isLocked("typeA.lock"));
        // B should not be locked
        assertFalse(guard1TypeB.isLocked());
        assertFalse(conflictsGuard1.isLocked("typeB.lock"));
        // request another 'type' provider, expect the same behaviour as before
        ConflictsGuard<String> conflictsGuard2 = conflictGuardProvider.getConflictsGuard("type");
        assertTrue(conflictsGuard2.isLocked("typeA.lock"));
        assertFalse(conflictsGuard2.isLocked("typeB.lock"));
        ConflictGuard guard2TypeA = conflictsGuard2.getLock("typeA.lock");
        assertTrue(guard2TypeA.isLocked());
        // request another provider with different name, expect not to be locked
        ConflictsGuard<String> conflictsGuard3 = conflictGuardProvider.getConflictsGuard("otherType");
        assertFalse(conflictsGuard3.isLocked("typeA.lock"));
        ConflictGuard guard3TypeA = conflictsGuard3.getLock("typeA.lock");
        assertFalse(guard3TypeA.isLocked());
        // unlock and test is unlocked
        conflictsGuard1.unlock("typeA.lock");
        assertFalse(conflictsGuard1.isLocked("typeA.lock"));
        assertFalse(guard1TypeA.isLocked());
        assertFalse(guard2TypeA.isLocked());
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testConflictsGuardsGeneralBlocked() {
        // can't ask for 'general' guard - it's reserved!
        JvmConflictGuardProvider conflictGuardProvider = new JvmConflictGuardProvider();
        conflictGuardProvider.getConflictsGuard("general");
    }
}