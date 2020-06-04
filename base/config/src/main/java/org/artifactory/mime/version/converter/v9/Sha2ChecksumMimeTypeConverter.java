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

package org.artifactory.mime.version.converter.v9;

import org.artifactory.mime.version.converter.MimeTypeConverterBase;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Adds sha256 to the list of supported checksums
 *
 * @author Dan Feldman
 */
public class Sha2ChecksumMimeTypeConverter extends MimeTypeConverterBase {

    @Override
    public void convert(Document doc) {
        Element rootElement = doc.getRootElement();
        Namespace namespace = rootElement.getNamespace();
        for (Object mimetype : rootElement.getChildren("mimetype", namespace)) {
            Element mimeTypeElement = (Element) mimetype;
            if ("application/x-checksum".equals(mimeTypeElement.getAttributeValue("type", namespace))) {
                mimeTypeElement.getAttribute("extensions").setValue("sha1, sha256, md5");
            }
        }
    }
}
