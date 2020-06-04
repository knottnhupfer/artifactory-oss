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

package org.artifactory.request.range.stream;

import com.google.common.io.ByteStreams;
import org.artifactory.request.range.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Returns single sub-stream by wrapping the stream and skipping irrelevant bytes
 *
 * @author Gidi Shabat
 */
public class SingleRangeSkipInputStream extends FilterInputStream {
    public static final Logger log = LoggerFactory.getLogger(SingleRangeSkipInputStream.class);

    private static InputStream wrapInputStream(Range range, InputStream inputStream) throws IOException {
        // Skip irrelevant bytes
        ByteStreams.skipFully(inputStream, range.getStart());
        // Limit the stream
        return ByteStreams.limit(inputStream, range.getEnd() - range.getStart() + 1);
    }

    public SingleRangeSkipInputStream(Range range, InputStream inputStream) throws IOException {
        super(wrapInputStream(range, inputStream));
    }
}
