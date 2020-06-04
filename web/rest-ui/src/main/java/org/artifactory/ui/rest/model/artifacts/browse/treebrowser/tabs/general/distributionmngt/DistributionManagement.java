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

package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.distributionmngt;

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.repo.*;
import org.artifactory.rest.common.model.BaseModel;
import org.artifactory.util.HttpUtils;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Chen Keinan
 */
@JsonIgnoreProperties({"repoKey", "path","httpServletRequest","firstIndention","secondIndention","isCache"})
public class DistributionManagement extends BaseModel {

    private String firstIndention = "";
    private String secondIndention = "    ";
    private boolean isCache = false;
    private String distributedManagement;


    public String getDistributedManagement() {
        return distributedManagement;
    }

    public void setDistributedManagement(String distributedManagement) {
        this.distributedManagement = distributedManagement;
    }


    public StringBuilder populateDistributionManagement(RepoDescriptor repo, CentralConfigService cc, HttpServletRequest httpServletRequest) {
         final StringBuilder sb = new StringBuilder();
        sb.delete(0, sb.length());
         String id = cc.getServerName();
        String repoUrl = buildRepoUrl(repo,httpServletRequest);
        isCache = repo instanceof LocalRepoDescriptor && ((LocalRepoDescriptor)repo).isCache();

        boolean handleReleases = false, handleSnapshots = false;
        if (repo instanceof VirtualRepoDescriptor) {
            VirtualRepoDescriptor virtualRepo = (VirtualRepoDescriptor)repo;
            LocalRepoDescriptor defaultLocal = virtualRepo.getDefaultDeploymentRepo();
            if (defaultLocal != null) {
                handleReleases = defaultLocal.isHandleReleases();
                handleSnapshots = defaultLocal.isHandleSnapshots();
            }
        }
        else {
            LocalRepoDescriptor localRepo = (LocalRepoDescriptor)repo;
            handleReleases = localRepo.isHandleReleases();
            handleSnapshots = localRepo.isHandleSnapshots();
        }

        setIndentions(isCache);
        if (!isCache) {
            sb.append("<distributionManagement>\n");
        }
        if (handleReleases) {
            sb.append(firstIndention);
            sb.append("<repository>\n");
            sb.append(secondIndention);
            sb.append("<id>");
            sb.append("central");
            sb.append("</id>\n");
            sb.append(secondIndention);
            sb.append("<name>");
            sb.append(id);
            sb.append("-releases</name>\n");
            sb.append(secondIndention);
            sb.append("<url>");
            sb.append(repoUrl);
            sb.append("</url>\n");
            sb.append(firstIndention);
            sb.append("</repository>\n");
        }

        if (handleSnapshots) {
            sb.append(firstIndention);
            sb.append("<snapshotRepository>\n");
            sb.append(secondIndention);
            sb.append("<id>");
            sb.append("snapshots");
            sb.append("</id>\n");
            sb.append(secondIndention);
            sb.append("<name>");
            sb.append(id);
            sb.append("-snapshots</name>\n");
            sb.append(secondIndention);
            sb.append("<url>");
            sb.append(repoUrl);
            sb.append("</url>\n");
            sb.append(firstIndention);
            sb.append("</snapshotRepository>\n");
        }
        if (!isCache) {
            sb.append("</distributionManagement>");
        }
        return sb;
    }

    private String buildRepoUrl(RepoDescriptor repo,HttpServletRequest httpServletRequest) {
        String servletContextUrl = HttpUtils.getServletContextUrl(httpServletRequest);
        if (!servletContextUrl.endsWith("/")) {
            servletContextUrl += "/";
        }
        StringBuilder sb = new StringBuilder();
        if (repo instanceof LocalCacheRepoDescriptor) {
            RemoteRepoDescriptor remoteRepoDescriptor = ((LocalCacheRepoDescriptor) repo).getRemoteRepo();
            if (remoteRepoDescriptor != null) {
                sb.append(servletContextUrl).append(remoteRepoDescriptor.getKey());
            } else {
                String fixedKey = StringUtils.remove(repo.getKey(), "-cache");
                sb.append(servletContextUrl).append(fixedKey);
            }
        } else {
            sb.append(servletContextUrl).append(repo.getKey());
        }
        return sb.toString();
    }
    private void setIndentions(boolean isCache) {
        if (!isCache) {
            firstIndention += "    ";
            secondIndention += "    ";
        }
    }
}
