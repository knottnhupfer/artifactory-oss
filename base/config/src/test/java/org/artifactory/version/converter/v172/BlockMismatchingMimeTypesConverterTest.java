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

package org.artifactory.version.converter.v172;

import org.artifactory.convert.XmlConverterTest;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

/**
 * @author Dan Feldman
 */
public class BlockMismatchingMimeTypesConverterTest extends XmlConverterTest {

    @Test
    public void convert() throws Exception {
        Document document = convertXml("/config/test/config-1.6.8-expires_in.xml", new BlockMismatchingMimeTypesConverter());
        Element rootElement = document.getRootElement();
        Namespace namespace = rootElement.getNamespace();
        rootElement.getChild("remoteRepositories", namespace).getChildren().stream()
                .forEach(remoteRepo -> {
                    String blockMime = remoteRepo.getChild("blockMismatchingMimeTypes", namespace).getText();
                    assertTrue(blockMime.equals("true"));
                });
    }
}
