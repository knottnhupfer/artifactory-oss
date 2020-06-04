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

package org.artifactory.rest.common.service.admin.reverseProxies;

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.repo.ReverseProxyDescriptor;
import org.artifactory.descriptor.repo.ReverseProxyMethod;
import org.artifactory.descriptor.repo.ReverseProxyRepoConfig;
import org.artifactory.descriptor.repo.WebServerType;
import org.artifactory.rest.common.model.reverseproxy.ReverseProxyDescriptorModel;
import org.artifactory.rest.common.model.reverseproxy.ReverseProxyRepoConfigs;
import org.artifactory.rest.common.model.reverseproxy.ReverseProxyRepositories;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.rest.common.util.AolUtils;
import org.artifactory.util.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Shay Yaakov
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetReverseProxiesService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(GetReverseProxiesService.class);

    @Autowired
    private CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        AolUtils.assertNotAol("GetReverseProxies");
        fetchSingleOrMultiReverseProxy(response, request);
    }

    private void fetchSingleOrMultiReverseProxy(RestResponse artifactoryResponse,
            ArtifactoryRestRequest artifactoryRequest) {
        String proxyKey = artifactoryRequest.getPathParamByKey("id");
        if (StringUtils.isEmpty(proxyKey)) {
            proxyKey = "dummy";
        }
        if (StringUtils.isEmpty(proxyKey)) {
            buildBadRequestResponse(artifactoryResponse);
            return;
        }
        if (!(proxyKey.equals(WebServerType.NGINX.toString()) ||
                proxyKey.equals(WebServerType.APACHE.toString()) || proxyKey.equals("dummy"))) {
            artifactoryResponse.responseCode(HttpServletResponse.SC_NOT_FOUND);
            artifactoryResponse.error("Reverse proxy id is missing");
            return;
        }

        URI baseUri = null;
        try {
            baseUri = new URI(HttpUtils.getServletContextUrl(artifactoryRequest.getServletRequest()));
        } catch (URISyntaxException e) {
            // can't set defaults.
            log.error("Can't have base url", e);
        }
        if (isMultiProxy(proxyKey)) {
            updateResponseWithMultiProxyInfo(artifactoryResponse, baseUri);
        } else {
            updateResponseWithSingleProxyInfo(artifactoryResponse, proxyKey, baseUri);
        }
    }

    /**
     * build bad request response
     *
     * @param artifactoryResponse - artifactory response
     */
    private void buildBadRequestResponse(RestResponse artifactoryResponse) {
        artifactoryResponse.responseCode(HttpServletResponse.SC_BAD_REQUEST);
        artifactoryResponse.error("Reverse proxy id is missing");
    }

    /**
     * build response with multi proxy info
     *
     * @param artifactoryResponse
     * @param baseUrl
     */
    private void updateResponseWithMultiProxyInfo(RestResponse artifactoryResponse, URI baseUrl) {
        List<ReverseProxyDescriptor> reverseProxies = centralConfigService.getMutableDescriptor().getReverseProxies();
        reverseProxies.stream().forEach( it -> updateReverseProxyWithURI(it, baseUrl));
        artifactoryResponse.iModelList(reverseProxies);
    }

    /**
     * update response with single proxy response
     *  @param artifactoryResponse - artifactory response
     * @param proxyKey
     * @param defaultBase
     */
    private void updateResponseWithSingleProxyInfo(RestResponse artifactoryResponse, String proxyKey, URI defaultBase) {
        ReverseProxyDescriptor reverseProxy = centralConfigService.getMutableDescriptor().getCurrentReverseProxy();
        if (reverseProxy != null) {
            if (!((reverseProxy.getKey().equals(proxyKey)) || (proxyKey.equals("dummy")))) {
                artifactoryResponse.responseCode(HttpServletResponse.SC_NOT_FOUND);
                artifactoryResponse.error("Reverse proxy with id:" + proxyKey + " not found");
                return;
            }
        }
        ReverseProxyDescriptorModel reverseProxyDescriptorModel = descriptorToModel(reverseProxy);
        updateReverseProxyWithURI(reverseProxyDescriptorModel, defaultBase);
        artifactoryResponse.iModel(reverseProxyDescriptorModel);
    }

    private static void updateReverseProxyWithURI(ReverseProxyDescriptorModel reverseProxy, URI defaultBase) {
        if (defaultBase!=null){
            if (StringUtils.isBlank(reverseProxy.getServerName())){
                reverseProxy.setServerName(defaultBase.getHost());
            }
            if (StringUtils.isBlank(reverseProxy.getArtifactoryServerName())){
                reverseProxy.setArtifactoryServerName(defaultBase.getHost());
            }

            String path = defaultBase.getPath();
            // remove / at front
            if (path.startsWith("/")){
                path = path.substring(1);
            }
            if (StringUtils.isBlank(reverseProxy.getArtifactoryAppContext())){
                reverseProxy.setArtifactoryAppContext(path);
            }

            if (StringUtils.isBlank(reverseProxy.getPublicAppContext())){
                reverseProxy.setPublicAppContext(path);
            }

            if (WebServerType.DIRECT.equals(reverseProxy.getWebServerType())){
                reverseProxy.setServerName(defaultBase.getHost());
                reverseProxy.setPublicAppContext(path);
                if (defaultBase.getScheme().equals("http")){
                    reverseProxy.setUseHttp(true);
                    reverseProxy.setUseHttps(false);
                    reverseProxy.setHttpPort(defaultBase.getPort());
                } else {
                    reverseProxy.setUseHttp(false);
                    reverseProxy.setUseHttps(true);
                    reverseProxy.setHttpPort(defaultBase.getPort());
                }
            }
        }
        if (reverseProxy.getDockerReverseProxyMethod()==null){
            reverseProxy.setDockerReverseProxyMethod(ReverseProxyMethod.defaultValue());
        }
    }

    private static void updateReverseProxyWithURI(ReverseProxyDescriptor reverseProxy, URI defaultBase) {
        if (defaultBase!=null){
            if (StringUtils.isBlank(reverseProxy.getServerName())){
                reverseProxy.setServerName(defaultBase.getHost());
            }
            if (StringUtils.isBlank(reverseProxy.getArtifactoryServerName())){
                reverseProxy.setArtifactoryServerName(defaultBase.getHost());
            }

            String path = defaultBase.getPath();
            // remove / at front
            if (path.startsWith("/")){
                path = path.substring(1);
            }
            if (StringUtils.isBlank(reverseProxy.getArtifactoryAppContext())){
                reverseProxy.setArtifactoryAppContext(path);
            }

            if (StringUtils.isBlank(reverseProxy.getPublicAppContext())){
                reverseProxy.setPublicAppContext(path);
            }
            if (WebServerType.DIRECT.equals(reverseProxy.getWebServerType())){
                reverseProxy.setServerName(defaultBase.getHost());
                reverseProxy.setPublicAppContext(path);
                if (defaultBase.getScheme().equals("http")){
                    reverseProxy.setUseHttp(true);
                    reverseProxy.setUseHttps(false);
                    reverseProxy.setHttpPort(defaultBase.getPort());
                } else {
                    reverseProxy.setUseHttp(false);
                    reverseProxy.setUseHttps(true);
                    reverseProxy.setHttpPort(defaultBase.getPort());
                }
            }

        }

        if (reverseProxy.getDockerReverseProxyMethod()==null){
            reverseProxy.setDockerReverseProxyMethod(ReverseProxyMethod.defaultValue());
        }
    }

    /**
     * check if multi proxy
     *
     * @param proxyKey - proxy key
     * @return if true - return multi proxy
     */
    private boolean isMultiProxy(String proxyKey) {
        return proxyKey == null || proxyKey.length() == 0;
    }


    /**
     * convert descriptor to ui model
     * @param descriptor - reverse proxy descriptor
     * @return - reverse proxy model
     */
    private ReverseProxyDescriptorModel descriptorToModel(ReverseProxyDescriptor descriptor){
        if (descriptor == null){
            return new ReverseProxyDescriptorModel();
        }

        List<ReverseProxyRepoConfig> reverseProxyRepoConfigs = descriptor.getReverseProxyRepoConfigs();
        ReverseProxyRepositories reverseProxyRepositories = new ReverseProxyRepositories();
         final List<ReverseProxyRepoConfigs> finalReverseProxyRepositoriesList = new ArrayList<>();
        reverseProxyRepoConfigs.forEach(reverseProxyRepoConfig -> {
            finalReverseProxyRepositoriesList.add(new ReverseProxyRepoConfigs(reverseProxyRepoConfig));
         });
        ReverseProxyDescriptorModel reverseProxyDescriptorModel = new ReverseProxyDescriptorModel();
        reverseProxyDescriptorModel.setKey(descriptor.getKey());
        reverseProxyDescriptorModel.setArtifactoryPort(descriptor.getArtifactoryPort());
        reverseProxyDescriptorModel.setArtifactoryServerName(descriptor.getArtifactoryServerName());
        reverseProxyDescriptorModel.setDockerReverseProxyMethod(descriptor.getDockerReverseProxyMethod());
        reverseProxyDescriptorModel.setArtifactoryAppContext(descriptor.getArtifactoryAppContext());
        reverseProxyDescriptorModel.setHttpPort(descriptor.getHttpPort());
        reverseProxyDescriptorModel.setSslPort(descriptor.getSslPort());
        reverseProxyDescriptorModel.setPublicAppContext(descriptor.getPublicAppContext());
        reverseProxyDescriptorModel.setServerNameExpression(descriptor.getServerNameExpression());
        reverseProxyDescriptorModel.setUpStreamName(descriptor.getUpStreamName());
        reverseProxyDescriptorModel.setUseHttp(descriptor.isUseHttp());
        reverseProxyDescriptorModel.setUseHttps(descriptor.isUseHttps());
        reverseProxyDescriptorModel.setWebServerType(descriptor.getWebServerType());
        reverseProxyDescriptorModel.setSslKey(descriptor.getSslKey());
        reverseProxyDescriptorModel.setSslCertificate(descriptor.getSslCertificate());
        reverseProxyDescriptorModel.setServerName(descriptor.getServerName());
        if (!finalReverseProxyRepositoriesList.isEmpty()){
            reverseProxyRepositories.setReverseProxyRepoConfigs(finalReverseProxyRepositoriesList);
            reverseProxyDescriptorModel.setReverseProxyRepositories(reverseProxyRepositories);
        }
        return reverseProxyDescriptorModel;
    }
}
