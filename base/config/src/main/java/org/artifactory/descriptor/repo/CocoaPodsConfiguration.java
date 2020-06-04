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

import org.artifactory.descriptor.Descriptor;
import org.jfrog.common.config.diff.GenerateDiffFunction;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Dan Feldman
 */
@XmlType(name = "CocoaPodsConfigurationType", propOrder = {"cocoaPodsSpecsRepoUrl", "specRepoProvider"},
        namespace = Descriptor.NS)
@GenerateDiffFunction
public class CocoaPodsConfiguration implements Descriptor {

    @XmlElement(defaultValue = "https://github.com/CocoaPods/Specs", required = false)
    private String cocoaPodsSpecsRepoUrl = "https://github.com/CocoaPods/Specs";

    @XmlElement(name = "specRepoProvider")
    private VcsGitConfiguration specRepoProvider = new VcsGitConfiguration();

    public String getCocoaPodsSpecsRepoUrl() {
        return cocoaPodsSpecsRepoUrl;
    }

    public void setCocoaPodsSpecsRepoUrl(String cocoaPodsSpecsRepoUrl) {
        this.cocoaPodsSpecsRepoUrl = cocoaPodsSpecsRepoUrl;
    }

    public VcsGitConfiguration getSpecRepoProvider() {
        return specRepoProvider;
    }

    public void setSpecRepoProvider(VcsGitConfiguration specRepoProvider) {
        this.specRepoProvider = specRepoProvider;
    }
}
