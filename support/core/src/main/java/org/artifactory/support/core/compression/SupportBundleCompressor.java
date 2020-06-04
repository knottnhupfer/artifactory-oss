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

package org.artifactory.support.core.compression;

import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.util.ZipUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Provides content compression services
 *
 * @author Michael Pasternak
 */
public class SupportBundleCompressor {
    private static final Logger log = LoggerFactory.getLogger(SupportBundleCompressor.class);

    public static final String ARCHIVE_EXTENSION = "zip";

    /**
     * Compresses given directory into zip file with the same name of the directory
     *
     * @param sourceDirectory   the content to compress
     * @param targetDirectory   target directory of zipped archive/s
     * @return                  zipped archive/s
     */
    public static List<File> compress(File sourceDirectory, File targetDirectory) {
        List<File> destinationArchives = Lists.newLinkedList();
        try {
            File destinationArchive = new File(targetDirectory, sourceDirectory.getName() + "." + ARCHIVE_EXTENSION);
            ZipUtils.archive(sourceDirectory, destinationArchive, true);
            destinationArchives.add(destinationArchive);
            if (isParentTempWorkDir(sourceDirectory)) {
                cleanup(sourceDirectory);
            }
        } catch (IOException e) {
            log.error("Content compression has failed, - {}", e.getMessage());
            log.debug("Cause: {}", e);
        }
        return destinationArchives;
    }

    private static boolean isParentTempWorkDir(File sourceDirectory) {
        return sourceDirectory.getParentFile().equals(ArtifactoryHome.get().getTempWorkDir());
    }

    /**
     * Deletes the given directory
     *
     * @param directory content to clean
     */
    private static void cleanup(File directory) {
        try {
             FileUtils.deleteDirectory(directory);
        } catch (IOException e) {
            log.error("Error deleting directory {}", directory.getAbsolutePath());
            log.debug("Error deleting directory {}", directory.getAbsolutePath(), e);
        }
    }
}
