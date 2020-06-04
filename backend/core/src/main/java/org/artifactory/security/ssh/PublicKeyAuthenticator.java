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

package org.artifactory.security.ssh;

import org.apache.commons.codec.binary.Base64;
import org.apache.sshd.server.PublickeyAuthenticator;
import org.apache.sshd.server.session.ServerSession;
import org.artifactory.security.UserInfo;
import org.artifactory.storage.security.service.UserGroupStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;

/**
 * @author Noam Y. Tenne
 */
public class PublicKeyAuthenticator implements PublickeyAuthenticator {

    private static final Logger log = LoggerFactory.getLogger(PublicKeyAuthenticator.class);

    private UserGroupStoreService userGroupStoreService;

    public PublicKeyAuthenticator(UserGroupStoreService userGroupStoreService) {
        this.userGroupStoreService = userGroupStoreService;
    }

    @Override
    public boolean authenticate(String username, PublicKey key, ServerSession session) {
        String sshPublicKey;
        try {
            sshPublicKey = decodedPublicKey(key);
        } catch (IOException e) {
            log.error("Failed to read public key as blob", e);
            return false;
        }
        UserInfo userInfo = userGroupStoreService.findUserByProperty("sshPublicKey", sshPublicKey, true);
        if (userInfo != null) {
            session.setAttribute(new UsernameAttributeKey(), userInfo.getUsername());
            return true;
        }
        return false;
    }

    /**
     * decode public key and return it as string
     * @param key - public key
     * @return
     * @throws IOException
     */
    private String decodedPublicKey(PublicKey key) throws IOException {
        String algorithm = key.getAlgorithm();
        if ("RSA".equals(algorithm)) {
            byte[] rawKey = rsaKeyToBytes((RSAPublicKey) key);
            return "ssh-rsa " + Base64.encodeBase64String(rawKey);
        }
        return "";
    }

    /**
     * convert rsa key to bytes
     * @param publicKey - rsa public key
     * @return rsa key as byte array
     * @throws IOException
     */
    private static byte[] rsaKeyToBytes(RSAPublicKey publicKey) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeLengthFirst("ssh-rsa".getBytes(), out);
        writeLengthFirst(publicKey.getPublicExponent().toByteArray(), out);
        writeLengthFirst(publicKey.getModulus().toByteArray(), out);
        return out.toByteArray();
    }

    // http://www.ietf.org/rfc/rfc4253.txt

    /**
     * write public key data to byte array output stream
     * @param array - byte array
     * @param out - byte array output stream
     * @throws IOException
     */
    private static void writeLengthFirst(byte[] array, ByteArrayOutputStream out) throws IOException {
        out.write((array.length >>> 24) & 0xFF);
        out.write((array.length >>> 16) & 0xFF);
        out.write((array.length >>> 8) & 0xFF);
        out.write((array.length) & 0xFF);
        if (array.length == 1 && array[0] == (byte) 0x00) {
            out.write(new byte[0]);
        } else {
            out.write(array);
        }
    }
}
