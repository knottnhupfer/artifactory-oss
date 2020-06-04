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

package org.artifactory.version.converter.v215;

import org.artifactory.convert.XmlConverterTest;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;

/**
 * RTFACT-16408 - Test conan default distribution ru converter.
 *
 * @author Dudi Morad
 */
public class ConanDefaultDistributionRuleConverterTest extends XmlConverterTest {

    private final ConanDefaultDistributionRuleConverter converter = new ConanDefaultDistributionRuleConverter();

    @Test
    public void testConvert() throws Exception {
        String CONFIG_XML = "/config/test/config.2.1.5.with_malformed_conan_distribution_rule.xml";
        Document actual = convertXml(CONFIG_XML, converter);
        validateXml(actual);
    }

    private void validateXml(Document document) {
        Element root = document.getRootElement();
        Namespace ns = root.getNamespace();

        List<Element> distributionRepositories = root.getChild("distributionRepositories", ns).getChildren();
        Element distLocal = distributionRepositories.get(0);
        Element conanDefaultRule = distLocal.getChild("rules", ns).getChildren().get(2);
        assertCoordinates(conanDefaultRule, ns, "${module}:${org}", "${baseRev}:${channel}", "${artifactPath}");

        Element conanDefault2Rule = distLocal.getChild("rules", ns).getChildren().get(3);
        assertCoordinates(conanDefault2Rule, ns, "${module}:${org}", "${baseRev}:${channel}", "${artifactPath}");

        Element dist_local2 = distributionRepositories.get(1);
        conanDefaultRule = dist_local2.getChild("rules", ns).getChildren().get(2);
        assertCoordinates(conanDefaultRule, ns, "${module}:${org}", "${baseRev}:${channel}", "${artifactPath}");
    }

    private void assertCoordinates(Element rule, Namespace ns, String pkg, String version, String path) {
        Element distributionCoordinates = rule.getChild("distributionCoordinates", ns);
        assertEquals(distributionCoordinates.getChildText("pkg", ns), pkg);
        assertEquals(distributionCoordinates.getChildText("version", ns), version);
        assertEquals(distributionCoordinates.getChildText("path", ns), path);
    }

}