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

package org.artifactory.descriptor.security.signingkeys;

import org.artifactory.descriptor.Descriptor;
import org.jfrog.common.config.diff.GenerateDiffFunction;

import javax.xml.bind.annotation.XmlType;

/**
 * @author Gidi Shabat
 */
@XmlType(name = "SigningKeysSettingsType",
        propOrder = {"passphrase","keyStorePassword"},
        namespace = Descriptor.NS)
@GenerateDiffFunction
public class SigningKeysSettings implements Descriptor {

    private String passphrase;

    private String keyStorePassword;

    public String getPassphrase() {
        return passphrase;
    }

    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SigningKeysSettings that = (SigningKeysSettings) o;

        if (passphrase != null ? !passphrase.equals(that.passphrase) : that.passphrase != null) {
            return false;
        }
        return (keyStorePassword != null ? keyStorePassword.equals(that.keyStorePassword) : that.keyStorePassword == null);
    }

    @Override
    public int hashCode() {
        int result = passphrase != null ? passphrase.hashCode() : 0;
        result = 31 * result + (keyStorePassword != null ? keyStorePassword.hashCode() : 0);
        return result;
    }
}
