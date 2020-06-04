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

package org.artifactory.sapi.interceptor.context;

import org.artifactory.checksum.ChecksumsInfo;
import org.artifactory.fs.FileInfo;

import javax.annotation.Nullable;

/**
 * A context to pass when performing item move or copy.
 *
 * @author Yuval Reches
 */
public class InterceptorMoveCopyContext {

    // Checksum of the target *before* the copy/move event
    private final ChecksumsInfo targetOriginalChecksumsInfo;

    private boolean overrideProperties;

    public InterceptorMoveCopyContext() {
        targetOriginalChecksumsInfo = new ChecksumsInfo();
    }

    public InterceptorMoveCopyContext(@Nullable FileInfo targetFileInfo) {
        targetOriginalChecksumsInfo = targetFileInfo == null ? new ChecksumsInfo() :
                new ChecksumsInfo(targetFileInfo.getChecksumsInfo());
    }

    public ChecksumsInfo getTargetOriginalChecksumsInfo() {
        return targetOriginalChecksumsInfo;
    }

    public boolean isOverrideProperties() {
        return overrideProperties;
    }

    public void setOverrideProperties(boolean overrideProperties) {
        this.overrideProperties = overrideProperties;
    }
}
