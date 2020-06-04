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

package org.artifactory.addon.binary.provider;

import org.artifactory.addon.Addon;
import org.artifactory.api.rest.binary.services.Part;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * @author Gal Ben Ami
 */
public interface BinaryProviderApiAddon extends Addon {

    default InputStream getBinaryBySha256(String sha256) {
        return null;
    }

    default InputStream getBinaryPartBySha256(String sha256, long start, long end) {
        return null;
    }

    default void getBinaryPartsBySha256(String sha256, List<Part> ranges, InputStream is, OutputStream outputStream) {
        return;
    }


}
