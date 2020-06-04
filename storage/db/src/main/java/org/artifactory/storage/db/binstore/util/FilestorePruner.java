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

package org.artifactory.storage.db.binstore.util;

import com.google.common.collect.Sets;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.common.ConstantValues;
import org.artifactory.storage.StorageException;
import org.artifactory.storage.db.binstore.entity.BinaryEntity;
import org.jfrog.storage.binstore.common.BinaryElementRequestImpl;
import org.jfrog.storage.binstore.ifc.model.BinaryElement;
import org.jfrog.storage.binstore.providers.base.BinaryProviderBase;
import org.jfrog.storage.common.StorageUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.jfrog.storage.binstore.providers.collector.BinaryProviderFileListCollector.getFileListStream;

/**
 * Prunes the underlying filestore, receives {@link BinaryElement} prune candidates from an incoming stream supplied by
 * the given {@link BinaryProviderBase}.
 *
 * @author Gidi Shabat
 * @author Dan Feldman
 */
public class FilestorePruner {
    private static final Logger log = LoggerFactory.getLogger(FilestorePruner.class);

    private Predicate<String> isActivelyUsed;
    private BiFunction<ChecksumType, Collection<String>, Collection<BinaryEntity>> searchInRegistry;
    private BinaryProviderBase pruneFrom;
    private BasicStatusHolder statusHolder;
    private long filesMoved = 0;
    private long totalSize = 0;
    private int chunkSize = ConstantValues.binaryProviderPruneChunkSize.getInt();

    public FilestorePruner(BinaryProviderBase pruneFrom, Predicate<String> isActivelyUsed,
            BiFunction<ChecksumType, Collection<String>, Collection<BinaryEntity>> searchInRegistry,
            BasicStatusHolder statusHolder) {
        this.isActivelyUsed = isActivelyUsed;
        this.searchInRegistry = searchInRegistry;
        this.pruneFrom = pruneFrom;
        this.statusHolder = statusHolder;
    }

    public void prune() {
        long start = System.currentTimeMillis();
        try (PipedInputStream in = new PipedInputStream();
             PipedOutputStream out = new PipedOutputStream(in);
             ObjectInputStream stream = getFileListStream(pruneFrom, null, null, in, out)) {
            HashSet<BinaryElement> chunk = Sets.newHashSet();
            while (true) {
                BinaryElement binaryInfo = addNextObjectToChunk(stream, chunk);
                if (chunk.size() >= chunkSize || binaryInfo == null) {
                    Map<String, BinaryEntity> inStore = validExistingChecksums(chunk);
                    processChunk(chunk, inStore);
                    if (binaryInfo == null) {
                        break;
                    } else {
                        chunk.clear();
                    }
                }
            }
        } catch (Exception e) {
            statusHolder.error("Failed to execute prune, cause: " + e.getMessage(), e, log);
            log.error("Failed to execute prune, cause: ", e);
        }

        long tt = (System.currentTimeMillis() - start);
        statusHolder.status("Removed " + filesMoved
                + " files in total size of " + StorageUnit.toReadableString(totalSize)
                + " (" + tt + "ms).", log);
    }

    /**
     * Runs on all prune candidates in the given {@param chunk}. A binary will be deleted from the filestore if:
     * 1. It is not registered in the Binary Registry (Binaries Table in this case)
     * 2. It is not actively used currently (i.e. open as a stream for download).
     *
     * @param chunk              The chunk of prune candidates to process
     * @param binariesInRegistry All Binaries that are currently in the Binaries table (binary registry)
     */
    private void processChunk(HashSet<BinaryElement> chunk, Map<String, BinaryEntity> binariesInRegistry) {
        for (BinaryElement data : chunk) {
            if (!binariesInRegistry.keySet().contains(data.getSha1())) {
                if (isActivelyUsed.test(data.getSha1())) {
                    statusHolder.status("Skipping deletion for in-use artifact record: " + data.getSha1(), log);
                } else {
                    boolean delete = pruneFrom.delete(new BinaryElementRequestImpl(data.getSha1()));
                    if (!delete) {
                        statusHolder.error("Could not delete file " + data.getSha1(), log);
                    } else {
                        filesMoved++;
                        totalSize += data.getLength();
                    }
                }
            }
        }
    }

    private BinaryElement addNextObjectToChunk(ObjectInputStream stream, HashSet<BinaryElement> chunk) throws IOException, ClassNotFoundException {
        BinaryElement binaryInfo = null;
        try {
            binaryInfo = (BinaryElement) stream.readObject();
            if (binaryInfo != null) {
                chunk.add(binaryInfo);
            }
        } catch (EOFException eof) {
            // Just in case
            log.debug("EOF received on incoming file list stream: ", eof);
        }
        return binaryInfo;
    }

    /**
     * @return A mapping of sha1 -> {@link BinaryEntity} of all {@link BinaryElement} in the current {@param chunk}
     * That are valid and exist in the Binaries table.
     */
    private Map<String, BinaryEntity> validExistingChecksums(HashSet<BinaryElement> chunk) {
        return isInBinaryRegistry(chunk.stream()
                .map(BinaryElement::getSha1)
                .collect(Collectors.toSet()));
    }

    /**
     * @return A mapping of sha1 -> {@link BinaryEntity} of all checksums from {@param sha1List} that exist in the
     * Binaries table.
     */
    private Map<String, BinaryEntity> isInBinaryRegistry(Set<String> sha1List) {
        try {
            return searchInRegistry.apply(ChecksumType.sha1, sha1List)
                    .stream()
                    .collect(Collectors.toMap(BinaryEntity::getSha1, binaryEntity -> binaryEntity));
        } catch (Exception e) {
            throw new StorageException("Could search for checksum list!", e);
        }
    }
}
