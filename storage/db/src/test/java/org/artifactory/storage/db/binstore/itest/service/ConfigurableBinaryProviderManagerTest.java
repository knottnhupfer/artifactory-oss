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

package org.artifactory.storage.db.binstore.itest.service;

import org.artifactory.common.ArtifactoryHome;
import org.artifactory.environment.BinaryStoreProperties;
import org.artifactory.storage.StorageProperties.BinaryProviderType;
import org.artifactory.storage.db.binstore.service.BlobBinaryProvider;
import org.artifactory.storage.db.itest.DbBaseTest;
import org.jfrog.storage.binstore.ifc.BinaryProviderConfig;
import org.jfrog.storage.binstore.ifc.BinaryProviderManager;
import org.jfrog.storage.binstore.manager.BinaryProviderManagerImpl;
import org.jfrog.storage.binstore.providers.EmptyBinaryProvider;
import org.jfrog.storage.binstore.providers.FileBinaryProvider;
import org.jfrog.storage.binstore.providers.FileBinaryProviderImpl;
import org.jfrog.storage.binstore.providers.FileCacheBinaryProviderImpl;
import org.jfrog.storage.binstore.providers.base.BinaryProviderBase;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;

import static org.jfrog.common.ResourceUtils.getResourceAsFile;

/**
 * @author Gidi Shabat
 */
@Test
public class ConfigurableBinaryProviderManagerTest extends DbBaseTest {

    private String dataDir;
    private String securityDir;
    private BinaryProviderManager manager;

    @BeforeClass
    private void init() {
        ArtifactoryHome artifactoryHome = ArtifactoryHome.get();
        dataDir = artifactoryHome.getDataDir().getAbsolutePath();
        securityDir = artifactoryHome.getSecurityDir().getAbsolutePath();
    }

    @Test
    public void binaryProviderWithOverrideProviderTest() {
        BinaryProviderConfig defaultValues = new BinaryStoreProperties(dataDir, securityDir).toDefaultValues();
        File configFile = getResourceAsFile("/binarystore/config/binarystoreWithOverideProviders.xml");
        defaultValues.setBinaryStoreXmlPath(configFile.getAbsolutePath());
        manager = new BinaryProviderManagerImpl(defaultValues);
        BinaryProviderBase blobBinaryProvider = (BinaryProviderBase) ReflectionTestUtils.getField(manager,
                "firstBinaryProvider");
        Assert.assertTrue(blobBinaryProvider instanceof BlobBinaryProvider);
        BinaryProviderBase empty = blobBinaryProvider.next();
        Assert.assertTrue(empty instanceof EmptyBinaryProvider);
    }

    @Test
    public void binaryProviderWithTemplateTest() {
        BinaryProviderConfig defaultValues = new BinaryStoreProperties(dataDir, securityDir).toDefaultValues();
        File configFile = getResourceAsFile("/binarystore/config/binarystore-filesystem-template.xml");
        defaultValues.setBinaryStoreXmlPath(configFile.getAbsolutePath());
        manager = new BinaryProviderManagerImpl(defaultValues);
        BinaryProviderBase fileSystem = (BinaryProviderBase) ReflectionTestUtils.getField(manager,
                "firstBinaryProvider");
        Assert.assertTrue(fileSystem instanceof FileBinaryProviderImpl);
        BinaryProviderBase empty = fileSystem.next();
        Assert.assertTrue(empty instanceof EmptyBinaryProvider);
        Assert.assertEquals("filestore", ((FileBinaryProvider) fileSystem).getBinariesDir().getName());
    }

    @Test
    public void binaryProviderWithExistingProviderTest() {
        BinaryProviderConfig defaultValues = new BinaryStoreProperties(dataDir, securityDir).toDefaultValues();
        File configFile = getResourceAsFile("/binarystore/config/binarystoreWithExistingProviders.xml");
        defaultValues.setBinaryStoreXmlPath(configFile.getAbsolutePath());
        manager = new BinaryProviderManagerImpl(defaultValues);
        BinaryProviderBase blobBinaryProvider = (BinaryProviderBase) ReflectionTestUtils.getField(manager,
                "firstBinaryProvider");
        Assert.assertTrue(blobBinaryProvider instanceof BlobBinaryProvider);
        BinaryProviderBase empty = blobBinaryProvider.next();
        Assert.assertTrue(empty instanceof EmptyBinaryProvider);
    }

    @Test
    public void binaryProviderWithTemplateAndProviderTest() {
        BinaryProviderConfig defaultValues = new BinaryStoreProperties(dataDir, securityDir).toDefaultValues();
        File configFile = getResourceAsFile("/binarystore/config/binarystoreWithTemplateAndProvider.xml");
        defaultValues.setBinaryStoreXmlPath(configFile.getAbsolutePath());
        manager = new BinaryProviderManagerImpl(defaultValues);
        BinaryProviderBase cache = (BinaryProviderBase) ReflectionTestUtils.getField(manager,
                "firstBinaryProvider");
        Assert.assertTrue(cache instanceof FileCacheBinaryProviderImpl);
        BinaryProviderBase fileSystem = cache.next();
        Assert.assertTrue(fileSystem instanceof FileBinaryProviderImpl);
        BinaryProviderBase empty = fileSystem.next();
        Assert.assertTrue(empty instanceof EmptyBinaryProvider);
        Assert.assertEquals("test654", ((FileBinaryProvider) fileSystem).getBinariesDir().getName());
    }

    @Test
    public void binaryProviderWithUserChainTest() {
        BinaryProviderConfig defaultValues = new BinaryStoreProperties(dataDir, securityDir).toDefaultValues();
        File configFile = getResourceAsFile("/binarystore/config/binarystoreWithUserChain.xml");
        defaultValues.setBinaryStoreXmlPath(configFile.getAbsolutePath());
        manager = new BinaryProviderManagerImpl(defaultValues);
        BinaryProviderBase cacheFs = (BinaryProviderBase) ReflectionTestUtils.getField(manager,
                "firstBinaryProvider");
        Assert.assertTrue(cacheFs instanceof FileCacheBinaryProviderImpl);
        BinaryProviderBase retry = cacheFs.next();
        BinaryProviderBase fileSystem2 = retry.next();
        Assert.assertTrue(fileSystem2 instanceof FileBinaryProviderImpl);
        BinaryProviderBase empty = fileSystem2.next();
        Assert.assertTrue(empty instanceof EmptyBinaryProvider);
        Assert.assertEquals("test89", ((FileBinaryProvider) cacheFs).getBinariesDir().getName());
        Assert.assertEquals("test99", ((FileBinaryProvider) fileSystem2).getBinariesDir().getName());
    }

    @Test
    public void oldGenerationWithFullDB() {
        BinaryProviderConfig defaultValues = new BinaryStoreProperties(dataDir, securityDir).toDefaultValues();
        defaultValues.addParam("maxCacheSize", "1000");
        defaultValues.addParam("storageType", BinaryProviderType.fullDb.name());
        manager = new BinaryProviderManagerImpl(defaultValues);
        BinaryProviderBase cacheFs = (BinaryProviderBase) ReflectionTestUtils.getField(manager,
                "firstBinaryProvider");
        Assert.assertTrue(cacheFs instanceof FileCacheBinaryProviderImpl);
        BinaryProviderBase blob = cacheFs.next();
        Assert.assertTrue(blob instanceof BlobBinaryProvider);
        BinaryProviderBase empty = blob.next();
        Assert.assertTrue(empty instanceof EmptyBinaryProvider);
        Assert.assertEquals("cache", ((FileBinaryProvider) cacheFs).getBinariesDir().getName());
    }

    @Test
    public void oldGenerationWithFullDBDirect() {
        // If the cache size is 0 then no cache binary provider will be created
        BinaryProviderConfig defaultValues = new BinaryStoreProperties(dataDir, securityDir).toDefaultValues();
        defaultValues.addParam("maxCacheSize", "0");
        defaultValues.addParam("storageType", BinaryProviderType.fullDb.name());
        manager = new BinaryProviderManagerImpl(defaultValues);
        BinaryProviderBase blob = (BinaryProviderBase) ReflectionTestUtils.getField(manager,
                "firstBinaryProvider");
        Assert.assertTrue(blob instanceof BlobBinaryProvider);
        BinaryProviderBase empty = blob.next();
        Assert.assertTrue(empty instanceof EmptyBinaryProvider);
    }

    @Test
    public void oldGenerationWithFileSystemDBDirect() {
        BinaryProviderConfig defaultValues = new BinaryStoreProperties(dataDir, securityDir).toDefaultValues();
        defaultValues.addParam("storageType", BinaryProviderType.filesystem.name());
        manager = new BinaryProviderManagerImpl(defaultValues);
        BinaryProviderBase fileSystem = (BinaryProviderBase) ReflectionTestUtils.getField(manager,
                "firstBinaryProvider");
        Assert.assertTrue(fileSystem instanceof FileBinaryProviderImpl);
        BinaryProviderBase empty = fileSystem.next();
        Assert.assertTrue(empty instanceof EmptyBinaryProvider);
    }

    @Test
    public void oldGenerationWithCacheAndFile() {
        BinaryProviderConfig defaultValues = new BinaryStoreProperties(dataDir, securityDir).toDefaultValues();
        defaultValues.addParam("storageType", BinaryProviderType.cachedFS.name());
        manager = new BinaryProviderManagerImpl(defaultValues);
        BinaryProviderBase cacheFS = (BinaryProviderBase) ReflectionTestUtils.getField(manager,
                "firstBinaryProvider");
        Assert.assertTrue(cacheFS instanceof FileCacheBinaryProviderImpl);
        BinaryProviderBase fileSystem = cacheFS.next();
        Assert.assertTrue(fileSystem instanceof FileBinaryProviderImpl);
        BinaryProviderBase empty = fileSystem.next();
        Assert.assertTrue(empty instanceof EmptyBinaryProvider);
    }
}
