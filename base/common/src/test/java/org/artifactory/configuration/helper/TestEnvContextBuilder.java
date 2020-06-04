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

import com.beust.jcommander.internal.Lists;
import com.beust.jcommander.internal.Maps;
import org.jfrog.security.crypto.EncryptionWrapper;
import org.jfrog.security.crypto.EncryptionWrapperFactory;

import java.util.List;
import java.util.Map;

import static org.jfrog.common.ResourceUtils.getResourceAsString;

/**
 * @author gidis
 */
public class TestEnvContextBuilder {
    private List<ConfigQueryMetData> queries = Lists.newArrayList();
    private List<FileMetaData> modifiedFiles = Lists.newArrayList();
    private Map<FileCreationStage, FileMetaData> masterKey = Maps.newHashMap();
    private Map<FileCreationStage, FileMetaData> dbProperties = Maps.newHashMap();
    private Map<FileCreationStage, FileMetaData> artifactorySystemProperties = Maps.newHashMap();
    private Map<FileCreationStage, FileMetaData> artifactoryKey = Maps.newHashMap();
    private Map<FileCreationStage, FileMetaData> artifactoryBinarystorexXml = Maps.newHashMap();
    private Map<FileCreationStage, FileMetaData> artifactoryProperties = Maps.newHashMap();
    private Map<FileCreationStage, FileMetaData> artifactoryLogbackXml = Maps.newHashMap();
    private Map<FileCreationStage, FileMetaData> artifactoryMimetypes = Maps.newHashMap();
    private Map<FileCreationStage, FileMetaData> artifactoryServiceId = Maps.newHashMap();
    private Map<FileCreationStage, FileMetaData> artifactoryRootCrt = Maps.newHashMap();
    private Map<FileCreationStage, FileMetaData> accessPrivate = Maps.newHashMap();
    private Map<FileCreationStage, FileMetaData> accessRootCrt = Maps.newHashMap();
    private Map<FileCreationStage, FileMetaData> accessLogback = Maps.newHashMap();
    private Map<FileCreationStage, FileMetaData> accessDbProperties = Maps.newHashMap();
    private Map<FileCreationStage, FileMetaData> pluginGroovyFile = Maps.newHashMap();
    private String masterKeyValueDb;
    private boolean createConfigTable;
    private boolean removeEncryptionWrapper;
    private boolean removePluginGroovyFile;

    public static TestEnvContextBuilder create() {
        return new TestEnvContextBuilder();
    }

    public void includeMasterKeyInDb() {
        String content = getResourceAsString("/basic/valid.master.key");
        EncryptionWrapper masterEncryptionWrapper = EncryptionWrapperFactory.aesKeyWrapperFromString(content);
        masterKeyValueDb = masterEncryptionWrapper.getFingerprint();
    }

    public void includeConfigTable() {
        createConfigTable = true;
    }

    public void touchArtifactoryBinarystore() {
        FileMetaData fileMetaData = new FileMetaData();
        fileMetaData.setPath("etc/binarystore.xml");
        modifiedFiles.add(fileMetaData);
    }

    public void removeMasterEncryptionWrapper() {
        removeEncryptionWrapper = true;
    }

    public void removePluginGroovyFile() {
        removePluginGroovyFile = true;
    }

    public void includeMasterKey(FileCreationStage stage) {
        FileMetaData fileMetaData = new FileMetaData();
        fileMetaData.setPath("etc/security/master.key");
        fileMetaData.setContent(getResourceAsString("/basic/valid.master.key"));
        masterKey.put(stage, fileMetaData);
    }

    public void includeArtifactoryKeyInDb() {
        ConfigQueryMetData query = new ConfigQueryMetData();
        query.setBlob(getResourceAsString("/basic/valid.artifactory.key"));
        query.setPath("artifactory.security.artifactory.key");
        queries.add(query);
    }

    public void includeBinarystoreXmlInDb() {
        ConfigQueryMetData query = new ConfigQueryMetData();
        query.setBlob(getResourceAsString("/basic/valid.artifactory.binarystore.xml"));
        query.setPath("artifactory.binarystore.xml");
        queries.add(query);
    }

    public void includeMasterKey2(FileCreationStage stage) {
        FileMetaData fileMetaData = new FileMetaData();
        fileMetaData.setPath("etc/security/master.key");
        fileMetaData.setContent(getResourceAsString("/basic/valid.master.key2"));
        masterKey.put(stage, fileMetaData);
    }

    public void includeDbProperties(FileCreationStage stage) {
        FileMetaData fileMetaData = new FileMetaData();
        fileMetaData.setPath("etc/db.properties");
        fileMetaData.setContent(getResourceAsString("/basic/valid.artifactory.db.properties"));
        dbProperties.put(stage, fileMetaData);
    }

    public void includeCorruptedDbProperties(FileCreationStage stage) {
        FileMetaData fileMetaData = new FileMetaData();
        fileMetaData.setPath("etc/db.properties");
        fileMetaData.setContent(getResourceAsString("/basic/corrupted.artifactory.db.properties"));
        dbProperties.put(stage, fileMetaData);
    }

    public void includeCorruptedArtifactoryKey(FileCreationStage stage) {
        FileMetaData fileMetaData = new FileMetaData();
        fileMetaData.setPath("etc/security/artifactory.key");
        fileMetaData.setContent(getResourceAsString("/basic/corrupted.artifactory.key"));
        artifactoryKey.put(stage, fileMetaData);
    }

    public void includeImportArtifactoryKey(FileCreationStage stage) {
        FileMetaData fileMetaData = new FileMetaData();
        fileMetaData.setPath("etc/security/artifactory.key.import");
        fileMetaData.setContent(getResourceAsString("/basic/imported.artifactory.key"));
        artifactoryKey.put(stage, fileMetaData);
    }

    public void includeArtifactorySystemProperties(FileCreationStage stage) {
        FileMetaData fileMetaData = new FileMetaData();
        fileMetaData.setPath("etc/artifactory.system.properties");
        fileMetaData.setContent(getResourceAsString("/basic/valid.artifactory.system.properties"));
        artifactorySystemProperties.put(stage, fileMetaData);
    }

    public void includeArtifactoryProperties(FileCreationStage stage) {
        FileMetaData fileMetaData = new FileMetaData();
        fileMetaData.setPath("etc/artifactory.properties");
        fileMetaData.setContent(getResourceAsString("/basic/valid.artifactory.properties"));
        artifactoryProperties.put(stage, fileMetaData);
    }

    public void includeArtifactoryKey(FileCreationStage stage) {
        FileMetaData fileMetaData = new FileMetaData();
        fileMetaData.setPath("etc/security/artifactory.key");
        fileMetaData.setContent(getResourceAsString("/basic/valid.artifactory.key"));
        artifactoryKey.put(stage, fileMetaData);
    }

    public void includeArtifactoryBinarystore(FileCreationStage stage) {
        FileMetaData fileMetaData = new FileMetaData();
        fileMetaData.setPath("etc/binarystore.xml");
        fileMetaData.setContent(getResourceAsString("/basic/valid.artifactory.binarystore.xml"));
        artifactoryBinarystorexXml.put(stage, fileMetaData);
    }

    public void includeArtifactoryLogback(FileCreationStage stage) {
        FileMetaData fileMetaData = new FileMetaData();
        fileMetaData.setPath("etc/logback.xml");
        fileMetaData.setContent(getResourceAsString("/basic/valid.artifactory.logback.xml"));
        artifactoryLogbackXml.put(stage, fileMetaData);
    }

    public void includeArtifactoryMimeTypes(FileCreationStage stage) {
        FileMetaData fileMetaData = new FileMetaData();
        fileMetaData.setPath("etc/mimetypes.xml");
        fileMetaData.setContent(getResourceAsString("/basic/valid.artifactory.mimetypes.xml"));
        artifactoryMimetypes.put(stage, fileMetaData);
    }

    public void includeArtifactoryServiceId(FileCreationStage stage) {
        FileMetaData fileMetaData = new FileMetaData();
        fileMetaData.setPath("etc/security/access/keys/service.id");
        fileMetaData.setContent(getResourceAsString("/basic/valid.artifactory.service.id"));
        artifactoryServiceId.put(stage, fileMetaData);
    }

    public void includeArtifactoryRootCert(FileCreationStage stage) {
        FileMetaData fileMetaData = new FileMetaData();
        fileMetaData.setPath("etc/security/access/keys/root.crt");
        fileMetaData.setContent(getResourceAsString("/basic/valid.artifactory.root.crt"));
        artifactoryRootCrt.put(stage, fileMetaData);
    }

    public void includeAccessPrivate(FileCreationStage stage) {
        FileMetaData fileMetaData = new FileMetaData();
        fileMetaData.setPath("access/etc/keys/private.key");
        fileMetaData.setContent(getResourceAsString("/basic/valid.access.private.key"));
        accessPrivate.put(stage, fileMetaData);
    }

    public void includeAccessRootCrt(FileCreationStage stage) {
        FileMetaData fileMetaData = new FileMetaData();
        fileMetaData.setPath("access/etc/keys/root.crt");
        fileMetaData.setContent(getResourceAsString("/basic/valid.access.root.crt"));
        accessRootCrt.put(stage, fileMetaData);
    }

    public void includeAccessDbProperties(FileCreationStage stage) {
        FileMetaData fileMetaData = new FileMetaData();
        fileMetaData.setPath("access/etc/db.properties");
        fileMetaData.setContent(getResourceAsString("/basic/valid.access.db.properties"));
        accessDbProperties.put(stage, fileMetaData);
    }

    public void includeAccessLogback(FileCreationStage stage) {
        FileMetaData fileMetaData = new FileMetaData();
        fileMetaData.setPath("access/etc/logback.xml");
        fileMetaData.setContent(getResourceAsString("/basic/valid.access.logback.xml"));
        accessLogback.put(stage, fileMetaData);
    }

    public void includePluginGroovyFile(FileCreationStage stage) {
        FileMetaData fileMetaData = new FileMetaData();
        fileMetaData.setPath("etc/plugins/foo.groovy");
        fileMetaData.setContent(getResourceAsString("/basic/valid.artifactory.plugin.foo.groovy"));
        pluginGroovyFile.put(stage, fileMetaData);
    }


    public boolean isCreateConfigTable() {
        return createConfigTable;
    }

    public EnvContext build() {
        EnvContext envContext = new EnvContext();
        envContext.setMasterKey(masterKey);
        envContext.setDbProperties(dbProperties);
        envContext.setArtifactorySystemProperties(artifactorySystemProperties);
        envContext.setArtifactoryKey(artifactoryKey);
        envContext.setArtifactoryBinarystorexXml(artifactoryBinarystorexXml);
        envContext.setArtifactoryProperties(artifactoryProperties);
        envContext.setArtifactoryLogbackXml(artifactoryLogbackXml);
        envContext.setArtifactoryMimetypes(artifactoryMimetypes);
        envContext.setArtifactoryServiceId(artifactoryServiceId);
        envContext.setArtifactoryRootCrt(artifactoryRootCrt);
        envContext.setAccessPrivate(accessPrivate);
        envContext.setAccessRootCert(accessRootCrt);
        envContext.setAccessLogback(accessLogback);
        envContext.setAccessDbProperties(accessDbProperties);
        envContext.setPluginGroovyFile(pluginGroovyFile);
        envContext.setBlobQueries(queries);
        envContext.setMasterKeyDBValue(masterKeyValueDb);
        envContext.setCreateConfigTable(createConfigTable);
        envContext.setModifiedFiles(modifiedFiles);
        envContext.setRemoveEncryptionWrapper(removeEncryptionWrapper);
        envContext.setRemovePluginGroovyFile(removePluginGroovyFile);

        return envContext;
    }
}