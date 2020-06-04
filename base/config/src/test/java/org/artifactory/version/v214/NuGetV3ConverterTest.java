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

package org.artifactory.version.v214;

import org.artifactory.convert.XmlConverterTest;
import org.artifactory.util.XmlUtils;
import org.jdom2.Document;
import org.jfrog.common.ResourceUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.InputStream;

/**
 * Test the addition 'v3FeedUrl' under each remote NuGet config section.
 *
 * @author Maxim Yurkovsky
 */
public class NuGetV3ConverterTest extends XmlConverterTest {

    private final NuGetV3Converter converter = new NuGetV3Converter();

    @Test
    public void testConvert() throws Exception {
        String CONFIG_XML = "/config/test/config.2.1.4.without_nuget_v3_feed_url.xml";
        String CONFIG_XML_EXPECTED = "/config/test/config.2.1.4.with_nuget_v3_feed_url.xml";
        InputStream is = ResourceUtils.getResource(CONFIG_XML_EXPECTED);
        Document expected = XmlUtils.parse(is);

        Document actual = convertXml(CONFIG_XML, converter);

        Assert.assertEquals(expected.getRootElement().getTextNormalize(), actual.getRootElement().getTextNormalize());
    }
}