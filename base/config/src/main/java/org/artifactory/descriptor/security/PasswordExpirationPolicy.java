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

import lombok.Data;
import org.artifactory.descriptor.Descriptor;
import org.jfrog.common.config.diff.GenerateDiffFunction;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Optional;

/**
 * Password expiration configuration
 *
 * @author Michael Pasternak
 */
@XmlType(name = "PasswordExpirationPolicyType",
        propOrder = {"enabled", "passwordMaxAge", "notifyByEmail", "currentPasswordValidFor"},
        namespace = Descriptor.NS
)
@GenerateDiffFunction
@Data
public class PasswordExpirationPolicy implements Descriptor {

    @XmlElement(defaultValue = "false", required = false)
    private Boolean enabled = false;

    /**
     * number of days for password to get expired (general password live time)
     */
    @XmlElement(defaultValue = "60", required = false)
    private Integer passwordMaxAge = 60;

    @XmlElement(defaultValue = "true", required = false)
    private Boolean notifyByEmail = true;

    /**
     * number of days till password should be changed
     */
    @XmlElement(required = false)
    private Integer currentPasswordValidFor;

    public void setNotifyByEmail(Boolean notifyByEmail) {
        this.notifyByEmail = Optional.ofNullable(notifyByEmail).orElse(true);
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = Optional.ofNullable(enabled).orElse(false);
    }

    public void setPasswordMaxAge(Integer passwordMaxAge) {
        this.passwordMaxAge = Optional.ofNullable(passwordMaxAge).orElse(60);
    }
}
