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

package org.artifactory.repo.interceptor;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.module.ModuleInfo;
import org.artifactory.api.properties.PropertiesService;
import org.artifactory.api.repo.exception.maven.BadPomException;
import org.artifactory.common.MutableStatusHolder;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.maven.PomTargetPathValidator;
import org.artifactory.md.Properties;
import org.artifactory.mime.MavenNaming;
import org.artifactory.mime.NamingUtils;
import org.artifactory.repo.LocalRepo;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.StoringRepo;
import org.artifactory.repo.interceptor.storage.StorageInterceptorAdapter;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.sapi.fs.VfsFile;
import org.artifactory.sapi.fs.VfsItem;
import org.artifactory.sapi.interceptor.ImportInterceptor;
import org.artifactory.sapi.interceptor.context.InterceptorCreateContext;
import org.artifactory.storage.fs.MutableVfsFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;

/**
 * An interceptor that checks the validity of POM files and if it represents a maven plugin.
 *
 * @author Yossi Shaul
 */
public class MavenPomInterceptor extends StorageInterceptorAdapter implements ImportInterceptor {
    private static final Logger log = LoggerFactory.getLogger(MavenPomInterceptor.class);

    @Autowired
    private InternalRepositoryService repositoryService;

    @Override
    public void afterCreate(VfsItem fsItem, MutableStatusHolder statusHolder, InterceptorCreateContext ctx) {
        if (!fsItem.isFile()) {
            return;
        }
        VfsFile fsFile = (VfsFile) fsItem;
        if (MavenNaming.isPom(fsItem.getName())) {
            InternalRepositoryService repoService = ContextHelper.get().beanForType(InternalRepositoryService.class);
            String repoKey = fsItem.getRepoKey();
            StoringRepo storingRepo = repoService.storingRepositoryByKey(repoKey);
            LocalRepoDescriptor descriptor = repoService.localCachedOrDistributionRepoDescriptorByKey(repoKey);
            if (!isStoringRepoReal(storingRepo) || !repoIsMaven(descriptor)) {
                return;
            }
            boolean suppressPomConsistencyChecks = storingRepo.isSuppressPomConsistencyChecks();
            ModuleInfo moduleInfo = storingRepo.getItemModuleInfo(fsItem.getPath());
            PomTargetPathValidator pomValidator = new PomTargetPathValidator(fsItem.getPath(), moduleInfo);
            InputStream is = fsFile.getStream();
            try {
                pomValidator.validate(is, suppressPomConsistencyChecks);
            } catch (BadPomException e) {
                throw new RuntimeException("Failed to validate pom file: " + e.getMessage(), e);
            } catch (IOException e) {
                throw new RuntimeException("Failed to parse pom file: " + e.getMessage(), e);
            } finally {
                IOUtils.closeQuietly(is);
            }

            if (pomValidator.isMavenPlugin()) {
                log.debug("Marking {} as maven plugin", fsItem.getRepoPath());
                MutableVfsFile mutableFile = storingRepo.getMutableFile(fsItem.getRepoPath());
                Properties properties = mutableFile.getProperties();
                properties.put(PropertiesService.MAVEN_PLUGIN_PROPERTY_NAME, Boolean.toString(true));
                mutableFile.setProperties(properties);
            }
        }
    }

    @Override
    public boolean isCopyOrMoveAllowed(VfsItem sourceItem, RepoPath targetRepoPath, MutableStatusHolder status) {
        String targetRepoKey = targetRepoPath.getRepoKey();
        LocalRepo targetRepo = repositoryService.localOrCachedRepositoryByKey(targetRepoKey);
        if (targetRepo == null) {
            targetRepo = repositoryService.distributionRepoByKey(targetRepoKey);
        }
        if (targetRepo == null) {
            targetRepo = repositoryService.releaseBundleRepositoryByKey(targetRepoKey);
        }
        if (targetRepo == null) {
            log.debug("Could not find local or distribution repository named '{}'", targetRepoKey);
            return false;
        }
        String targetPath = targetRepoPath.getPath();
        if (shouldValidatePomTransfer(sourceItem, targetRepo, targetPath)) {
            ModuleInfo moduleInfo = targetRepo.getItemModuleInfo(targetPath);
            try (InputStream resourceStream = ((VfsFile) sourceItem).getStream()) {
                new PomTargetPathValidator(targetPath, moduleInfo).validate(resourceStream, false);
            } catch (Exception e) {
                status.error("Failed to validate target path of pom: " + targetPath, HttpStatus.SC_BAD_REQUEST, e, log);
                return false;
            }
        }
        return true;
    }

    @Override
    public void afterImport(VfsItem fsItem, MutableStatusHolder statusHolder) {
        afterCreate(fsItem, statusHolder, new InterceptorCreateContext());
    }

    private boolean shouldValidatePomTransfer(VfsItem sourceItem, LocalRepo targetRepo, String targetPath) {
        LocalRepoDescriptor targetDescriptor = (LocalRepoDescriptor) targetRepo.getDescriptor();
        return targetRepo != null &&  repoIsMaven(targetDescriptor) && sourceItem.isFile()
                && NamingUtils.isPom(sourceItem.getRepoPath().getPath()) && NamingUtils.isPom(targetPath)
                && !targetDescriptor.isSuppressPomConsistencyChecks();
    }

    private boolean isStoringRepoReal(StoringRepo storingRepo) {
        return storingRepo != null && storingRepo.isReal();
    }

    private boolean repoIsMaven(LocalRepoDescriptor descriptor) {
        return descriptor != null && descriptor.getType().isMavenGroup();
    }
}
