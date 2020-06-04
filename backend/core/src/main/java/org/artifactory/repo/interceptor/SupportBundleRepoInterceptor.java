package org.artifactory.repo.interceptor;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.support.SupportAddon;
import org.artifactory.common.MutableStatusHolder;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.interceptor.storage.StorageInterceptorAdapter;
import org.artifactory.sapi.fs.VfsItem;
import org.artifactory.sapi.interceptor.context.InterceptorCreateContext;
import org.jfrog.client.util.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.artifactory.descriptor.repo.SupportBundleRepoDescriptor.SUPPORT_BUNDLE_REPO_NAME;
import static org.artifactory.util.RepoPathUtils.isTrash;

/**
 * @author Tamir Hadad
 */
public class SupportBundleRepoInterceptor extends StorageInterceptorAdapter {
    private static final Logger log = LoggerFactory.getLogger(SupportBundleRepoInterceptor.class);

    private AddonsManager addonsManager;

    @Autowired
    public SupportBundleRepoInterceptor(AddonsManager addonsManager) {
        this.addonsManager = addonsManager;
    }

    @Override
    public boolean isCopyOrMoveAllowed(VfsItem sourceItem, RepoPath targetRepoPath, MutableStatusHolder status) {
        if (shouldTakeAction(sourceItem)) {
            if (isTrash(targetRepoPath)) {
                return true;
            } else {
                status.error("Copy/Move from " + SUPPORT_BUNDLE_REPO_NAME + " repo is forbidden ", SC_FORBIDDEN, log);
                return false;
            }
        }
        if (addonsManager.addonByType(SupportAddon.class).isSupportBundlesRepo(targetRepoPath.getRepoKey())) {
            if (isTrash(sourceItem.getRepoPath())) {
                return true;
            } else {
                status.error("Copy/Move into " + SUPPORT_BUNDLE_REPO_NAME + " repo is forbidden ", SC_FORBIDDEN, log);
                return false;
            }
        }
        return true;
    }

    @Override
    public void afterCreate(VfsItem fsItem, MutableStatusHolder statusHolder, InterceptorCreateContext ctx) {
        if (shouldTakeAction(fsItem) && fsItem.isFile() && "json".equals(PathUtils.getExtension(fsItem.getName()))) {
            addonsManager.addonByType(SupportAddon.class).cleanUp();
        }
    }

    private boolean shouldTakeAction(VfsItem fsItem) {
        return addonsManager.addonByType(SupportAddon.class).isSupportBundlesRepo(fsItem.getRepoKey());
    }
}
