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

package org.artifactory.addon.common.gpg;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author Yoav Luft
 */
public interface GpgKeyStore {

    /**
     * @return The GPG signing public key file location (the file might not exist).
     */
    File getPublicKeyFile();

    /**
     * @return The ASCII Armored GPG signing key. Null if public key file doesn't exist
     * @throws IOException If failed to read the key from file
     */
    @Nullable
    String getPublicKey() throws IOException;

    List<byte[]> getPublicKeys() throws IOException;

    void savePrivateKey(String privateKey) throws Exception;

    void savePublicKey(String publicKey) throws Exception;

    void savePassPhrase(String password);

    boolean hasPrivateKey();

    String getPrivateKey() throws IOException;

    void removePublicKey();

    void removePrivateKey();

    boolean verify(String passphrase);

    boolean hasPublicKey();
}
