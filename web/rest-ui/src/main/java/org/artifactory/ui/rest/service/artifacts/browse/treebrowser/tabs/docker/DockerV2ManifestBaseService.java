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

package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.docker;

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.fs.FileInfo;
import org.artifactory.fs.ItemInfo;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.RestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;

import static org.artifactory.mime.DockerNaming.MANIFEST_FILENAME;
import static org.artifactory.mime.DockerNaming.SHA2_PREFIX;

/**
 * @author ortalh
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DockerV2ManifestBaseService {
    private static final Logger log = LoggerFactory.getLogger(DockerV2ManifestBaseService.class);

    private static final int SKIP_DIGEST_SHA256 = 17;
    private static final int PACKAGEID_LEN = 12;

    @Autowired
    private RepositoryService repoService;

    @Autowired
    private AuthorizationService authorizationService;

    public ItemInfo getManifest(String repoKey, String path, RestResponse response) {
        RepoPath repoPath = InternalRepoPathFactory.create(repoKey, path);
        if (repoService.isVirtualRepoExist(repoKey)) {
            repoPath = repoService.getVirtualItemInfo(repoPath).getRepoPath();
        }
        if (!repoService.exists(repoPath)) {
            response.error("path " + repoKey + "/" + path + " is not exist");
            response.responseCode(404);
            log.error("Unable to find Docker manifest under '" + repoKey + "/" + path + "'");
            return null;
        }
        if (!authorizationService.canRead(repoPath)) {
            response.responseCode(HttpServletResponse.SC_FORBIDDEN).buildResponse();
            log.error("Forbidden UI REST call from user: '{}'", authorizationService.currentUsername());
            return null;
        }
        ItemInfo manifest = repoService.getChildren(repoPath)
                .stream()
                .filter(itemInfo -> itemInfo.getName().equals(MANIFEST_FILENAME))
                .findFirst()
                .orElse(null);

        if (manifest == null) {
            response.error("Unable to find Docker manifest under '" + repoKey + "/" + path);
            response.responseCode(404);
            log.error("Unable to find Docker manifest under '" + repoKey + "/" + path + "'");
        }
        return manifest;
    }

    /**
     * Image ID as the client shows it is defined to be the digest of the config layer.
     * Apart from the ability to compare this to the string you see in the docker images list this value has no usage.
     *
     * @link https://windsock.io/explaining-docker-image-ids/
     */
    String getImageId(ItemInfo manifest) {
        String fileContent = repoService.getStringContent((FileInfo) manifest);
        String packageId = "";
        int intIndex = fileContent.indexOf("\"config\": {");
        if(intIndex != -1) {
            int digestIndex = fileContent.indexOf("digest", intIndex) + SKIP_DIGEST_SHA256;
            packageId = fileContent.substring(digestIndex, digestIndex + PACKAGEID_LEN);
        }
        return packageId;
    }

    /**
     * The digest or sha256 properties retrieved from the manifest (which we tag upon deployment or remote download)
     * can be used to pull the image.
     *
     * @link https://docs.docker.com/engine/reference/commandline/pull/#pull-an-image-by-digest-immutable-identifier
     */
    protected String getPackageIdFromDigestProperty(String digest) {
        String normalized = null;
        if (StringUtils.isNotBlank(digest)) {
            normalized = digest.replace(SHA2_PREFIX, "");
        }
        return normalized == null ? "" : normalized;
    }

    void setRepoService(RepositoryService repoService) {
        this.repoService = repoService;
    }

    void setAuthorizationService(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }
}
