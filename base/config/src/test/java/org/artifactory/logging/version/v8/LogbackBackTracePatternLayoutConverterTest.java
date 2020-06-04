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

package org.artifactory.logging.version.v8;

import org.artifactory.convert.XmlConverterTest;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.testng.annotations.Test;

import java.util.Optional;

import static org.artifactory.logging.version.v8.LogbackBackTracePatternLayoutConverter.NEW_LAYOUT_CLASS_NAME;
import static org.artifactory.logging.version.v8.LogbackBackTracePatternLayoutConverter.OLD_LAYOUT_CLASS_NAME;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

/**
 * @author Yinon Avraham.
 */
public class LogbackBackTracePatternLayoutConverterTest extends XmlConverterTest {

    @Test
    public void testConvert() throws Exception {
        Document doc = convertXml("/org/artifactory/logging/version/v8/logback_before.xml",
                new LogbackBackTracePatternLayoutConverter());
        Element root = doc.getRootElement();
        Namespace ns = root.getNamespace();

        Optional<Element> oldClassName = root.getChildren("appender", ns).stream()
                .flatMap(appender -> appender.getChildren("encoder", ns).stream())
                .flatMap(encoder -> encoder.getChildren("layout", ns).stream())
                .filter(layout -> OLD_LAYOUT_CLASS_NAME.equals(layout.getAttributeValue("class", ns)))
                .findFirst();
        assertFalse(oldClassName.isPresent(), "Old class name is still present - was not converted.");

        long count = root.getChildren("appender", ns).stream()
                .flatMap(appender -> appender.getChildren("encoder", ns).stream())
                .flatMap(encoder -> encoder.getChildren("layout", ns).stream())
                .filter(layout -> NEW_LAYOUT_CLASS_NAME.equals(layout.getAttributeValue("class", ns)))
                .count();
        assertEquals(count, 2, "Number of new layout class name is not as expected.");
    }
}