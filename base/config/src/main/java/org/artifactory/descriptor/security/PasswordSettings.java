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

package org.artifactory.descriptor.security;

import org.artifactory.descriptor.Descriptor;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.jfrog.common.config.diff.GenerateDiffFunction;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Optional;

/**
 * The password policy related settings.
 *
 * @author Yossi Shaul
 */
@XmlType(name = "PasswordSettingsType", namespace = Descriptor.NS)
@JsonIgnoreProperties(value = {"encryptionEnabled","encryptionRequired"})
@GenerateDiffFunction
public class PasswordSettings implements Descriptor {

    @XmlElement(defaultValue = "supported", required = false)
    private EncryptionPolicy encryptionPolicy = EncryptionPolicy.SUPPORTED;

    @XmlElement(name = "expirationPolicy", required = false)
    private PasswordExpirationPolicy expirationPolicy = new PasswordExpirationPolicy();

    @XmlElement(name = "resetPolicy", required = false)
    private PasswordResetPolicy resetPolicy = new PasswordResetPolicy();


    public EncryptionPolicy getEncryptionPolicy() {
        return encryptionPolicy;
    }

    public void setEncryptionPolicy(EncryptionPolicy encryptionPolicy) {
        this.encryptionPolicy = encryptionPolicy;
    }

    /**
     * @return True if encryption is required.
     */
    public boolean isEncryptionRequired() {
        return EncryptionPolicy.REQUIRED.equals(encryptionPolicy);
    }

    /**
     * @return True if encryption is supported\required. False if not.
     */
    public boolean isEncryptionEnabled() {
        return (EncryptionPolicy.SUPPORTED.equals(encryptionPolicy) ||
                EncryptionPolicy.REQUIRED.equals(encryptionPolicy));
    }

    public PasswordExpirationPolicy getExpirationPolicy() {
        return expirationPolicy;
    }

    public void setExpirationPolicy(PasswordExpirationPolicy expirationPolicy) {
        this.expirationPolicy = Optional.ofNullable(expirationPolicy).orElseGet(PasswordExpirationPolicy::new);
    }

    public PasswordResetPolicy getResetPolicy() {
        return resetPolicy;
    }

    public void setResetPolicy(PasswordResetPolicy resetPolicy) {
        this.resetPolicy = Optional.ofNullable(resetPolicy).orElseGet(PasswordResetPolicy::new);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PasswordSettings that = (PasswordSettings) o;

        if (encryptionPolicy != that.encryptionPolicy) return false;
        if (expirationPolicy != null ? !expirationPolicy.equals(that.expirationPolicy) : that.expirationPolicy != null)
            return false;
        return resetPolicy != null ? resetPolicy.equals(that.resetPolicy) : that.resetPolicy == null;

    }

    @Override
    public int hashCode() {
        int result = encryptionPolicy != null ? encryptionPolicy.hashCode() : 0;
        result = 31 * result + (expirationPolicy != null ? expirationPolicy.hashCode() : 0);
        result = 31 * result + (resetPolicy != null ? resetPolicy.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("encryptionPolicy: ");
        sb.append(encryptionPolicy);
        sb.append("expirationPolicy: ");
        sb.append(expirationPolicy);
        sb.append("resetPolicy: ");
        sb.append(resetPolicy);
        return sb.toString();
    }
}