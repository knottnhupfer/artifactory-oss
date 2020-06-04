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

package org.artifactory.ui.rest.model.setmeup;

import org.artifactory.rest.common.model.BaseModel;

/**
 * @author Chen Keinan
 */
public class ReverseProxySetMeUpDataModel extends BaseModel {

    private boolean usingHttps;
    private boolean methodSelected;
    private boolean usingPorts;
    private String  serverName;
    private Integer repoPort;

    public boolean isUsingPorts() {
        return usingPorts;
    }

    public void setUsingPorts(boolean usingPorts) {
        this.usingPorts = usingPorts;
    }

    public boolean isUsingHttps() {
        return usingHttps;
    }

    public void setUsingHttps(boolean usingHttps) {
        this.usingHttps = usingHttps;
    }

    public boolean isMethodSelected() {
        return methodSelected;
    }

    public void setMethodSelected(boolean methodSelected) {
        this.methodSelected = methodSelected;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public Integer getRepoPort() {
        return repoPort;
    }

    public void setRepoPort(Integer repoPort) {
        this.repoPort = repoPort;
    }

}
