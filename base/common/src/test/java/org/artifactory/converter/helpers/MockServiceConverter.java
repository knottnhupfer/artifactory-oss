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

package org.artifactory.converter.helpers;

import org.apache.commons.io.FileUtils;
import org.artifactory.common.property.ArtifactoryConverter;
import org.artifactory.version.CompoundVersionDetails;

import java.io.File;

/**
 * @author Gidi Shabat
 */
public class MockServiceConverter implements ArtifactoryConverter {
    private String path;
    private Boolean forceUpdate;

    public MockServiceConverter(String path, Boolean forceUpdate) {
        this.path = path;
        this.forceUpdate = forceUpdate;
    }

    @Override
    public void convert(CompoundVersionDetails source, CompoundVersionDetails target) {
        try {
            FileUtils.touch(new File(path));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isInterested(CompoundVersionDetails source, CompoundVersionDetails target) {
        return forceUpdate != null ? forceUpdate :
                ArtifactoryConverter.super.isInterested(source, target);
    }
}
