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

package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes.archive;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.lang.StringUtils;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.rest.common.model.continues.ContinuePage;
import org.artifactory.api.rest.common.model.continues.ContinueResult;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.fs.ZipEntryInfo;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.exception.BadRequestException;
import org.jfrog.common.StreamSupportUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Chen Keinan
 */
public class ArchiveEntriesTree  {
    private static final Logger log = LoggerFactory.getLogger(ArchiveEntriesTree.class);

    private ArchiveTreeNode root;
    private Map<String,ArchiveTreeNode> treeNodeMap = new HashMap<>();

    public ArchiveEntriesTree() {
        this.root = new ArchiveTreeNode("", true, "root", "");
    }

    public ContinueResult<? extends RestModel> buildChildren(String repoKey, String path) {
        RepoPath repositoryPath = InternalRepoPathFactory.create(repoKey, path);
        RepositoryService repoService = ContextHelper.get().beanForType(RepositoryService.class);
        try (ArchiveInputStream archiveInputStream = repoService.archiveInputStream(repositoryPath)) {
            ArchiveEntry archiveEntry;
            while ((archiveEntry = archiveInputStream.getNextEntry()) != null) {
                insert(InfoFactoryHolder.get().createArchiveEntry(archiveEntry), repoKey, path);
            }
        } catch (Exception e) {
            log.error("Failed to get zip Input Stream: " + e.getMessage());
            log.debug("Failed to get zip Input Stream", e);
            throw new BadRequestException("Failed to get zip Input Stream: " + e.getMessage());
        }

        if (!root.hasChildren()) {
            return new ContinueResult<>(null, Collections.emptyList());
        }
        // create archive nodes from main root elements
        List<RestModel> children = StreamSupportUtils.stream(root.getChildren()).collect(Collectors.toList());
        return new ContinueResult<>(null, children);
    }

    public void insert(ZipEntryInfo entry, String repoKey, String archivePath) {
        String[] pathElements = entry.getPath().split("/");
        ArchiveTreeNode parent = root;
        // get or create parent nodes
        StringBuilder pathBuilder = new StringBuilder();
        // iterate and create node parent folder if not exist already
        for (int i = 0; i < pathElements.length - 1; i++) {
            pathBuilder.append(pathElements[i]).append("/");
            parent = addNode(parent, pathElements[i], true, pathBuilder.toString(), archivePath, repoKey, entry);
        }
        // create node for current entry
        addNode(parent, pathElements[pathElements.length - 1],
                entry.isDirectory(), entry.getPath(), archivePath, repoKey, entry);
    }

    /**
     * get parent child from map or create new child if needed
     * @param parent - node parent
     * @param pathElement - node path
     * @param directory - if true is a directory
     * @param fullPath - node full path
     * @param archivePath - archive path
     * @param repoKey - repository key
     * @param entry - entry
     */
    private ArchiveTreeNode addNode(ArchiveTreeNode parent, String pathElement,
            boolean directory, String fullPath, String archivePath, String repoKey, ZipEntryInfo entry) {
        // get child/ parent ref in map
        ArchiveTreeNode child = treeNodeMap.get(fullPath);
        if (child == null) {
            // create new child
            String path = StringUtils.isNotBlank(parent.getTempPath()) ?
                    parent.getTempPath() + "/" + pathElement : pathElement;
            child = new ArchiveTreeNode(path, directory, pathElement, archivePath);
            child.setRepoKey(repoKey);
            child.setZipEntry(entry);
            parent.addChild(child);
            treeNodeMap.put(fullPath, child);
        }
        return child;
    }
}
