package org.artifactory.repo.cleanup;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.docker.DockerAddon;
import org.artifactory.common.ConstantValues;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.schedule.TaskBase;
import org.artifactory.schedule.TaskService;
import org.artifactory.schedule.TaskUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author dudim
 */
@Service
public class DockerUploadsFolderCleanupServiceImpl implements DockerUploadsFolderCleanupService {

    private static final Logger log = LoggerFactory.getLogger(DockerUploadsFolderCleanupServiceImpl.class);

    private TaskService taskService;
    private InternalRepositoryService repositoryService;
    private AddonsManager addonsManager;

    @Autowired
    public DockerUploadsFolderCleanupServiceImpl(InternalRepositoryService repositoryService, AddonsManager addonsManager,
            TaskService taskService) {
        this.repositoryService = repositoryService;
        this.taskService = taskService;
        this.addonsManager = addonsManager;
    }

    @Override
    public void onContextCreated() {
        TaskBase dockerUploadsTmpFolderCleanupTask = TaskUtils.createRepeatingTask(DockerUploadsFolderCleanupJob.class,
                TimeUnit.SECONDS.toMillis(ConstantValues.dockerCleanupUploadsTmpFolderJobMillis.getLong()),0L);
        taskService.startTask(dockerUploadsTmpFolderCleanupTask, false);
    }

    @Override
    public void clean() {
        long startTime = System.currentTimeMillis();
        log.info("Starting docker temp folder cleanup");
        doCleanup();
        log.info("Docker temp folder cleanup finished, time took: {} millis", (System.currentTimeMillis() - startTime));

    }

    private void doCleanup() {
        List<LocalRepoDescriptor> dockerLocalRepos = repositoryService.getLocalRepoDescriptors()
                .stream()
                .filter(localRepo -> RepoType.Docker.equals(localRepo.getType()))
                .collect(Collectors.toList());

        for (LocalRepoDescriptor dockerRepo : dockerLocalRepos) {
            addonsManager.addonByType(DockerAddon.class).searchAndCleanupTempFolders(dockerRepo.getKey());
        }
    }
}