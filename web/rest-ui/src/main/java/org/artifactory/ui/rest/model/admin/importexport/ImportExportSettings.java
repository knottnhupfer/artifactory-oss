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

package org.artifactory.ui.rest.model.admin.importexport;

import org.artifactory.rest.common.model.BaseModel;

/**
 * @author Chen Keinan
 */
public class ImportExportSettings extends BaseModel {

    private String path;
    private boolean excludeMetadata;
    private boolean verbose;
    private boolean excludeContent;
    private String repository;
    private boolean excludeBuilds;
    private boolean m2;
    private boolean createArchive;
    private boolean zip;

    ImportExportSettings() {
    }

    public ImportExportSettings(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Boolean isExcludeMetadata() {
        return excludeMetadata;
    }

    public void setExcludeMetadata(Boolean excludeMetadata) {
        this.excludeMetadata = excludeMetadata;
    }

    public Boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(Boolean verbose) {
        this.verbose = verbose;
    }

    public Boolean isExcludeContent() {
        return excludeContent;
    }

    public void setExcludeContent(Boolean excludeContent) {
        this.excludeContent = excludeContent;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public Boolean isExcluudeBuillds() {
        return excludeBuilds;
    }

    public void setExcludeBuilds(Boolean excludeBuilds) {
        this.excludeBuilds = excludeBuilds;
    }

    public Boolean isCreateM2CompatibleExport() {
        return m2;
    }

    public void setM2(Boolean m2) {
        this.m2 = m2;
    }

    public Boolean isCreateZipArchive() {
        return createArchive;
    }

    public void setCreateArchive(Boolean createArchive) {
        this.createArchive = createArchive;
    }

    public boolean isZip() {
        return zip;
    }

    public void setZip(boolean zip) {
        this.zip = zip;
    }
}
