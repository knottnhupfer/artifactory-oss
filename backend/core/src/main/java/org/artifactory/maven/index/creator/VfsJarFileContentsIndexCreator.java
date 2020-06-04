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
/*
 * Additional contributors:
 *    JFrog Ltd.
 */

package org.artifactory.maven.index.creator;

import org.apache.maven.index.ArtifactContext;
import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.creator.JarFileContentsIndexCreator;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.fs.ZipEntryInfo;
import org.artifactory.storage.fs.service.ArchiveEntriesService;
import org.artifactory.storage.fs.tree.file.JavaIOFileAdapter;

import java.io.IOException;
import java.util.Set;

/**
 * @author yoavl
 */
public class VfsJarFileContentsIndexCreator extends JarFileContentsIndexCreator {

    @Override
    public void populateArtifactInfo(ArtifactContext artifactContext) throws IOException {
        ArtifactInfo ai = artifactContext.getArtifactInfo();
        JavaIOFileAdapter artifactFile = (JavaIOFileAdapter) artifactContext.getArtifact();

        if (artifactFile != null && artifactFile.getName().endsWith(".jar")) {
            updateArtifactInfo(ai, artifactFile);
        }
    }

    private void updateArtifactInfo(ArtifactInfo ai, JavaIOFileAdapter file)
            throws IOException {
        ArchiveEntriesService entriesService = ContextHelper.get().beanForType(ArchiveEntriesService.class);
        Set<ZipEntryInfo> archiveEntries = entriesService.getArchiveEntries(file.getFileInfo().getSha1());
        StringBuilder sb = new StringBuilder();
        for (ZipEntryInfo e : archiveEntries) {
            String name = e.getName();
            if (name.endsWith(".class")) {
                // TODO verify if class is public or protected
                // TODO skip all inner classes for now

                int i = name.indexOf('$');

                if (i == -1) {
                    if (name.charAt(0) != '/') {
                        sb.append('/');
                    }

                    // class name without ".class"
                    sb.append(name.substring(0, name.length() - 6)).append('\n');
                }
            }
        }

        if (sb.toString().trim().length() != 0) {
            ai.classNames = sb.toString();
        } else {
            ai.classNames = null;
        }
    }
}
