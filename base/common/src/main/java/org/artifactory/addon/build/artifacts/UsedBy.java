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

import org.artifactory.build.BuildRun;
/**
 * @author Chen Keinan
 */
public class UsedBy {

    private String name;
    private String number;
    private String ciUrl;
    private String releaseStatus;
    private String moduleID;
    private String scope;
    private String started;

    public UsedBy(BuildRun dependencyBuild,String moduleID,String scope) {
        if (dependencyBuild != null){
            this.name = dependencyBuild.getName();
            this.number = dependencyBuild.getNumber();
             this.ciUrl = dependencyBuild.getCiUrl();
            this.releaseStatus = dependencyBuild.getReleaseStatus();
            this.moduleID = moduleID;
            this.scope = scope;
            this.started= new Long(dependencyBuild.getStartedDate().getTime()).toString();
        }
    }

    public UsedBy() {

    }


    public String getStarted() {
        return started;
    }

    public void setStarted(String started) {
        this.started = started;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
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

    public String getModuleID() {
        return moduleID;
    }

    public void setModuleID(String moduleID) {
        this.moduleID = moduleID;
    }
}
