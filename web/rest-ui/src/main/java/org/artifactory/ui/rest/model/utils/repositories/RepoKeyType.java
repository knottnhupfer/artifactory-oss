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

package org.artifactory.ui.rest.model.utils.repositories;

import org.artifactory.descriptor.repo.DockerApiVersion;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.repo.RepoDetailsType;
import org.artifactory.rest.common.model.BaseModel;

/**
 * @author Chen keinan
 */
public class RepoKeyType extends BaseModel {

    private String repoKey;
    private RepoType RepoType;
    private String layoutPattern;
    private String layoutFolderItegRevRegex;
    private String layoutFileItegRevRegex;
    private String dockerApiVersion;
    private String type;
    private Boolean canDeploy;
    private Boolean canRead;
    private Boolean isLocal;
    private Boolean isRemote;
    private Boolean isVirtual;
    private Boolean isDistribution;
    private Boolean isDefaultLocalConfigured;

    public RepoKeyType() {

    }

    public RepoKeyType(String type, String repoKey) {
        this.repoKey = repoKey;
        setRepoType(type);
    }

    public RepoKeyType(RepoType repoType, String repoKey) {
        this.repoKey = repoKey;
        this.RepoType = repoType;
    }

    public RepoKeyType(String type, RepoType repoType, String repoKey) {
        setRepoType(type);
        this.repoKey = repoKey;
        this.RepoType = repoType;
    }

    public RepoKeyType(String type, RepoType repoType, String repoKey, DockerApiVersion dockerApiVersion) {
        setRepoType(type);
        this.repoKey = repoKey;
        this.RepoType = repoType;
        this.dockerApiVersion = dockerApiVersion.name();
    }

    private void setRepoType(String type) {
        this.type = type.toLowerCase();
        switch (type) {
            case RepoDetailsType.LOCAL_REPO:
                isLocal = true;
                isRemote = false;
                isVirtual = false;
                isDistribution = false;
                break;
            case RepoDetailsType.REMOTE_REPO:
                isLocal = false;
                isRemote = true;
                isVirtual = false;
                isDistribution = false;
                break;
            case RepoDetailsType.VIRTUAL_REPO:
                isLocal = false;
                isRemote = false;
                isVirtual = true;
                isDistribution = false;
                break;
            case RepoDetailsType.DISTRIBUTION_REPO:
                isLocal = false;
                isRemote = false;
                isVirtual = false;
                isDistribution = true;
                break;
            default:
                //nada
        }
    }

    public String getRepoKey() {
        return repoKey;
    }

    public void setRepoKey(String repoKey) {
        this.repoKey = repoKey;
    }

    public org.artifactory.descriptor.repo.RepoType getRepoType() {
        return RepoType;
    }

    public void setRepoType(org.artifactory.descriptor.repo.RepoType repoType) {
        RepoType = repoType;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getCanDeploy() {
        return canDeploy;
    }

    public void setCanDeploy(Boolean canDeploy) {
        this.canDeploy = canDeploy;
    }

    public Boolean getCanRead() {
        return canRead;
    }

    public void setCanRead(Boolean canRead) {
        this.canRead = canRead;
    }

    public Boolean getIsLocal() {
        return isLocal;
    }

    public void setIsLocal(Boolean isLocal) {
        this.isLocal = isLocal;
    }

    public Boolean getIsRemote() {
        return isRemote;
    }

    public void setIsRemote(Boolean isRemote) {
        this.isRemote = isRemote;
    }

    public Boolean getIsVirtual() {
        return isVirtual;
    }

    public void setIsVirtual(Boolean isVirtual) {
        this.isVirtual = isVirtual;
    }

    public Boolean isDistribution() {
        return isDistribution;
    }

    public void setDistribution(Boolean distribution) {
        isDistribution = distribution;
    }

    public String getDockerApiVersion() {
        return dockerApiVersion;
    }

    public Boolean getIsDefaultDeploymentConfigured() {
        return isDefaultLocalConfigured;
    }

    public void setIsDefaultDeploymentConfigured(Boolean isDefaultLocalConfigured) {
        this.isDefaultLocalConfigured = isDefaultLocalConfigured;
    }

    public String getLayoutPattern() {
        return layoutPattern;
    }

    public void setLayoutPattern(String layoutPattern) {
        this.layoutPattern = layoutPattern;
    }

    public String getLayoutFileItegRevRegex() {
        return layoutFileItegRevRegex;
    }

    public void setLayoutFileItegRevRegex(String layoutFileItegRevRegex) {
        this.layoutFileItegRevRegex = layoutFileItegRevRegex;
    }

    public String getLayoutFolderItegRevRegex() {
        return layoutFolderItegRevRegex;
    }

    public void setLayoutFolderItegRevRegex(String layoutFolderItegRevRegex) {
        this.layoutFolderItegRevRegex = layoutFolderItegRevRegex;
    }

}
