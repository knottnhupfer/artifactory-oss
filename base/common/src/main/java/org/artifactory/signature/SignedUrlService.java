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

package org.artifactory.signature;

import org.artifactory.spring.ReloadableBean;
import org.jfrog.storage.binstore.exceptions.SignedUrlException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Rotem Kfir
 */
public interface SignedUrlService extends ReloadableBean {

    /**
     * Generates a random signing key, and stores it encrypted in CONFIGS table
     */
    void createAndStoreSigningKey();

    /**
     * Sets the URL signing key
     * @param signingKey the key, not encrypted
     */
    void setSigningKey(byte[] signingKey);

    /**
     * Gets the URL signing key
     * @return the key, not encrypted
     */
    @Nullable
    byte[] getSigningKey();

    /**
     * Creates a Signed Token with the given path's checksum
     *
     * @param path path to a file
     * @param validForSeconds the number of seconds until signature's expiration
     * @return JSON web signature (JWS)
     * @throws SignedUrlException if the JWS object couldn't be signed
     */
    @Nonnull
    String createSignedToken(@Nonnull String path, Long validForSeconds);

    /**
     * Verifies the signature
     *
     * @param path path to the file
     * @param signedToken signed token to verify
     * @return the token's issuer
     * @throws SignedUrlException if the signed token is not valid
     */
    String verifySignedToken(@Nonnull String path, @Nonnull String signedToken);
}
