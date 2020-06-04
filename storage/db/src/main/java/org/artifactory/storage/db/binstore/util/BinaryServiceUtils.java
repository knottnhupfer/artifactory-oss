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

import org.artifactory.checksum.ChecksumType;
import org.artifactory.storage.binstore.service.BinaryInfo;
import org.artifactory.storage.db.binstore.dao.BinariesDao;
import org.artifactory.storage.db.binstore.entity.BinaryEntity;
import org.artifactory.storage.db.binstore.service.BinaryServiceInputStream;
import org.artifactory.storage.db.servers.model.ArtifactoryServer;
import org.artifactory.storage.model.FileBinaryProviderInfo;
import org.jfrog.storage.binstore.common.ReaderTrackingInputStream;
import org.jfrog.storage.binstore.ifc.BinaryProviderInfo;
import org.jfrog.storage.binstore.ifc.ClusterNode;
import org.jfrog.storage.binstore.ifc.UsageTracking;
import org.jfrog.storage.binstore.ifc.model.BinaryTreeElement;

import javax.annotation.Nonnull;
import java.io.File;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;

/**
 * @author Dan Feldman
 */
public class BinaryServiceUtils {

    private BinaryServiceUtils() {
    }

    /**
     * Collects a list of file binaries providers
     */
    public static void collectBinaryProviderInfo(List<BinaryTreeElement<BinaryProviderInfo>> list,
            BinaryTreeElement<BinaryProviderInfo> binaryProvidersInfo) {
        if (binaryProvidersInfo == null) {
            return;
        }
        list.add(binaryProvidersInfo);
        collectBinaryProviderInfo(list, binaryProvidersInfo.getNextBinaryTreeElement());
        for (BinaryTreeElement<BinaryProviderInfo> elements : binaryProvidersInfo.getSubBinaryTreeElements()) {
            collectBinaryProviderInfo(list, elements);
        }
    }

    /**
     * Collects a list of file binaries providers
     */
    public static void collectFileBinaryProvidersDirsInternal(List<FileBinaryProviderInfo> list,
            BinaryTreeElement<BinaryProviderInfo> binaryProvidersInfo) {
        if (binaryProvidersInfo == null) {
            return;
        }
        String type = binaryProvidersInfo.getData().getProperties().get("type");
        if ("file-system".equals(type)) {
            FileBinaryProviderInfo info = createFileBinaryProviderInfo(binaryProvidersInfo, type);
            list.add(info);
        }
        if ("cache-fs".equals(type)) {
            FileBinaryProviderInfo info = createCacheFileBinaryProviderInfo(binaryProvidersInfo, type);
            list.add(info);
        }
        collectFileBinaryProvidersDirsInternal(list, binaryProvidersInfo.getNextBinaryTreeElement());
        for (BinaryTreeElement<BinaryProviderInfo> elements : binaryProvidersInfo.getSubBinaryTreeElements()) {
            collectFileBinaryProvidersDirsInternal(list, elements);
        }
    }

    public static boolean isDuplicatedEntryException(SQLException exception) {
        String message = exception.getMessage();
        return message.contains("duplicate key") // Derby message
                || message.contains("Duplicate entry") // MySQL message
                || message.contains("unique constraint"); // Oracle message
    }

    public static ClusterNode clusterNodeFromArtServer(ArtifactoryServer artifactoryServer) {
        return new ClusterNode(artifactoryServer.getContextUrl(), artifactoryServer.getServerId(),
                artifactoryServer.getArtifactoryVersion(), artifactoryServer.getArtifactoryRevision());
    }

    /**
     * Util to allow outer classes to query the {@link BinariesDao} without exposing it.
     *
     * @throws RuntimeException on {@link SQLException} thrown by underlying method
     */
    public static BiFunction<ChecksumType, Collection<String>, Collection<BinaryEntity>> binariesDaoSearch(BinariesDao binariesDao) throws RuntimeException {
        return (checksumType, checksumList) -> {
            try {
                return binariesDao.search(checksumType, checksumList);
            } catch (SQLException sql) {
                throw new RuntimeException(sql);
            }
        };
    }

    private static FileBinaryProviderInfo createCacheFileBinaryProviderInfo(BinaryTreeElement<BinaryProviderInfo> binaryProvidersInfo, String type) {
        return createFileBinaryProviderInfo(binaryProvidersInfo, type);
    }

    private static FileBinaryProviderInfo createFileBinaryProviderInfo(BinaryTreeElement<BinaryProviderInfo> binaryProvidersInfo, String type) {
        String temp = binaryProvidersInfo.getData().getProperties().get("tempDir");
        String binariesDir = binaryProvidersInfo.getData().getProperties().get("binariesDir");
        File tempDir = new File(new File(binariesDir), temp);
        File fileStoreDir = new File(binariesDir);
        return new FileBinaryProviderInfo(tempDir, fileStoreDir, type);
    }

    public static class BinaryServiceInputStreamWrapper extends ReaderTrackingInputStream implements BinaryServiceInputStream {
        private BinaryInfo bi;

        public BinaryServiceInputStreamWrapper(BinaryInfo bi, UsageTracking usageTracking) {
            super(null, bi.getSha1(), usageTracking);
            this.bi = bi;
        }

        @Nonnull
        @Override
        public BinaryInfo getBinaryInfo() {
            return bi;
        }
    }
}
