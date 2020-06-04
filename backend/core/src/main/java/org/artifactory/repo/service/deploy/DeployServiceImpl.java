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

package org.artifactory.repo.service.deploy;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.artifactory.api.artifact.ArtifactInfo;
import org.artifactory.api.artifact.UnitInfo;
import org.artifactory.api.build.request.BuildArtifactoryRequest;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.maven.MavenArtifactInfo;
import org.artifactory.api.maven.MavenService;
import org.artifactory.api.module.ModuleInfo;
import org.artifactory.api.repo.DeployService;
import org.artifactory.api.repo.exception.RepoRejectException;
import org.artifactory.api.request.UploadService;
import org.artifactory.api.rest.artifact.RestFileInfo;
import org.artifactory.build.InternalBuildService;
import org.artifactory.descriptor.repo.RealRepoDescriptor;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.descriptor.repo.VirtualRepoDescriptor;
import org.artifactory.md.Properties;
import org.artifactory.mime.MavenNaming;
import org.artifactory.mime.NamingUtils;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.LocalRepo;
import org.artifactory.repo.Repo;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.repo.virtual.VirtualRepo;
import org.artifactory.request.InternalCapturingResponse;
import org.artifactory.sapi.common.RepositoryRuntimeException;
import org.artifactory.spring.InternalContextHelper;
import org.artifactory.util.GlobalExcludes;
import org.artifactory.util.ZipUtils;
import org.jfrog.client.util.PathUtils;
import org.jfrog.common.JsonParsingException;
import org.jfrog.common.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Provides artifacts deploy services from the UI.
 *
 * @author Yossi Shaul
 */
@Service
public class DeployServiceImpl implements DeployService {
    private static final Logger log = LoggerFactory.getLogger(DeployServiceImpl.class);

    private InternalRepositoryService repositoryService;
    private UploadService uploadService;
    private MavenService mavenService;
    private InternalBuildService buildService;

    @Autowired
    public DeployServiceImpl(InternalRepositoryService repositoryService, UploadService uploadService,
            MavenService mavenService, InternalBuildService buildService) {
        this.repositoryService = repositoryService;
        this.uploadService = uploadService;
        this.mavenService = mavenService;
        this.buildService = buildService;
    }

    @Override
    public void deploy(RepoDescriptor targetRepo, UnitInfo artifactInfo, File file, Properties properties)
            throws RepoRejectException {
        String pomString = mavenService.getPomModelString(file);
        deploy(targetRepo, artifactInfo, file, pomString, false, false, properties);
    }

    @Override
    public void deploy(RepoDescriptor targetRepo, UnitInfo artifactInfo, File fileToDeploy, String pomString,
            boolean forceDeployPom, boolean partOfBundleDeploy, Properties properties) throws RepoRejectException {
        String path = artifactInfo.getPath();
        if (!artifactInfo.isValid()) {
            throw new IllegalArgumentException("Invalid unit info for '" + path + "'.");
        }

        //Sanity check
        if (targetRepo == null) {
            throw new IllegalArgumentException("No target repository selected for deployment.");
        }

        if (targetRepo instanceof VirtualRepoDescriptor) {
            final VirtualRepo virtualRepo = repositoryService.virtualRepositoryByKey(targetRepo.getKey());
            if (virtualRepo == null || virtualRepo.getDescriptor().getDefaultDeploymentRepo() == null) {
                throw new IllegalArgumentException("No target repository found for deployment.");
            }
        } else {
            final LocalRepo localRepo = repositoryService.localRepositoryByKey(targetRepo.getKey());
            if (localRepo == null) {
                throw new IllegalArgumentException("No target repository found for deployment.");
            }
        }

        RepoPath repoPath = InternalRepoPathFactory.create(targetRepo.getKey(), path);

        // upload the main file
        InternalCapturingResponse response = new InternalCapturingResponse();
        boolean isBuildDeployRequest = RepoType.BuildInfo.equals(targetRepo.getType());
        try {
            ArtifactoryDeployRequest request = new ArtifactoryDeployRequestBuilder(repoPath)
                    .fileToDeploy(fileToDeploy)
                    .properties(properties)
                    .trustServerChecksums(true)
                    .isBuildDeployRequest(isBuildDeployRequest)
                    .build();
            if (isBuildDeployRequest) {
                //Trust me, I'm an engineer.
                buildService.handleBuildUploadRedirect((BuildArtifactoryRequest) request, response);
            } else {
                request.setSkipJarIndexing(partOfBundleDeploy);
                uploadService.upload(request, response);
            }
            validateResponseAndAlterPathIfNeeded(fileToDeploy.getName(), response, artifactInfo);
        } catch (IOException e) {
            String msg = "Cannot deploy file " + fileToDeploy.getName() + ". Cause: " + e.getMessage();
            log.debug(msg, e);
            throw new RepositoryRuntimeException(msg, e);
        }

        //Handle extra pom deployment - add the metadata with the generated pom file to the artifact
        if (forceDeployPom && artifactInfo.isMavenArtifact() && StringUtils.isNotBlank(pomString)) {
            MavenArtifactInfo mavenArtifactInfo = (MavenArtifactInfo) artifactInfo;
            RepoPath pomPath = InternalRepoPathFactory.create(repoPath.getParent(),
                    mavenArtifactInfo.getArtifactId() + "-" + mavenArtifactInfo.getVersion() + ".pom");
            RepoPath uploadPomPath = InternalRepoPathFactory.create(targetRepo.getKey(), pomPath.getPath());
            try {
                ArtifactoryDeployRequest pomRequest = new ArtifactoryDeployRequestBuilder(uploadPomPath)
                        .inputStream(IOUtils.toInputStream(pomString, Charsets.UTF_8.name()))
                        .contentLength(pomString.getBytes().length)
                        .lastModified(fileToDeploy.lastModified())
                        .properties(properties)
                        .trustServerChecksums(true)
                        .build();
                InternalCapturingResponse pomResponse = new InternalCapturingResponse();
                // upload the POM if needed
                uploadService.upload(pomRequest, pomResponse);
                validateResponseAndAlterPathIfNeeded(fileToDeploy.getName(), pomResponse, artifactInfo);
            } catch (IOException e) {
                String msg = "Cannot deploy file " + pomPath.getName() + ". Cause: " + e.getMessage();
                log.debug(msg, e);
                throw new RepositoryRuntimeException(msg, e);
            }
        }
    }

    private void validateResponseAndAlterPathIfNeeded(String deployedFileName, InternalCapturingResponse response,
            UnitInfo artifactInfo) throws RepoRejectException {
        if (!response.isSuccessful()) {
            StringBuilder errorMessageBuilder = new StringBuilder("Cannot deploy file '").append(deployedFileName).
                    append("'. ");
            String statusMessage = response.getStatusMessage();
            if (StringUtils.isNotBlank(statusMessage)) {
                errorMessageBuilder.append(statusMessage);
                if (!StringUtils.endsWith(statusMessage, ".")) {
                    errorMessageBuilder.append(".");
                }
            } else {
                errorMessageBuilder.append("Please view the logs for further information.");
            }
            throw new RepoRejectException(errorMessageBuilder.toString());
        } else {
            alterResponsePath(response, artifactInfo);
        }
    }

    /**
     * Checks for deployment final path change (due to move during the deploy interceptors)
     * and update the response path accordingly.
     *
     * @param response of the deployment
     * @param artifactInfo the model we return to the client, with altered path if needed
     */
    private void alterResponsePath(InternalCapturingResponse response, UnitInfo artifactInfo) {
        String resultAsString = response.getResultAsString();
        if (resultAsString == null) {
            log.debug("Deployment response is empty, cannot check for alternate path");
            return;
        }
        try {
            RestFileInfo responseStorageInfo = JsonUtils.getInstance().readValue(resultAsString, RestFileInfo.class);
            String newPath = responseStorageInfo != null ? PathUtils.trimLeadingSlashes(responseStorageInfo.path) : "";
            if (StringUtils.isNotBlank(newPath)
                    && !StringUtils.equals(newPath, artifactInfo.getPath())) {
                artifactInfo.setPath(newPath);
                log.debug("Changed deployment path to alternate path {}", newPath);
            }
        } catch (JsonParsingException e) {
            log.error("Couldn't parse deployment response, cannot check for alternate path", e);
        }
    }

    @Override
    public void deployBundle(File bundle, RealRepoDescriptor targetRepo, BasicStatusHolder status, boolean failFast) {
        deployBundle(bundle, targetRepo, status, failFast, "", null);
    }

    @Override
    public void deployBundle(File bundle, RealRepoDescriptor targetRepo, final BasicStatusHolder status,
            boolean failFast, @Nonnull String prefix, Properties properties) {
        long start = System.currentTimeMillis();
        if (!bundle.exists()) {
            String message = "Specified location '" + bundle + "' does not exist. Deployment aborted.";
            status.error(message, log);
            return;
        }
        File extractFolder = getExtractedArchiveFolder(bundle, status);
        if (extractFolder == null) {
            //We have errors
            return;
        }
        try {
            List<File> archiveContent = getDeployableFiles(status, extractFolder);
            Repo repo = repositoryService.repositoryByKey(targetRepo.getKey());
            if (repo == null) {
                log.error("No such repo {} to deploy to.", targetRepo.getKey());
                return;
            }
            for (File file : archiveContent) {
                String relPath = getFileRelativePath(prefix, extractFolder, file);
                if (isModuleValid(repo, file, relPath, targetRepo, status)) {
                    deployFile(targetRepo, status, properties, file, relPath);
                }
                if (status.hasErrors() && failFast) {
                    return;
                }
            }
            logAction(bundle, status, start, archiveContent);
        } catch (Exception e) {
            status.error(e.getMessage(), e, log);
        } finally {
            FileUtils.deleteQuietly(extractFolder);
        }
    }

    @Override
    public void deployBundleAtomic(File bundle, RealRepoDescriptor targetRepo, BasicStatusHolder status,
            boolean failFast, @Nonnull String prefix, Properties properties) {
        deployBundle(bundle, targetRepo, status, failFast, prefix, properties);
    }

    private String getFileRelativePath(@Nonnull String prefix, File extractFolder, File file) {
        String parentPath = extractFolder.getAbsolutePath();
        String filePath = file.getAbsolutePath();
        return PathUtils.trimSlashes(prefix + "/" + PathUtils.getRelativePath(parentPath, filePath));
    }

    private File getExtractedArchiveFolder(File bundle, BasicStatusHolder status) {
        try {
            return extractArchive(status, bundle);
        } catch (Exception e) {
            status.error(e.getLocalizedMessage(), e, log);
            throw new RestClientException(e.getMessage(), e);
        }
    }

    private List<File> getDeployableFiles(final BasicStatusHolder status, File extractFolder) {
        IOFileFilter deployableFilesFilter = new AbstractFileFilter() {
            @Override
            public boolean accept(File file) {
                if (NamingUtils.isSystem(file.getAbsolutePath()) || GlobalExcludes.isInGlobalExcludes(file) ||
                        file.getName().contains(MavenNaming.MAVEN_METADATA_NAME)) {
                    status.debug("Excluding '" + file.getAbsolutePath() + "' from bundle deployment.", log);
                    return false;
                }

                return true;
            }
        };
        List<File> archiveContent = Lists.newArrayList(FileUtils.listFiles(extractFolder, deployableFilesFilter,
                DirectoryFileFilter.DIRECTORY));
        Collections.sort(archiveContent);
        return archiveContent;
    }

    private boolean isModuleValid(Repo repo, File file, String relPath, RealRepoDescriptor targetRepo,
            BasicStatusHolder status) {
        ModuleInfo moduleInfo = repo.getItemModuleInfo(relPath);
        if (MavenNaming.isPom(file.getName())) {
            try {
                mavenService.validatePomFile(file, relPath, moduleInfo, targetRepo.isSuppressPomConsistencyChecks());
            } catch (Exception e) {
                String msg = "The pom: " + file.getName() + " could not be validated, and thus was not deployed.";
                status.error(msg, e, log);
                return false;
            }
        }
        return true;
    }

    private void deployFile(RealRepoDescriptor targetRepo, BasicStatusHolder status, Properties properties, File file,
            String relPath) {
        try {
            getTransactionalMe().deploy(targetRepo, new ArtifactInfo(relPath), file, null, false, true, properties);
        } catch (IllegalArgumentException iae) {
            status.error(iae.getMessage(), iae, log);
        } catch (Exception e) {
            status.error("Error during deployment: " + e.getMessage(), e, log);
        }
    }

    private void logAction(File bundle, BasicStatusHolder status, long start, List<File> archiveContent) {
        String bundleName = bundle.getName();
        String timeTaken = DurationFormatUtils.formatPeriod(start, System.currentTimeMillis(), "s");
        int archiveContentSize = archiveContent.size();
        String msg;
        if (status.hasErrors()) {
            msg = "Deployment of archive " + bundleName + " finished with errors.";
        } else if (status.hasWarnings()) {
            msg = "Deployment of archive " + bundleName + " finished with warnings, " + archiveContentSize
                    + " artifacts were deployed (" + timeTaken + " seconds).";
        } else {
            msg = "Successfully deployed " + archiveContentSize + " artifacts from archive: " + bundleName
                    + " (" + timeTaken + " seconds).";
        }
        status.status(msg, log);
    }

    private File extractArchive(BasicStatusHolder status, File archive) throws Exception {
        String archiveName = archive.getName();
        String fixedArchiveName = new String(archiveName.getBytes(Charsets.UTF_8.name()), Charsets.UTF_8);
        File fixedArchive = new File(archive.getParentFile(), fixedArchiveName);
        try {
            if (!fixedArchive.exists()) {
                FileUtils.moveFile(archive, fixedArchive);
            }
        } catch (IOException e) {
            throw new Exception("Could not encode archive name to UTF-8.", e);
        }
        File extractFolder = new File(ContextHelper.get().getArtifactoryHome().getTempUploadDir(),
                fixedArchive.getName() + "_extracted_" + System.currentTimeMillis());
        if (extractFolder.exists()) {
            //Clean up any existing folder
            try {
                FileUtils.deleteDirectory(extractFolder);
            } catch (IOException e) {
                status.error("Could not delete existing extracted archive folder: " +
                        extractFolder.getAbsolutePath() + ".", e, log);
                return null;
            }
        }
        try {
            FileUtils.forceMkdir(extractFolder);
        } catch (IOException e) {
            log.error("Could not created the extracted archive folder: " +
                    extractFolder.getAbsolutePath() + ".", log);
            return null;
        }

        try {
            ZipUtils.extract(fixedArchive, extractFolder);
        } catch (Exception e) {
            FileUtils.deleteQuietly(extractFolder);
            if (e.getMessage() == null) {
                String errorMessage;
                if (e instanceof IllegalArgumentException) {
                    errorMessage =
                            "Please make sure the textual values in the archive are encoded in UTF-8.";
                } else {
                    errorMessage = "Please ensure the integrity of the selected archive";
                }
                throw new Exception(errorMessage, e);
            }
            throw e;
        }
        return extractFolder;
    }

    private static DeployService getTransactionalMe() {
        return InternalContextHelper.get().beanForType(DeployService.class);
    }
}
