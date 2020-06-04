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

package org.artifactory.lock;

import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;

/**
 * @author gidis
 */
public enum LockingProviderTypeEnum {
    jvm("jvm"), distributed("distributed"), optimistic("optimistic"), db("db");

    private String nativeName;

    LockingProviderTypeEnum(String nativeName) {
        this.nativeName = nativeName;
    }

    public static LockingProviderTypeEnum getLockMethod() {
        // If HA is not configured the return JVM method
        try {
            if (!ArtifactoryHome.get().isHaConfigured()) {
                return jvm;
            }
        } catch (Exception e) {
            return jvm;
        }
        // This is HA environment, in this case select method according to the provided value in the properties
        String nativeName = ConstantValues.lockingProviderType.getString();
        for (LockingProviderTypeEnum lockingProviderTypeEnum : values()) {
            if (lockingProviderTypeEnum.nativeName.toLowerCase().equals(nativeName.trim().toLowerCase())) {
                return lockingProviderTypeEnum;
            }
        }
        throw new RuntimeException("Unsupported distributed lock method: " + nativeName);
    }

    public static boolean isDistributed() {
        return distributed == getLockMethod();
    }

    public static boolean isDb() {
        return db == getLockMethod();
    }

    public static boolean isOptimistic() {
        return optimistic == getLockMethod();
    }
}
