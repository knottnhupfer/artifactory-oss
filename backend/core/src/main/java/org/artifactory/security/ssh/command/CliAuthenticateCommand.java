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

package org.artifactory.security.ssh.command;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.security.access.CreatedTokenInfo;
import org.artifactory.security.ssh.UsernameAttributeKey;
import org.artifactory.storage.security.service.UserGroupStoreService;
import org.jfrog.client.util.PathUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Chen Keinan
 */
public class CliAuthenticateCommand extends AbstractAuthenticateCommand {

    public static final String COMMAND_NAME = "jfrog-authenticate";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String OAUTH_TOKEN_PREFIX = "Bearer ";

    public CliAuthenticateCommand(CentralConfigService centralConfigService,
            UserGroupStoreService userGroupStoreService,
            String command) {
        super(centralConfigService, userGroupStoreService, command);
    }

    @Override
    protected void parseCommandDetails(String command) {
        // do nothing
    }

    protected void sendAuthHeader() throws IOException {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> header = new HashMap<>();
        // update header href
        updateHeaderHref(response);
        // update header authorization
        updateHeaderAuthorization(header);
        // write response
        writeResponse(response, header);
    }

    /**
     * update header basic authentication
     *
     * @param header - header object
     */
    private void updateHeaderAuthorization(Map<String, Object> header) {
        String username = serverSession.getAttribute(new UsernameAttributeKey());
        CreatedTokenInfo tokenInfo = generateUniqueToken(username);
        header.put(AUTHORIZATION_HEADER,
                OAUTH_TOKEN_PREFIX + tokenInfo.getTokenValue());
    }

    protected void updateResponseHeader(Map<String, Object> response, Map<String, Object> header) {
        response.put("headers", header);
    }

    /**
     * update header href
     *
     * @param response - response map
     */
    private void updateHeaderHref(Map<String, Object> response) {
        String urlBase = getBaseUrl();
        response.put(HREF, PathUtils.addTrailingSlash(urlBase));
    }
}
