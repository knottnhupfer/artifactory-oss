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

package org.artifactory.md;

import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.fs.FileInfo;
import org.artifactory.fs.MutableFileInfo;

/**
 * @author freds
 */
public class FileInfoPersistenceHandler extends AbstractPersistenceHandler<FileInfo, MutableFileInfo> {

    public FileInfoPersistenceHandler(XmlMetadataProvider<FileInfo, MutableFileInfo> xmlProvider) {
        super(xmlProvider);
    }

    @Override
    public MutableFileInfo copy(FileInfo original) {
        return InfoFactoryHolder.get().copyFileInfo(original);
    }
}
