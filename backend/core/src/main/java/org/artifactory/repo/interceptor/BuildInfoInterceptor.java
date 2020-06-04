package org.artifactory.repo.interceptor;

import org.artifactory.api.repo.RepositoryService;
import org.artifactory.build.InternalBuildService;
import org.artifactory.common.MutableStatusHolder;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.exception.CancelException;
import org.artifactory.md.Properties;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.interceptor.storage.StorageInterceptorAdapter;
import org.artifactory.sapi.fs.VfsItem;
import org.artifactory.sapi.interceptor.ImportInterceptor;
import org.artifactory.sapi.interceptor.context.DeleteContext;
import org.artifactory.sapi.interceptor.context.InterceptorCreateContext;
import org.artifactory.sapi.interceptor.context.InterceptorMoveCopyContext;
import org.artifactory.storage.fs.MutableVfsFile;
import org.jfrog.build.api.Build;
import org.jfrog.client.util.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.Optional;

import static org.apache.http.HttpStatus.*;
import static org.artifactory.build.BuildServiceUtils.buildUnderWrongPath;
import static org.artifactory.build.BuildServiceUtils.getBuildJsonPathInRepo;

/**
 * Intercept actions performed on Build Info repository
 *
 * @author Yuval Reches
 */
public class BuildInfoInterceptor extends StorageInterceptorAdapter implements ImportInterceptor {
    private static final Logger log = LoggerFactory.getLogger(BuildInfoInterceptor.class);

    private static final String ERR_INNER_COPY_MOVE = "Copy and Move operations are not allowed within the Build Info repository";
    private static final String ERR_BAD_PATH = "Trying to position build '%s:%s' under wrong path '%s' is not allowed, " +
                    "the expected path for this build-info json is '%s'";

    private RepositoryService repositoryService;
    private InternalBuildService buildService;

    @Autowired
    public BuildInfoInterceptor(RepositoryService repositoryService, InternalBuildService buildService) {
        this.repositoryService = repositoryService;
        this.buildService = buildService;
    }

    @Override
    public void beforeCreate(VfsItem fsItem, MutableStatusHolder statusHolder) {
        RepoPath path = fsItem.getRepoPath();
        if (isActionWithNonJsonFileInBuildInfoRepo(fsItem, path)) {
            //Reporting the CancelException in the status holder will fail the deployment.
            String err = "The '" + path.getRepoKey() + "' repository rejected the deployment of '" +
                    path.getPath() + "'. Only valid Build Info .json files are supported.";
            statusHolder.error(err, SC_CONFLICT, new CancelException(err, SC_CONFLICT), log);
        }
    }

    @Override
    public void afterCreate(VfsItem fsItem, MutableStatusHolder statusHolder, InterceptorCreateContext ctx) {
        if (shouldTakeAction(fsItem)) {
            doAction(fsItem, statusHolder, ctx);
        }
    }

    @Override
    public void afterImport(VfsItem fsItem, MutableStatusHolder statusHolder) {
        if (shouldTakeAction(fsItem)) {
            doAction(fsItem, statusHolder, null);
        }
    }

    private void doAction(VfsItem fsItem, MutableStatusHolder statusHolder, @Nullable InterceptorCreateContext ctx) {
        RepoPath deploymentPath = fsItem.getRepoPath();
        String fullPath = deploymentPath.toPath();
        log.debug("Build Info json deployed to '{}'", fullPath);
        Build build = buildService.getBuildModelFromFile(deploymentPath);
        //This is the path the json should actually go to
        RepoPath targetPath = getBuildJsonPathInRepo(build, buildService.getBuildInfoRepoKey());
        // We don't process builds under wrong path, it will be deleted by caller
        if (buildUnderWrongPath(build, deploymentPath, buildService.getBuildInfoRepoKey())) {
            log.debug("Build deployed to wrong path, ignoring it");
            return;
        }
        if (fsItem instanceof MutableVfsFile && !((MutableVfsFile) fsItem).isOriginallyNew()) {
            //If this is an override we just need to kick the build out of the db before re-adding it.
            log.info("Override of build at path '{}' detected.", fullPath);
            log.debug("Removing build that matches path '{}' from db", fullPath);
            buildService.deleteBuildInternal(build, statusHolder);
        }
        // In case of error - exception is thrown and deployment of .json file fails
        buildService.addBuildInternal(build);
        // Update the response with the right coordinates of the artifact after it has been moved
        if (ctx != null) {
            ctx.setAlternateRepoPath(targetPath);
        }
    }

    /**
     * In case {@link DeleteContext#isAvoidBuildDeleteInterceptor()} is on, it means we wish to delete the entire
     * repo and not trigger build delete per build number / build name.
     *
     * Upon delete of a folder this interceptor is called upon every file inside.
     */
    @Override
    public void afterDelete(VfsItem fsItem, MutableStatusHolder statusHolder, DeleteContext ctx) {
        if (!ctx.isAvoidBuildDeleteInterceptor() && shouldTakeAction(fsItem)) {
            RepoPath buildJsonPath = fsItem.getRepoPath();
            if (log.isDebugEnabled()) {
                log.debug("Trying to delete build according to file '{}'", fsItem.getRepoPath().toPath());
            }
            Build buildFromFile = buildService.getBuildModelFromFile(buildJsonPath);
            // Validating path here in order to assume the path in the buildService later
            if (buildUnderWrongPath(buildFromFile, buildJsonPath, buildService.getBuildInfoRepoKey())) {
                if (log.isDebugEnabled()) {
                    log.debug("Build {}:{} is deleted from repo only, since its path doesn't match the layout: {}",
                            buildFromFile.getName(), buildFromFile.getNumber(), buildJsonPath.toPath());
                }
                return;
            }
            //By default does not remove artifacts (since interceptor might also be triggered by undeploy)
            // REST calls do the build artifact deletion manually
            buildService.deleteBuildInternal(buildFromFile, statusHolder);
        }
    }

    /**
     * We allow move/copy into the repo to support copy/move from REST and trashcan restore (which is move).
     * Copy and Move within the repo makes no sense since the deployment path is calculated based on the content of the
     * json and obviously since the content doesn't change when you copy/move then the json will end in the same path.
     */
    @Override
    public boolean isCopyOrMoveAllowed(VfsItem sourceItem, RepoPath targetPath, MutableStatusHolder status) {
         if (shouldTakeAction(sourceItem, targetPath)) {
             RepoPath sourcePath = sourceItem.getRepoPath();
             if (isBuildRepo(sourcePath)) {
                 status.error(ERR_INNER_COPY_MOVE, SC_BAD_REQUEST, new CancelException(ERR_INNER_COPY_MOVE, SC_BAD_REQUEST), log);
                 return false;
             } else {
                 try {
                     Build buildToAdd = buildService.getBuildModelFromFile(sourcePath);
                     RepoPath expectedPath = getBuildJsonPathInRepo(buildToAdd, buildService.getBuildInfoRepoKey());
                     if (!expectedPath.equals(targetPath)) {
                         String err = badPathError(buildToAdd, expectedPath, targetPath);
                         status.error(err, SC_BAD_REQUEST, new CancelException(err, SC_BAD_REQUEST), log);
                         return false;
                     }
                 } catch (Exception e) {
                     String err = "Failed to parse Build Info from source path '" + sourcePath + "'. Operation is blocked.";
                     status.error(err, SC_BAD_REQUEST, new CancelException(err, SC_BAD_REQUEST), log);
                     return false;
                 }
             }
        }
        return true;
    }

    @Override
    public void beforeCopy(VfsItem sourceItem, RepoPath targetRepoPath, MutableStatusHolder statusHolder, Properties properties) {
        isCopyOrMoveAllowed(sourceItem, targetRepoPath, statusHolder);
    }

    @Override
    public void beforeMove(VfsItem sourceItem, RepoPath targetRepoPath, MutableStatusHolder statusHolder, Properties properties) {
        isCopyOrMoveAllowed(sourceItem, targetRepoPath, statusHolder);
    }

    @Override
    public void afterMove(VfsItem sourceItem, VfsItem targetItem, MutableStatusHolder statusHolder, Properties properties, InterceptorMoveCopyContext ctx) {
        afterMoveCopy(targetItem, statusHolder);
    }

    @Override
    public void afterCopy(VfsItem sourceItem, VfsItem targetItem, MutableStatusHolder statusHolder, Properties properties, InterceptorMoveCopyContext ctx) {
        afterMoveCopy(targetItem, statusHolder);
    }

    /**
     * {@link #isCopyOrMoveAllowed} limits the scope of this method to run only on copy/move operations *into* the
     * build info repo.
     */
    private void afterMoveCopy(VfsItem targetItem, MutableStatusHolder status) {
        if (shouldTakeAction(targetItem)) {
            Build buildToAdd = buildService.getBuildModelFromFile(targetItem.getRepoPath());
            RepoPath expectedPath = getBuildJsonPathInRepo(buildToAdd, buildService.getBuildInfoRepoKey());
            RepoPath targetPath = targetItem.getRepoPath();
            if (!expectedPath.equals(targetPath)) {
                //Shouldn't really reach here since the 'before' and 'allowed' triggers prevent this but what the heck
                String err = badPathError(buildToAdd, expectedPath, targetPath);
                status.error(err, SC_BAD_REQUEST, new CancelException(err, SC_BAD_REQUEST), log);
                return;
            }

            buildService.addBuildInternal(buildToAdd);
        }
    }

    @Override
    public void assertDeleteRepoAllowed(String repoKey, MutableStatusHolder status) {
        String buildInfoRepoKey = buildService.getBuildInfoRepoKey();
        if (buildInfoRepoKey.equals(repoKey)) {
            status.error("Deletion of Build Info repository " + buildInfoRepoKey + " is not allowed.", SC_FORBIDDEN, log);
        }
    }

    private boolean shouldTakeAction(VfsItem item) {
        RepoPath targetPath = item.getRepoPath();
        return shouldTakeAction(item, targetPath);
    }

    private boolean shouldTakeAction(VfsItem item, RepoPath targetPath) {
        return item.isFile() && isJsonFile(targetPath) && isBuildRepo(targetPath);
    }

    private boolean isActionWithNonJsonFileInBuildInfoRepo(VfsItem vfsItem, RepoPath targetPath) {
        return vfsItem.isFile() && !isJsonFile(targetPath) && isBuildRepo(targetPath);
    }

    private boolean isBuildRepo(RepoPath path) {
        log.debug("Checking if '{}' is a potential for build processing", path.getPath());
        RepoDescriptor descriptor = repositoryService.localRepoDescriptorByKey(path.getRepoKey());
        return descriptor != null && RepoType.BuildInfo.equals(descriptor.getType());
    }

    private boolean isJsonFile(RepoPath path) {
        String extension = Optional.ofNullable(PathUtils.getExtension(path.getName())).orElse("");
        return extension.equalsIgnoreCase("json");
    }

    private String badPathError(Build build, RepoPath expectedPath, RepoPath targetPath) {
        return String.format(ERR_BAD_PATH, build.getName(), build.getNumber(), targetPath.toPath(), expectedPath.toPath());
    }
}