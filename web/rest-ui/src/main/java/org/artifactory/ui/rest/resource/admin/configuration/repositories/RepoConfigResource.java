/*
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

package org.artifactory.ui.rest.resource.admin.configuration.repositories;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.rest.common.resource.BaseResource;
import org.artifactory.ui.rest.model.admin.configuration.repository.RepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.distribution.rule.DistributionRuleModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.local.LocalRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.remote.RemoteRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.replication.local.LocalReplicationConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.typespecific.DistRepoTypeSpecificConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.virtual.VirtualRepositoryConfigModel;
import org.artifactory.ui.rest.service.admin.configuration.ConfigServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

import static org.artifactory.ui.rest.resource.admin.configuration.repositories.RepoResourceConstants.PATH_REPOSITORIES;

/**
 * @author Aviad Shikloshi
 * @author Dan Feldman
 */
@Component
@Path(PATH_REPOSITORIES)
@RolesAllowed({AuthorizationService.ROLE_ADMIN})
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Produces(MediaType.APPLICATION_JSON)
public class RepoConfigResource extends BaseResource {

    @Autowired
    protected ConfigServiceFactory configServiceFactory;

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateRepositoryConfig(RepositoryConfigModel repositoryConfigModel) throws Exception {
        return runService(configServiceFactory.updateRepositoryConfig(), repositoryConfigModel);
    }

    @GET
    @Path("validatereponame")
    public Response validateRepoName() throws Exception {
        return runService(configServiceFactory.validateRepoName());
    }

    @GET
    @Path("{repoType: local|remote|virtual|distribution|releaseBundles}/{repoKey}")
    public Response getRepositoryConfigByType() throws Exception {
        return runService(configServiceFactory.getRepositoryConfig());
    }

    @GET
    @Path("availablechoices")
    public Response getAvailableRepositoryFieldChoices() throws Exception {
        return runService(configServiceFactory.getAvailableRepositoryFieldChoices());
    }

    @GET
    @Path("defaultvalues")
    public Response getRepoConfigDefaultValues() throws Exception {
        return runService(configServiceFactory.getDefaultRepositoryValues());
    }

    @GET
    @Path("remoteUrlMap")
    public Response getRemoteReposUrlMapping() throws Exception {
        return runService(configServiceFactory.getRemoteReposUrlMapping());
    }

    @GET
    @Path("{repoType: local|remote|virtual|distribution}/info")
    public Response getRepositoriesInfo() throws Exception {
        return runService(configServiceFactory.getRepositoriesInfo());
    }

    @GET
    @Path("availablerepositories")
    public Response getAvailableRepositories() {
        return runService(configServiceFactory.getAvailableRepositories());
    }

    @GET
    @Path("indexeravailablerepositories")
    public Response getIndexerAvailableRepositories() {
        return runService(configServiceFactory.getIndexerAvailableRepositories());
    }

    @POST
    @Path("resolvedrepositories")
    public Response getResolvedRepositories(VirtualRepositoryConfigModel virtualConfigModel) {
        return runService(configServiceFactory.getResolvedRepositories(), virtualConfigModel);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createRepository(RepositoryConfigModel repositoryConfigModel) throws Exception {
        return runService(configServiceFactory.createRepositoryConfig(), repositoryConfigModel);
    }

    @POST
    @Path("testremote")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response remoteRepositoryUrlTest(RemoteRepositoryConfigModel remoteRepositoryModel) {
        return runService(configServiceFactory.remoteRepositoryTestUrl(), remoteRepositoryModel);
    }

    @POST
    @Path("discoversmartrepocapabilities")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response discoverSmartRepoCapabilities(RemoteRepositoryConfigModel remoteRepositoryModel) {
        return runService(configServiceFactory.discoverSmartRepoCapabilities(), remoteRepositoryModel);
    }

    @POST
    @Path("validatelocalreplication")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response validateLocalReplicationConfig(LocalReplicationConfigModel localReplication) {
        return runService(configServiceFactory.validateLocalReplication(), localReplication);
    }

    @POST
    @Path("testlocalreplication")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response testLocalReplicationConfig(LocalRepositoryConfigModel localRepoModel) {
        return runService(configServiceFactory.testLocalReplication(), localRepoModel);
    }

    @POST
    @Path("testremotereplication")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response testRemoteReplicationConfig(RemoteRepositoryConfigModel remoteRepositoryModel) {
        return runService(configServiceFactory.testRemoteReplication(), remoteRepositoryModel);
    }

    @POST
    @Path("exeucteremotereplication")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response executeRemoteReplicationNow() {
        return runService(configServiceFactory.executeImmediateReplication());
    }

    @DELETE
    @Path("{repoKey}/delete")
    public Response deleteRepository() throws Exception {
        return runService(configServiceFactory.deleteRepositoryConfig());
    }

    @POST
    @Path("executeall")
    public Response executeAllLocalReplications() throws Exception {
        return runService(configServiceFactory.executeAllLocalReplications());
    }

    @POST
    @Path("executereplicationnow")
    public Response executeLocalReplication(LocalRepositoryConfigModel localRepoModel) {
        return runService(configServiceFactory.executeLocalReplication(), localRepoModel);
    }

    @POST
    @Path("{repoType: local|remote|virtual|distribution}/reorderrepositories")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response reorderRepositories(List<String> newOrderList) {
        return runService(configServiceFactory.reorderRepositories(), newOrderList);
    }

    @GET
    @Path("isjcenterconfigured")
    @RolesAllowed({AuthorizationService.ROLE_USER, AuthorizationService.ROLE_ADMIN})
    public Response isJcenterConfigured() {
        return runService(configServiceFactory.isJcenterConfigured());
    }

    @POST
    @Path("createdefaultjcenterrepo")
    public Response createDefaultJcenterRepo() {
        return runService(configServiceFactory.createDefaultJcenterRepo());
    }

    @GET
    @Path("getdockerstatus")
    public Response docker() {
        return runService(configServiceFactory.getDockerRepo());
    }

    @PUT
    @Path("savebintrayoauthconfig")
    public Response saveBintrayOauthConfig(DistRepoTypeSpecificConfigModel distTypeSpecific) {
        return runService(configServiceFactory.saveBintrayOauthConfig(), distTypeSpecific);
    }

    @POST
    @Path("testdistributionrule")
    public Response testDistributionRule(DistributionRuleModel distRule) {
        return runService(configServiceFactory.testDistributionRule(), distRule);
    }
}
