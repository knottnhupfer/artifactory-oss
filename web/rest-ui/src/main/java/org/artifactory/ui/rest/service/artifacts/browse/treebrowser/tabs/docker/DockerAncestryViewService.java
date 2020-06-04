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

import org.artifactory.api.jackson.JacksonReader;
import org.artifactory.api.properties.PropertiesService;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.search.ItemSearchResults;
import org.artifactory.api.search.SearchService;
import org.artifactory.api.search.property.PropertySearchControls;
import org.artifactory.api.search.property.PropertySearchResult;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.fs.FileInfo;
import org.artifactory.md.Properties;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.resource.ResourceStreamHandle;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.docker.ancestry.DockerAncestryArtifactInfo;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.docker.ancestry.DockerLinkedImage;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DockerAncestryViewService implements RestService {

    private static final Logger log = LoggerFactory.getLogger(DockerAncestryViewService.class);
    private static final String IMAGE_ID_PROP = "docker.imageId";
    private static final String SIZE_PROP = "docker.size";

    @Autowired
    private SearchService searchService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private PropertiesService propertiesService;

    @Autowired
    private AuthorizationService authorizationService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        DockerAncestryArtifactInfo dockerArtifactInfo = (DockerAncestryArtifactInfo) request.getImodel();
        String path = dockerArtifactInfo.getPath();
        String repoKey = dockerArtifactInfo.getRepoKey();
        RepoPath repoPath = InternalRepoPathFactory.create(repoKey, path);
        if (repositoryService.isVirtualRepoExist(repoKey)) {
            repoPath = repositoryService.getVirtualFileInfo(repoPath).getRepoPath();
        }
        // read permission checks
        if (!authorizationService.canRead(repoPath)) {
            response.responseCode(HttpServletResponse.SC_FORBIDDEN).buildResponse();
            log.error("Forbidden UI REST call from user: '{}'", authorizationService.currentUsername());
            return;
        }
        FileInfo fileInfo = repositoryService.getFileInfo(repoPath);
        String basePath = repoPath.getPath().split("/")[0];
        List<String> images = getImages(fileInfo);
        DockerLinkedImage dockerLinkedImage = getDockerLinkedImage(fileInfo, images, basePath);
        dockerArtifactInfo.clearRepoData();
        dockerArtifactInfo.setDockerLinkedImage(dockerLinkedImage);
        response.iModel(dockerArtifactInfo);

    }

    /**
     * get docker linked image
     *
     * @param fileInfo - docker file info
     * @param images   - images list
     */
    private DockerLinkedImage getDockerLinkedImage(FileInfo fileInfo, List<String> images, String basePath) {
        int size = images.size();
        long virtualSize = 0;
        String image;
        String fileRepoKey;
        DockerLinkedImage root = null;
        // create root element
        if (size >= 1) {
            image = images.get(size - 1);
            fileRepoKey = fileInfo.getRepoKey();
            virtualSize += getVirtualSize(fileRepoKey, image);
            root = new DockerLinkedImage(image.substring(0, 12), humanReadableByteCount(virtualSize, true),
                    basePath + "/" + image.substring(0, 2) + "/" + image);
        }
        DockerLinkedImage dockerLinkedImage = root;
        // build docker linked image
        if (size >= 2) {
            for (int i = size - 2; i >= 0; i--) {
                image = images.get(i);
                fileRepoKey = fileInfo.getRepoKey();
                virtualSize += getVirtualSize(fileRepoKey, image);
                DockerLinkedImage newParent = new DockerLinkedImage(image.substring(0, 12),
                        humanReadableByteCount(virtualSize, true),
                        basePath + "/" + image.substring(0, 2) + "/" + image);
                dockerLinkedImage.addChild(newParent);
                dockerLinkedImage = newParent;
            }
        }
        return root;
    }

    /**
     * get docker images data
     *
     * @param fileInfo - docker file info
     * @return list of images id
     */
    private List<String> getImages(FileInfo fileInfo) {
        try {
            ResourceStreamHandle handle = repositoryService.getResourceStreamHandle(fileInfo.getRepoPath());
            return JacksonReader.streamAsValueTypeReference(handle.getInputStream(),
                    new TypeReference<List<String>>() {
                    });
        } catch (IOException e) {
            log.debug(e.toString());
        }
        return null;
    }


    private long getVirtualSize(String repoKey, String imageId) {
        RepoPath parent = findImageRepoPath(repoKey, imageId);
        if (parent != null) {
            Properties properties = propertiesService.getProperties(parent);
            String sizeProp = properties.getFirst(SIZE_PROP);
            if (sizeProp != null) {
                return Long.parseLong(sizeProp);
            }
        }
        return 0;
    }

    private RepoPath findImageRepoPath(String repoKey, String imageId) {
        PropertySearchControls searchControls = new PropertySearchControls();
        searchControls.setLimitSearchResults(false);
        searchControls.addRepoToSearch(repoKey);
        searchControls.put(IMAGE_ID_PROP, imageId, false);
        ItemSearchResults<PropertySearchResult> itemSearchResults = searchService.searchProperty(searchControls);
        List<PropertySearchResult> resultList = itemSearchResults.getResults();

        //Return only the first result as there shouldn't really be any duplicates
        if ((resultList != null) && resultList.size() > 0) {
            PropertySearchResult propertySearchResult = resultList.get(0);
            return propertySearchResult.getItemInfo().getRepoPath();
        }
        return null;
    }

    /**
     * convert file size to human readable format
     *
     * @param bytes - bytes as long
     * @param si    - if true divide by 1000
     * @return byte as string in human readable format
     */
    public String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) {
            return bytes + " bit";
        }
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sbit", bytes / Math.pow(unit, exp), pre);
    }
}
