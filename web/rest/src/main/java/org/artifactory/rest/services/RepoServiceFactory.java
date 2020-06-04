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

package org.artifactory.rest.services;

import org.artifactory.rest.common.service.admin.xray.ClearAllXrayIndexTasksService;
import org.artifactory.rest.common.service.admin.xray.GetXrayIndexStatsService;
import org.artifactory.rest.common.service.admin.xray.GetXrayLicenseService;
import org.artifactory.rest.common.service.admin.xray.IndexXrayService;
import org.artifactory.rest.common.service.trash.EmptyTrashService;
import org.artifactory.rest.common.service.trash.RestoreArtifactService;
import org.springframework.beans.factory.annotation.Lookup;

/**
 * @author Shay Yaakov
 */
public abstract class RepoServiceFactory {

    @Lookup
    public abstract EmptyTrashService emptyTrash();

    @Lookup
    public abstract RestoreArtifactService restoreArtifact();

    @Lookup
    public abstract IndexXrayService indexXray();

    @Lookup
    public abstract ClearAllXrayIndexTasksService clearAllIndexTasks();

    @Lookup
    public abstract GetXrayLicenseService getXrayLicense();

    @Lookup
    public abstract GetXrayIndexStatsService getXrayIndexStats();
}
