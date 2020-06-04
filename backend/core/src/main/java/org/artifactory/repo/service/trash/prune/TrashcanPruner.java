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

package org.artifactory.repo.service.trash.prune;

import org.artifactory.api.context.ContextHelper;
import org.artifactory.common.ConstantValues;
import org.artifactory.repo.trash.TrashService;
import org.artifactory.storage.GCCandidate;
import org.artifactory.storage.binstore.service.GarbageCollectorInfo;
import org.artifactory.storage.binstore.service.GarbageCollectorListener;
import org.artifactory.storage.db.binstore.service.garbage.TrashGCProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Shay Yaakov
 */
public class TrashcanPruner implements GarbageCollectorListener {
    private static final Logger log = LoggerFactory.getLogger(TrashcanPruner.class);

    private final TrashService trashService;

    public TrashcanPruner() {
        this.trashService = ContextHelper.get().beanForType(TrashService.class);
    }

    @Override
    public void start(GarbageCollectorInfo result) {
        undeployExpiredTrashItems(result);
    }

    void undeployExpiredTrashItems(GarbageCollectorInfo result) {
        log.debug("Starting trash pruning..");
        TrashGCProvider trashGCProvider = new TrashGCProvider(trashService);
        int expectedBatchSize = ConstantValues.trashcanMaxSearchResults.getInt();
        boolean shouldStop = false;
        List<GCCandidate> batch;
        while (!shouldStop && !((batch = trashGCProvider.getBatch()).isEmpty())) {
            shouldStop = batch.size() < expectedBatchSize;
            batch.forEach(candidate -> trashGCProvider.getAction().accept(candidate, result));
        }
    }

    @Override
    public void finished() {

    }

    @Override
    public void destroy() {

    }
}
