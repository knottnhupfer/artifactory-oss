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

package org.artifactory.version.converter.v171;

import org.artifactory.convert.XmlConverterTest;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Shay Yaakov
 */
public class SimpleLayoutConverterTest extends XmlConverterTest {

    @Test
    public void convert() throws Exception {
        Document document = convertXml("/config/test/config-1.6.8-expires_in.xml", new SimpleLayoutConverter());
        Element rootElement = document.getRootElement();
        Namespace namespace = rootElement.getNamespace();
        List<Element> repoLayouts = rootElement.getChild("repoLayouts", namespace).getChildren();
        boolean foundSimpleLayout = false;
        for (Element repoLayout : repoLayouts) {
            String layoutName = repoLayout.getChild("name", namespace).getText();
            if (layoutName.equals("simple-default")) {
                foundSimpleLayout = true;
                String pattern = repoLayout.getChild("artifactPathPattern", namespace).getText();
                assertEquals(pattern, "[orgPath]/[module]/[module]-[baseRev].[ext]");
                break;
            }
        }
        assertTrue(foundSimpleLayout);
    }
}