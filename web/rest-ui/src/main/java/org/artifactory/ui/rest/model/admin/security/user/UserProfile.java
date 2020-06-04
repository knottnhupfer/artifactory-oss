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

package org.artifactory.ui.rest.model.admin.security.user;

import org.artifactory.rest.common.model.BaseModel;
import org.artifactory.ui.rest.model.admin.configuration.bintray.BintrayUIModel;
import org.artifactory.ui.rest.model.admin.configuration.ssh.SshClientUIModel;

/**
 * @author Chen Keinan
 */
public class UserProfile extends BaseModel {

    private User user;
    private BintrayUIModel bintray;
    private SshClientUIModel ssh;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public BintrayUIModel getBintray() {
        return bintray;
    }

    public void setBintray(BintrayUIModel bintray) {
        this.bintray = bintray;
    }

    public SshClientUIModel getSsh() {
        return ssh;
    }

    public void setSsh(SshClientUIModel ssh) {
        this.ssh = ssh;
    }
}
