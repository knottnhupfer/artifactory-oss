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

import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.jfrog.common.TomcatUtilsTest;
import org.testng.annotations.Test;

import javax.management.*;
import java.lang.management.ManagementFactory;

import static org.testng.Assert.assertEquals;

/**
 * Unit tests for {@link ListeningPortDetector}.
 *
 * @author Yossi Shaul
 */
@Test
public class ListeningPortDetectorTest extends ArtifactoryHomeBoundTest {

    public void detectWithoutAnyPortDataAvailable() {
        assertEquals(ListeningPortDetector.detect(), -1);
    }

    public void detectByFakingTomcatConnector()
            throws MalformedObjectNameException, NotCompliantMBeanException, InstanceAlreadyExistsException,
            MBeanRegistrationException, InstanceNotFoundException {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName name = new ObjectName("Tomcat:type=Connector,port=9091");
        try {
            mbs.registerMBean(new TomcatUtilsTest.DummyTomcatConnector(), name);
            assertEquals(ListeningPortDetector.detect(), 9091, "Wrong tomcat port detected");
        } finally {
            if (mbs.isRegistered(name)) {
                mbs.unregisterMBean(name);
            }
        }
    }

    public void detectByFakingTomcatHttpsConnector() throws Exception {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName https = new ObjectName("Tomcat:type=Connector,port=9043");
        try {
            mbs.registerMBean(new TomcatUtilsTest.DummyTomcatConnector(), https);
            assertEquals(ListeningPortDetector.detect(), 9043, "Wrong tomcat port detected");
        } finally {
            if (mbs.isRegistered(https)) {
                mbs.unregisterMBean(https);
            }
        }
    }

    public void detectBySystemProperty() {
        System.setProperty(ListeningPortDetector.SYS_ARTIFACTORY_PORT, "8787");
        try {
            assertEquals(ListeningPortDetector.detect(), 8787);
        } finally {
            System.clearProperty(ListeningPortDetector.SYS_ARTIFACTORY_PORT);
        }
    }

    public void detectByBadSystemProperty() {
        System.setProperty(ListeningPortDetector.SYS_ARTIFACTORY_PORT, "not_int");
        try {
            assertEquals(ListeningPortDetector.detect(), -1);
        } finally {
            System.clearProperty(ListeningPortDetector.SYS_ARTIFACTORY_PORT);
        }
    }
}