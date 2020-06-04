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

package org.artifactory.io.checksum.policy;

import org.artifactory.checksum.ChecksumInfo;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.descriptor.repo.ChecksumPolicyType;
import org.artifactory.repo.RepoPath;

import java.io.Serializable;
import java.util.Set;

/**
 * Base abstract implementation of the ChecksumPolicy.
 *
 * @author Yossi Shaul
 */
public abstract class ChecksumPolicyBase implements ChecksumPolicy, Serializable {

    abstract boolean verifyChecksum(ChecksumInfo checksumInfo);

    abstract String getChecksum(ChecksumInfo checksumInfo);

    /**
     * Signals the storage layer if client-sent checksums should be verified against actual checksums when saving a binary.
     */
    public abstract boolean shouldVerifyBadClientChecksum();

    @Override
    public boolean verify(Set<ChecksumInfo> checksumInfos) {
        //Because we treat sha2 as always trusted we need to ignore it when testing checksum policy
        return checksumInfos.stream()
                .filter(info -> !ChecksumType.sha256.equals(info.getType()))
                .map(this::verifyChecksum)
                .reduce((bool1, bool2) -> bool1 || bool2) //enough that one passes for the verification to pass
                .orElse(false);
    }

    @Override
    public String getChecksum(ChecksumType checksumType, Set<ChecksumInfo> checksumInfos) {
        ChecksumInfo info = getChecksumInfo(checksumType, checksumInfos);
        if (info != null) {
            return getChecksum(info);
        }
        return null;
    }

    @Override
    public String getChecksum(ChecksumType checksumType, Set<ChecksumInfo> checksumInfos, RepoPath repoPath) {
        // remote checksum policies don't care about the repo path
        return getChecksum(checksumType, checksumInfos);
    }

    private ChecksumInfo getChecksumInfo(ChecksumType type, Set<ChecksumInfo> infos) {
        for (ChecksumInfo info : infos) {
            if (type.equals(info.getType())) {
                return info;
            }
        }
        return null;
    }

    public static ChecksumPolicy getByType(ChecksumPolicyType type) {
        switch (type) {
            case GEN_IF_ABSENT:
                return new ChecksumPolicyGenerateIfAbsent();
            case FAIL:
                return new ChecksumPolicyFail();
            case IGNORE_AND_GEN:
                return new ChecksumPolicyIgnoreAndGenerate();
            case PASS_THRU:
                return new ChecksumPolicyPassThru();
            default:
                throw new IllegalArgumentException("No checksum policy found for type " + type);
        }
    }

    /**
     * @return The checksum policy type this checksum policy implements.
     */
    abstract ChecksumPolicyType getChecksumPolicyType();

    @Override
    public String toString() {
        return getChecksumPolicyType().toString();
    }

}
