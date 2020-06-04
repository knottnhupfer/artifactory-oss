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

import java.util.List;

/**
 * @author Chen Keinan
 */
public class KeyStore extends BaseModel {

    private Boolean isKeyValid;
    private String password;
    private String alias;
    private List<String> availableAliases;
    private String keyPairName;
    private String fileName;
    private String privateKeyPassword;
    private boolean keyStoreExist;
    private List<String> keyStorePairNames;

    public KeyStore() {
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public KeyStore(Boolean isKeyValid, List<String> availableAliases, String fileName, String password) {
        this.isKeyValid = isKeyValid;
        this.availableAliases = availableAliases;
        this.fileName = fileName;
        this.password = password;
    }

    public Boolean isKeyValid() {
        return isKeyValid;
    }

    public void setIsKeyValid(Boolean isKeyValid) {
        this.isKeyValid = isKeyValid;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getKeyPairName() {
        return keyPairName;
    }

    public void setKeyPairName(String keyPairName) {
        this.keyPairName = keyPairName;
    }


    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getPrivateKeyPassword() {
        return privateKeyPassword;
    }

    public void setPrivateKeyPassword(String privateKeyPassword) {
        this.privateKeyPassword = privateKeyPassword;
    }

    public boolean isKeyStoreExist() {
        return keyStoreExist;
    }

    public void setKeyStoreExist(boolean keyStoreExist) {
        this.keyStoreExist = keyStoreExist;
    }

    public List<String> getKeyStorePairNames() {
        return keyStorePairNames;
    }

    public void setKeyStorePairNames(List<String> keyStorePairNames) {
        this.keyStorePairNames = keyStorePairNames;
    }

    public List<String> getAvailableAliases() {
        return availableAliases;
    }

    public void setAvailableAliases(List<String> availableAliases) {
        this.availableAliases = availableAliases;
    }
}
