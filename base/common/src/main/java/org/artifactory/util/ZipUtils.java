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

import com.google.common.collect.Sets;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jfrog.common.ArchiveUtils;
import org.jfrog.common.archive.ArchiveType;
import org.artifactory.common.ConstantValues;
import org.artifactory.sapi.fs.VfsFile;
import org.jfrog.client.util.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.jfrog.common.archive.ArchiveType.*;

/**
 * A utility class to perform different archive related actions
 *
 * @author Noam Tenne
 */
public abstract class ZipUtils {
    private static final Logger log = LoggerFactory.getLogger(ZipUtils.class);

    /**
     * Archives the contents of the given directory into the given archive using the apache commons compress tools
     *
     * @param sourceDirectory    Directory to archive
     * @param destinationArchive Archive file to create
     * @param recurse            True if should recurse file scan of source directory. False if not
     * @throws IOException              Any exceptions that might occur while handling the given files and used streams
     * @throws IllegalArgumentException Thrown when given invalid destinations
     * @see ArchiveUtils#archive(java.io.File, java.io.File, boolean, org.jfrog.common.archive.ArchiveType)
     */
    public static void archive(File sourceDirectory, File destinationArchive, boolean recurse)
            throws IOException {
        ArchiveUtils.archive(sourceDirectory, destinationArchive, recurse, ArchiveType.ZIP);
    }

    /**
     * Extracts the given archive file into the given directory
     *
     * @param sourceArchive        Archive to extract
     * @param destinationDirectory Directory to extract achive to
     * @throws Exception                Any exception which are thrown
     * @throws IllegalArgumentException Thrown when given invalid destinations
     * @throws Exception                Thrown when any error occures while extracting
     */
    public static void extract(File sourceArchive, File destinationDirectory) throws Exception {
        if ((sourceArchive == null) || (destinationDirectory == null)) {
            throw new IllegalArgumentException("Supplied destinations cannot be null.");
        }
        if (!sourceArchive.isFile()) {
            throw new IllegalArgumentException("Supplied source archive must be an existing file.");
        }
        String sourcePath = sourceArchive.getAbsolutePath();
        String destinationPath = destinationDirectory.getAbsolutePath();
        log.debug("Beginning extraction of '{}' into '{}'", sourcePath, destinationPath);
        extractFiles(sourceArchive, destinationDirectory.getCanonicalFile());
        log.debug("Completed extraction of '{}' into '{}'", sourcePath, destinationPath);
    }

    /**
     * @param zis       The zip input stream
     * @param entryPath The entry path to search for
     * @return The entry if found, null otherwise
     *
     * @throws IOException On failure to read the stream
     * @see ZipUtils#locateArchiveEntry(ArchiveInputStream, String, List)
     */
    public static ArchiveEntry locateArchiveEntry(ArchiveInputStream zis, String entryPath) throws IOException {
        return locateArchiveEntry(zis, entryPath, null);
    }

    /**
     * @param zis       The zip input stream
     * @param entryPath The entry path to search for
     * @return The entry if found, null otherwise
     *
     * @throws IOException On failure to read the stream
     * @see ZipUtils#locateEntry(ZipInputStream, String, List)
     */
    public static ZipEntry locateEntry(ZipInputStream zis, String entryPath) throws IOException {
        return locateEntry(zis, entryPath, null);
    }


    /**
     * Searches for an entry inside the zip stream by entry path. If there are alternative extensions, will also look
     * for entry with alternative extension. The search stops reading the stream when the entry is found, so calling
     * read on the stream will read the returned entry. <p/>
     * The zip input stream doesn't support mark/reset so once this method is used you cannot go back - either the
     * stream was fully read (when entry is not found) or the stream was read until the current entry.
     *
     * @param zis                   The zip input stream
     * @param entryPath             The entry path to search for
     * @param alternativeExtensions List of alternative file extensions to try if the main entry path is not found.
     * @return The entry if found, null otherwise
     *
     * @throws IOException On failure to read the stream
     */
    public static ZipEntry locateEntry(ZipInputStream zis, String entryPath, List<String> alternativeExtensions)
            throws IOException {
        ZipEntry zipEntry;
        while ((zipEntry = zis.getNextEntry()) != null) {
            String zipEntryName = zipEntry.getName();
            if (zipEntryName.equals(entryPath)) {
                return zipEntry;
            } else if (alternativeExtensions != null) {
                String basePath = PathUtils.stripExtension(entryPath);
                for (String alternativeExtension : alternativeExtensions) {
                    String alternativeSourcePath = basePath + "." + alternativeExtension;
                    if (zipEntryName.equals(alternativeSourcePath)) {
                        return zipEntry;
                    }
                }
            }
        }
        return null;
    }


    /**
     * Searches for an entry inside the zip stream by entry path. If there are alternative extensions, will also look
     * for entry with alternative extension. The search stops reading the stream when the entry is found, so calling
     * read on the stream will read the returned entry. <p/>
     * The zip input stream doesn't support mark/reset so once this method is used you cannot go back - either the
     * stream was fully read (when entry is not found) or the stream was read until the current entry.
     *
     * @param zis                   The ar input stream
     * @param entryPath             The entry path to search for
     * @param alternativeExtensions List of alternative file extensions to try if the main entry path is not found.
     * @return The entry if found, null otherwise
     *
     * @throws IOException On failure to read the stream
     */
    public static ArchiveEntry locateArchiveEntry(ArchiveInputStream zis, String entryPath,
            List<String> alternativeExtensions)
            throws IOException {
        ArchiveEntry archiveEntry;
        while ((archiveEntry = zis.getNextEntry()) != null) {
            String zipEntryName = archiveEntry.getName();
            if (zipEntryName.equals(entryPath)) {
                return archiveEntry;
            } else if (alternativeExtensions != null) {
                String basePath = PathUtils.stripExtension(entryPath);
                for (String alternativeExtension : alternativeExtensions) {
                    String alternativeSourcePath = basePath + "." + alternativeExtension;
                    if (zipEntryName.equals(alternativeSourcePath)) {
                        return archiveEntry;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Extracts the given archive file into the given directory
     *
     * @param sourceArchive        Archive to extract
     * @param destinationDirectory Directory to extract archive to
     */
    private static void extractFiles(File sourceArchive, File destinationDirectory) {
        ArchiveInputStream archiveInputStream = null;
        try {
            archiveInputStream = createArchiveInputStream(sourceArchive);
            extractFiles(archiveInputStream, destinationDirectory);
        } catch (IOException ioe) {
            log.error("Error while extracting {} : {}", sourceArchive.getPath(), ioe.getCause());
            log.debug("Error while extracting {}", sourceArchive.getPath(), ioe);
            throw new RuntimeException("Error while extracting " + sourceArchive.getPath(), ioe);
        } finally {
            IOUtils.closeQuietly(archiveInputStream);
        }
    }

    public static void extractFiles(ArchiveInputStream archiveInputStream, File destinationDirectory)
            throws IOException {
        ArchiveEntry entry;
        while ((entry = archiveInputStream.getNextEntry()) != null) {
            //Validate entry name before extracting
            String validatedEntryName = validateEntryName(entry.getName());

            if (StringUtils.isNotBlank(validatedEntryName)) {
                // ZipArchiveEntry does not carry relevant info
                // for symlink identification, thus it not supported
                // at this stage
                if (!entry.isDirectory() && entry instanceof TarArchiveEntry
                        && ((TarArchiveEntry) entry).isSymbolicLink()) {
                    extractTarSymLink(destinationDirectory, (TarArchiveEntry) entry);
                } else {
                    extractFile(destinationDirectory, archiveInputStream, validatedEntryName,
                            entry.getLastModifiedDate(), entry.isDirectory());
                }
            }
        }
    }

    /**
     * TarArchiveEntry does not recognize soft link attributes
     * on file decompression and extracts it as ordinal file,
     * this method is manual symlink reconstruction
     *
     * @param dir   destination directory
     * @param entry {@link TarArchiveEntry} entry to create symlink from
     */
    private static void extractTarSymLink(File dir, TarArchiveEntry entry) {
        if (StringUtils.isBlank(entry.getLinkName())) {
            log.warn("Symbolic link '{}' is malformed, ignoring it ...", entry.getName());
            return;
        }
        File linkFile = org.codehaus.plexus.util.FileUtils.resolveFile(dir, entry.getName());
        // we have no clear way in link to understand if link target path is from cwd
        // or from root other than checking if link path starts with pathSeparator
        String linkTarget = entry.getLinkName().startsWith(File.separator) ?
                entry.getLinkName()
                :
                linkFile.getParentFile().getPath() + File.separator + entry.getLinkName();
        try {
            File targetCanonicalFile = new File(linkTarget).getCanonicalFile();
            if (!targetCanonicalFile.getPath().startsWith(dir.getPath())) {
                log.warn("Symlink '{}' is trying to access outer filesystem, ignoring it ...", linkFile.getName());
                log.debug("Symlink '{}' attempted to access outer filesystem '{}'", linkFile.getName(),
                        targetCanonicalFile.getPath());
                return;
            } else if (!targetCanonicalFile.exists()) {
                // we cannot assume that file not exist at this point
                // as the order of extracted content is unpredictable,
                // i.e we may process symlink before actual file is get
                // extracted
                log.debug("Symlink '{}' is referring non existent file '{}', either it not exist or not extracted yet",
                        linkFile.getName(), targetCanonicalFile.getPath());
            }
            log.debug("Creating symlink '{}' to {}", linkFile.getPath(), targetCanonicalFile.getPath());
            java.nio.file.Files.createSymbolicLink(linkFile.toPath(), targetCanonicalFile.toPath());
        } catch (IOException | UnsupportedOperationException e) {
            log.error("Creating symlink has failed, " + e.getMessage());
            log.debug("Cause: {}", e);
        }
    }

    /**
     * get archive input stream from File Object
     *
     * @param sourceArchive - archive File
     * @return archive input stream
     */
    private static ArchiveInputStream createArchiveInputStream(File sourceArchive) {
        String fileName = sourceArchive.getName();
        String extension;
        if (fileName.contains(".tar")) {
            extension = PathUtils.getTarFamilyExtension(fileName);
        } else {
            extension = PathUtils.getExtension(fileName);
        }
        verifySupportedExtension(extension);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(sourceArchive);
            return returnArchiveInputStream(fis, extension);
        } catch (Exception e) {
            IOUtils.closeQuietly(fis);
        }
        throw new IllegalArgumentException("Unsupported archive extension: '" + extension + "'");
    }

    /**
     * get archive input stream from VfsFile Object
     *
     * @param file - archive vfs file
     * @return archive input stream
     */
    public static ArchiveInputStream getArchiveInputStream(VfsFile file) throws IOException {
        String archiveSuffix = file.getPath().toLowerCase();
        return returnArchiveInputStream(file.getStream(), archiveSuffix);
    }

    /**
     * return archive input stream
     *
     * @param inputStream   - file  input Stream
     * @param archiveSuffix - archive suffix
     * @return archive input stream
     */
    public static ArchiveInputStream returnArchiveInputStream(InputStream inputStream, String archiveSuffix)
            throws IOException {
        if (isZipFamilyArchive(archiveSuffix)) {
            return new ZipArchiveInputStream(inputStream);
        }
        if (isTarArchive(archiveSuffix)) {
            return new TarArchiveInputStream(inputStream);
        }
        if (isTgzFamilyArchive(archiveSuffix) || isGzCompress(archiveSuffix)) {
            return new TarArchiveInputStream(new GzipCompressorInputStream(inputStream));
        }
        if (is7ZipArchive(archiveSuffix)) {
            return new SevenZInputStreamWrapper(inputStream);
        }
        if (isTarBzip2Archive(archiveSuffix)) {
            return new TarArchiveInputStream(new BZip2CompressorInputStream(inputStream));
        }

        return new ZipArchiveInputStream(inputStream);
    }

    private static boolean isTarBzip2Archive(String archiveSuffix) {
        return archiveSuffix.endsWith(TARBZ2.value());
    }

    /**
     * get archive input stream array
     *
     * @param file   - file archive
     * @param length - length of array
     * @return -array of archive input stream
     */
    public static ArchiveInputStream[] getArchiveInputStreamArray(String file, int length) {
        String archiveSuffix = file.toLowerCase();
        if (isZipFamilyArchive(archiveSuffix)) {
            return new ZipArchiveInputStream[length];
        }
        if (isTarArchive(archiveSuffix) || isTgzFamilyArchive(archiveSuffix) || isTarBzip2Archive(archiveSuffix)) {
            return new TarArchiveInputStream[length];
        }
        if (is7ZipArchive(archiveSuffix)) {
            return new SevenZInputStreamWrapper[length];
        }
        return new ZipArchiveInputStream[length];
    }

    /**
     * is file suffix related to gz compress
     *
     * @param archiveSuffix - archive file suffix
     */
    private static boolean isGzCompress(String archiveSuffix) {
        return archiveSuffix.equals("gz");
    }

    /**
     * is file suffix related to tar archive
     *
     * @param archiveSuffix - archive suffix
     */
    private static boolean isTarArchive(String archiveSuffix) {
        return archiveSuffix.endsWith(TAR.value());
    }

    private static boolean isTgzFamilyArchive(String archiveSuffix) {
        return archiveSuffix.endsWith(TARGZ.value()) || archiveSuffix.endsWith(TGZ.value());
    }

    private static boolean isZipFamilyArchive(String archiveSuffix) {
        return archiveSuffix.endsWith(ZIP.value()) || archiveSuffix.endsWith("jar") || archiveSuffix.toLowerCase().endsWith(
                "nupkg") || archiveSuffix.toLowerCase().endsWith("conda") || archiveSuffix.endsWith("war");
    }

    /**
     * is file suffix related to 7z compress
     */
    private static boolean is7ZipArchive(String archiveSuffix) {
        return archiveSuffix.endsWith("7z");
    }

    private static void verifySupportedExtension(String extension) {
        Set<String> supportedExtensions;
        try {
            String supportedExtensionsNames = ConstantValues.requestExplodedArchiveExtensions.getString();
            supportedExtensions = Arrays.stream(StringUtils.split(supportedExtensionsNames, ","))
                    .map(input -> StringUtils.isBlank(input) ? input : input.trim()).collect(Collectors.toSet());
        } catch (Exception e) {
            supportedExtensions = Sets.newHashSet();
            log.error("Failed to parse global default excludes. Using default values: {}", e.getMessage());
        }

        if (StringUtils.isBlank(extension) || !supportedExtensions.contains(extension)) {
            throw new IllegalArgumentException("Unsupported archive extension: '" + extension + "'");
        }
    }

    /**
     * Extracts the given zip entry
     *
     * @param destinationDirectory Extracted file destination
     * @param zipInputStream       Input stream of archive
     * @param entryName            Entry to extract
     * @param entryDate            Last modification date of zip entry
     * @param isEntryDirectory     Indication if the entry is a directory or not
     */
    private static void extractFile(File destinationDirectory, InputStream zipInputStream,
            String entryName, Date entryDate, boolean isEntryDirectory) throws IOException {

        File resolvedEntryFile = org.codehaus.plexus.util.FileUtils.resolveFile(destinationDirectory, entryName);
        try {
            File parentFile = resolvedEntryFile.getParentFile();

            //If the parent file isn't null, attempt to create it because it might not exist
            if (parentFile != null && !parentFile.exists() && !parentFile.mkdirs()) {
                log.error("Failed to create directory '{}'", parentFile.getAbsolutePath());
            }

            if (isEntryDirectory) {
                //Create directory entry
                if (!resolvedEntryFile.exists() && resolvedEntryFile.mkdirs()) {
                    log.debug("Created directory '{}'", resolvedEntryFile.getAbsolutePath());
                }
            } else {
                //Extract file entry
                byte[] buffer = new byte[1024];
                int length;
                FileOutputStream fileOutputStream = null;
                try {
                    fileOutputStream = new FileOutputStream(resolvedEntryFile);

                    while ((length = zipInputStream.read(buffer)) >= 0) {
                        fileOutputStream.write(buffer, 0, length);
                    }
                } finally {
                    IOUtils.closeQuietly(fileOutputStream);
                }
            }

            //Preserve last modified date
            if (!resolvedEntryFile.setLastModified(entryDate.getTime())) {
                log.error("Failed to set last modified date of file '{}' to {}", resolvedEntryFile.getAbsolutePath(),
                        entryDate.getTime());
            }
        } catch (FileNotFoundException ex) {
            throw new RuntimeException("Can't extract file " + entryName, ex);
        }
    }

    /**
     * Validates the given entry name by removing different slashes that might appear in the begining of the name and
     * any occurences of relative paths like "../", so we can protect from path traversal attacks
     *
     * @param entryName Name of zip entry
     */
    private static String validateEntryName(String entryName) {
        entryName = FilenameUtils.separatorsToUnix(entryName);
        entryName = PathUtils.trimLeadingSlashes(entryName);
        entryName = removeDotSegments(entryName);

        return entryName;
    }

    //"Borrowed" from com.sun.jersey.server.impl.uri.UriHelper
    // alg taken from http://gbiv.com/protocols/uri/rfc/rfc3986.html#relative-dot-segments
    // the alg works as follows:
    //       1. The input buffer is initialized with the now-appended path components and the output buffer is initialized to the empty string.
    //   2. While the input buffer is not empty, loop as follows:
    //         A. If the input buffer begins with a prefix of "../" or "./", then remove that prefix from the input buffer; otherwise,
    //         B. if the input buffer begins with a prefix of "/./"
    //            or "/.", where "." is a complete path segment, then replace that prefix with "/" in the input buffer; otherwise,
    //         C. if the input buffer begins with a prefix of "/../"
    //            or "/..", where ".." is a complete path segment,
    //            then replace that prefix with "/" in the input buffer and remove the last segment and its preceding "/" (if any) from the output buffer; otherwise,
    //         D. if the input buffer consists only of "." or "..", then remove that from the input buffer; otherwise,
    //         E. get the first path segment in the input, validate the segment.
    //            In case the segment is valid, move it from the buffer to the end of the output buffer,
    //            including the initial "/" character (if any) and any subsequent characters up to, but not including,
    //            the next "/" character or the end of the input buffer.
    //            Otherwise, leave the segment as part of the input buffer.
    //   3. Finally, the output buffer is returned as the result of remove_dot_segments.

    @SuppressWarnings({"OverlyComplexMethod"})
    static String removeDotSegments(String path) {

        if (null == path) {
            return null;
        }

        List<String> outputSegments = new LinkedList<>();

        while (path.length() > 0) {
            if (path.startsWith("../")) {   // rule 2A
                path = PathUtils.trimLeadingSlashes(path.substring(3));
            } else if (path.startsWith("./")) { // rule 2A
                path = PathUtils.trimLeadingSlashes(path.substring(2));
            } else if (path.startsWith("/./")) { // rule 2B
                path = "/" + PathUtils.trimLeadingSlashes(path.substring(3));
            } else if ("/.".equals(path)) { // rule 2B
                path = "/";
            } else if (path.startsWith("/../")) { // rule 2C
                path = "/" + PathUtils.trimLeadingSlashes(path.substring(4));
                if (!outputSegments.isEmpty()) { // removing last segment if any
                    outputSegments.remove(outputSegments.size() - 1);
                }
            } else if ("/..".equals(path)) { // rule 2C
                path = "/";
                if (!outputSegments.isEmpty()) { // removing last segment if any
                    outputSegments.remove(outputSegments.size() - 1);
                }
            } else if ("..".equals(path) || ".".equals(path)) { // rule 2D
                path = "";
            } else { // rule E
                int slashStartSearchIndex;
                if (path.startsWith("/")) {
                    path = "/" + PathUtils.trimLeadingSlashes(path.substring(1));
                    slashStartSearchIndex = 1;
                } else {
                    slashStartSearchIndex = 0;
                }
                int segLength = path.indexOf('/', slashStartSearchIndex);
                if (-1 == segLength) {
                    segLength = path.length();
                }
                String segment = path.substring(0, segLength);
                if(!isValidSegment(segment)) {
                    continue;
                }
                outputSegments.add(segment);
                path = path.substring(segLength);
            }
        }

        StringBuilder result = new StringBuilder();
        for (String segment : outputSegments) {
            result.append(segment);
        }

        return PathUtils.trimLeadingSlashes(result.toString());
    }

    private static boolean isValidSegment(String segment) {
        switch (segment) {
            case "/../":
            case "/./":
            case "/.":
            case "/..":
                return false;
            default:
                return true;
        }
    }
}
