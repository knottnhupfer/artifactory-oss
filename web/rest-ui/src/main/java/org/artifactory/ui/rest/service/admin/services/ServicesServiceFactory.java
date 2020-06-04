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

package org.artifactory.ui.rest.service.admin.services;

import org.artifactory.ui.rest.service.admin.services.backups.*;
import org.artifactory.ui.rest.service.admin.services.filesystem.BrowseFileSystemService;
import org.artifactory.ui.rest.service.admin.services.indexer.GetIndexerService;
import org.artifactory.ui.rest.service.admin.services.indexer.RunIndexNowService;
import org.artifactory.ui.rest.service.admin.services.indexer.UpdateIndexerService;
import org.springframework.beans.factory.annotation.Lookup;

/**
 * @author Chen Keinan
 */
public abstract class ServicesServiceFactory {
    //backups services
    @Lookup
    public abstract CreateBackupService createBackupService();

    @Lookup
    public abstract UpdateBackupService updateBackupService();

    @Lookup
    public abstract GetBackupService getBackupService();

    @Lookup
    public abstract DeleteBackupService deleteBackupService();

    @Lookup
    public abstract RunNowBackupService runNowBackupService();
    // file system browser
    @Lookup
    public abstract BrowseFileSystemService browseFileSystemService();
    //indexer service
    @Lookup
    public abstract UpdateIndexerService updateIndexerService();

    @Lookup
    public abstract GetIndexerService getIndexerService();

    @Lookup
    public abstract RunIndexNowService runIndexNowService();


}
