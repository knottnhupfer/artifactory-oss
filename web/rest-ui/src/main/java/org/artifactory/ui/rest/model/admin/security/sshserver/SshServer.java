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

package org.artifactory.ui.rest.model.admin.security.sshserver;

import org.artifactory.descriptor.security.sshserver.SshServerSettings;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.util.JsonUtil;
import org.artifactory.ui.rest.model.admin.security.signingkey.SignKey;
import org.jfrog.common.config.diff.DiffIgnore;

/**
 * @author Noam Y. Tenne
 */
public class SshServer extends SshServerSettings implements RestModel {

    @DiffIgnore
    private SignKey serverKey;

    private String customUrlBase;

    public SshServer() {
    }

    public SshServer(SshServerSettings sshServerSettings) {
        if (sshServerSettings != null) {
            super.setEnableSshServer(sshServerSettings.isEnableSshServer());
            super.setSshServerPort(sshServerSettings.getSshServerPort());
        }
    }

    public SignKey getServerKey() {
        return serverKey;
    }

    public void setServerKey(SignKey serverKey) {
        this.serverKey = serverKey;
    }

    public String getCustomUrlBase() { return customUrlBase; }

    public void setCustomUrlBase(String customUrlBase) { this.customUrlBase = customUrlBase; }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
