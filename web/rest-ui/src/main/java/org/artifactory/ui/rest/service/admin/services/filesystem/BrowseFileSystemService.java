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

package org.artifactory.ui.rest.service.admin.services.filesystem;

import com.google.common.collect.Lists;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.rest.common.util.AolUtils;
import org.artifactory.ui.rest.model.admin.services.filesystem.FileSystemBrowser;
import org.artifactory.ui.rest.model.admin.services.filesystem.FileSystemItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class BrowseFileSystemService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(BrowseFileSystemService.class);

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {

        AolUtils.assertNotAol("BrowseFileSystem");
        boolean includeZipFiles = Boolean.valueOf(request.getQueryParamByKey("includeZip"));
        // get list of files
        FileSystemBrowser fileSystem = getFileOrFoldersList(request, includeZipFiles);
        // update response with data
        response.iModel(fileSystem);
    }

    /**
     * get files or folder list 1st level only by path
     *
     * @param artifactoryRequest
     * @param includeZipFiles
     * @return list of file system item found
     */
    private FileSystemBrowser getFileOrFoldersList(ArtifactoryRestRequest artifactoryRequest, boolean includeZipFiles) {
        boolean isWin = System.getProperty("os.name").toLowerCase().indexOf("win") >= 0;
        FileSystemBrowser fileSystemBrowser = new FileSystemBrowser();
        fileSystemBrowser.setWindows(isWin);
        // update file system roots
        File[] fsRoots = File.listRoots();
        updateFileSystemRoots(fileSystemBrowser, fsRoots);
        // update roots file system
        updateRootsData(artifactoryRequest, fileSystemBrowser, includeZipFiles);
        return fileSystemBrowser;
    }

    /**
     * get root file system and update files system browser model
     *
     * @param artifactoryRequest - encapsulate data related to request
     * @param fileSystemBrowser  - file system browser model
     * @param includeZipFiles
     */
    private void updateRootsData(ArtifactoryRestRequest artifactoryRequest, FileSystemBrowser fileSystemBrowser,
            boolean includeZipFiles) {

        String selectedPath = artifactoryRequest.getQueryParamByKey("path");
        if (selectedPath.length() == 0) {
            List<String> roots = fileSystemBrowser.getRoots();
            if (roots != null && roots.size() > 0) {
                selectedPath = roots.get(0);
            }
        }
        List<FileSystemItem> fileSystemItems = Lists.newArrayList();
        File[] children = new File(selectedPath).listFiles();
        if (children != null) {
            try {
                Arrays.asList(children).forEach(
                        file -> addFolderAndZipFileToList(file, includeZipFiles, fileSystemItems));
            } catch (Exception e) {
                log.error("error with retrieving folder data , bad path name:" + selectedPath, e);
            }
        } else {
            log.debug("Cannot read directory content from: '{}'", selectedPath);
        }
        fileSystemBrowser.setFileSystemItems(sort(fileSystemItems));
    }

    private void addFolderAndZipFileToList(File file, boolean includeZipFiles, List<FileSystemItem> fileSystemItems) {
        if (includeZipFiles) {
            if (file.isDirectory() || file.getName().endsWith(".zip")) {
                fileSystemItems.add(new FileSystemItem(file));
            }
        } else {
            if (file.isDirectory()) {
                fileSystemItems.add(new FileSystemItem(file));
            }
        }
    }

    private List<FileSystemItem> sort(List<FileSystemItem> fileSystemItems) {
        fileSystemItems.sort((o1, o2) -> {
            if (o1 == null) {
                if (o2 == null) return 0;
                return -1;
            }
            if (o2 == null) return 1;
            if (o1.isFolder() && !o2.isFolder()) return -1;
            if (!o1.isFolder() && o2.isFolder()) return 1;
            return o1.getFileSystemItemName().compareToIgnoreCase(o2.getFileSystemItemName());
        });
        return fileSystemItems;
    }

    /**
     * update file system roots
     *
     * @param fileSystemBrowser - file system browser model
     */
    private void updateFileSystemRoots(FileSystemBrowser fileSystemBrowser, File[] fsRoots) {
        List<String> roots = null;
        if (fsRoots != null && fsRoots.length > 0) {
            roots = new ArrayList<>();
            for (File file : fsRoots) {
                roots.add(file.getAbsolutePath());
            }
            Collections.sort(roots);
        }
        fileSystemBrowser.setRoots(roots);
    }
}
