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

package org.artifactory.ui.rest.model.artifacts.deploy;


import org.artifactory.api.artifact.UnitInfo;
import org.artifactory.rest.common.model.BaseModel;
import org.artifactory.ui.rest.model.utils.FileUpload;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;

/**
 * @author Chen Keinan
 */
public class UploadArtifactInfo extends BaseModel {

    private FileUpload fileUpload;
    private UnitInfo unitInfo;
    private String fileName;
    private String repoKey;
    private String unitConfigFileContent;
    private boolean publishUnitConfigFile;
    private String handlingNode;    //Node id of the instance that handled the temp upload, used for HA only

    public UploadArtifactInfo() {
    }

    public UploadArtifactInfo(FormDataMultiPart fileUpload) {
        this.fileUpload = new FileUpload(fileUpload);
    }

    public FormDataMultiPart fetchFormDataMultiPart() {
        return fileUpload.getFormDataMultiPart();
    }

    public UnitInfo getUnitInfo() {
        return unitInfo;
    }

    public void setUnitInfo(UnitInfo unitInfo) {
        this.unitInfo = unitInfo;
    }

    public void cleanData() {
        fileUpload = null;
    }

    public String getFileName() {
        verifyFileName(fileName);
        return fileName;
    }

    public void setFileName(String fileName) {
        verifyFileName(fileName);
        this.fileName = fileName;
    }

    private void verifyFileName(String fileName) {
        // security check
        if (fileName != null && fileName.contains("..")) {
            throw new IllegalArgumentException("File name cannot contain relative paths");
        }
    }

    public String getRepoKey() {
        return repoKey;
    }

    public void setRepoKey(String repoKey) {
        this.repoKey = repoKey;
    }

    public String getUnitConfigFileContent() {
        return unitConfigFileContent;
    }

    public void setUnitConfigFileContent(String unitConfigFileContent) {
        this.unitConfigFileContent = unitConfigFileContent;
    }

    public boolean isPublishUnitConfigFile() {
        return publishUnitConfigFile;
    }

    public void setPublishUnitConfigFile(boolean publishUnitConfigFile) {
        this.publishUnitConfigFile = publishUnitConfigFile;
    }

    public String getHandlingNode() {
        return handlingNode;
    }

    public void setHandlingNode(String handlingNode) {
        this.handlingNode = handlingNode;
    }
}
