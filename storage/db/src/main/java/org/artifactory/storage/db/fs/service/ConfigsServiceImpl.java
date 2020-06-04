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

package org.artifactory.storage.db.fs.service;

import org.artifactory.storage.StorageException;
import org.artifactory.storage.db.fs.dao.ConfigsDao;
import org.artifactory.storage.fs.service.ConfigsService;
import org.jfrog.storage.wrapper.BlobWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;

/**
 * Stores and retrieves configs from the database.
 *
 * @author Yossi Shaul
 */
@Service
public class ConfigsServiceImpl implements ConfigsService {
    private static final Logger log = LoggerFactory.getLogger(ConfigsServiceImpl.class);

    @Autowired
    private ConfigsDao configsDao;

    @Override
    public void addConfig(String name, String data, long lastModification) {
        addConfigInternal(name, data, lastModification);
    }

    @Override
    public void addConfigInNewTransaction(String name, String data, long lastModification) {
        addConfigInternal(name, data, lastModification);
    }

    private void addConfigInternal(String name, String data, long lastModification) {
        try {
            if (hasConfig(name)) {
                throw new IllegalStateException("Attempt to add an existing config: '" + name + "'");
            }
            configsDao.createConfig(name, data, lastModification);
        } catch (SQLException | UnsupportedEncodingException e) {
            throw new StorageException("Failed to create config '" + name + "': " + e.getMessage(), e);
        }
    }

    private void addConfig(String name, InputStream data, long length, long lastModification) {
        try {
            if (hasConfig(name)) {
                throw new IllegalStateException("Attempt to add an existing config: '" + name + "'");
            }
            configsDao.createConfig(name, new BlobWrapper(data, length), lastModification);
        } catch (SQLException e) {
            throw new StorageException("Failed to create config '" + name + "': " + e.getMessage(), e);
        }
    }

    @Override
    public boolean hasConfig(String name) {
        try {
            return configsDao.hasConfig(name);
        } catch (SQLException e) {
            throw new StorageException("Failed to check for config '" + name + "' existence: " + e.getMessage(), e);
        }
    }

    @Override
    public String getConfig(String name) {
        try {
            return configsDao.loadConfig(name);
        } catch (SQLException e) {
            throw new StorageException("Failed to retrieve config '" + name + "': " + e.getMessage(), e);
        }
    }

    @Override
    public InputStream getStreamConfig(String name) {
        try {
            return configsDao.loadStreamConfig(name);
        } catch (SQLException e) {
            throw new StorageException("Failed to retrieve config '" + name + "': " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteConfig(String name) {
        try {
            int deleted = configsDao.deleteConfig(name);
            if (deleted == 0) {
                log.debug("Deletion of config entry " + name + " did not delete anything in DB!");
            } else {
                log.debug("Deleted config entry " + name);
            }
        } catch (SQLException e) {
            throw new StorageException("Failed to delete config '" + name + "': " + e.getMessage(), e);
        }
    }

    @Override
    public void updateConfig(String name, String data, long lastModification) {
        updateConfigInternal(name, data, lastModification);
    }

    @Override
    public void updateConfigInNewTransaction(String name, String data, long lastModification) {
        updateConfigInternal(name, data, lastModification);
    }

    private void updateConfigInternal(String name, String data, long lastModification) {
        try {
            int updateCount = configsDao.updateConfig(name, data, lastModification);
            if (updateCount == 0) {
                throw new IllegalStateException("Failed to update config '" + name + "'. Config doesn't exist");
            }
        } catch (UnsupportedEncodingException | SQLException e) {
            throw new StorageException("Failed to update config '" + name + "': " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updateConfigIfLastModificationMatch(String name, String data, long expectedLastModified,
            long newLastModified) {
        try {
            int updateCount = configsDao.updateConfigWithExpectedLastModification(name, expectedLastModified, newLastModified, new BlobWrapper(data));
            return updateCount > 0;
        } catch (SQLException e) {
            throw new StorageException("Failed to update config '" + name + "': " + e.getMessage(), e);
        }
    }

    private void updateConfig(String name, InputStream data, long length) {
        try {
            int updateCount = configsDao.updateConfig(name, new BlobWrapper(data, length), null);
            if (updateCount == 0) {
                throw new IllegalStateException("Failed to update config '" + name + "'. Config doesn't exist");
            }
        } catch (SQLException e) {
            throw new StorageException("Failed to update config '" + name + "': " + e.getMessage(), e);
        }
    }

    @Override
    public boolean addOrUpdateConfig(String name, InputStream data, long length, long lastModification) {
        if (hasConfig(name)) {
            updateConfig(name, data, length);
            return false;
        } else {
            addConfig(name, data, length, lastModification);
            return true;
        }
    }

    @Override
    public boolean addOrUpdateConfig(String name, String data, long lastModification) {
        if (hasConfig(name)) {
            updateConfig(name, data, lastModification);
            return false;
        } else {
            addConfig(name, data, lastModification);
            return true;
        }
    }
}
