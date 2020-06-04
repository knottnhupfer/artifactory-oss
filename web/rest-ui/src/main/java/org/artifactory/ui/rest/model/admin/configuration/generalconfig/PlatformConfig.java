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

package org.artifactory.ui.rest.model.admin.configuration.generalconfig;

import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.rest.common.model.BaseModel;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * @author Yoaz Menda
 */
public class PlatformConfig extends BaseModel {

    @JsonProperty("server_name")
    private String serverName;
    @JsonProperty("custom_url_base")
    private String customUrlBase;
    @JsonProperty("force_base_url")
    private Boolean forceBaseUrl;

    public PlatformConfig() {
    }

    public PlatformConfig(CentralConfigDescriptor mutableDescriptor) {
        serverName = mutableDescriptor.getServerName();
        customUrlBase = mutableDescriptor.getUrlBase();
    }


    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getCustomUrlBase() {
        return customUrlBase;
    }

    public void setCustomUrlBase(String customUrlBase) {
        this.customUrlBase = customUrlBase;
    }

    public Boolean getForceBaseUrl() {
        return forceBaseUrl;
    }

    public void setForceBaseUrl(Boolean forceBaseUrl) {
        this.forceBaseUrl = forceBaseUrl;
    }
}