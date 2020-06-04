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

package org.artifactory.rest.resource.system;

import org.artifactory.api.request.DownloadService;
import org.artifactory.api.request.InternalArtifactoryRequest;
import org.artifactory.api.rest.constant.SystemRestConstants;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.rest.response.JerseyArtifactoryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * For Check/Test Jersey and find leaks.
 *
 * @author Saffi Hartal
 */
@Component
@Path(SystemRestConstants.PATH_ROOT + "/" + SystemRestConstants.PATH_CHECKUP)
@RolesAllowed({AuthorizationService.ROLE_ADMIN})
public class CheckupResource {

    private static final Logger log = LoggerFactory.getLogger(CheckupResource.class);
    @Autowired
    private DownloadService downloadService;

    private JerseyArtifactoryResponse.Stats stats = new JerseyArtifactoryResponse.Stats();
    public void resetStats(){
        stats = new JerseyArtifactoryResponse.Stats();
    }

    public JerseyArtifactoryResponse.Stats getStats() {
        return stats;
    }

    // real repo impl
    @HEAD
    @Path("{prefix:jersey|djersey}/head/{repoKey}/{filepath: .+}")
    public Response headFile(@PathParam("prefix") String prefix, @PathParam("repoKey") String repoKey, @PathParam("filepath") String filepath) {
        final boolean isHead = true;
        boolean debug = prefix.equals("djersey");
        return returnResponse(repoKey, filepath, isHead, debug);
    }

    @GET
    @Path("{prefix:jersey|djersey}/get/{repoKey}/{filepath: .+}")
    public Response getFile(@PathParam("prefix") String prefix, @PathParam("repoKey") String repoKey, @PathParam("filepath") String filepath) {
        final boolean isHead = false;
        boolean debug = prefix.equals("djersey");
        return returnResponse(repoKey, filepath, isHead, debug);
    }


    private Response returnResponse(String repoKey, String filepath, boolean isHead, boolean debug) {
        // for using is blob exist we need digest. is it in the file path ?
        RepoPath fileRepoPath = RepoPathFactory.create(repoKey, filepath);
        InternalArtifactoryRequest req = new InternalArtifactoryRequest(fileRepoPath, true){
            @Override
            public boolean isHeadOnly() {
                return isHead;
            }
        };

        // debug on
        JerseyArtifactoryResponse res = new JerseyArtifactoryResponse(debug);
        res.setStatsForTests(stats);
        try {
            downloadService.process(req, res);
            return res.build();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

}
