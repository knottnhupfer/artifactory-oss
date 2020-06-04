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

package org.artifactory.ui.rest.model.admin.security.signingkey;

import org.artifactory.rest.common.model.BaseModel;

import java.io.Serializable;

/**
 * @author Chen Keinan
 */
public class SignKey extends BaseModel implements Serializable {

    private String publicKeyLink;
    private String passPhrase;
    boolean publicKeyInstalled;
    boolean privateKeyInstalled;

    public SignKey() {
    }

    public SignKey(String publicKeyDownloadTarget) {
        this.publicKeyLink = publicKeyDownloadTarget;
    }

    public String getPublicKeyLink() {
        return publicKeyLink;
    }

    public void setPublicKeyLink(String publicKeyLink) {
        this.publicKeyLink = publicKeyLink;
    }

    public String getPassPhrase() {
        return passPhrase;
    }

    public void setPassPhrase(String passPhrase) {
        this.passPhrase = passPhrase;
    }

    public boolean isPublicKeyInstalled() {
        return publicKeyInstalled;
    }

    public void setPublicKeyInstalled(boolean publicKeyInstalled) {
        this.publicKeyInstalled = publicKeyInstalled;
    }

    public boolean isPrivateKeyInstalled() {
        return privateKeyInstalled;
    }

    public void setPrivateKeyInstalled(boolean privateKeyInstalled) {
        this.privateKeyInstalled = privateKeyInstalled;
    }
}
