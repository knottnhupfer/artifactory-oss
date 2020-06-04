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

package org.artifactory.rest.services;

import org.artifactory.rest.common.service.admin.reverseProxies.CreateReverseProxyService;
import org.artifactory.rest.common.service.admin.reverseProxies.GetReverseProxiesService;
import org.artifactory.rest.common.service.admin.reverseProxies.ReverseProxySnippetService;
import org.artifactory.rest.common.service.admin.reverseProxies.UpdateReverseProxyService;
import org.artifactory.rest.common.service.admin.userprofile.*;
import org.artifactory.rest.common.service.admin.xray.*;
import org.artifactory.rest.common.service.artifact.AddSha256ToArtifactService;
import org.artifactory.rest.services.replication.*;
import org.artifactory.rest.services.storage.CalculateReposStorageSummaryService;
import org.artifactory.rest.services.storage.GetStorageSummaryService;
import org.springframework.beans.factory.annotation.Lookup;

/**
 * @author Chen Keinan
 */
public abstract class ConfigServiceFactory {

    // get replication services
    @Lookup
    public abstract GetReplicationService getReplication();

    @Lookup
    public abstract GetAllReplicationsService getAllReplications();

    @Lookup
    public abstract CreateReplicationService createOrReplaceReplication();

    @Lookup
    public abstract CreateMultipleReplicationService createMultipleReplication();

    @Lookup
    public abstract UpdateReplicationService updateReplication();

    @Lookup
    public abstract UpdateMultipleReplicationsService updateMultipleReplications();

    @Lookup
    public abstract DeleteReplicationsService deleteReplicationsService();

    @Lookup
    public abstract EnableDisableReplicationsService enableDisableReplicationsService();

    @Lookup
    public abstract GetStorageSummaryService getStorageSummaryService();

    @Lookup
    public abstract CalculateReposStorageSummaryService calculateStorageSummaryService();

    @Lookup
    public abstract CreateXrayConfigService createXrayConfig();

    @Lookup
    public abstract UpdateXrayConfigService updateXrayConfig();

    @Lookup
    public abstract DeleteXrayConfigService deleteXrayConfig();

    @Lookup
    public abstract GetXrayConfiguredReposService getXrayConfiguredRepos();

    @Lookup
    public abstract GetIndexXrayService getXrayIndexedRepo();

    @Lookup
    public abstract GetNoneIndexXrayService getNoneXrayIndexedRepo();

    @Lookup
    public abstract UpdateXrayIndexRepos updateXrayIndexRepos();

    @Lookup
    public abstract GetApiKeyService getApiKey();

    @Lookup
    public abstract CreateApiKeyService createApiKey();

    @Lookup
    public abstract RevokeApiKeyService revokeApiKey();

    @Lookup
    public abstract UpdateApiKeyService regenerateApiKey();

    @Lookup
    public abstract GetUsersAndApiKeys getUsersAndApiKeys();

    @Lookup
    public abstract SyncUsersAndApiKeys syncUsersAndApiKeys();

    @Lookup
    public abstract AddSha256ToArtifactService addSha256ToArtifact();

    @Lookup
    public abstract CreateReverseProxyService createReverseProxy();

    @Lookup
    public abstract UpdateReverseProxyService updateReverseProxy();

    @Lookup
    public abstract GetReverseProxiesService getReverseProxies();

    @Lookup
    public abstract ReverseProxySnippetService getReverseProxySnippet();

}
