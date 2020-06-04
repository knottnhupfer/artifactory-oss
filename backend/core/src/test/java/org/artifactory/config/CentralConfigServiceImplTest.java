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

package org.artifactory.config;

import com.google.common.collect.Maps;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.CoreAddonsImpl;
import org.artifactory.addon.HaAddon;
import org.artifactory.addon.ha.HaCommonAddon;
import org.artifactory.api.context.ArtifactoryContextThreadBinder;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.descriptor.config.CentralConfigDescriptorImpl;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.property.PredefinedValue;
import org.artifactory.descriptor.property.Property;
import org.artifactory.descriptor.property.PropertySet;
import org.artifactory.descriptor.repo.HttpRepoDescriptor;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.ProxyDescriptor;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.descriptor.security.SecurityDescriptor;
import org.artifactory.schedule.CachedThreadPoolTaskExecutor;
import org.artifactory.spring.ArtifactoryApplicationContext;
import org.artifactory.storage.db.fs.service.ConfigsServiceImpl;
import org.artifactory.storage.db.security.service.VersioningCacheImpl;
import org.artifactory.storage.fs.service.ConfigsService;
import org.artifactory.test.ArtifactoryHomeStub;
import org.artifactory.version.ArtifactoryVersionProvider;
import org.artifactory.version.CompoundVersionDetails;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.joda.time.format.DateTimeFormat;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.easymock.EasyMock.*;
import static org.testng.Assert.*;

/**
 * Unit tests for the CentralConfigServiceImpl.
 *
 * @author Yossi Shaul
 */
public class CentralConfigServiceImplTest {

    private CentralConfigServiceImpl centralConfigService;
    private static CompoundVersionDetails earlyVersion = new CompoundVersionDetails(ArtifactoryVersionProvider.v500.get(),
            "EARLY_TEST", 0
    );
    private static CompoundVersionDetails recentVersion = new CompoundVersionDetails(ArtifactoryVersionProvider.v522m001.get(),
            "RECENT_TEST", 0
    );
    private Map<String, String> configs;
    private ConfigsService configsService;
    private ArtifactoryHomeStub artifactoryHome;

    @DataProvider
    public static Object[][] orderProvider() {
        return new Object[][]{
                { true },
                { false }
        };
    }

    @BeforeMethod
    public void setup() throws IOException {
        configs = new HashMap<>();
        InputStream is = getClass().getResourceAsStream("/META-INF/default/artifactory.config.xml");
        String defaultConfig = IOUtils.toString(is, Charset.defaultCharset());
        configs.put(ArtifactoryHome.ARTIFACTORY_CONFIG_FILE, defaultConfig);

        centralConfigService = new CentralConfigServiceImpl();

        AuthorizationService authService = EasyMock.createMock(AuthorizationService.class);
        AddonsManager addonsManager = EasyMock.createMock(AddonsManager.class);
        HaCommonAddon haCommonAddon = EasyMock.createMock(HaCommonAddon.class);
        HaAddon haAddon = EasyMock.createMock(HaAddon.class);
        ConfigurationChangesInterceptors interceptors = createMock(ConfigurationChangesInterceptors.class);

        configsService = EasyMock.createMock(ConfigsService.class);
        ArtifactoryApplicationContext context = EasyMock.createMock(ArtifactoryApplicationContext.class);

        expect(context.isReady()).andReturn(true).anyTimes();
        expect(authService.isAdmin()).andReturn(true).anyTimes();
        expect(haCommonAddon.isHaEnabled()).andReturn(false).anyTimes();
        expect(addonsManager.addonByType(HaCommonAddon.class)).andReturn(haCommonAddon).anyTimes();
        expect(addonsManager.addonByType(HaAddon.class)).andReturn(haAddon).anyTimes();
        interceptors.onBeforeSave(anyObject());
        expectLastCall().anyTimes();
        interceptors.onAfterSave(anyObject(), anyObject());
        expectLastCall().anyTimes();

        IAnswer<Boolean> answer = initConfigsService();
        expect(configsService.updateConfigIfLastModificationMatch(anyObject(), anyObject(), anyLong(), anyLong()))
                .andAnswer(answer).anyTimes();

        context.reload(anyObject(), anyObject());
        expectLastCall().anyTimes();

        ReflectionTestUtils.setField(centralConfigService, "authService", authService);
        ReflectionTestUtils.setField(centralConfigService, "addonsManager", addonsManager);
        ReflectionTestUtils.setField(centralConfigService, "interceptors", interceptors);
        ReflectionTestUtils.setField(centralConfigService, "configsService", configsService);

        artifactoryHome = new ArtifactoryHomeStub();
        expect(context.getArtifactoryHome()).andReturn(artifactoryHome).anyTimes();

        replay(authService, addonsManager, haCommonAddon, interceptors, configsService, context);
        ArtifactoryHome.bind(artifactoryHome);
        ArtifactoryContextThreadBinder.bind(context);

        CachedThreadPoolTaskExecutor cachedThreadPoolTaskExecutor = new CachedThreadPoolTaskExecutor();
        ReflectionTestUtils.setField(centralConfigService, "cachedThreadPoolTaskExecutor", cachedThreadPoolTaskExecutor);

        centralConfigService.init();
    }

    private IAnswer<Boolean> initConfigsService() {
        expect(configsService.getConfig(anyObject())).andAnswer(() -> configs.get(getCurrentArguments()[0].toString())).anyTimes();
        expect(configsService.hasConfig(anyObject())).andAnswer(() -> configs.containsKey(getCurrentArguments()[0].toString())).anyTimes();
        IAnswer<Boolean> answer = () -> {
            Object[] args = getCurrentArguments();
            configs.put((String) args[0], (String) args[1]);
            return true;
        };
        expect(configsService.addOrUpdateConfig(anyObject(), anyObject(), anyLong())).andAnswer(answer).anyTimes();
        configsService.addConfig(anyObject(), anyObject(), anyLong());
        expectLastCall().andAnswer(answer).anyTimes();
        configsService.updateConfig(anyObject(), anyObject(), anyLong());
        expectLastCall().andAnswer(answer).anyTimes();
        return answer;
    }

    @Test
    public void idRefsUseTheSameObject() throws Exception {
        MutableCentralConfigDescriptor cc = getConfigDescriptor();
        // set and duplicate the descriptor
        CentralConfigServiceImpl configService = new CentralConfigServiceImpl();
        initCache(configService, cc);
        MutableCentralConfigDescriptor copy = configService.getMutableDescriptor();

        // make sure proxy object was not duplicated
        ProxyDescriptor proxy = copy.getProxies().get(0);
        ProxyDescriptor httpProxy = ((HttpRepoDescriptor) copy.getRemoteRepositoriesMap().get("http")).getProxy();
        assertTrue(proxy == httpProxy, "Proxy object was duplicated!");

        // make sure the property set was not duplicated
        PropertySet propSetCopy = copy.getPropertySets().get(0);
        LocalRepoDescriptor local1Copy = copy.getLocalRepositoriesMap().get("local1");
        PropertySet propSetCopyFromRepo = local1Copy.getPropertySet("propSet1");
        assertTrue(propSetCopy == propSetCopyFromRepo, "Proxy set object was duplicated!");
    }

    @Test
    public void keyStorePasswordMovesToDescriptor() throws Exception {
        String PASSWORD_CONFIG_KEY = "keystore:password";
        String PASSWORD = "123456";

        // Create mocks
        ConfigsServiceImpl configsService = createMock(ConfigsServiceImpl.class);
        ReflectionTestUtils.setField(centralConfigService, "configsService", configsService);
        expect(configsService.getConfig(PASSWORD_CONFIG_KEY)).andReturn(PASSWORD).once();
        expect(configsService.hasConfig(eq(ArtifactoryHome.ARTIFACTORY_CONFIG_FILE))).andReturn(false).once();
        configsService.addConfig(eq(ArtifactoryHome.ARTIFACTORY_CONFIG_FILE), anyObject(), anyLong());
        expectLastCall().once();
        configsService.deleteConfig(PASSWORD_CONFIG_KEY);
        expectLastCall().once();
        replay(configsService);

        AddonsManager addonsManager = createMock(AddonsManager.class);
        ReflectionTestUtils.setField(centralConfigService, "addonsManager", addonsManager);
        expect(addonsManager.addonByType(anyObject())).andReturn(new CoreAddonsImpl()).anyTimes();
        replay(addonsManager);

        ConfigurationChangesInterceptors interceptors = createMock(ConfigurationChangesInterceptors.class);
        ReflectionTestUtils.setField(centralConfigService, "interceptors", interceptors);
        interceptors.onBeforeSave(anyObject());
        expectLastCall().anyTimes();
        replay(interceptors);

        // Create descriptor
        MutableCentralConfigDescriptor configDescriptor = getConfigDescriptor();
        configDescriptor.setSecurity(new SecurityDescriptor());
        initCache(centralConfigService, configDescriptor);

        centralConfigService.moveKeyStorePasswordToConfig(earlyVersion, recentVersion, configDescriptor);

        assertTrue(PASSWORD.equals(configDescriptor.getSecurity().getSigningKeysSettings().getKeyStorePassword()));
        verify(configsService);
    }

    @Test
    public void testMergeOfOneChange() throws IOException {
        initConfig();

        MutableCentralConfigDescriptor newDescriptor = centralConfigService.getMutableDescriptor();
        String newBaseUrl = "https://newUrlbase:8080";
        newDescriptor.setUrlBase(newBaseUrl);
        MutableCentralConfigDescriptor clone = SerializationUtils.clone(newDescriptor);
        centralConfigService.saveEditedDescriptorAndReload(newDescriptor);
        assertNotEquals(centralConfigService.getDescriptor().getRevision(), clone.getRevision());
        assertEquals(centralConfigService.getDescriptor().getUrlBase(), newBaseUrl);
    }

    @Test
    public void testSaveConfigAtFirstTime() throws InterruptedException {
        EasyMock.reset(configsService);
        initConfigsService();
        EasyMock.replay(configsService);

        File latestFile = artifactoryHome.getArtifactoryConfigLatestFile();
        latestFile.delete();
        assertFalse(latestFile.exists());

        MutableCentralConfigDescriptor config = centralConfigService.getMutableDescriptor();
        centralConfigService.saveEditedDescriptorAndReload(config);
        assertNotEquals(centralConfigService.getDescriptor().getRevision(), 0);

        Thread.sleep(200); // File is written in background
        assertTrue(latestFile.exists());
        EasyMock.verify(configsService);
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "Failed to read object from stream")
    public void testSaveInvalidXmlWithImport() throws IOException {
        initConfig();

        String invalidConfig = IOUtils
                .toString(getClass().getResourceAsStream("artifactory.config.invalid.xml"));
        FileUtils.write(ArtifactoryHome.get().getArtifactoryConfigImportFile(), invalidConfig);

        centralConfigService.init();
    }

    @Test
    public void testSaveValidXmlWithImport() throws IOException {
        initConfig();

        String validConfig = IOUtils
                .toString(getClass().getResourceAsStream("artifactory.config.valid.xml"));
        FileUtils.write(ArtifactoryHome.get().getArtifactoryConfigImportFile(), validConfig);

        centralConfigService.init();
    }

    @Test(dataProvider = "orderProvider")
    public void testMergeOfTwoChangesWithoutConflicts(boolean inOrder) {
        initConfig();

        MutableCentralConfigDescriptor newDescriptor = centralConfigService.getMutableDescriptor();
        String newBaseUrl = "https://newUrlbase:8080";
        newDescriptor.setUrlBase(newBaseUrl);

        MutableCentralConfigDescriptor newDescriptor2 = centralConfigService.getMutableDescriptor();
        String newServerName = "https://newUrlbase:8080";
        newDescriptor2.setServerName(newServerName);

        saveWithOrder(inOrder, newDescriptor, newDescriptor2);

        assertTrue(centralConfigService.getDescriptor().getRevision() > newDescriptor.getRevision());
        assertTrue(centralConfigService.getDescriptor().getRevision() > newDescriptor2.getRevision());
        assertEquals(centralConfigService.getDescriptor().getUrlBase(), newBaseUrl);
        assertEquals(centralConfigService.getDescriptor().getServerName(), newServerName);
    }

    @Test(dataProvider = "orderProvider")
    public void testMergeOfTwoChangesWithConflict(boolean inOrder) {
        initConfig();

        MutableCentralConfigDescriptor newDescriptor = centralConfigService.getMutableDescriptor();
        newDescriptor.setUrlBase("https://newUrlbase:8080");

        MutableCentralConfigDescriptor newDescriptor2 = centralConfigService.getMutableDescriptor();
        newDescriptor2.setUrlBase("https://newUrlbase222:8080");

        String toValidate;
        if (inOrder) {
            centralConfigService.saveEditedDescriptorAndReload(newDescriptor);
            centralConfigService.saveEditedDescriptorAndReload(newDescriptor2);
            toValidate = newDescriptor2.getUrlBase();
        } else {
            centralConfigService.saveEditedDescriptorAndReload(newDescriptor2);
            centralConfigService.saveEditedDescriptorAndReload(newDescriptor);
            toValidate = newDescriptor.getUrlBase();
        }

        assertTrue(centralConfigService.getDescriptor().getRevision() > newDescriptor.getRevision());
        assertTrue(centralConfigService.getDescriptor().getRevision() > newDescriptor2.getRevision());
        assertEquals(centralConfigService.getDescriptor().getUrlBase(), toValidate);
    }

    @Test(dataProvider = "orderProvider")
    public void testMergeOfMapObjects(boolean inOrder) {
        initConfig();

        MutableCentralConfigDescriptor newDescriptor = centralConfigService.getMutableDescriptor();
        LocalRepoDescriptor newRepository = new LocalRepoDescriptor();
        newRepository.setKey("hello-world");
        newDescriptor.addLocalRepository(newRepository);

        MutableCentralConfigDescriptor newDescriptor2 = centralConfigService.getMutableDescriptor();
        LocalRepoDescriptor newRepository2 = new LocalRepoDescriptor();
        newRepository2.setKey("hello-world2");
        newDescriptor2.addLocalRepository(newRepository2);

        saveWithOrder(inOrder, newDescriptor, newDescriptor2);

        assertTrue(centralConfigService.getDescriptor().getRevision() > newDescriptor.getRevision());
        assertTrue(centralConfigService.getDescriptor().getRevision() > newDescriptor2.getRevision());
        assertTrue(centralConfigService.getDescriptor().getLocalRepositoriesMap().containsKey("hello-world"));
        assertTrue(centralConfigService.getDescriptor().getLocalRepositoriesMap().containsKey("hello-world2"));
    }

    @Test(dataProvider = "orderProvider")
    public void testMergeListOfObjects(boolean inOrder) {
        initConfig();

        MutableCentralConfigDescriptor newDescriptor = centralConfigService.getMutableDescriptor();
        PropertySet propertySet = new PropertySet();
        propertySet.setName("name");
        newDescriptor.addPropertySet(propertySet);

        MutableCentralConfigDescriptor newDescriptor2 = centralConfigService.getMutableDescriptor();
        PropertySet propertySet2 = new PropertySet();
        propertySet2.setName("name2");
        newDescriptor2.addPropertySet(propertySet2);

        saveWithOrder(inOrder, newDescriptor, newDescriptor2);

        assertTrue(centralConfigService.getDescriptor().getRevision() > newDescriptor.getRevision());
        assertTrue(centralConfigService.getDescriptor().getRevision() > newDescriptor2.getRevision());
        assertEquals(centralConfigService.getDescriptor().getPropertySets().get(0).getName(), "name" + (inOrder ? "" : "2"));
        assertEquals(centralConfigService.getDescriptor().getPropertySets().get(1).getName(), "name" + (inOrder ? "2" : ""));
    }

    @Test(dataProvider = "orderProvider")
    public void testMergeListWithConflictOnKey(boolean inOrder) {
        initConfig();

        MutableCentralConfigDescriptor newDescriptor = centralConfigService.getMutableDescriptor();
        PropertySet propertySet = new PropertySet();
        propertySet.setName("name");
        propertySet.setVisible(false);
        newDescriptor.addPropertySet(propertySet);

        MutableCentralConfigDescriptor newDescriptor2 = centralConfigService.getMutableDescriptor();
        PropertySet propertySet2 = new PropertySet();
        propertySet2.setName("name");
        propertySet2.setVisible(true);
        newDescriptor2.addPropertySet(propertySet2);

        saveWithOrder(inOrder, newDescriptor, newDescriptor2);

        assertTrue(centralConfigService.getDescriptor().getRevision() > newDescriptor.getRevision());
        assertTrue(centralConfigService.getDescriptor().getRevision() > newDescriptor2.getRevision());
        assertEquals(centralConfigService.getDescriptor().getPropertySets().size(), 1);
        assertEquals(centralConfigService.getDescriptor().getPropertySets().get(0).isVisible(), inOrder);
    }

    @Test(dataProvider = "orderProvider")
    public void testMergeMapWithConflictOnKey(boolean inOrder) {
        initConfig();

        MutableCentralConfigDescriptor newDescriptor = centralConfigService.getMutableDescriptor();
        LocalRepoDescriptor newRepository = new LocalRepoDescriptor();
        newRepository.setKey("hello-world");
        newRepository.setNotes("notes");
        newDescriptor.addLocalRepository(newRepository);

        MutableCentralConfigDescriptor newDescriptor2 = centralConfigService.getMutableDescriptor();
        LocalRepoDescriptor newRepository2 = new LocalRepoDescriptor();
        newRepository2.setKey("hello-world");
        newRepository2.setNotes("notes2");
        newDescriptor2.addLocalRepository(newRepository2);

        saveWithOrder(inOrder, newDescriptor, newDescriptor2);

        assertTrue(centralConfigService.getDescriptor().getRevision() > newDescriptor.getRevision());
        assertTrue(centralConfigService.getDescriptor().getRevision() > newDescriptor2.getRevision());
        assertTrue(centralConfigService.getDescriptor().getLocalRepositoriesMap().containsKey("hello-world"));
        assertEquals(centralConfigService.getDescriptor().getLocalRepositoriesMap().get("hello-world").getNotes(), "notes" + (inOrder ? "2" : ""));
    }

    @Test
    public void testGetJfrogBaseUrlNoPath() {
        initConfig();

        MutableCentralConfigDescriptor newDescriptor = centralConfigService.getMutableDescriptor();
        String newBaseUrl = "https://newUrlbase:8080";
        newDescriptor.setUrlBase(newBaseUrl);
        centralConfigService.saveEditedDescriptorAndReload(newDescriptor);
        assertEquals(centralConfigService.getDescriptor().getJFrogUrlBase(), newBaseUrl);
    }

    @Test
    public void testGetJfrogBaseUrlWithPath() {
        initConfig();

        MutableCentralConfigDescriptor newDescriptor = centralConfigService.getMutableDescriptor();
        String jfrogBaseUrl = "https://newUrlbase:8080";
        String newBaseUrl = jfrogBaseUrl + "/artifactory";
        newDescriptor.setUrlBase(newBaseUrl);
        centralConfigService.saveEditedDescriptorAndReload(newDescriptor);
        assertEquals(centralConfigService.getDescriptor().getJFrogUrlBase(), jfrogBaseUrl);
        assertEquals(centralConfigService.getDescriptor().getUrlBase(), newBaseUrl);
    }

    private void initConfig() {
        MutableCentralConfigDescriptor config = centralConfigService.getMutableDescriptor();
        centralConfigService.saveEditedDescriptorAndReload(config);
        assertNotEquals(centralConfigService.getDescriptor().getRevision(), 0);

        config = centralConfigService.getMutableDescriptor();
        centralConfigService.saveEditedDescriptorAndReload(config);
        assertNotEquals(centralConfigService.getDescriptor().getRevision() > config.getRevision(),
                config.getRevision() + " " + centralConfigService.getDescriptor().getRevision());
        assertNotEquals(centralConfigService.getDescriptor().getRevision(), 1);
    }

    private void saveWithOrder(boolean inOrder, MutableCentralConfigDescriptor newDescriptor,
            MutableCentralConfigDescriptor newDescriptor2) {
        if (inOrder) {
            centralConfigService.saveEditedDescriptorAndReload(newDescriptor);
            centralConfigService.saveEditedDescriptorAndReload(newDescriptor2);
        } else {
            centralConfigService.saveEditedDescriptorAndReload(newDescriptor2);
            centralConfigService.saveEditedDescriptorAndReload(newDescriptor);
        }
    }

    private void initCache(CentralConfigServiceImpl centralConfigService, MutableCentralConfigDescriptor configDescriptor) {
        VersioningCacheImpl<CentralConfigServiceImpl.CentralConfigDescriptorCache> cache = new VersioningCacheImpl<>(3000,
                () -> new CentralConfigServiceImpl.CentralConfigDescriptorCache(configDescriptor,configDescriptor.getServerName(),
                        DateTimeFormat.forPattern(configDescriptor.getDateFormat())));
        ReflectionTestUtils.setField(centralConfigService, "descriptorCache",cache);
    }

    private MutableCentralConfigDescriptor getConfigDescriptor() {
        MutableCentralConfigDescriptor cc = new CentralConfigDescriptorImpl();
        cc.setServerName("mymy");
        cc.setDateFormat("dd-MM-yy HH:mm:ss z");

        LocalRepoDescriptor local1 = new LocalRepoDescriptor();
        local1.setKey("local1");
        Map<String, LocalRepoDescriptor> localReposMap = Maps.newLinkedHashMap();
        localReposMap.put(local1.getKey(), local1);
        cc.setLocalRepositoriesMap(localReposMap);

        ProxyDescriptor proxy = new ProxyDescriptor();
        proxy.setHost("localhost");
        proxy.setKey("proxy");
        proxy.setPort(8987);
        cc.setProxies(Arrays.asList(proxy));

        HttpRepoDescriptor httpRepo = new HttpRepoDescriptor();
        httpRepo.setKey("http");
        httpRepo.setProxy(proxy);
        httpRepo.setUrl("http://blabla");
        Map<String, RemoteRepoDescriptor> map = Maps.newLinkedHashMap();
        map.put(httpRepo.getKey(), httpRepo);
        cc.setRemoteRepositoriesMap(map);

        // property sets
        PropertySet propSet = new PropertySet();
        propSet.setName("propSet1");
        Property prop = new Property();
        prop.setName("prop1");
        PredefinedValue value1 = new PredefinedValue();
        value1.setValue("value1");
        prop.addPredefinedValue(value1);
        PredefinedValue value2 = new PredefinedValue();
        value2.setValue("value2");
        prop.addPredefinedValue(value2);
        propSet.addProperty(prop);
        cc.addPropertySet(propSet);

        local1.addPropertySet(propSet);

        return cc;
    }
}
