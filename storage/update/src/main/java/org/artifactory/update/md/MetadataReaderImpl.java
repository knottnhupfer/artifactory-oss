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

package org.artifactory.update.md;

import org.artifactory.common.MutableStatusHolder;
import org.artifactory.fs.MetadataEntryInfo;
import org.artifactory.sapi.fs.MetadataReader;
import org.artifactory.update.md.current.PassThroughMetadataReaderImpl;

import java.io.File;
import java.util.List;

/**
 * @author freds
 */
public class MetadataReaderImpl implements MetadataReader {


    private final MetadataReader delegate = new PassThroughMetadataReaderImpl();

    @Override
    public List<MetadataEntryInfo> getMetadataEntries(File file, MutableStatusHolder status) {
        return delegate.getMetadataEntries(file, status);
    }

    @Override
    public MetadataEntryInfo convertMetadataEntry(MetadataEntryInfo metadataEntryInfo) {
        return delegate.convertMetadataEntry(metadataEntryInfo);
    }
}
