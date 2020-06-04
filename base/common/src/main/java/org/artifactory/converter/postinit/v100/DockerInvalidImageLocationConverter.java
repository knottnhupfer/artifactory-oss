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

package org.artifactory.converter.postinit.v100;

import org.artifactory.api.common.MoveMultiStatusHolder;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.SecurityService;
import org.artifactory.aql.AqlService;
import org.artifactory.aql.api.domain.sensitive.AqlApiItem;
import org.artifactory.aql.result.AqlEagerResult;
import org.artifactory.aql.result.rows.AqlItem;
import org.artifactory.aql.util.AqlUtils;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.converter.postinit.PostInitConverter;
import org.artifactory.descriptor.repo.DockerApiVersion;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RepoBaseDescriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.fs.ItemInfo;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.version.ArtifactoryVersion;
import org.artifactory.version.CompoundVersionDetails;
import org.jfrog.client.util.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.artifactory.aql.api.domain.sensitive.AqlApiItem.*;
import static org.artifactory.aql.api.internal.AqlBase.and;
import static org.artifactory.mime.DockerNaming.MANIFEST_FILENAME;

/**
 * @author Nadav Yogev
 */
public class DockerInvalidImageLocationConverter extends PostInitConverter {
    private static final Logger log = LoggerFactory.getLogger(DockerInvalidImageLocationConverter.class);

    public DockerInvalidImageLocationConverter(ArtifactoryVersion from, ArtifactoryVersion until) {
        super(from, until);
    }

    @Override
    public void convert(CompoundVersionDetails source, CompoundVersionDetails target) {
        if (!isInterested(source, target)) {
            return;
        }
        // Get permission to read and move repositories
        SecurityService securityService = ContextHelper.get().beanForType(SecurityService.class);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        try {
            securityService.authenticateAsSystem();
            convert();
        } finally {
            // Restore previous permissions
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
    }

    private List<String> getLocalDockerRepos() {
        Map<String, LocalRepoDescriptor> localRepositoriesMap =
                ContextHelper.get().getCentralConfig().getDescriptor().getLocalRepositoriesMap();
        return new ArrayList<>(localRepositoriesMap.values())
                .stream()
                .filter(descriptor -> RepoType.Docker.equals(descriptor.getType()))
                .filter(descriptor -> DockerApiVersion.V2.equals(descriptor.getDockerApiVersion()))
                .map(RepoBaseDescriptor::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Find candidates for converter and move isolated images to the correct manifest.json folder
     */
    public void convert() {
        List<String> dockerRepos = getLocalDockerRepos();
        dockerRepos.forEach(repoKey -> {
            String path = repoKey + "/*";
            // Find candidates from db fore docker repo
            AqlApiItem query = AqlApiItem.create()
                    .filter(
                            and(
                                    repo().equal(repoKey),
                                    path().matches(path),
                                    type().equal("file"),
                                    depth().greaterEquals(4)
                            )
                    );
            AqlService aqlService = ContextHelper.get().beanForType(AqlService.class);
            AqlEagerResult<AqlItem> result = aqlService.executeQueryEager(query);
            // Convert candidates to RepoPath
            Set<RepoPath> repoPaths = result.getResults().stream()
                    .map(AqlUtils::fromAql)
                    .map(RepoPath::getParent)
                    .collect(Collectors.toSet());
            // Filter docker repos that need to be combined and move images to the correct location (manifest location)
            repoPaths.stream()
                    .filter(this::dockerRepoRequiresConversion)
                    .forEach(this::move);
        });
    }

    /**
     * Move files from imageParent to manifest path
     *
     * @param imageParent parent path to move from
     */
    private void move(RepoPath imageParent) {
        RepoPath manifestParent = getManifestRepoPath(imageParent);
        RepositoryService repositoryService = ContextHelper.get().getRepositoryService();
        List<ItemInfo> children = repositoryService.getChildren(imageParent);
        log.debug("Merging {} files from {} to {}", children.size(), imageParent, manifestParent);
        children.forEach(child -> {
            MoveMultiStatusHolder moveMultiStatusHolder = repositoryService
                    .move(child.getRepoPath(), manifestParent, false, true, true);
            if (moveMultiStatusHolder.isError()) {
                log.error("Failed to move {} to {}. Reason: {}", child, manifestParent,
                        moveMultiStatusHolder.getLastError().getMessage());
            }
        });
        RepoPath currentImage = imageParent;
        while (repositoryService.getArtifactCount(currentImage) == 0) {
            // remove folder
            log.debug("Removing empty folder {}", currentImage);
            repositoryService.undeploy(currentImage);
            if (currentImage != null) {
                currentImage = currentImage.getParent();
            }
        }
    }

    /**
     * Checks if images and manifest.json are separated and needs merging
     *
     * @param dockerImageFolder path of image files folder
     * @return returns true if images and manifest are separated and need combine
     */
    private boolean dockerRepoRequiresConversion(RepoPath dockerImageFolder) {
        RepoPath manifestRepoPath = getManifestRepoPath(dockerImageFolder);
        boolean isManifestExists = isManifestExists(manifestRepoPath);
        boolean isImagesIsolated = isImagesIsolated(dockerImageFolder);
        if (isImagesIsolated && isManifestExists) {
            log.info("Merging manifest.json in {} with its corresponding layers.", manifestRepoPath);
            return true;
        }
        return false;
    }

    /**
     * Build manifest probable path, without the starting path element
     *
     * @param repoPath path of possible separated images
     * @return manifest.json probable path
     */
    private RepoPath getManifestRepoPath(RepoPath repoPath) {
        String root = PathUtils.getFirstPathElement(repoPath.getPath());
        String manifestPath = repoPath.getPath().replaceFirst(root, "");
        manifestPath = PathUtils.removeDuplicateSlashes(manifestPath) + RepoPath.PATH_SEPARATOR;
        return RepoPathFactory.create(repoPath.getRepoKey(), manifestPath);
    }

    /**
     * Checks if there is no manifest.json in images folder
     *
     * @param imagePath RepoPath of docker image
     * @return true if no manifest.json file exists in folder
     */
    private boolean isImagesIsolated(RepoPath imagePath) {
        return countManifestJsonFilesUnderPath(imagePath) == 0;
    }

    /**
     * Checks if repoPath contains manifest.json
     *
     * @param manifestRepoPath RepoPath of probable manifest.json
     * @return true if manifest.json exists in path
     */
    private boolean isManifestExists(RepoPath manifestRepoPath) {
        return countManifestJsonFilesUnderPath(manifestRepoPath) == 1;
    }

    /**
     * Counts how many 'manifest.json' files are under {@param path}
     */
    private long countManifestJsonFilesUnderPath(RepoPath path) {
        List<ItemInfo> children = ContextHelper.get().getRepositoryService().getChildren(path);
        return children.stream()
                .map(ItemInfo::getName)
                .filter(MANIFEST_FILENAME::equals)
                .count();
    }

    @Override
    public void assertConversionPrecondition(ArtifactoryHome home, CompoundVersionDetails fromVersion,
            CompoundVersionDetails toVersion) {
        // not used
    }
}
