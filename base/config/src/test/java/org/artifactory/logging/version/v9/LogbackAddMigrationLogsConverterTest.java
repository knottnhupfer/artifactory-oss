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

package org.artifactory.logging.version.v9;

import org.artifactory.convert.XmlConverterTest;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.testng.annotations.Test;

/**
 * @author Dan Feldman
 */
public class LogbackAddMigrationLogsConverterTest extends XmlConverterTest {

    private static final String SHA2_LOGGER_NAME = "org.artifactory.storage.jobs.migration.sha256.Sha256MigrationJob";
    private static final String SHA2_APPENDER_NAME = "SHA256_MIGRATION";

    private static final String PATH_CHECKSUM_LOGGER_NAME = "org.artifactory.storage.jobs.migration.pathchecksum.RepoPathChecksumMigrationJob";
    private static final String PATH_CHECKSUM_APPENDER_NAME = "PATH_CHECKSUM_MIGRATION";

    @Test
    public void addAppendersAndLoggers() throws Exception {
        Document doc = convertXml("/org/artifactory/logging/version/v9/before_sha2Migration_logback.xml", new LogbackAddMigrationLogsConverter());
        Element root = doc.getRootElement();
        Namespace ns = root.getNamespace();

        // Assert new appenders exists
        assertAppenderExists(root, ns, SHA2_APPENDER_NAME);
        assertAppenderExists(root, ns, PATH_CHECKSUM_APPENDER_NAME);

        // Assert new loggers exists
        assertLoggerExists(root, ns, SHA2_LOGGER_NAME);
        assertLoggerExists(root, ns, PATH_CHECKSUM_LOGGER_NAME);
    }
}
