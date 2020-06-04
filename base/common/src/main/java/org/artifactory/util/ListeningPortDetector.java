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

package org.artifactory.util;

import lombok.experimental.UtilityClass;
import org.artifactory.common.ConstantValues;
import org.jfrog.common.TomcatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A helper class that attempts to detect the http port Artifactory is listening on.
 *
 * @author Yossi Shaul
 */
@UtilityClass
public class ListeningPortDetector {
    private static final Logger log = LoggerFactory.getLogger(ListeningPortDetector.class);

    public static final String SYS_ARTIFACTORY_PORT = "artifactory.port";
    private static final int ARTIFACTORY_PREFERRED_PORT = 8082;

    /**
     * @return The detected http port or -1 if not found
     */
    public static int detect() {
        int port = detectFromSystemProperty();
        if (port == -1) {
            port = detectFromTomcatMBean();
        }
        if (port == -1) {
            port = ConstantValues.testingPort.getInt();
        }
        return port;
    }

    private static int detectFromSystemProperty() {
        String portFromSystem = System.getProperty(SYS_ARTIFACTORY_PORT);
        if (portFromSystem != null) {
            try {
                return Integer.parseUnsignedInt(portFromSystem);
            } catch (NumberFormatException e) {
                log.warn("Unable to parse Artifactory port from system property: {}={}", SYS_ARTIFACTORY_PORT,
                        portFromSystem);
            }
        }
        return -1;
    }

    private static int detectFromTomcatMBean() {
        // the detector will return the port matches the preferred port or the first HTTP/1.1 connector
        int port = -1;
        try {
            port = TomcatUtils.getConnector("http", ARTIFACTORY_PREFERRED_PORT).getPort();
        } catch (IllegalStateException e) {
            log.error(e.getMessage(), e);
        }
        return port;
    }
}
