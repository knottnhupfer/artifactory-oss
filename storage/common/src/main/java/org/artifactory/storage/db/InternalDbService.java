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

package org.artifactory.storage.db;

import org.artifactory.api.repo.Async;

/**
 * @author Dan Feldman
 */
public interface InternalDbService extends DbService {

    void initDb() throws Exception;

    /**
     * TO BE USED ONLY BY THE SHA256 MIGRATION JOB
     */
    boolean verifySha256State();

    /**
     * Signifies the sha256 columns exists in appropriate db tables, and this instance can write their values into db.
     */
    boolean isSha256Ready();

    boolean verifyUniqueRepoPathChecksumState();

    boolean isUniqueRepoPathChecksumReady();

    @Async
    void verifyMigrations();
}
