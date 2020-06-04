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

package org.artifactory.configuration.helper;

import org.artifactory.common.ArtifactoryHome;
import org.artifactory.test.TestUtils;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * @author gidis
 */
public class EnvContext {
    private File homeDir;
    private Map<FileCreationStage, FileMetaData> dbProperties;
    private Map<FileCreationStage, FileMetaData> masterKey;
    private Map<FileCreationStage, FileMetaData> artifactorySystemProperties;
    private Map<FileCreationStage, FileMetaData> artifactoryKey;
    private Map<FileCreationStage, FileMetaData> artifactoryBinarystorexXml;
    private Map<FileCreationStage, FileMetaData> artifactoryProperties;
    private Map<FileCreationStage, FileMetaData> artifactoryLogbackXml;
    private Map<FileCreationStage, FileMetaData> artifactoryMimetypes;
    private Map<FileCreationStage, FileMetaData> artifactoryServiceId;
    private Map<FileCreationStage, FileMetaData> artifactoryRootCrt;
    private Map<FileCreationStage, FileMetaData> accessPrivate;
    private Map<FileCreationStage, FileMetaData> accessRootCert;
    private Map<FileCreationStage, FileMetaData> accessLogback;
    private Map<FileCreationStage, FileMetaData> accessDbProperties;
    private List<ConfigQueryMetData> blobQueries;
    private String masterKeyDBValue;
    private boolean createConfigTable;
    private ArtifactoryHome artifactoryHome;
    private BasicConfigurationManagerTestHelper.MockArtifactoryConfigurationAdapter adapter;
    private List<FileMetaData> modifiedFiles;
    private boolean removeEncryptionWrapper;
    private boolean removePluginGroovyFile;
    private Map<FileCreationStage, FileMetaData> pluginGroovyFile;

    EnvContext() {
        this.homeDir = new File(TestUtils.createTempDir(getClass())+"/.artifactory");
    }

    public ArtifactoryHome getArtifactoryHome() {
        return artifactoryHome;
    }

    public void setArtifactoryHome(ArtifactoryHome artifactoryHome) {
        this.artifactoryHome = artifactoryHome;
    }

    BasicConfigurationManagerTestHelper.MockArtifactoryConfigurationAdapter getAdapter() {
        return adapter;
    }

    public void setMockConfigurationAdapter(BasicConfigurationManagerTestHelper.MockArtifactoryConfigurationAdapter adapter) {
        this.adapter = adapter;
    }

    File getHomeDir() {
        return homeDir;
    }

    public List<ConfigQueryMetData> getBlobQueries() {
        return blobQueries;
    }

    FileMetaData getDbProperties(FileCreationStage stage) {
        return dbProperties.get(stage);
    }

    FileMetaData getMasterKey(FileCreationStage stage) {
        return masterKey.get(stage);
    }

    FileMetaData getArtifactorySystemProperties(FileCreationStage stage) {
        return artifactorySystemProperties.get(stage);
    }

    FileMetaData getArtifactoryKey(FileCreationStage stage) {
        return artifactoryKey.get(stage);
    }

    FileMetaData getArtifactoryBinarystorexXml(FileCreationStage stage) {
        return artifactoryBinarystorexXml.get(stage);
    }

    public void setRemovePluginGroovyFile(boolean removePluginGroovyFile) {
        this.removePluginGroovyFile = removePluginGroovyFile;
    }

    FileMetaData getArtifactoryProperties(FileCreationStage stage) {
        return artifactoryProperties.get(stage);
    }

    FileMetaData getArtifactoryLogbackXml(FileCreationStage stage) {
        return artifactoryLogbackXml.get(stage);
    }

    FileMetaData getArtifactoryMimetypes(FileCreationStage stage) {
        return artifactoryMimetypes.get(stage);
    }

    FileMetaData getArtifactoryServiceId(FileCreationStage stage) {
        return artifactoryServiceId.get(stage);
    }

    FileMetaData getArtifactoryRootCert(FileCreationStage stage) {
        return artifactoryRootCrt.get(stage);
    }

    FileMetaData getAccessPrivate(FileCreationStage stage) {
        return accessPrivate.get(stage);
    }

    FileMetaData getAccessRootCert(FileCreationStage stage) {
        return accessRootCert.get(stage);
    }

    FileMetaData getAccessLogback(FileCreationStage stage) {
        return accessLogback.get(stage);
    }

    FileMetaData getAccessDbProperties(FileCreationStage stage) {
        return accessDbProperties.get(stage);
    }


    public void setDbProperties(Map<FileCreationStage, FileMetaData> dbProperties) {
        this.dbProperties = dbProperties;
    }

    public void setArtifactorySystemProperties(Map<FileCreationStage, FileMetaData> artifactorySystemProperties) {
        this.artifactorySystemProperties = artifactorySystemProperties;
    }

    public void setArtifactoryKey(Map<FileCreationStage, FileMetaData> artifactoryKey) {
        this.artifactoryKey = artifactoryKey;
    }

    public void setArtifactoryBinarystorexXml(Map<FileCreationStage, FileMetaData> artifactoryBinarystorexXml) {
        this.artifactoryBinarystorexXml = artifactoryBinarystorexXml;
    }

    public void setArtifactoryProperties(Map<FileCreationStage, FileMetaData> artifactoryProperties) {
        this.artifactoryProperties = artifactoryProperties;
    }

    public void setArtifactoryLogbackXml(Map<FileCreationStage, FileMetaData> artifactoryLogbackXml) {
        this.artifactoryLogbackXml = artifactoryLogbackXml;
    }

    public void setArtifactoryMimetypes(Map<FileCreationStage, FileMetaData> artifactoryMimetypes) {
        this.artifactoryMimetypes = artifactoryMimetypes;
    }

    public void setArtifactoryServiceId(Map<FileCreationStage, FileMetaData> artifactoryServiceId) {
        this.artifactoryServiceId = artifactoryServiceId;
    }

    public void setAccessPrivate(Map<FileCreationStage, FileMetaData> accessPrivate) {
        this.accessPrivate = accessPrivate;
    }

    public void setAccessRootCert(Map<FileCreationStage, FileMetaData> accessRootCert) {
        this.accessRootCert = accessRootCert;
    }

    public void setAccessLogback(Map<FileCreationStage, FileMetaData> accessLogback) {
        this.accessLogback = accessLogback;
    }

    public void setAccessDbProperties(Map<FileCreationStage, FileMetaData> accessDbProperties) {
        this.accessDbProperties = accessDbProperties;
    }

    void setMasterKey(Map<FileCreationStage, FileMetaData> masterKey) {
        this.masterKey = masterKey;
    }

    public void setArtifactoryRootCrt(Map<FileCreationStage, FileMetaData> artifactoryRootCrt) {
        this.artifactoryRootCrt = artifactoryRootCrt;
    }

    public void setPluginGroovyFile(Map<FileCreationStage, FileMetaData> pluginGroovyFile) {
        this.pluginGroovyFile = pluginGroovyFile;
    }

    public FileMetaData getPluginGroovyFile(FileCreationStage stage) {
        return pluginGroovyFile.get(stage);
    }

    public boolean isCreateHome() {
        return dbProperties.keySet().contains(FileCreationStage.beforeHome)||
                masterKey.keySet().contains(FileCreationStage.beforeHome)||
                artifactorySystemProperties.keySet().contains(FileCreationStage.beforeHome)||
                artifactoryKey.keySet().contains(FileCreationStage.beforeHome)||
                artifactoryBinarystorexXml.keySet().contains(FileCreationStage.beforeHome)||
                artifactoryProperties.keySet().contains(FileCreationStage.beforeHome)||
                artifactoryLogbackXml.keySet().contains(FileCreationStage.beforeHome)||
                artifactoryMimetypes.keySet().contains(FileCreationStage.beforeHome)||
                artifactoryServiceId.keySet().contains(FileCreationStage.beforeHome)||
                artifactoryRootCrt.keySet().contains(FileCreationStage.beforeHome)||
                accessPrivate.keySet().contains(FileCreationStage.beforeHome)||
                accessRootCert.keySet().contains(FileCreationStage.beforeHome)||
                accessLogback.keySet().contains(FileCreationStage.beforeHome)||
                accessDbProperties.keySet().contains(FileCreationStage.beforeHome);
    }

    public void setBlobQueries(List<ConfigQueryMetData> blobQueries) {
        this.blobQueries = blobQueries;
    }

    public void setMasterKeyDBValue(String masterKeyDBValue) {
        this.masterKeyDBValue = masterKeyDBValue;
    }

    public String getMasterKeyDBValue() {
        return masterKeyDBValue;
    }

    public void setCreateConfigTable(boolean createConfigTable) {
        this.createConfigTable = createConfigTable;
    }

    public boolean isCreateConfigTable() {
        return createConfigTable;
    }

    public void setModifiedFiles(List<FileMetaData> modifiedFiles) {
        this.modifiedFiles = modifiedFiles;
    }

    List<FileMetaData> getModifiedFiles() {
        return modifiedFiles;
    }

    void setRemoveEncryptionWrapper(boolean removeEncryptionWrapper) {
        this.removeEncryptionWrapper = removeEncryptionWrapper;
    }

    public boolean isRemovePluginGroovyFile() {
        return removePluginGroovyFile;
    }

    boolean isRemoveEncryptionWrapper() {
        return removeEncryptionWrapper;
    }
}