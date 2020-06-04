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

import org.apache.commons.lang.StringUtils;
import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Removes the (-[fileItegRev]) section from the default simple layout
 *
 * @author Shay Yaakov
 */
public class SimpleLayoutConverter implements XmlConverter {

    private static final Logger log = LoggerFactory.getLogger(SimpleLayoutConverter.class);

    @Override
    public void convert(Document doc) {
        log.info("Starting simple layout conversion");
        Element rootElement = doc.getRootElement();
        Namespace namespace = rootElement.getNamespace();

        Element repoLayouts = rootElement.getChild("repoLayouts", namespace);
        if (repoLayouts != null) {
            for (Element repoLayout : repoLayouts.getChildren()) {
                String layoutName = repoLayout.getChild("name", namespace).getText();
                if (StringUtils.equals(layoutName, "simple-default")) {
                    Element patternElement = repoLayout.getChild("artifactPathPattern", namespace);
                    if (StringUtils.equals(patternElement.getText(), "[orgPath]/[module]/[module]-[baseRev](-[fileItegRev]).[ext]")) {
                        patternElement.setText("[orgPath]/[module]/[module]-[baseRev].[ext]");
                        break;
                    }
                }
            }
        }
        log.info("Finished simple layout conversion");
    }
}
