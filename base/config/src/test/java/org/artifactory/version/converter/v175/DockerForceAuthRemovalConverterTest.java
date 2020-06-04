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

package org.artifactory.version.converter.v175;

import org.artifactory.convert.XmlConverterTest;
import org.artifactory.util.XmlUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jfrog.common.ResourceUtils;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.util.List;

import static org.testng.Assert.*;

/**
 * Test the removal of the forceDockerAuthentication tag. Each repo type in the descriptor which used for this test
 * has one repo with the 'forceDockerAuthentication' enabled.
 *
 * @author Shay Bagants
 */
public class DockerForceAuthRemovalConverterTest extends XmlConverterTest {

    private String CONFIG_XML = "/config/test/config.1.7.5_docker_force_auth.xml";

    @Test
    public void convert() throws Exception {
        Document document = convertXml(CONFIG_XML, new DockerForceAuthRemovalConverter());
        Element rootElement = document.getRootElement();
        Namespace namespace = rootElement.getNamespace();
        validateConfig(rootElement.getChild("localRepositories", namespace), namespace);
        validateConfig(rootElement.getChild("remoteRepositories", namespace), namespace);
        validateConfig(rootElement.getChild("virtualRepositories", namespace), namespace);
        validateConfig(rootElement.getChild("distributionRepositories", namespace), namespace);
    }

    private void validateConfig(Element repos, Namespace namespace) {
        Element originalRepos = getOriginalRepos(repos.getName());
        List<Element> children = repos.getChildren();
        assertNotNull(children);
        assertFalse(children.isEmpty());
        //ensure that the original repos count and modified descriptor repo counts are equals
        assertEquals(children.size(), originalRepos.getChildren().size());

        children.forEach(repoElement -> assertNull(repoElement.getChild("forceDockerAuthentication", namespace)));
    }

    /**
     * @param repositoriesTag the relevant tag of repositories to return, e.g. 'localRepositories', 'remoteRepositories'.
     * @return Element object that represent the repositories from the original config descriptor
     */
    private Element getOriginalRepos(String repositoriesTag) {
        InputStream is = ResourceUtils.getResource(CONFIG_XML);
        Document originalDoc = XmlUtils.parse(is);
        Element rootElement = originalDoc.getRootElement();
        Namespace namespace = rootElement.getNamespace();
        return rootElement.getChild(repositoriesTag, namespace);
    }
}
