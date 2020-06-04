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

package org.artifactory.addon.plugin;

import org.artifactory.request.Request;
import org.artifactory.request.RequestThreadLocal;
import org.artifactory.webapp.servlet.HttpArtifactoryRequest;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Fred Simon
 */
public class ResponseCtx extends ResourceStreamCtx {

    public final static int UNSET_STATUS = -1;

    private int status = UNSET_STATUS;
    private String message;
    private Map<String, String> headers = new HashMap<>();
    private Request clientRequest;

    public ResponseCtx() {
        this.clientRequest = null;
        HttpServletRequest request = RequestThreadLocal.getRequest();
        if (request != null) {
            try {
                this.clientRequest = new HttpArtifactoryRequest(request);
            } catch (UnsupportedEncodingException e) {
                LoggerFactory.getLogger(ResponseCtx.class).warn("Creating response context partially failed, client request set to null.");
            }
        }
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

}