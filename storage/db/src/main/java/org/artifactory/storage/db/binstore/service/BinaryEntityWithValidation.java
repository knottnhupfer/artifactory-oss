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

package org.artifactory.storage.db.binstore.service;

import org.apache.commons.lang.StringUtils;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.storage.db.binstore.entity.BinaryEntity;

/**
 * @author gidis
 */
public class BinaryEntityWithValidation extends BinaryEntity {


    public BinaryEntityWithValidation(String sha1, String sha2, String md5, long length) {
        super(sha1, sha2, md5, length);
        isValid();
    }

    private void simpleValidation() {
        if (StringUtils.isBlank(getSha1()) || getSha1().length() != ChecksumType.sha1.length()) {
            throw new IllegalArgumentException("SHA1 value '" + getSha1() + "' is not a valid checksum");
        }
        if (StringUtils.isBlank(getSha2()) || getSha2().length() != ChecksumType.sha256.length()) {
            throw new IllegalArgumentException("SHA2 value '" + getSha2() + "' is not a valid checksum");
        }
        if (StringUtils.isBlank(getMd5()) || getMd5().length() != ChecksumType.md5.length()) {
            throw new IllegalArgumentException("MD5 value '" + getMd5() + "' is not a valid checksum");
        }
        if (getLength() < 0L) {
            throw new IllegalArgumentException("Length " + getLength() + " is not a valid length");
        }
    }

    public boolean isValid() {
        simpleValidation();
        return ChecksumType.sha1.isValid(getSha1())
                && ChecksumType.md5.isValid(getMd5())
                && ChecksumType.sha256.isValid(getSha2());
    }
}
