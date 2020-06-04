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

package org.artifactory.storage.db.binstore.entity;

import org.artifactory.storage.GCCandidate;

import java.io.Serializable;

/**
 * Represents a binary data entry in the database.
 *
 * @author Yossi Shaul
 */
public class BinaryEntity implements Serializable {

    private final String sha1;
    private final String sha2;
    private final String md5;
    private final long length;

    public BinaryEntity(String sha1, String sha2, String md5, long length) {
        this.sha1 = sha1;
        this.sha2 = sha2;
        this.md5 = md5;
        this.length = length;
    }

    public BinaryEntity(GCCandidate gcCandidate) {
        this.sha1 = gcCandidate.getSha1();
        this.sha2 = gcCandidate.getSha2();
        this.md5 = gcCandidate.getMd5();
        this.length = gcCandidate.getLength();
    }

    public String getSha1() {
        return sha1;
    }

    public String getSha2() {
        return sha2;
    }

    public String getMd5() {
        return md5;
    }

    public long getLength() {
        return length;
    }

    @Override
    public String toString() {
        return "{" + sha1 + ',' + sha2 + ',' + md5 + ',' + length + '}';
    }

}
