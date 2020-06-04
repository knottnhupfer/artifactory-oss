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

import org.apache.commons.io.IOUtils;
import org.artifactory.request.range.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Gidi Shabat
 */
public class SingleRangeInputStream extends InputStream {
    public static final Logger log = LoggerFactory.getLogger(SingleRangeSkipInputStream.class);
    public static final int BUFFER_SIZE = 1024 * 8;
    private long left;
    private InputStream inputStream;

    public SingleRangeInputStream(Range range, InputStream inputStream) throws IOException {
        // Skip unwanted bytes using a buffer to maximise use of the native buffer
        long toSkip = range.getStart();
        if (toSkip > 0L) {
            long currentPos = 0;
            byte[] bytes = new byte[(int) Math.min(BUFFER_SIZE, toSkip)];
            while (currentPos < toSkip) {
                int len = (int) Math.min((long) bytes.length, toSkip - currentPos);
                int read = inputStream.read(bytes, 0, len);
                if (read == -1) {
                    IOException ex = new IOException("Trying to manually skip to " + toSkip + " and got end of file");
                    log.error(ex.getMessage(), ex);
                    throw ex;
                }
                currentPos += read;
            }
        }

        // Limit the stream
        this.left = range.getEnd() - toSkip + 1;
        this.inputStream = inputStream;
    }

    @Override
    public int read() throws IOException {

        if (left == 0) {
            // Make sure to read the file until EOF and return -1 EOF for end of range
            int result = inputStream.read();
            while (result >= 0) {
                result = inputStream.read();
            }
            return result;
        }
        int result = inputStream.read();
        if (result != -1) {
            --left;
        }
        return result;
    }

    @Override
    public void close() {
        IOUtils.closeQuietly(inputStream);
    }

}
