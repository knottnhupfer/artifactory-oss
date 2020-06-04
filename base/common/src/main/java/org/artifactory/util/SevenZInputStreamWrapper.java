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

package org.artifactory.util;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.io.FileUtils;
import org.artifactory.api.context.ContextHelper;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * This is not really an input stream. 7Zip does not support streaming, as it jumps back and forth with all it's headers
 * The solution was saving it to a temp file, and opening file channel.
 *
 * @author nadavy
 */
public class SevenZInputStreamWrapper extends ArchiveInputStream {

    private SevenZFile sevenZFile;
    private File file;

    SevenZInputStreamWrapper(InputStream inputStream) throws IOException {
        file = ContextHelper.get() != null ?
                new File(ContextHelper.get().getArtifactoryHome().getTempWorkDir(), UUID.randomUUID().toString()) :
                File.createTempFile(UUID.randomUUID().toString(), ".7z");
        Files.writeToFileAndClose(inputStream, file);
        sevenZFile = new SevenZFile(file);
    }

    @Override
    public int read(@Nonnull byte[] b) throws IOException {
        return sevenZFile.read(b);
    }

    @Override
    public int read(@Nonnull byte[] b, int off, int len) throws IOException {
        return sevenZFile.read(b, off, len);
    }

    @Override
    public ArchiveEntry getNextEntry() throws IOException {
        return sevenZFile.getNextEntry();
    }

    @Override
    public void close() throws IOException {
        sevenZFile.close();
        FileUtils.deleteQuietly(file);
    }

    @Override
    protected void pushedBackBytes(long pushedBack) {
        throw new UnsupportedOperationException(
                "SevenZInputStreamWrapper does not support pushback method");
    }

    @Override
    public long skip(long n) {
        throw new UnsupportedOperationException(
                "SevenZInputStreamWrapper does not support skip method");
    }

    @Override
    public int available() {
        throw new UnsupportedOperationException(
                "SevenZInputStreamWrapper does not support available method");
    }
}
