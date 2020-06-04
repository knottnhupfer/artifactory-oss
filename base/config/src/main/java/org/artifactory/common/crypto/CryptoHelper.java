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

package org.artifactory.common.crypto;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.artifactory.common.ArtifactoryHome;
import org.jfrog.security.crypto.CipherAlg;
import org.jfrog.security.crypto.EncodingType;
import org.jfrog.security.file.SecurityFolderHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.artifactory.common.ArtifactoryHome.ARTIFACTORY_KEY_DEFAULT_TEMP_FILE_NAME;
import static org.jfrog.security.file.SecurityFolderHelper.PERMISSIONS_MODE_600;

/**
 * Helper class for encrypting/decrypting passwords.
 *
 * @author Yossi Shaul
 */
public abstract class CryptoHelper {
    private static final Logger log = LoggerFactory.getLogger(CryptoHelper.class);

    private CryptoHelper() {
        // utility class
    }

    public static boolean isArtifactoryKeyEncrypted(String in) {
        return EncodingType.ARTIFACTORY_MASTER.isEncodedByMe(in);
    }

    public static boolean isEncryptedUserPassword(String in) {
        return EncodingType.ARTIFACTORY_PASSWORD.isEncodedByMe(in);
    }

    public static boolean isMasterKeyEncrypted(String in) {
        return EncodingType.MASTER_LEVEL.isEncodedByMe(in);
    }

    public static boolean isApiKey(String in) {
        return EncodingType.ARTIFACTORY_API_KEY.isEncodedByMe(in);
    }

    public static String encryptIfNeeded(ArtifactoryHome home, String password) {
        if(StringUtils.isBlank(password)) {
             //No password, no encryption
            return password;
        }
        return home.getArtifactoryEncryptionWrapper().encryptIfNeeded(password);
    }

    public static String decryptIfNeeded(ArtifactoryHome home, String password) {
        if (isArtifactoryKeyEncrypted(password)) {
            File keyFile = home.getArtifactoryKey();
            if (keyFile.exists()) {
                return home.getArtifactoryEncryptionWrapper().decryptIfNeeded(password).getDecryptedData();
            } else {
                log.warn("Encrypted password found and no Artifactory Key file exists at {}",  keyFile.getAbsolutePath());
            }
        }
        return password;
    }

    /**
     * Encrypt using the Master key (not the artifactory.key file)
     */
    public static String encryptWithMasterKeyIfNeeded(ArtifactoryHome home, String password) {
        if (StringUtils.isBlank(password)) {
            //No password, no encryption
            return password;
        }
        return home.getMasterEncryptionWrapper().encryptIfNeeded(password);
    }

    /**
     * Decrypt using the Master key (not the artifactory.key file)
     */
    public static String decryptWithMasterKeyIfNeeded(ArtifactoryHome home, String password) {
        if (isMasterKeyEncrypted(password)) {
            File masterKeyFile = home.getMasterKeyFile();
            if (masterKeyFile.exists()) {
                return home.getMasterEncryptionWrapper().decryptIfNeeded(password).getDecryptedData();
            } else {
                log.warn("Encrypted password found and no Master Key file exists at {}", masterKeyFile.getAbsolutePath());
            }
        }
        return password;
    }

    /**
     * Renames the master key file, effectively disabling encryption.
     * @return the renamed master key file
     */
    public static File removeArtifactoryKeyFile(ArtifactoryHome home) {
        File keyFile = home.getArtifactoryKey();
        File renamedKey = SecurityFolderHelper.removeKeyFile(keyFile);
        unsetArtifactoryEncryptionWrapper(home);
        return renamedKey;
    }

    public static void unsetArtifactoryEncryptionWrapper(ArtifactoryHome home) {
        home.unsetArtifactoryEncryptionWrapper();
    }

    /**
     * Creates a master encryption key file. Throws an exception if the key file already exists of on any failure with
     * file or key creation.
     */
    public static void createArtifactoryKeyFile(ArtifactoryHome home) {
        // must support lookup by token when encrypted CipherAlg.AES128)
        createArtifactoryKeyFile(home, CipherAlg.AES128);
    }
    public static void createArtifactoryKeyFile(ArtifactoryHome home, CipherAlg useCipherAlg) {
        File targetKeyFile = home.getArtifactoryKey();
        File tmpKeyFile = new File(home.getSecurityDir(), ARTIFACTORY_KEY_DEFAULT_TEMP_FILE_NAME);
        try {
            Files.deleteIfExists(tmpKeyFile.toPath());
            SecurityFolderHelper.createKeyFile(tmpKeyFile, useCipherAlg);
            moveAndSecureKey(tmpKeyFile, targetKeyFile);
        } catch (IOException e) {
            throw new RuntimeException("Could not save key file to " + targetKeyFile.getParentFile().getAbsolutePath(), e);
        } finally {
            try {
                Files.deleteIfExists(tmpKeyFile.toPath());
            } catch (IOException e) {
                log.error("Could not delete temporary key file {}. {}", tmpKeyFile.getAbsolutePath(), e.getMessage());
                log.trace("", e);
            }
        }
        home.unsetArtifactoryEncryptionWrapper();
    }

    private static void moveAndSecureKey(File tmpKeyFile, File targetKeyFile) throws IOException {
        try {
            SecurityFolderHelper.setPermissionsOnSecurityFolder(targetKeyFile.getParentFile());
        } catch (IOException e) {
            throw new RuntimeException(
                    "Could not create and set permission on " + targetKeyFile.getParentFile().getAbsolutePath(), e);
        }
        FileUtils.moveFile(tmpKeyFile, targetKeyFile);
        SecurityFolderHelper.setPermissionsOnSecurityFile(targetKeyFile.toPath(), PERMISSIONS_MODE_600);
    }

    public static boolean hasArtifactoryKey(ArtifactoryHome home) {
        return home.getArtifactoryKey().exists();
    }

    public static File getArtifactoryKey(ArtifactoryHome home) {
        return home.getArtifactoryKey();
    }
}
