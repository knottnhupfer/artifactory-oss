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

package org.artifactory.backup;

import com.google.common.collect.Lists;
import org.artifactory.api.build.BuildService;
import org.artifactory.api.config.ExportSettingsImpl;
import org.artifactory.api.repo.BackupService;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.descriptor.backup.BackupDescriptor;
import org.artifactory.descriptor.repo.RealRepoDescriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.fs.ItemInfo;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.storage.build.service.BuildStoreService;
import org.artifactory.storage.fs.service.ConfigsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Estimate disk space of a backup descriptor
 *
 * @author nadavy
 */
public class BackupSizeCalculator {

    private static final long METADATA_AVG_SIZE = 512;
    private static final int ROUND_UP_SIZE = 4096;

    private static final Logger log = LoggerFactory.getLogger(BackupSizeCalculator.class);

    private ConfigsService configsService;
    private RepositoryService repositoryService;
    private BuildStoreService buildStoreService;
    private BuildService buildService;
    private BackupService backupService;
    private File dataDir;
    private File etcDir;

    public BackupSizeCalculator(RepositoryService repositoryService, BuildStoreService buildStoreService, BuildService buildService,
            ConfigsService configsService, BackupService backupService, File dataDir, File etcDir) {
        this.repositoryService = repositoryService;
        this.buildStoreService = buildStoreService;
        this.buildService = buildService;
        this.configsService = configsService;
        this.backupService = backupService;
        this.dataDir = dataDir;
        this.etcDir = etcDir;
    }

    /**
     * Calculate the disk space needed for running backup. return false if backup size is higher than  available
     * free disk space
     */
    public boolean isEnoughFreeSpace(@Nonnull BackupDescriptor backup) {
        ExportSettingsImpl settings = getExportSettings(backup);
        settings.setOutputFile(backup.getDir());
        return isEnoughFreeSpace(settings);
    }

    public long getFreeSpace(ExportSettingsImpl settings) throws IOException {
        long freeDiskSpace;
        if (settings.getOutputFile() == null) {
            freeDiskSpace = Files.getFileStore(dataDir.toPath()).getUnallocatedSpace();
        } else {
            freeDiskSpace = Files.getFileStore(settings.getOutputFile().toPath()).getUnallocatedSpace();
        }
        return freeDiskSpace;
    }

    boolean isEnoughFreeSpace(@Nonnull ExportSettingsImpl settings) {
        if (!settings.isPrecalculate()) {
            return true;
        }
        List<String> repoKeys = settings.getRepositories();
        long freeDiskSpace;
        try {
            freeDiskSpace = getFreeSpace(settings);
            log.info("Free space available for backup: {}", freeDiskSpace);
        } catch (Exception e) {
            log.error("Can't estimate free space in backup target location path. starting backup without space check", e);
            return true;
        }
        long backupSize = 0;

        // calculate and add repository size
        for (String repoKey : repoKeys) {
            RepoPath repoPath = InternalRepoPathFactory.create(repoKey, ".");
            backupSize += calculateRepoPathSize(repositoryService.getItemInfo(repoPath));
        }
        // calculate and add config files size
        backupSize += getBackupFilesSize();

        // calculate and add build files size (optional)
        if (!settings.isExcludeBuilds()) {
            backupSize += getBuildsSize();
        }
        // calculate and add license file size
        if (configsService.getConfig("licenses.xml") != null) {
            backupSize += roundSizeUpTo(configsService.getConfig("licenses.xml").length());
        }
        log.debug("Estimated backup size: {}", backupSize);
        return backupSize < freeDiskSpace;
    }

    private long calculateRepoPathSize(ItemInfo itemInfo) {
        if (itemInfo.isFolder()) {
            return METADATA_AVG_SIZE + repositoryService.getChildren(itemInfo.getRepoPath())
                    .stream()
                    .mapToLong(this::calculateRepoPathSize)
                    .sum();
        } else {
            return roundSizeUpTo(
                            repositoryService.getFileInfo(itemInfo.getRepoPath()).getSize() + METADATA_AVG_SIZE
            );
        }
    }

    private long getBackupFilesSize() {
        File[] etcDirFiles = etcDir.listFiles();

        List<File> configFiles = Lists.newArrayList();
        if (etcDirFiles != null) {
            configFiles.addAll(Arrays.asList(etcDirFiles));
        }
        File[] files = dataDir.listFiles();
        if (files != null) {
            File artifactoryProperties = findFile(files, "artifactory.properties");
            if (artifactoryProperties != null) {
                configFiles.add(artifactoryProperties);
            }
        }
        if (etcDirFiles != null) {
            File artifactoryConfigXml = findFile(etcDirFiles, "artifactory.config.xml");
            if (artifactoryConfigXml != null) {
                configFiles.add(artifactoryConfigXml);
            }
        }
        return configFiles.stream().mapToLong(file -> roundSizeUpTo(file.length())).sum();
    }

    private File findFile(File[] files, String fileName) {
        for (File file : files) {
            if (file.getName().equals(fileName)) {
                return file;
            }
        }
        return null;
    }

    /**
     * Calculate the size for the backed up builds
     *
     * @return size in bytes of the backed up builds
     */
    private long getBuildsSize() {
        return buildStoreService.getAllBuildNames().stream()
                .map(buildStoreService::findBuildsByName)
                .flatMap(Collection::stream)
                .map(buildService::getExportableBuild)
                .mapToLong(exportedBuild -> roundSizeUpTo(exportedBuild.getJson().length()))
                .sum();
    }

    /**
     * Round up x to the closest multiple multiple
     */
    private long roundSizeUpTo(long x) {
        return (x + (long) BackupSizeCalculator.ROUND_UP_SIZE - 1L) / (long) BackupSizeCalculator.ROUND_UP_SIZE *
                (long) BackupSizeCalculator.ROUND_UP_SIZE;
    }

    private ExportSettingsImpl getExportSettings(@Nonnull BackupDescriptor backup) {
        List<RealRepoDescriptor> excludeRepositories = backup.getExcludedRepositories();
        boolean isExcludeBuilds = excludeRepositories.stream().anyMatch(repo -> repo.getType() == RepoType.BuildInfo);
        List<String> backedupRepos = backupService.getBackedupRepos(excludeRepositories);
        boolean createArchive = backup.isCreateArchive();
        boolean incremental = backup.isIncremental();
        ExportSettingsImpl settings = new ExportSettingsImpl(new File(""));
        settings.setRepositories(backedupRepos);
        settings.setCreateArchive(createArchive);
        settings.setIncremental(incremental);
        settings.addCallback(new SystemBackupPauseCallback());
        settings.setExcludeBuilds(isExcludeBuilds);
        settings.setPrecalculate(backup.isPrecalculate());
        return settings;
    }
}
