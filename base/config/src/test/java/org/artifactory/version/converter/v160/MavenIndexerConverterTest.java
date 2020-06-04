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

package org.artifactory.version.converter.v160;

import com.google.common.collect.Lists;
import org.artifactory.convert.XmlConverterTest;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.testng.annotations.Test;

import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * @author Shay Yaakov
 */
@Test
public class MavenIndexerConverterTest extends XmlConverterTest {

    @Test
    public void convertIndexerEnabled() throws Exception {
        Document document = convertXml("/config/test/config-1.5.13-maven_indexer_enabled.xml",
                new SingleRepoTypeConverter());
        new MavenIndexerConverter().convert(document);
        Element rootElement = document.getRootElement();
        Namespace namespace = rootElement.getNamespace();

        Element indexer = rootElement.getChild("indexer", namespace);
        assertNull(indexer.getChild("excludedRepositories", namespace));
        Element includedRepositories = indexer.getChild("includedRepositories", namespace);
        assertEquals(includedRepositories.getChildren().stream().map(Element::getText).collect(Collectors.toList()),
                Lists.newArrayList("local2", "remote1", "virtual1"));
    }

    @Test
    public void convertNoExcludedRepos() throws Exception {
        Document document = convertXml("/config/test/config-1.5.13-maven_indexer_no_excluded.xml",
                new MavenIndexerConverter());
        Element rootElement = document.getRootElement();
        Namespace namespace = rootElement.getNamespace();

        Element indexer = rootElement.getChild("indexer", namespace);
        assertNull(indexer.getChild("excludedRepositories", namespace));
        assertNull(indexer.getChild("includedRepositories", namespace));
    }
}