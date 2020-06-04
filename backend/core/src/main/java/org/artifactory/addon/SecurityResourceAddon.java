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

package org.artifactory.addon;

import org.artifactory.rest.exception.MissingRestAddonException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * @author Chen Keinan
 */
public interface SecurityResourceAddon extends Addon {

     default Response createOrReplaceSecurityEntity(String entityType, String entityKey, HttpServletRequest request)
            throws IOException {
         throw new MissingRestAddonException();
     }

    default Response getSecurityEntities(HttpServletRequest request, String entityType) throws UnsupportedEncodingException {
        throw new MissingRestAddonException();
    }

    default Response getSecurityEntity(String entityType, String entityKey, boolean includeUsers) {
        throw new MissingRestAddonException();
    }

    default Response deleteSecurityEntity(String entityType, String entityKey) {
        throw new MissingRestAddonException();
    }

    default Response updateSecurityEntity(String entityType, String entityKey, HttpServletRequest request) throws IOException {
        throw new MissingRestAddonException();
    }

}
