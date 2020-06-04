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

package org.artifactory.factory.xstream;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.artifactory.checksum.ChecksumInfo;
import org.artifactory.checksum.ChecksumsInfo;

import java.util.HashSet;
import java.util.Set;

/**
 * Date: 8/6/11
 * Time: 5:28 PM
 *
 * @author Fred Simon
 */
public class ChecksumsInfoConverter implements Converter {

    private static final String CHECKSUMS_FIELD_NAME = "checksums";

    @Override
    public boolean canConvert(Class type) {
        return type.equals(ChecksumsInfo.class);
    }

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        ChecksumsInfo checksumsInfo = (ChecksumsInfo) source;
        writer.startNode(CHECKSUMS_FIELD_NAME);
        context.convertAnother(checksumsInfo.getChecksums());
        writer.endNode();
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        reader.moveDown();
        if (!CHECKSUMS_FIELD_NAME.equals(reader.getNodeName())) {
            throw new ConversionException("ChecksumsInfo field should contains a checksums entry!");
        }
        ChecksumsInfo result = new ChecksumsInfo();
        Set<ChecksumInfo> checksums = (Set<ChecksumInfo>) context.convertAnother(result, HashSet.class);
        result.setChecksums(checksums);
        reader.moveUp();
        return result;
    }
}
