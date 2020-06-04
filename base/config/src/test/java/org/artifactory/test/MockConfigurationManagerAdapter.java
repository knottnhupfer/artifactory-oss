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

package org.artifactory.test;

import org.artifactory.common.ArtifactoryConfigurationAdapter;
import org.jfrog.config.BroadcastChannel;
import org.jfrog.config.DbChannel;
import org.jfrog.config.Home;
import org.jfrog.config.LogChannel;

/**
 * @author gidis
 */
public class MockConfigurationManagerAdapter extends ArtifactoryConfigurationAdapter {

    public MockConfigurationManagerAdapter(Home home) {
        super(home);
    }

    @Override
    public void initialize() {
        this.home.initArtifactorySystemProperties();
        this.primary = home.getArtifactoryHaNodePropertiesFile().exists() && home.getHaNodeProperties() != null
                && home.getHaNodeProperties().isPrimary();
        this.ha = home.getArtifactoryHaNodePropertiesFile().exists();
    }

    @Override
    public LogChannel getLogChannel() {
        return null;
    }

    @Override
    public DbChannel getDbChannel() {
        return null;
    }

    @Override
    public BroadcastChannel getBroadcastChannel() {
        return null;
    }

    @Override
    public void destroy() {
    }

    @Override
    public void setPermanentLogChannel() {
    }

    @Override
    public void setPermanentBroadcastChannel(BroadcastChannel broadcastChannel) {
    }

    @Override
    public void setPermanentDBChannel(DbChannel permanentDbChannel) {
    }
}
