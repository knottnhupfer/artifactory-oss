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

package org.artifactory.storage.db;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * @author Roman Gurevich
 */
public class DbMetaData {
    private String productName;
    private String productVersion;
    private String driverName;
    private String driverVersion;
    private String url;

    DbMetaData(DatabaseMetaData metaData) throws SQLException {
        if (metaData == null) {
            return;
        }
        productName = metaData.getDatabaseProductName();
        productVersion = metaData.getDatabaseProductVersion();
        driverName = metaData.getDriverName();
        driverVersion = metaData.getDriverVersion();
        url = metaData.getURL();
    }

    public String getProductName() {
        return productName;
    }

    public String getProductVersion() {
        return productVersion;
    }

    public String getDriverName() {
        return driverName;
    }

    public String getDriverVersion() {
        return driverVersion;
    }

    public String getURL() {
        return url;
    }
}
