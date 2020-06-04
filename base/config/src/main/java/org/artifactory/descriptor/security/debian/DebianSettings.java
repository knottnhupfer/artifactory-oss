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

package org.artifactory.descriptor.security.debian;

import org.artifactory.descriptor.Descriptor;

import javax.xml.bind.annotation.XmlType;

/**
 * @author Gidi Shabat
 */
@XmlType(name = "DebianSettingsType",
        propOrder = {"passphrase"},
        namespace = Descriptor.NS)
public class DebianSettings implements Descriptor {

    private String passphrase;

    public String getPassphrase() {
        return passphrase;
    }

    public void setPassphrase(String test) {
        this.passphrase = test;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DebianSettings that = (DebianSettings) o;

        return passphrase != null ? passphrase.equals(that.passphrase) : that.passphrase == null;
    }

    @Override
    public int hashCode() {
        int result = passphrase != null ? passphrase.hashCode() : 0;
        return result;
    }
}
