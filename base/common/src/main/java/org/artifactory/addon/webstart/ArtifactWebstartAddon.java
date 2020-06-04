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

package org.artifactory.addon.webstart;

import org.artifactory.addon.Addon;
import org.artifactory.exception.InvalidCertificateException;

import java.io.File;
import java.io.IOException;
import java.security.Key;
import java.security.KeyStore;
import java.util.List;

/**
 * Webstart addon factory.
 *
 * @author Yossi Shaul
 */
public interface ArtifactWebstartAddon extends Addon {
    // Hardcoded alias prefix for client SSL certificates
    String SSL_CERT_ALIAS_PREFIX = "artsslcert:";

    default KeyStore loadKeyStore(File keyStoreFile, String password) {
        return null;
    }

    default String getKeystorePassword() {
        return null;
    }

    default KeyStore getExistingKeyStore() throws KeyStoreNotFoundException {
        return null;
    }

    default Key getAliasKey(KeyStore keyStore, String alias, String password) {
        return null;
    }

    default void addKeyPair(File file, String pairName, String keyStorePassword, String alias, String privateKeyPassword) throws IOException {

    }

    default boolean keyStoreExist() {
        return false;
    }

    default List<String> getKeyPairNames() {
        return null;
    }

    default List<String> getSslCertNames() {
        return null;
    }

    default boolean hasKeyPair(String keyPairName) {
        return false;
    }

    default boolean removeKeyPair(String keyPairName) {
        return false;
    }

    default void setKeyStorePassword(String password) {
    }

    default void removeKeyStorePassword() {
    }

    default void addPemCertificateToKeystore(String pemContent, String alias) throws InvalidCertificateException {
        throw new UnsupportedOperationException(
                "Client certificate is only available on licensed Artifactory versions.");
    }
}
