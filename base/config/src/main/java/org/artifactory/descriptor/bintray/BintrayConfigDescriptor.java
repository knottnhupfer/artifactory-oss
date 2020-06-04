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

package org.artifactory.descriptor.bintray;

import org.apache.commons.lang.StringUtils;
import org.artifactory.descriptor.Descriptor;
import org.jfrog.common.config.diff.GenerateDiffFunction;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * The Bintray global(default) user descriptor
 *
 * @author Dan Feldman
 */
@XmlType(name = "BintrayConfigType", propOrder = {"userName", "apiKey", "fileUploadLimit"}, namespace = Descriptor.NS)
@GenerateDiffFunction
public class BintrayConfigDescriptor implements Descriptor {

    @XmlElement(required = true)
    private String userName;
    @XmlElement(required = true)
    private String apiKey;
    @XmlElement(required = true, defaultValue = "200")
    private int fileUploadLimit = 200;


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getBintrayAuth() {
        if (StringUtils.isNotBlank(userName) && StringUtils.isNotBlank(apiKey)) {
            return userName + ":" + apiKey;
        }
        return null;
    }

    public int getFileUploadLimit() {
        return fileUploadLimit;
    }

    public void setFileUploadLimit(int fileUploadLimit) {
        this.fileUploadLimit = fileUploadLimit;
    }

    public boolean globalCredentialsExist() {
        return (StringUtils.isNotEmpty(userName)) && (StringUtils.isNotEmpty(apiKey));
    }
}
