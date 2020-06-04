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

package org.artifactory.logging.version.v10;

import org.artifactory.convert.XmlConverterTest;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.testng.annotations.Test;

import static org.artifactory.logging.version.v10.LogbackAddEventLogConverter.EVENT_APPENDER_NAME;
import static org.artifactory.logging.version.v10.LogbackAddEventLogConverter.EVENT_LOGGER_NAME;

/**
 * @author Yossi Shaul
 */
@Test
public class LogbackAddEventLogConverterTest extends XmlConverterTest {

    public void addAppendersAndLoggers() throws Exception {
        Document doc = convertXml("/org/artifactory/logging/version/v10/before_event_migration_logback.xml",
                new LogbackAddEventLogConverter());
        Element root = doc.getRootElement();
        Namespace ns = root.getNamespace();

        assertAppenderExists(root, ns, EVENT_APPENDER_NAME);
        assertLoggerExists(root, ns, EVENT_LOGGER_NAME);
    }
}
