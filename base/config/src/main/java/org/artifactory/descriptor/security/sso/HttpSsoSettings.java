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

package org.artifactory.descriptor.security.sso;

import lombok.Data;
import org.artifactory.descriptor.Descriptor;
import org.jfrog.common.config.diff.GenerateDiffFunction;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * The HTTP SSO related settings
 *
 * @author Noam Y. Tenne
 */
@XmlType(name = "HttpSsoSettingsType",
        propOrder = {"httpSsoProxied", "noAutoUserCreation", "allowUserToAccessProfile", "remoteUserRequestVariable", "syncLdapGroups"}, namespace = Descriptor.NS)
@Data
@GenerateDiffFunction
public class HttpSsoSettings implements Descriptor {

    @XmlElement(defaultValue = "false")
    private boolean httpSsoProxied = false;

    @XmlElement(defaultValue = "false")
    private boolean noAutoUserCreation = false;

    @XmlElement(defaultValue = "false")
    private boolean allowUserToAccessProfile = false;

    @XmlElement(defaultValue = "REMOTE_USER")
    private String remoteUserRequestVariable = "REMOTE_USER";

    @XmlElement(defaultValue = "false")
    private boolean syncLdapGroups = false;
}