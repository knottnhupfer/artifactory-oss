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

package org.artifactory.repo.reverseProxy;

import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.ReverseProxyDescriptor;
import org.artifactory.descriptor.repo.ReverseProxyRepoConfig;
import org.artifactory.storage.db.servers.model.ArtifactoryServer;
import org.artifactory.util.HttpUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author Chen Keinan
 */
@Component
public class NginxTemplateProvider extends ReverseProxyTemplateProvider {

    /**
     * return reverse proxy snippet
     *
     * @param repoDescriptor         - repo descriptor
     * @param reverseProxyRepoConfig - reverse proxy descriptor
     * @param generalOnly            - is general data
     * @return snippet
     */
    protected String getReverseProxySnippet(ReverseProxyDescriptor reverseProxy, RepoDescriptor repoDescriptor,
            ReverseProxyRepoConfig reverseProxyRepoConfig,
            boolean generalOnly) {
        if (isGeneralSslAndDockerPortAreTheSame(reverseProxy, reverseProxyRepoConfig)) {
            return "";
        }
        return buildDockerTemplate(reverseProxy, repoDescriptor, reverseProxyRepoConfig, generalOnly, "/templates/nginx.ftl");
    }

    /**
     * generate reverse proxy snippet
     *
     * @param reverseProxyDescriptor - reverse proxy descriptor
     * @return reverse proxy snippet
     */
    protected String getGeneralReverseProxySnippet(ReverseProxyDescriptor reverseProxyDescriptor,
            List<String> repoKeys) {
        return buildGeneralTemplate(reverseProxyDescriptor, "/templates/nginx.ftl", repoKeys, new ReverseProxyPorts(), true);
    }


    @Override
    protected String getServerBalancerKey(ArtifactoryServer server) {
        return HttpUtils.getServerAndPortFromContext(server.getContextUrl());
    }

    @Override
    public boolean addHaApacheForDocker(Map<Object, Object> params) {
        params.put("addHa", false);
        return false;
    }

    @Override
    protected boolean isNginx() {
        return true;
    }

}
