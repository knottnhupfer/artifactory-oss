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

package org.artifactory.environment.converter;

import org.artifactory.common.ArtifactoryHome;
import org.artifactory.converter.ConverterPreconditionException;
import org.artifactory.version.CompoundVersionDetails;

import java.io.File;

/**
 * @author Gidi Shabat
 */
public interface BasicEnvironmentConverter {

    String BACKUP_FILE_EXT = ".back";

    boolean isInterested(ArtifactoryHome home, CompoundVersionDetails source, CompoundVersionDetails target);

    void convert(ArtifactoryHome artifactoryHome, CompoundVersionDetails source, CompoundVersionDetails target);

    /**
     * Assert that the preconditions for the converters are met, or throw a {@link ConverterPreconditionException}.
     * Before running the converters, assert that all of the interested converters satisfy their prerequisites conditions
     * {@see RTFACT-14343}
     */
    void assertConversionPreconditions(ArtifactoryHome home) throws ConverterPreconditionException;

    default void assertFilePermissions(File file) {
        if (file != null && file.exists() && (!file.canRead() || !file.canWrite())) {
            throw new ConverterPreconditionException("File at '" + file.getPath() +
                    "' doesn't have the read and write permissions required for this conversion to run.");
        }
    }

    default void assertFileReadPermission(File file) {
        if (file != null && file.exists() && !file.canRead()) {
            throw new ConverterPreconditionException("File at '" + file.getPath() +
                    "' doesn't have read permissions required for this conversion to run.");
        }
    }

    default void assertTargetFilePermissions(File file) {
        if (file == null || ((file.exists() && !file.canWrite()) || !file.getParentFile().canWrite())) {
            throw new ConverterPreconditionException("File at '" + (file == null ? "undefined" : file.getPath()) +
                    "' doesn't have read and write permissions required for this conversion to run.");
        }
    }
}
