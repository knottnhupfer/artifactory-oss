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

package org.artifactory.version.converter.v180;

import com.google.common.collect.Lists;
import org.artifactory.convert.XmlConverterTest;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.output.XMLOutputter;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Dan Feldman
 */
@Test
public class XrayRepoConfigConverterTest extends XmlConverterTest {

    @Test
    public void convert() throws Exception {
        Document document = convertXml("/config/test/config.1.7.9_multi_repos_and_xray.xml",
                new XrayRepoConfigConverter());
        //old tags should be removed
        assertFalse(new XMLOutputter().outputString(document).contains("<xrayIndex>"));
        Element rootElement = document.getRootElement();
        Namespace namespace = rootElement.getNamespace();

        //Existing tags with false should get false in enabled and true in block unscanned
        validateNewTagAdded(rootElement, namespace, false,
                Lists.newArrayList("coco-local", "deb-local", "bower-remote", "docker-rem"));
        //Existing tags with true should get true in enabled and false in block unscanned
        validateNewTagAdded(rootElement, namespace, true, Lists.newArrayList("bower-local", "coco-remote"));
        //distribution and virtual tags should be removed
        validateReposHaveNoTags(rootElement, namespace, "distributionRepositories");
        validateReposHaveNoTags(rootElement, namespace, "virtualRepositories");
    }

    private void validateNewTagAdded(Element rootElement, Namespace namespace, boolean enabled, List<String> repos) {
        List<Element> localRepos = rootElement.getChild("localRepositories", namespace).getChildren();
        List<Element> remoteRepos = rootElement.getChild("remoteRepositories", namespace).getChildren();

        Stream.concat(localRepos.stream(), remoteRepos.stream())
                .filter(repo -> repos.contains(repo.getChild("key", namespace).getText()))
                .forEach(repo -> {
                    Element xray = repo.getChild("xray", namespace);
                    assertTrue(xray != null);
                    assertTrue(Boolean.parseBoolean(xray.getChild("enabled", namespace).getText()) == enabled);
                    assertTrue(xray.getChild("minimumBlockedSeverity", namespace) == null);
                    assertFalse(Boolean.parseBoolean(xray.getChild("blockUnscannedArtifacts", namespace).getText()));
                });
    }

    private void validateReposHaveNoTags(Element rootElement, Namespace namespace, String repoType) {
        Element repos = rootElement.getChild(repoType, namespace);
        repos.getChildren().forEach(repo -> assertTrue(repo.getChild("xrayIndex", namespace) == null));
    }
}
