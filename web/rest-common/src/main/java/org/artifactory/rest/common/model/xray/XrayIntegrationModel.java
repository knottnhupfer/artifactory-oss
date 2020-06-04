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

package org.artifactory.rest.common.model.xray;

import org.artifactory.rest.common.model.BaseModel;

/**
 * @author Yuval Reches
 */
public class XrayIntegrationModel extends BaseModel {
    private boolean xrayEnabled;
    private boolean xrayAllowBlocked;
    private boolean xrayAllowWhenUnavailable;
    private boolean bypassDefaultProxy;
    private String proxy;
    private Integer blockUnscannedTimeoutSeconds;


    public XrayIntegrationModel(boolean xrayEnabled, boolean xrayAllowBlocked, boolean xrayAllowWhenUnavailable
    , boolean bypassDefaultProxy, String proxy, Integer blockUnscannedTimeoutSeconds) {
        this.xrayEnabled = xrayEnabled;
        this.xrayAllowBlocked = xrayAllowBlocked;
        this.xrayAllowWhenUnavailable = xrayAllowWhenUnavailable;
        this.bypassDefaultProxy = bypassDefaultProxy;
        this.proxy = proxy;
        this.blockUnscannedTimeoutSeconds = blockUnscannedTimeoutSeconds;
    }

    public XrayIntegrationModel() {
    }

    public boolean isXrayEnabled() {
        return xrayEnabled;
    }

    public void setXrayEnabled(boolean xrayEnabled) {
        this.xrayEnabled = xrayEnabled;
    }

    public boolean isXrayAllowBlocked() {
        return xrayAllowBlocked;
    }

    public void setXrayAllowBlocked(boolean xrayIgnoreBlocked) {
        this.xrayAllowBlocked = xrayIgnoreBlocked;
    }

    public boolean isXrayAllowWhenUnavailable() {
        return xrayAllowWhenUnavailable;
    }

    public void setXrayAllowWhenUnavailable(boolean xrayAllowWhenOffline) {
        this.xrayAllowWhenUnavailable = xrayAllowWhenOffline;
    }

    public String getProxy() {
        return proxy;
    }

    public void setProxy(String proxy) {
        this.proxy = proxy;
    }


    public boolean isBypassDefaultProxy() {
        return bypassDefaultProxy;
    }

    public void setBypassDefaultProxy(boolean bypassDefaultProxy) {
        this.bypassDefaultProxy = bypassDefaultProxy;
    }

    public Integer getBlockUnscannedTimeoutSeconds() {
        return blockUnscannedTimeoutSeconds;
    }

    public void setBlockUnscannedTimeoutSeconds(Integer blockUnscannedTimeoutSeconds) {
        this.blockUnscannedTimeoutSeconds = blockUnscannedTimeoutSeconds;
    }
}
