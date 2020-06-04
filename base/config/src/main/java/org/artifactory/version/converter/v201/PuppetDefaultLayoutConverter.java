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

package org.artifactory.version.converter.v201;

import org.artifactory.version.converter.XmlConverter;
import org.artifactory.version.converter.v160.AddonsDefaultLayoutConverterHelper;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jainish shah
 */
public class PuppetDefaultLayoutConverter implements XmlConverter {
    private static final Logger log = LoggerFactory.getLogger(PuppetDefaultLayoutConverter.class);

    @Override
    public void convert(Document doc) {
        log.info("Starting the puppet repository layout conversion");
        Element rootElement = doc.getRootElement();
        Namespace namespace = rootElement.getNamespace();

        log.debug("Adding puppet default layouts");
        Element repoLayoutsElement = rootElement.getChild("repoLayouts", namespace);
        addPuppetDefaultLayout(repoLayoutsElement, namespace);

        log.info("Ending the puppet repository layout conversion");
    }

    private void addPuppetDefaultLayout(Element repoLayoutsElement, Namespace namespace) {
        repoLayoutsElement.addContent(
                AddonsDefaultLayoutConverterHelper.getRepoLayoutElement(repoLayoutsElement, namespace,
                        "puppet-default",
                        "[orgPath]/[module]/[orgPath]-[module]-[baseRev].tar.gz",
                        "false", null,
                        ".*",
                        ".*"));
    }
}
