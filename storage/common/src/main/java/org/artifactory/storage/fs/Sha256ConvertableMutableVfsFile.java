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

package org.artifactory.storage.fs;

/**
 * DANGER!! DO NOT use this, its meant only for the sha256 converter.
 *
 * This interface is used to update the sha256 value for existing nodes WHICH YOU SHOULD NEVER DO - the choice was
 * between an extra DB call (using {@link MutableVfsFile#tryUsingExistingBinary} or this interface.
 *
 * As a great leader once said:
 * "History Will Absolve Me" -Fidel Castro

 * @author Dan Feldman
 */
@Deprecated
public interface Sha256ConvertableMutableVfsFile {

    void setSha2(String sha2);
}
