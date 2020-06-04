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

package org.artifactory.descriptor.repo;

import org.jfrog.common.config.diff.GenerateDiffFunction;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

/**
 * @author Shay Yaakov
 */
@XmlEnum(String.class)
@GenerateDiffFunction
public enum ReverseProxyMethod {

    @XmlEnumValue("subDomain")SUBDOMAIN("subDomain"),
    @XmlEnumValue("path")REPOPATHPREFIX("path"),
    @XmlEnumValue("portPerRepo")PORTPERREPO("portPerRepo"),
    @XmlEnumValue("noValue")NOVALUE("noValue");

    private final String val;

    ReverseProxyMethod(String val) {
        this.val = val;
    }

    public String toString() {
        return val;
    }

    // NOVALUE - was left for migraion.
    public boolean isRepoPathPrefix(){ return this.equals(REPOPATHPREFIX);}
    static public ReverseProxyMethod defaultValue(){return SUBDOMAIN;}

    public String getVal() {
        return val;
    }
}