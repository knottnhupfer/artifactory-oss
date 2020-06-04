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

package org.artifactory.io.checksum;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.artifactory.checksum.ChecksumInfo;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.checksum.ChecksumsInfo;
import org.jfrog.security.util.Pair;
import org.jfrog.storage.binstore.utils.Checksum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @author Chen Keinan
 */
public abstract class ChecksumUtils {

    private static final Logger log = LoggerFactory.getLogger(ChecksumUtils.class);

    /**
     * Reads and formats the checksum value from the given stream of a checksum file
     *
     * @param inputStream The input stream of a checksum file
     * @return Extracted checksum value
     *
     * @throws IOException If failed to read from the input stream
     */
    @SuppressWarnings({"unchecked"})
    public static String checksumStringFromStream(InputStream inputStream) throws IOException {
        List<String> lineList = IOUtils.readLines(inputStream, "utf-8");
        for (String line : lineList) {
            //Make sure the line isn't blank or commented out
            if (StringUtils.isNotBlank(line) && !line.startsWith("//")) {
                //Remove white spaces at the end
                line = line.trim();
                //Check for 'MD5 (name) = CHECKSUM'
                int prefixPos = line.indexOf(")= ");
                if (prefixPos != -1) {
                    line = line.substring(prefixPos + 3);
                }
                //We don't simply return the file content since some checksum files have more
                //characters at the end of the checksum file.
                return StringUtils.split(line)[0];
            }
        }
        return "";
    }

    /**
     * Calculates the sha1 checksum of the input stream. The input stream is consumed fully and closed by this method.
     *
     * @param is The input stream to calculates checksum on
     * @return SHA-1 checksum of the input stream content
     */
    public static String calculateSha1(InputStream is) {
        return getChecksum(is, ChecksumType.sha1);
    }

    /**
     * Calculates the sha2 checksum of the input stream. The input stream is consumed fully and closed by this method.
     *
     * @param is The input stream to calculates checksum on
     * @return SHA-256 checksum of the input stream content
     */
    public static String calculateSha2(InputStream is) {
        return getChecksum(is, ChecksumType.sha256);
    }

    /**
     * Calculate a single checksum for the input stream
     *
     * @param inputStream  The input stream to calculate the checkum value of
     * @param checksumType The type of checksum to calculate
     * @return The hexadecimal string of the checksum
     */
    public static String getChecksum(InputStream inputStream, ChecksumType checksumType) {
        Checksum checksum = createFromType(checksumType);
        try {
            byte[] dataBytes = new byte[1024];
            int nread;
            while ((nread = inputStream.read(dataBytes)) != -1) {
                checksum.update(dataBytes, 0, nread);
            }
        } catch (IOException e) {
            log.error("Could not calculate checksum " + checksumType.name() + " due to: " + e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        checksum.calc();
        return checksum.getChecksum();
    }

    public static ChecksumsInfo getChecksumsInfo(File file) throws IOException {
        return getChecksumsInfo(calculate(file, ChecksumType.BASE_CHECKSUM_TYPES));
    }

    public static ChecksumsInfo getChecksumsInfo(InputStream in) throws IOException {
        return getSizeAndChecksumsInfo(in).getSecond();
    }

    public static Pair<Long, ChecksumsInfo> getSizeAndChecksumsInfo(InputStream in) throws IOException {
        ChecksumType[] types = ChecksumType.BASE_CHECKSUM_TYPES;
        return getSizeAndChecksumsInfo(in, types);
    }

    public static Pair<Long, ChecksumsInfo> getSizeAndChecksumsInfoWithoutClosingIS(InputStream in) throws IOException {
        Pair<Long, Checksum[]> sizeAndChecksums = calculateWithLength(in, false, ChecksumType.BASE_CHECKSUM_TYPES);
        return new Pair<>(sizeAndChecksums.getFirst(), getChecksumsInfo(sizeAndChecksums.getSecond()));
    }

    static Pair<Long, ChecksumsInfo> getSizeAndChecksumsInfo(InputStream in, ChecksumType[] types) throws IOException {
        Pair<Long, Checksum[]> sizeAndChecksums = calculateWithLength(in,true, types);
        return new Pair<>(sizeAndChecksums.getFirst(), getChecksumsInfo(sizeAndChecksums.getSecond()));
    }

    private static ChecksumsInfo getChecksumsInfo(Checksum[] checksums) {
        ChecksumsInfo result = new ChecksumsInfo();
        for (Checksum checksum : checksums) {
            ChecksumType checksumType = ChecksumType.forAlgorithm(checksum.getAlgorithm());
            String calculatedChecksum = checksum.getChecksum();
            ChecksumInfo missingChecksumInfo = new ChecksumInfo(checksumType, calculatedChecksum, calculatedChecksum);
            result.addChecksumInfo(missingChecksumInfo);
        }
        return result;
    }

    private static Checksum createFromType(ChecksumType type) {
        return new Checksum(type.alg(), type.length());
    }

    /**
     * Calculate checksums for all the input types. Closes the input stream when requested.
     *
     * @param in    Input streams for which checksums are calculated
     * @param types Checksum types to calculate
     * @return Pair where the first element is the amount of bytes read and the second is array of all computed
     * checksums
     *
     * @throws IOException On any exception reading from the stream
     */
    private static Pair<Long, Checksum[]> calculateWithLength(InputStream in, boolean closeInputStreamWhenDone, ChecksumType... types) throws IOException {
        Checksum[] checksums = new Checksum[types.length];
        for (int i = 0; i < types.length; i++) {
            checksums[i] = createFromType(types[i]);
        }

        long bytesRead = 0L;
        try {
            byte[] bytes = new byte[1024];
            int nread;
            while ((nread = in.read(bytes)) != -1) {
                bytesRead += nread;
                for (Checksum checksum : checksums) {
                    checksum.update(bytes, 0, nread);
                }
            }
        } finally {
            if (closeInputStreamWhenDone) {
                IOUtils.closeQuietly(in);
            }
            for (Checksum checksum : checksums) {
                checksum.calc();
            }
        }
        return new Pair<>(bytesRead, checksums);
    }

    /**
     * Calculate checksums for all the input types. Closes the input stream when done.
     *
     * @param in    Input streams for which checksums are calculated
     * @param types Checksum types to calculate
     * @return Array of all computed checksums
     *
     * @throws IOException On any exception reading from the stream
     */
    private static Checksum[] calculate(InputStream in, ChecksumType... types) throws IOException {
        return calculateWithLength(in,true, types).getSecond();
    }

    /**
     * Calculate checksums for all the input types.
     *
     * @param file  File for which checksums are calculated
     * @param types Checksum types to calculate
     * @return Array of all computed checksums
     *
     * @throws IOException On any exception reading from the file
     */
    private static Checksum[] calculate(File file, ChecksumType[] types) throws IOException {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            return calculate(fileInputStream, types);
        } finally {
            IOUtils.closeQuietly(fileInputStream);
        }
    }

}
