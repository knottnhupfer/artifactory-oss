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

package org.artifactory.ui.rest.model.admin.configuration.repository.remote;

import org.artifactory.rest.common.util.JsonUtil;
import org.artifactory.ui.rest.model.admin.configuration.repository.RepositoryNetworkConfigModel;

import java.util.List;

import static org.artifactory.repo.config.RepoConfigDefaultValues.DEFAULT_COOKIE_MANAGEMENT;
import static org.artifactory.repo.config.RepoConfigDefaultValues.DEFAULT_LENIENENT_HOST_AUTH;

/**
 * @author Aviad Shikloshi
 * @author Dan Feldman
 */
public class RemoteNetworkRepositoryConfigModel extends RepositoryNetworkConfigModel {

    protected String localAddress;
    protected String selectedInstalledCertificate;
    protected Boolean lenientHostAuth = DEFAULT_LENIENENT_HOST_AUTH;
    protected Boolean cookieManagement = DEFAULT_COOKIE_MANAGEMENT;
    protected List<String> installedCertificatesList;

    public String getLocalAddress() {
        return localAddress;
    }

    public void setLocalAddress(String localAddress) {
        this.localAddress = localAddress;
    }

    public Boolean getLenientHostAuth() {
        return lenientHostAuth;
    }

    public void setLenientHostAuth(Boolean lenientHostAuth) {
        this.lenientHostAuth = lenientHostAuth;
    }

    public Boolean getCookieManagement() {
        return cookieManagement;
    }

    public void setCookieManagement(Boolean cookieManagement) {
        this.cookieManagement = cookieManagement;
    }

    public String getSelectedInstalledCertificate() {
        return selectedInstalledCertificate;
    }

    public void setSelectedInstalledCertificate(String selectedInstalledCertificate) {
        this.selectedInstalledCertificate = selectedInstalledCertificate;
    }

    public List<String> getInstalledCertificatesList() {
        return installedCertificatesList;
    }

    public void setInstalledCertificatesList(List<String> installedCertificatesList) {
        this.installedCertificatesList = installedCertificatesList;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
