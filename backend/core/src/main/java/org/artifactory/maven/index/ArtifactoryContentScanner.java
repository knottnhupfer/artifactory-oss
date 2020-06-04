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
package org.artifactory.maven.index;

import org.apache.maven.index.*;
import org.apache.maven.index.context.IndexingContext;
import org.artifactory.util.Files;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

/**
 * A repository scanner to scan the content of a single repository.
 *
 * @author Yossi Shaul
 */
public class ArtifactoryContentScanner extends AbstractLogEnabled implements Scanner {
    private static final Logger log = LoggerFactory.getLogger(ArtifactoryContentScanner.class);

    private ArtifactContextProducer artifactContextProducer;

    public ArtifactoryContentScanner(ArtifactoryArtifactContextProducer artifactContextProducer) {
        this.artifactContextProducer = artifactContextProducer;
    }

    @Override
    public ScanningResult scan(ScanningRequest request) {
        request.getArtifactScanningListener().scanningStarted(request.getIndexingContext());

        ScanningResult result = new ScanningResult(request);

        scanDirectory(request.getStartingDirectory(), request);

        request.getArtifactScanningListener().scanningFinished(request.getIndexingContext(), result);

        return result;
    }

    private void scanDirectory(File dir, ScanningRequest request) {
        if (dir == null) {
            return;
        }

        File[] fileArray = dir.listFiles();

        if (fileArray == null) {
            log.debug("Unexpected null file list returned from {}: {}", dir.getAbsolutePath(),
                    Files.readFailReason(dir));
            return;
        }

        Set<File> files = new TreeSet<>(new ScannerFileComparator());

        files.addAll(Arrays.asList(fileArray));

        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectory(file, request);
            } else {
                processFile(file, request);
            }
        }
    }

    private void processFile(File file, ScanningRequest request) {
        try {
            if (!file.getName().startsWith(".")) {
                IndexingContext context = request.getIndexingContext();
                ArtifactContext ac = artifactContextProducer.getArtifactContext(context, file);

                if (ac != null) {
                    request.getArtifactScanningListener().artifactDiscovered(ac);
                }
            }
        } catch (Throwable t) {
            log.info("Failed to add {} to the maven index: {}", file.getAbsolutePath(), t.getMessage());
            log.debug("Failed to add file to the maven index", t);
        }
    }

    /**
     * A special comparator to overcome some very bad limitations of nexus-indexer during scanning: using this
     * comparator, we force to "discover" POMs last, before the actual artifact file. The reason for this, is to
     * guarantee that scanner will provide only "best" informations 1st about same artifact, since the POM->artifact
     * direction of discovery is not trivial at all (pom read -> packaging -> extension -> artifact file). The artifact
     * -> POM direction is trivial.
     */
    private static class ScannerFileComparator
            implements Comparator<File> {
        @Override
        public int compare(File o1, File o2) {
            if (o1.getName().endsWith(".pom") && !o2.getName().endsWith(".pom")) {
                // 1st is pom, 2nd is not
                return 1;
            } else if (!o1.getName().endsWith(".pom") && o2.getName().endsWith(".pom")) {
                // 2nd is pom, 1st is not
                return -1;
            } else {
                // both are "same" (pom or not pom)
                // Use reverse order so that timestamped snapshots
                // use latest - not first
                return o2.getName().compareTo(o1.getName());

            }
        }
    }
}
