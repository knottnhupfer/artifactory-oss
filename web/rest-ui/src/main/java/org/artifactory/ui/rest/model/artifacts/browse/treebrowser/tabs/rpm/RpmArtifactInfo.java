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

package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.rpm;

import org.artifactory.addon.yum.*;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.BaseArtifactInfo;

import java.io.File;
import java.util.List;

/**
 * @author Chen Keinan
 */
public class RpmArtifactInfo extends BaseArtifactInfo {

    RpmArtifactInfo() {
    }

    private GeneralRpmMetadata generalRpmMetadata;
    private MiscRpmMetadata miscRpmMetadata;
    private String description;
    private List<MetadataEntry> provide;
    private List<MetadataEntry> require;
    private List<MetadataEntry> conflict;
    private List<MetadataEntry> obsolete;
    private List<File> files;
    private List<MetadataChangeLog> changeLogs;

    public RpmArtifactInfo(ArtifactRpmMetadata artifactRpmMetadata) {
        this.generalRpmMetadata = artifactRpmMetadata.getGeneralRpmMetadata();
        this.miscRpmMetadata = artifactRpmMetadata.getMiscRpmMetadata();
        this.description = artifactRpmMetadata.getDescription();
        this.provide = artifactRpmMetadata.getProvide();
        this.require = artifactRpmMetadata.getRequire();
        this.conflict = artifactRpmMetadata.getConflict();
        this.obsolete = artifactRpmMetadata.getObsolete();
        this.files = artifactRpmMetadata.getFiles();
        this.changeLogs = artifactRpmMetadata.getChangeLogs();
    }

    public GeneralRpmMetadata getGeneralRpmMetadata() {
        return generalRpmMetadata;
    }

    public void setGeneralRpmMetadata(GeneralRpmMetadata generalRpmMetadata) {
        this.generalRpmMetadata = generalRpmMetadata;
    }

    public MiscRpmMetadata getMiscRpmMetadata() {
        return miscRpmMetadata;
    }

    public void setMiscRpmMetadata(MiscRpmMetadata miscRpmMetadata) {
        this.miscRpmMetadata = miscRpmMetadata;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<MetadataEntry> getProvide() {
        return provide;
    }

    public void setProvide(List<MetadataEntry> provide) {
        this.provide = provide;
    }

    public List<MetadataEntry> getRequire() {
        return require;
    }

    public void setRequire(List<MetadataEntry> require) {
        this.require = require;
    }

    public List<MetadataEntry> getConflict() {
        return conflict;
    }

    public void setConflict(List<MetadataEntry> conflict) {
        this.conflict = conflict;
    }

    public List<MetadataEntry> getObsolete() {
        return obsolete;
    }

    public void setObsolete(List<MetadataEntry> obsolete) {
        this.obsolete = obsolete;
    }

    public List<File> getFiles() {
        return files;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }

    public List<MetadataChangeLog> getChangeLogs() {
        return changeLogs;
    }

    public void setChangeLogs(List<MetadataChangeLog> changeLogs) {
        this.changeLogs = changeLogs;
    }
}
