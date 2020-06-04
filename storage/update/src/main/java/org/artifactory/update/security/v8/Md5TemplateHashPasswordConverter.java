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

package org.artifactory.update.security.v8;

import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jfrog.access.user.HashEncoderUtils;

import java.util.List;

/**
 * @author Noam Shemesh
 */
public class Md5TemplateHashPasswordConverter implements XmlConverter {

    @Override
    public void convert(Document doc) {
        Element root = doc.getRootElement();
        Namespace namespace = root.getNamespace();
        Element child = root.getChild("users", namespace);
        List users = child.getChildren("user", namespace);
        if (users != null && !users.isEmpty()) {
            for (Object user : users) {
                Element userElement = (Element) user;
                Element password = userElement.getChild("password", namespace);
                if (password != null) {
                    Element salt = userElement.getChild("salt", namespace);

                    String passwordTemplate = convertPasswordHash(password.getText(), salt != null ? salt.getText() : null);
                    if (salt != null) {
                        userElement.removeChild("salt", namespace);
                    }

                    password.setText(passwordTemplate);
                }
            }
        }
    }

    public static String convertPasswordHash(String password, String salt) {
        return HashEncoderUtils.md5(password, salt == null ? "" : salt, 1);
    }
}
