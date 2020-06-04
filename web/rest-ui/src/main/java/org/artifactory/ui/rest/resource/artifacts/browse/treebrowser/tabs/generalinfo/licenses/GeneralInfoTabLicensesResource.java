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

package org.artifactory.ui.rest.resource.artifacts.browse.treebrowser.tabs.generalinfo.licenses;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.rest.common.resource.BaseResource;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.licenses.GeneralTabLicenseModel;
import org.artifactory.ui.rest.service.artifacts.browse.BrowseServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * @author Dan Feldman
 */
@Path("generalTabLicenses")
@RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GeneralInfoTabLicensesResource extends BaseResource {

    @Autowired
    private BrowseServiceFactory browseFactory;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllAvailableLicenses() {
        return runService(browseFactory.getAllAvailableLicenses());
    }

    @GET
    @Path("getArchiveLicenseFile")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getArchiveLicenseFile() {
        return runService(browseFactory.getArchiveLicenseFile());
    }

    @PUT
    @Path("setLicensesOnPath")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setLicensesOnPath(List<GeneralTabLicenseModel> licenses) {
        return runService(browseFactory.setLicensesOnPath(), licenses);
    }

    @GET
    @Path("scanArtifact")
    @Produces(MediaType.APPLICATION_JSON)
    public Response scanArtifactForLicenses() {
        return runService(browseFactory.scanArtifactForLicenses());
    }
}
