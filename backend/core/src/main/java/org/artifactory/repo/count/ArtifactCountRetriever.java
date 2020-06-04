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

package org.artifactory.repo.count;

import org.artifactory.storage.fs.service.FileService;
import org.jfrog.common.CachedValue;
import org.springframework.core.task.AsyncTaskExecutor;

import java.util.concurrent.TimeUnit;

/**
 * Retrieves the total artifact (file) count and caching the result if the action takes too long.
 *
 * @author Shay Yaakov
 */
public class ArtifactCountRetriever {

    private final CachedValue<Integer> artifactCount;

    public ArtifactCountRetriever(FileService fileService, AsyncTaskExecutor executor) {
        this.artifactCount = CachedValue.loadUsing(fileService::getFilesCount)
                .async(executor::submit)
                .initialLoadTimeout(1, TimeUnit.MINUTES) // Wait for the count in the initial load
                .expireAfterRefresh(0, TimeUnit.SECONDS) // Always trigger a refresh when requested
                .defaultValue(0)
                .name("ArtifactCount")
                .build();
    }

    public int getCount() {
        return artifactCount.get();
    }
}