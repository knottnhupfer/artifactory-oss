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

package org.artifactory.security;

import org.artifactory.spring.ContextCreationListener;

import java.io.File;

/**
 * Service to perform encryption/decryption of config files passwords with the artifactory encryption key.
 *
 * @author Yossi Shaul
 */
public interface ArtifactoryEncryptionService extends ContextCreationListener {

    /**
     * Encrypts the configuration files passwords with the master encryption key.
     * The key is created if not already exists.
     */
    void encrypt();

    /**
     * Decrypts the configuration files passwords using the existing master encryption key.
     * The key is renamed after decryption and never used again.
     */
    void decrypt();

    /**
     * In case the artifactory key has been created - propagate the change to other nodes
     */
    void notifyArtifactoryKeyCreated();

    /**
     * In case the artifactory key has been deleted - propagate the change to other nodes: first notify of delete,
     * then notify about create of the renamed artifactory key file
     */
    void notifyArtifactoryKeyDeleted(File oldKeyFile, File renamedKeyFile);

    /**
     * Encrypts or Decrypts the db properties file passwords with the master encryption key.
     *
     * @param encrypt encrypt if true
     */
    void encryptDecryptDbProperties(boolean encrypt);
}
