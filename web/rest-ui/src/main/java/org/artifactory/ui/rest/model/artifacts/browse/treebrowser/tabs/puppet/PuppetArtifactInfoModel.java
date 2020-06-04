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

package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.puppet;

import org.artifactory.addon.puppet.PuppetInfo;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.BaseArtifactInfo;

import java.util.List;

/**
 * @author jainishshah
 */
public class PuppetArtifactInfoModel extends BaseArtifactInfo {

    private PuppetInfo puppetInfo;
    private List<PuppetKeywordInfoModel> puppetKeywords;
    private List<PuppetDependencyInfoModel> puppetDependencies;

    public PuppetInfo getPuppetInfo() {
        return puppetInfo;
    }

    public void setPuppetInfo(PuppetInfo puppetInfo) {
        this.puppetInfo = puppetInfo;
    }

    public List<PuppetKeywordInfoModel> getPuppetKeywords() {
        return puppetKeywords;
    }

    public void setPuppetKeywords(List<PuppetKeywordInfoModel> puppetKeywords) {
        this.puppetKeywords = puppetKeywords;
    }

    public List<PuppetDependencyInfoModel> getPuppetDependencies() {
        return puppetDependencies;
    }

    public void setPuppetDependencies(List<PuppetDependencyInfoModel> puppetDependencies) {
        this.puppetDependencies = puppetDependencies;
    }
}
