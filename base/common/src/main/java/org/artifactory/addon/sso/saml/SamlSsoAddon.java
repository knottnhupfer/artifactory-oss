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

package org.artifactory.addon.sso.saml;

import org.artifactory.addon.Addon;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Chen Keinan
 */
public interface SamlSsoAddon extends Addon {
    String REALM = "saml";
    String SAML_SSO_ENCRYPTED_ASSERTION = "artifactory.saml.encrypted.assertion.crt";

    default String getSamlLoginIdentityProviderUrl(HttpServletRequest request, String redirectTo) {
        return null;
    }

    default void createCertificate(String certificate) throws Exception {
    }

    default Boolean isSamlAuthentication(HttpServletRequest servletRequest, HttpServletResponse servletResponse)
            throws SamlException {
        return false;
    }

    default boolean isSamlAuthentication() {
        return false;
    }

    default String createStoreAndGetKeyPair(boolean regeneratePublicKey) throws SamlException {
        return null;
    }

}
