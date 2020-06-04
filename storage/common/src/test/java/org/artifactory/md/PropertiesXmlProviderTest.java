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

package org.artifactory.md;

import org.testng.annotations.Test;

import java.util.Set;

import static org.artifactory.mime.DockerNaming.SHA2_PREFIX;
import static org.junit.Assert.*;

/**
 * @author Rotem Kfir
 */
public class PropertiesXmlProviderTest {

    @Test
    public void testFromXmlPropertyNameWithColon() {
        final String propertiesStr =
                "<?xml version='1.0' encoding='UTF-8'?>\n" +
                        "<properties>\n" +
                        "  <sha256>2f6fb811f836424acf6120ee35e3698ee65861f290332cb009c709c3d5088a82</sha256>\n" +
                        "  <docker.manifest.digest>sha256:2f6fb811f836424acf6120ee35e3698ee65861f290332cb009c709c3d5088a82</docker.manifest.digest>\n" +
                        "  <docker.manifest>image.with.label</docker.manifest>\n" +
                        "  <docker.repoName>repo1</docker.repoName>\n" +
                        "  <docker.manifest.type>application/vnd.docker.distribution.manifest.v2+json</docker.manifest.type>\n" +
                        "  <docker.label.a:b>c</docker.label.a:b>\n" +
                        "</properties>";
        PropertiesXmlProvider xmlProvider = new PropertiesXmlProvider();
        MutablePropertiesInfo propertiesInfo = xmlProvider.fromXml(propertiesStr);
        assertNotNull(propertiesInfo);
        Set<String> properties = propertiesInfo.keySet();
        assertNotNull(properties);
        assertEquals(properties.size(), 6);
        // Check parsing of property with colon in name
        assertTrue(properties.contains("docker.label.a:b"));
        assertEquals(propertiesInfo.get("docker.label.a:b").size(), 1);
        assertEquals(propertiesInfo.getFirst("docker.label.a:b"), "c");
        // Check parsing of property with colon in value
        assertTrue(properties.contains("docker.manifest.digest"));
        assertEquals(propertiesInfo.get("docker.manifest.digest").size(), 1);
        assertEquals(propertiesInfo.getFirst("docker.manifest.digest"), SHA2_PREFIX + "2f6fb811f836424acf6120ee35e3698ee65861f290332cb009c709c3d5088a82");
    }
}