package org.artifactory.build;

import org.apache.commons.io.FileUtils;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.SecurityService;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.descriptor.backup.BackupDescriptor;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.repo.RealRepoDescriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * @author Tamir Hadad
 */
public class BuildBackupConverter {
    private static final Logger log = LoggerFactory.getLogger(BuildBackupConverter.class);
    private CentralConfigService centralConfigService;
    private RepositoryService repositoryService;
    private SecurityService securityService;


    BuildBackupConverter(CentralConfigService centralConfigService, RepositoryService repositoryService,
            SecurityService securityService) {
        this.centralConfigService = centralConfigService;
        this.repositoryService = repositoryService;
        this.securityService = securityService;
    }

    public void convert() {
        log.info("Starting backup configuration builds conversion");
        File createBackupExcludedBuildNames = ArtifactoryHome.get().getCreateBackupExcludedBuildNames();
        File backupFile;
        try {
            String[] backupKeys = getBackupKeyFromMarkerFile(createBackupExcludedBuildNames);
            RealRepoDescriptor buildInfoRepo = null;
            for (RealRepoDescriptor repo : repositoryService.getLocalAndRemoteRepoDescriptors()) {
                if (repo.getType() == RepoType.BuildInfo) {
                    buildInfoRepo = repo;
                    break;
                }
            }

            if (buildInfoRepo == null) {
                log.error("Couldn't find build-info repo");
                return;
            }

            MutableCentralConfigDescriptor mutableDescriptor = centralConfigService.getMutableDescriptor();
            if (CollectionUtils.isNullOrEmpty(mutableDescriptor.getBackups())) {
                return;
            }
            for (String key : backupKeys) {
                BackupDescriptor backup = mutableDescriptor.getBackup(key);
                if (backup == null) {
                    return;
                }

                // Before adding new excluded repo we need to allow exclude new repos
                boolean excludeNewRepositories = backup.isExcludeNewRepositories();
                backup.setExcludeNewRepositories(true);
                backup.addExcludedRepository(buildInfoRepo);
                backup.setExcludeNewRepositories(excludeNewRepositories);
            }
            // we must change/delete the file before saving the configuration.
            // If we don't we will have a loop and then save operation will fail.
            backupFile = backupMarkerFile(createBackupExcludedBuildNames);
            securityService.doAsSystem(() -> centralConfigService.saveEditedDescriptorAndReload(mutableDescriptor));
        } catch (IOException e) {
            log.error("Failed to read backups marker file");
            log.debug("", e);
            return;
        }
        log.info("Deleting backup file {}", backupFile.getAbsolutePath());
        FileUtils.deleteQuietly(backupFile);
    }

    private String[] getBackupKeyFromMarkerFile(File createBackupExcludedBuildNames) throws IOException {
        String content = FileUtils.readFileToString(createBackupExcludedBuildNames);
        String replace = content.replace("]", "").replace("[", "");
        return replace.split(",");
    }

    private File backupMarkerFile(File createBackupExcludedBuildNames) throws IOException {
        File backupFile = new File(createBackupExcludedBuildNames.getAbsolutePath()
                .replace(createBackupExcludedBuildNames.getName(),
                        createBackupExcludedBuildNames.getName() + ".bck"));
        FileUtils.moveFile(createBackupExcludedBuildNames, backupFile);
        return backupFile;
    }
}
