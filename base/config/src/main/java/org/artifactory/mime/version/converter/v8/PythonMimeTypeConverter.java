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

package org.artifactory.mime.version.converter.v8;

import org.artifactory.mime.MimeType;
import org.artifactory.mime.MimeTypeBuilder;
import org.artifactory.mime.version.converter.MimeTypeConverterBase;
import org.jdom2.Document;

/**
 * Add <code>*.py</code> files as viewable in the Artifactory UI.
 * <p>
 * Adds the following entry to the <code>mimetypes.xml</code> file if it doesn't already exist:
 * <pre>&lt;mimetype type="text/x-python" extensions="py" viewable="true" syntax="python"/&gt;</pre>
 * </p>
 *
 * @author Yinon Avraham
 */
public class PythonMimeTypeConverter extends MimeTypeConverterBase {

    @Override
    public void convert(Document doc) {
        MimeType pyMimeType = new MimeTypeBuilder("text/x-python")
                .extensions("py")
                .viewable(true)
                .css("python")
                .syntax("python")
                .build();
        addIfNotExist(doc, pyMimeType);
    }
}
