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

package org.artifactory.rest.common.model.proxies;

import com.google.common.collect.Lists;
import org.artifactory.rest.common.model.BaseModel;

import java.util.List;

/**
 * @author Gidi Shabat
 */
public class ProxiesModel extends BaseModel {
    private List<String> proxyKeys = Lists.newArrayList();
    private Boolean portAvailable;

    public Boolean getPortAvailable() {
        return portAvailable;
    }

    public void setPortAvailable(Boolean portAvailable) {
        this.portAvailable = portAvailable;
    }

    public List<String> getProxyKeys() {
        return proxyKeys;
    }

    public void addProxy(String proxyKey) {
        proxyKeys.add(proxyKey);
    }

    public void setProxyKeys(List<String> proxyKeys) {
        this.proxyKeys = proxyKeys;
    }
}

