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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.CoreAddons;
import org.artifactory.api.build.BuildService;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.common.ImportExportStatusHolder;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.config.ExportSettingsImpl;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.mail.MailService;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.StatusEntry;
import org.artifactory.config.CentralConfigKey;
import org.artifactory.descriptor.backup.BackupDescriptor;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.repo.*;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.schedule.*;
import org.artifactory.spring.InternalArtifactoryContext;
import org.artifactory.spring.InternalContextHelper;
import org.artifactory.spring.Reloadable;
import org.artifactory.storage.build.service.BuildStoreService;
import org.artifactory.storage.fs.service.ConfigsService;
import org.artifactory.util.CollectionUtils;
import org.artifactory.util.EmailException;
import org.artifactory.version.CompoundVersionDetails;
import org.jfrog.common.config.diff.DataDiff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Yoav Landman
 */
@Service
@Reloadable(beanClass = InternalBackupService.class,
        initAfter = {InternalRepositoryService.class, TaskService.class},
        listenOn = CentralConfigKey.backups)
public class BackupServiceImpl implements InternalBackupService {
    private static final Logger log = LoggerFactory.getLogger(BackupServiceImpl.class);

    private CentralConfigService centralConfig;

    private InternalRepositoryService repositoryService;

    private MailService mailService;

    private AddonsManager addonsManager;

    private BuildStoreService buildStoreService;

    private BuildService buildService;

    private ConfigsService configsService;

    @Autowired
    public BackupServiceImpl(CentralConfigService centralConfig, InternalRepositoryService repositoryService,
            MailService mailService, AddonsManager addonsManager, BuildStoreService buildStoreService,
            BuildService buildService, ConfigsService configsService) {
        this.centralConfig = centralConfig;
        this.repositoryService = repositoryService;
        this.mailService = mailService;
        this.addonsManager = addonsManager;
        this.buildStoreService = buildStoreService;
        this.buildService = buildService;
        this.configsService = configsService;
    }

    @Override
    public void init() {
        reload(null, ImmutableList.of());
    }

    @Override
    public void reload(@Nullable CentralConfigDescriptor oldDescriptor, List<DataDiff<?>> configDiff) {
        List<BackupDescriptor> backupDescriptors = centralConfig.getDescriptor().getBackups();
        BackupDescriptorHandler backupDescriptorHandler = new BackupDescriptorHandler(backupDescriptors,
                (oldDescriptor != null) ? oldDescriptor.getBackups() : null);
        backupDescriptorHandler.reschedule();
    }

    @Override
    public void destroy() {
        new BackupDescriptorHandler(null, null).unschedule();
    }

    @Override
    public void convert(CompoundVersionDetails source, CompoundVersionDetails target) {
    }

    @Override
    public void backupRepos(File backupDir, ExportSettingsImpl exportSettings) {
        List<String> backedupRepos = getBackedupRepos(Collections.emptyList());
        ExportSettingsImpl settings = new ExportSettingsImpl(backupDir, exportSettings);
        settings.setRepositories(backedupRepos);
        repositoryService.exportTo(settings);
    }


    @Override
    public void scheduleImmediateSystemBackup(BackupDescriptor backupDescriptor, BasicStatusHolder statusHolder) {
        TaskService taskService = InternalContextHelper.get().getTaskService();
        String backupKey = backupDescriptor.getKey();
        taskService.checkCanStartManualTask(BackupJob.class, statusHolder, backupKey);
        if (!statusHolder.isError()) {
            try {
                statusHolder.status("Activating manual system backup '" + backupKey + "'", log);
                TaskBase task = TaskUtils.createManualTask(BackupJob.class, 0L, "Backup - " + backupKey);
                task.addAttribute(BackupJob.MANUAL_BACKUP, backupDescriptor);
                task.addAttribute(BackupJob.BACKUP_KEY, backupKey);
                String taskToken = taskService.startTask(task, true, true);
                statusHolder.status("Started " + taskToken + " successfully", log);
            } catch (Exception e) {
                statusHolder.error("Error scheduling manual system backup '" + backupKey + "'", e, log);
            }
        }
    }

    @Override
    public boolean isEnoughFreeSpace(BackupDescriptor backupDescriptor) {
        return getBackupSizeCalculator().isEnoughFreeSpace(backupDescriptor);
    }

    @Override
    public boolean isEnoughFreeSpace(ExportSettingsImpl settings) {
        return getBackupSizeCalculator().isEnoughFreeSpace(settings);
    }

    private BackupSizeCalculator getBackupSizeCalculator() {
        InternalArtifactoryContext context = InternalContextHelper.get();
        File dataDir = context.getArtifactoryHome().getDataDir();
        File etcDir = context.getArtifactoryHome().getEtcDir();

        return new BackupSizeCalculator(repositoryService, buildStoreService, buildService, configsService, this, dataDir, etcDir);
    }

    @Override
    public BasicStatusHolder backupSystem(InternalArtifactoryContext context, @Nonnull BackupDescriptor backup) {
        ImportExportStatusHolder status = new ImportExportStatusHolder();
        if (!backup.isEnabled()) {
            status.error("Backup: '" + backup.getKey() + "' is disabled. Backup was not performed.", log);
            return status;
        }
        List<RealRepoDescriptor> excludeRepositories = backup.getExcludedRepositories();
        boolean excludeBuilds = excludeRepositories.stream().anyMatch(repo -> repo.getType() == RepoType.BuildInfo);
        List<String> backedupRepos = getBackedupRepos(excludeRepositories);
        File backupDir = getBackupDir(backup);
        boolean createArchive = backup.isCreateArchive();
        boolean incremental = backup.isIncremental();
        boolean precalculate = backup.isPrecalculate();
        if (incremental && createArchive) {
            status.warn("An incremental backup cannot be archived!\n" +
                    "Please change the configuration of backup " + backup.getKey() + ".", log);
            createArchive = false;
        }
        ExportSettingsImpl settings = new ExportSettingsImpl(backupDir, status);
        settings.setRepositories(backedupRepos);
        settings.setCreateArchive(createArchive);
        settings.setIncremental(incremental);
        settings.addCallback(new SystemBackupPauseCallback());
        settings.setExcludeBuilds(excludeBuilds);
        settings.setPrecalculate(precalculate);
        context.exportTo(settings);

        return status;
    }

    @Override
    public void cleanupOldBackups(Date now, String backupKey) {
        BackupDescriptor descriptor = getBackup(backupKey);
        if (descriptor == null) {
            return;
        }
        int retentionPeriodHours = descriptor.getRetentionPeriodHours();
        //No action if retention is 0 (or less)
        if (retentionPeriodHours <= 0) {
            return;
        }
        File backupDir = getBackupDir(descriptor);
        File[] children = backupDir.listFiles();
        if (children == null || CollectionUtils.isNullOrEmpty(children)) {
            log.debug("No old backup files to remove.");
            return;
        }

        //Calculate last valid time
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.HOUR, -retentionPeriodHours);
        Date validFrom = calendar.getTime();
        log.debug("Removing backups older than {}.", validFrom);
        //Delete anything not newer than the last valid time
        for (File child : children) {
            if (!FileUtils.isFileNewer(child, validFrom)) {
                try {
                    log.debug("Removing old backup file '{}'.", child.getPath());
                    FileUtils.forceDelete(child);
                } catch (IOException e) {
                    log.warn("Failed to remove old backup file or folder '" + child.getPath() + "'.", e);
                }
            } else {
                log.debug("Skipping new backup file '{}'.", child.getPath());
            }
        }
    }

    @Override
    public File getBackupDir(BackupDescriptor descriptor) {
        File dir = descriptor.getDir();
        File backupDir;
        if (dir == null) {
            ArtifactoryHome artifactoryHome = ContextHelper.get().getArtifactoryHome();
            backupDir = new File(artifactoryHome.getBackupDir(), descriptor.getKey());
        } else {
            backupDir = dir;
            try {
                FileUtils.forceMkdir(backupDir);
            } catch (IOException e) {
                throw new IllegalArgumentException("Backup directory provided in configuration: '" +
                        backupDir.getAbsolutePath() + "' cannot be created or is not a directory.");
            }
        }
        return backupDir;
    }

    @Override
    public void sendBackupErrorNotification(String backupName, BasicStatusHolder statusHolder) throws Exception {
        try (InputStream stream = getClass().getResourceAsStream("/org/artifactory/email/messages/backupError.properties")) {
            ResourceBundle resourceBundle = new PropertyResourceBundle(stream);
            String body = resourceBundle.getString("body");
            String errorListBlock = getErrorListBlock(statusHolder);

            CoreAddons coreAddons = addonsManager.addonByType(CoreAddons.class);
            List<String> adminEmails = coreAddons.getUsersForBackupNotifications();
            if (CollectionUtils.isNullOrEmpty(adminEmails)) {
                log.warn("Couldn't find admin account with valid email address. " +
                        "Skipping backup failure email notification");
            }
            adminEmails.stream()
                    .filter(StringUtils::isNotBlank)
                    .forEach(adminEmail -> sendBackupErrorMail(backupName, body, errorListBlock, adminEmail));
        }
    }

     private void sendBackupErrorMail(String backupName, String body, String errorListBlock, String adminEmail) {
         try {
             log.debug("Sending backup error notification to '{}'.", adminEmail);
             StringBuilder artifactoryUrlMessage = new StringBuilder();
             String artifactoryUrl = centralConfig.getDescriptor().getServerUrlForEmail();
             if (StringUtils.isNotBlank(artifactoryUrl)) {
                 String artifactoryLink = createArtifactoryLinkFromUrl(artifactoryUrl);
                 artifactoryUrlMessage.append("Your Artifactory base URL is: ").append(artifactoryLink);
             } else {
                 artifactoryUrlMessage.append("No Artifactory base URL is configured");
             }
             String message = MessageFormat.format(body, backupName, artifactoryUrlMessage, errorListBlock);
             mailService.sendMail(new String[]{adminEmail}, "Backup Error Notification", message);
         } catch (EmailException e) {
             log.error("Error while trying to send Email to: {}, due to: {}", adminEmail,
                     e.getMessage());
         }
     }

    private String createArtifactoryLinkFromUrl(String artifactoryUrl) {
        return "<a href=" + artifactoryUrl + " target=\"blank\"" + ">" + artifactoryUrl + "<a/>";
    }

    @Override
    public List<String> getBackedupRepos(List<RealRepoDescriptor> excludeRepositories) {
        List<LocalRepoDescriptor> localRepos = repositoryService.getLocalAndCachedRepoDescriptors();
        List<String> localRepoKeys = localRepos.stream()
                .map(RepoBaseDescriptor::getKey)
                .collect(Collectors.toList());
        repositoryService.getDistributionRepoDescriptors().forEach(descriptor -> {
            localRepos.add(descriptor);
            localRepoKeys.add(descriptor.getKey());
        });

        repositoryService.getReleaseBundlesRepoDescriptors().forEach(descriptor -> {
            localRepos.add(descriptor);
            localRepoKeys.add(descriptor.getKey());
        });

        if (CollectionUtils.isNullOrEmpty(excludeRepositories)) {
            return localRepoKeys; // nothing is excluded return all local repositories
        }
        List<String> backedupRepos = new ArrayList<>();
        for (LocalRepoDescriptor repo : localRepos) {
            //Skip excluded repositories
            RealRepoDescriptor checkForExclusionRepo;
            if (repo.isCache()) {
                checkForExclusionRepo = ((LocalCacheRepoDescriptor) repo).getRemoteRepo();
            } else {
                checkForExclusionRepo = repo;
            }
            //Skip excluded repositories
            boolean excluded = false;
            for (RealRepoDescriptor excludedRepo : excludeRepositories) {
                if (excludedRepo.getKey().equals(checkForExclusionRepo.getKey())) {
                    excluded = true;
                    break;
                }
            }
            if (!excluded) {
                backedupRepos.add(repo.getKey());
            }
        }
        return backedupRepos;
    }

    @Override
    public BackupDescriptor getBackup(String backupKey) {
        final List<BackupDescriptor> list = centralConfig.getDescriptor().getBackups();
        for (BackupDescriptor backupDescriptor : list) {
            if (backupKey.equals(backupDescriptor.getKey())) {
                return backupDescriptor;
            }
        }
        //This might happen after the first time a backup has been turned off if the scheduler
        //wakes up before old jobs were cleaned up
        log.warn("Skipping empty backup config " + backupKey + "!\n" +
                "Probably a leftover from an old to-be-deleted backup job.");
        return null;
    }

    /**
     * Returns an HTML list block of errors extracted from the status holder
     *
     * @param statusHolder Status holder containing errors that should be included in the notification
     * @return HTML list block
     */
    private String getErrorListBlock(BasicStatusHolder statusHolder) {
        StringBuilder builder = new StringBuilder();
        List<StatusEntry> errors = statusHolder.getErrors();
        if (errors.size() > 0) {
            for (StatusEntry errorEntry : errors) {
                convertErrorEntryToString(builder, errorEntry);
            }
        } else {
            convertErrorEntryToString(builder, statusHolder.getLastError());
        }
        builder.append("<p>");
        return builder.toString();
    }

    private void convertErrorEntryToString(StringBuilder builder, StatusEntry errorEntry) {
        //Make one error per row
        builder.append(errorEntry.getMessage());
        Throwable throwable = errorEntry.getException();
        if (throwable != null) {
            String throwableMessage = throwable.getMessage();
            if (StringUtils.isNotBlank(throwableMessage)) {
                builder.append(": ");
                builder.append(throwableMessage);
            }
        }
        builder.append("<br>");
    }

    static class BackupDescriptorHandler extends BaseTaskServiceDescriptorHandler<BackupDescriptor> {
        final List<BackupDescriptor> newBackupDescriptors;
        final List<BackupDescriptor> oldBackupDescriptors;

        BackupDescriptorHandler(List<BackupDescriptor> newBackupDescriptors,
                List<BackupDescriptor> oldBackupDescriptors) {
            this.newBackupDescriptors = newBackupDescriptors;
            if (oldBackupDescriptors != null) {
                this.oldBackupDescriptors = oldBackupDescriptors;
            } else {
                this.oldBackupDescriptors = Lists.newArrayList();
            }
        }

        @Override
        public String jobName() {
            return "Backup";
        }

        @Override
        public List<BackupDescriptor> getNewDescriptors() {
            return this.newBackupDescriptors;
        }

        @Override
        public List<BackupDescriptor> getOldDescriptors() {
            return this.oldBackupDescriptors;
        }

        @Override
        public Predicate<Task> getAllPredicate() {
            return input -> BackupJob.class.isAssignableFrom(input.getType());
        }

        @Override
        public Predicate<Task> getPredicate(@Nonnull final BackupDescriptor descriptor) {
            return input -> BackupJob.class.isAssignableFrom(input.getType()) &&
                    descriptor.getKey().equals(input.getAttribute(BackupJob.BACKUP_KEY));
        }

        @Override
        public void activate(BackupDescriptor descriptor, boolean manual) {
            //Schedule the cron'd backup
            String key = descriptor.getKey();
            String cronExp = descriptor.getCronExp();
            if (descriptor.isEnabled()) {
                if (cronExp == null) {
                    log.warn("No backup cron expression is configured. Backup " + key + " will be disabled.");
                    return;
                }
                try {
                    TaskBase task = TaskUtils.createCronTask(BackupJob.class, cronExp,
                            "Backup - " + key);
                    task.addAttribute(BackupJob.BACKUP_KEY, key);
                    InternalContextHelper.get().getBean(TaskService.class).startTask(task, false, manual);
                    log.debug("Backup '{}' activated with cron expression '{}'.", key, cronExp);
                } catch (Exception e) {
                    log.warn("Activation of backup " + key + ":" + descriptor + " failed:" + e.getMessage(), e);
                }
            }
        }

        @Override
        public BackupDescriptor findOldFromNew(@Nonnull BackupDescriptor newDescriptor) {
            for (BackupDescriptor oldBackupDescriptor : oldBackupDescriptors) {
                if (oldBackupDescriptor.getKey().equals(newDescriptor.getKey())) {
                    return oldBackupDescriptor;
                }
            }
            return null;
        }
    }
}
