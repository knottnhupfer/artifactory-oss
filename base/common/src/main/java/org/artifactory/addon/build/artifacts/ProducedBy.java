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

package org.artifactory.addon.build.artifacts;

import org.artifactory.api.context.ContextHelper;
import org.artifactory.build.BuildRun;

/**
 * @author Chen Keinan
 */
public class ProducedBy {

    private String name;
    private String number;
    private long started;
    private String startedString;
    private String ciUrl;
    private String releaseStatus;
    private String moduleID;

    public ProducedBy(BuildRun artifactBuild, String module) {
        if (artifactBuild != null) {
            this.name = artifactBuild.getName();
            this.number = artifactBuild.getNumber();
            this.started = artifactBuild.getStartedDate().getTime();
            this.startedString = ContextHelper.get().getCentralConfig().format(this.started);
            this.ciUrl = artifactBuild.getCiUrl();
            this.releaseStatus = artifactBuild.getReleaseStatus();
            this.moduleID = module;
        }
    }

    public ProducedBy() {
    }

    public String getModuleID() {
        return moduleID;
    }

    public void setModuleID(String module) {
        this.moduleID = module;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public long getStarted() {
        return started;
    }

    public void setStarted(long started) {
        this.started = started;
    }

    public String getStartedString() {
        return startedString;
    }

    public void setStartedString(String startedString) {
        this.startedString = startedString;
    }

    public String getCiUrl() {
        return ciUrl;
    }

    public void setCiUrl(String ciUrl) {
        this.ciUrl = ciUrl;
    }

    public String getReleaseStatus() {
        return releaseStatus;
    }

    public void setReleaseStatus(String releaseStatus) {
        this.releaseStatus = releaseStatus;
    }
}
