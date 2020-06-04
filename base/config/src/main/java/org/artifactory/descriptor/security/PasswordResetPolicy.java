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
import org.jfrog.common.config.diff.GenerateDiffFunction;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Password reset protection policy configuration
 *
 * @author Yinon Avraham
 */
@XmlType(name = "PasswordResetPolicyType",
        propOrder = {"enabled", "maxAttemptsPerAddress", "timeToBlockInMinutes"},
        namespace = Descriptor.NS
)
@GenerateDiffFunction
public class PasswordResetPolicy implements Descriptor {

    @XmlElement(defaultValue = "true", required = false)
    private boolean enabled = true;

    @XmlElement(defaultValue = "3", required = false)
    private int maxAttemptsPerAddress = 3;

    @XmlElement(defaultValue = "60", required = false)
    private int timeToBlockInMinutes = 60;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getMaxAttemptsPerAddress() {
        return maxAttemptsPerAddress;
    }

    public void setMaxAttemptsPerAddress(int maxAttemptsPerAddress) {
        this.maxAttemptsPerAddress = maxAttemptsPerAddress;
    }

    public int getTimeToBlockInMinutes() {
        return timeToBlockInMinutes;
    }

    public void setTimeToBlockInMinutes(int timeToBlockInMinutes) {
        this.timeToBlockInMinutes = timeToBlockInMinutes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PasswordResetPolicy policy = (PasswordResetPolicy) o;

        if (enabled != policy.enabled) return false;
        if (maxAttemptsPerAddress != policy.maxAttemptsPerAddress) return false;
        return timeToBlockInMinutes == policy.timeToBlockInMinutes;

    }

    @Override
    public int hashCode() {
        int result = (enabled ? 1 : 0);
        result = 31 * result + maxAttemptsPerAddress;
        result = 31 * result + timeToBlockInMinutes;
        return result;
    }
}
