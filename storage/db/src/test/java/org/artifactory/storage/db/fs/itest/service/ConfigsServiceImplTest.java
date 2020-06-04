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

package org.artifactory.storage.db.fs.itest.service;

import org.artifactory.storage.db.itest.DbBaseTest;
import org.artifactory.storage.fs.service.ConfigsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;

import static org.testng.Assert.*;

/**
 * Tests the {@link org.artifactory.storage.fs.service.ConfigsService}
 *
 * @author Yossi Shaul
 */
public class ConfigsServiceImplTest extends DbBaseTest {

    @Autowired
    private ConfigsService configsService;

    public void addConfig() throws SQLException, UnsupportedEncodingException {
        configsService.addConfig("confName", "confinput", System.currentTimeMillis());
    }

    @Test(dependsOnMethods = "addConfig")
    public void hasConfig() throws SQLException {
        assertTrue(configsService.hasConfig("confName"));
    }

    @Test(dependsOnMethods = "addConfig")
    public void getConfig() throws SQLException {
        String data = configsService.getConfig("confName");
        assertNotNull(data);
        assertEquals(data, "confinput");
    }

    @Test(dependsOnMethods = "getConfig")
    public void updateConfig() throws SQLException, UnsupportedEncodingException {
        configsService.updateConfig("confName", "newdata", System.currentTimeMillis());
        String result = configsService.getConfig("confName");
        assertNotNull(result);
        assertEquals(result, "newdata");
    }

    public void hasConfigNoConfig() throws SQLException {
        assertFalse(configsService.hasConfig("nosuchconfig"));
    }

    public void loadConfigNoConfig() throws SQLException {
        assertNull(configsService.getConfig("nosuchconfig"));
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void updateConfigNoConfig() throws SQLException, UnsupportedEncodingException {
        configsService.updateConfig("nosuchconfig", "newdata", System.currentTimeMillis());
    }

    @Test(dependsOnMethods = "addConfig", expectedExceptions = IllegalStateException.class)
    public void addConfigExistingConfig() throws SQLException {
        configsService.addConfig("confName", "jojjoh", System.currentTimeMillis());
    }

    public void addOrUpdateConfig() {
        boolean created = configsService.addOrUpdateConfig("artifactory", "somedata", System.currentTimeMillis());
        assertTrue(created);
        assertEquals(configsService.getConfig("artifactory"), "somedata");
        created = configsService.addOrUpdateConfig("artifactory", "newdata", System.currentTimeMillis());
        assertFalse(created);
        assertEquals(configsService.getConfig("artifactory"), "newdata");
    }

}
