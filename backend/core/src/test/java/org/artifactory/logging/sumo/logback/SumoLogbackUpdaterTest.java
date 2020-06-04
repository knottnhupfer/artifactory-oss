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

package org.artifactory.logging.sumo.logback;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SystemUtils;
import org.artifactory.descriptor.repo.ProxyDescriptor;
import org.artifactory.descriptor.sumologic.SumoLogicConfigDescriptor;
import org.artifactory.logging.sumo.logback.SumoLogbackUpdater.UpdateData;
import org.jfrog.common.ResourceUtils;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.reporters.Files;

import java.io.File;
import java.io.IOException;

import static org.fest.assertions.Assertions.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Shay Yaakov
 */
@Test
public class SumoLogbackUpdaterTest {

    private SumoLogbackUpdater sumoLogbackUpdater;
    private File baseTestDir;

    @BeforeMethod
    public void createTempDir() {
        skipOnWindows();
        baseTestDir = new File(System.getProperty("java.io.tmpdir"), "sumologictest");
        baseTestDir.mkdirs();
        assertTrue(baseTestDir.exists(), "Failed to create base test dir");
        sumoLogbackUpdater = new SumoLogbackUpdater();
    }

    private void skipOnWindows() {
        if (SystemUtils.IS_OS_WINDOWS) {
            throw new SkipException("Skipping test on windows OS");
        }
    }

    @AfterMethod
    public void deleteTempDir() throws IOException {
        org.apache.commons.io.FileUtils.deleteDirectory(baseTestDir);
    }

    @Test
    public void testUpdateSumoAppenders() throws Exception {
        File logbackFile = new File(baseTestDir, "logback.xml");
        Files.copyFile(ResourceUtils.getResource("/org/artifactory/sumologic/logback.xml"), logbackFile);

        // --- Add sumo appenders for the first time ---
        SumoLogicConfigDescriptor sumoConfig = createSumoLogicConfigDescriptor();
        sumoConfig.setCollectorUrl("the-collector-url");
        sumoConfig.setEnabled(true);
        sumoLogbackUpdater.update(logbackFile, new UpdateData(sumoConfig, "art.host", "art.node"));

        File expectedFile = ResourceUtils.getResourceAsFile("/org/artifactory/sumologic/logback_enabled_no_proxy.xml");
        assertThat(FileUtils.readFileToString(logbackFile)).isEqualTo(FileUtils.readFileToString(expectedFile));

        // --- Update sumo appenders - disable and change collector url ---
        sumoConfig = createSumoLogicConfigDescriptor();
        sumoConfig.setCollectorUrl("the-other-collector-url");
        sumoConfig.setEnabled(false);
        sumoLogbackUpdater.update(logbackFile, new UpdateData(sumoConfig, "art.host", "art.node"));

        expectedFile = ResourceUtils.getResourceAsFile("/org/artifactory/sumologic/logback_disabled_no_proxy.xml");
        assertThat(FileUtils.readFileToString(logbackFile)).isEqualTo(FileUtils.readFileToString(expectedFile));

        // --- Update sumo appenders - enable, change collector url and add proxy ---
        sumoConfig = createSumoLogicConfigDescriptor();
        sumoConfig.setCollectorUrl("the-collector-url");
        sumoConfig.setEnabled(true);
        sumoConfig.setProxy(createProxy("proxy-host", 8888));
        sumoLogbackUpdater.update(logbackFile, new UpdateData(sumoConfig, "art.host", null));

        expectedFile = ResourceUtils.getResourceAsFile("/org/artifactory/sumologic/logback_enabled_with_proxy.xml");
        assertThat(FileUtils.readFileToString(logbackFile)).isEqualTo(FileUtils.readFileToString(expectedFile));

        // --- Update sumo appenders - remove proxy ---
        sumoConfig = createSumoLogicConfigDescriptor();
        sumoConfig.setCollectorUrl("the-collector-url");
        sumoConfig.setEnabled(true);
        sumoConfig.setProxy(null);
        sumoLogbackUpdater.update(logbackFile, new UpdateData(sumoConfig, "art.host", "art.node"));

        expectedFile = ResourceUtils.getResourceAsFile("/org/artifactory/sumologic/logback_enabled_no_proxy.xml");
        assertThat(FileUtils.readFileToString(logbackFile)).isEqualTo(FileUtils.readFileToString(expectedFile));
    }

    @Test
    public void clearSumoConfig() throws IOException {
        File source = ResourceUtils.getResourceAsFile("/org/artifactory/sumologic/logback_enabled_no_proxy.xml");
        File logbackFile = new File(baseTestDir, "logback.xml");
        FileUtils.copyFile(source, logbackFile);
        SumoLogbackUpdater.removeSumoLogicFromXml(logbackFile);
        assertEquals(FileUtils.readFileToString(logbackFile), FileUtils.readFileToString(
                ResourceUtils.getResourceAsFile("/org/artifactory/sumologic/semi_clean_logback.xml")));

    }

    @Test
    public void fixCollectorUrl() throws IOException {
        File source = ResourceUtils.getResourceAsFile("/org/artifactory/sumologic/logback_enabled_no_proxy.xml");
        File logbackFile = new File(baseTestDir, "logback.xml");
        FileUtils.copyFile(source, logbackFile);
        SumoLogbackUpdater.verifyAndUpdateCollectorUrl(logbackFile,"test-curl");
        assertEquals(FileUtils.readFileToString(logbackFile), FileUtils.readFileToString(
                ResourceUtils.getResourceAsFile("/org/artifactory/sumologic/fixed_collector_logback.xml")));

    }

    private ProxyDescriptor createProxy(String host, int port) {
        ProxyDescriptor proxy = new ProxyDescriptor();
        proxy.setHost(host);
        proxy.setPort(port);
        return proxy;
    }

    private SumoLogicConfigDescriptor createSumoLogicConfigDescriptor() {
        SumoLogicConfigDescriptor sumoConfig = new SumoLogicConfigDescriptor();
        sumoConfig.setClientId("the-client-id-" + System.currentTimeMillis());
        sumoConfig.setSecret("the-secret-" + System.currentTimeMillis());
        sumoConfig.setDashboardUrl("the-dashboard-url-" + System.currentTimeMillis());
        sumoConfig.setBaseUri("the-base-uri-" + System.currentTimeMillis());
        return sumoConfig;
    }
}