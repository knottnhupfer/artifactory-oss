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

import org.artifactory.storage.binstore.service.BinaryInfo;

import javax.annotation.Nonnull;

/**
 * A streams that reads from the binary store. The binary itself is guaranteed to be in the binary store while this
 * stream is open.
 *
 * @author Yossi Shaul
 */
public interface BinaryServiceInputStream {

    /**
     * @return The full Binary Info object used from checksum deploy.
     */
    @Nonnull
    BinaryInfo getBinaryInfo();
}
