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

package org.artifactory.ui.rest.service.admin.importexport;

import org.artifactory.ui.rest.service.admin.importexport.exportdata.ExportRepositoryService;
import org.artifactory.ui.rest.service.admin.importexport.exportdata.ExportSystemService;
import org.artifactory.ui.rest.service.admin.importexport.importdata.ImportRepositoryService;
import org.artifactory.ui.rest.service.admin.importexport.importdata.ImportSystemService;
import org.artifactory.ui.rest.service.admin.importexport.importdata.UploadExtractedZipService;
import org.artifactory.ui.rest.service.admin.importexport.importdata.UploadSystemExtractedZipService;
import org.springframework.beans.factory.annotation.Lookup;

/**
 * @author Chen Keinan
 */
public abstract class ImportExportServiceFactory {

    @Lookup
    public abstract ImportRepositoryService importRepositoryService();

    @Lookup
    public abstract UploadExtractedZipService uploadExtractedZip();

    @Lookup
    public abstract ImportSystemService importSystem();

    @Lookup
    public abstract ExportRepositoryService exportRepository();

    @Lookup
    public abstract ExportSystemService exportSystem();

    @Lookup
    public abstract UploadSystemExtractedZipService uploadSystemExtractedZip();
}
