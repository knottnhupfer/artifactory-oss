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

package org.artifactory.repo.reverseProxy;

/**
 * @author Chen Keinan
 */
public class ReverseProxyPorts {

    private boolean http;
    private boolean https;
    private boolean bothPorts;

    public ReverseProxyPorts() {
    }

    public ReverseProxyPorts(boolean http, boolean https, boolean bothPorts) {
        this.http = http;
        this.https = https;
        this.bothPorts = bothPorts;
    }

    public boolean isHttp() {
        return http;
    }

    public void setHttp(boolean http) {
        this.http = http;
    }

    public boolean isHttps() {
        return https;
    }

    public void setHttps(boolean https) {
        this.https = https;
    }

    public boolean isBothPorts() {
        return bothPorts;
    }

    public void setBothPorts(boolean bothPorts) {
        this.bothPorts = bothPorts;
    }
}
