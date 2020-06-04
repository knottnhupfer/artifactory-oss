package org.artifactory.engine;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.CoreAddons;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.common.ConstantValues;
import org.artifactory.descriptor.download.DownloadRedirectConfigDescriptor;
import org.artifactory.descriptor.repo.RealRepoDescriptor;
import org.artifactory.fs.RepoResource;
import org.artifactory.repo.Repo;
import org.jfrog.storage.common.StorageUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

import static org.artifactory.common.ConstantValues.listOfReposAllowedSendRedirectUrl;
import static org.artifactory.request.range.Range.log;

/**
 * @author Lior Gur
 */

@Component
public class DownloadRedirectHelper {

    private final AddonsManager addonsManager;

    private final CentralConfigService centralConfig;

    @Autowired
    public DownloadRedirectHelper(AddonsManager addonsManager,
            CentralConfigService centralConfig) {
        this.addonsManager = addonsManager;
        this.centralConfig = centralConfig;
    }

    public boolean isRedirectEnable(Repo responseRepo, RealRepoDescriptor descriptor) {
        if (addonsManager.addonByType(CoreAddons.class).isAol()) {
            return isRedirectEnableForThisRepoType(responseRepo);
        } else {
            return descriptor.isDownloadRedirect();
        }
    }

    private DownloadRedirectConfigDescriptor getDownloadRedirectGlobalConfig() {
        DownloadRedirectConfigDescriptor downloadRedirectConfig = centralConfig.getDescriptor().getDownloadRedirectConfig();
        return downloadRedirectConfig != null ? downloadRedirectConfig : new DownloadRedirectConfigDescriptor();
    }

    private boolean isRedirectEnableForThisRepoType(Repo responseRepo) {
        return Arrays.stream(listOfReposAllowedSendRedirectUrl.getString().split(",")).anyMatch(
                repoType -> repoType.equalsIgnoreCase(responseRepo.getDescriptor().getType().getDisplayName()));
    }

    public boolean isArtifactGreaterEqualsRedirectThreshold(RepoResource resource) {
        long fileSizeInBytes = resource.getSize();
        long thresholdInBytes;
        if (addonsManager.addonByType(CoreAddons.class).isAol()) {
            thresholdInBytes = ConstantValues.cloudBinaryProviderRedirectThresholdInBytes.getLong();
        } else {
            thresholdInBytes = (long) StorageUnit.MB.toBytes(getDownloadRedirectGlobalConfig().getFileMinimumSize());
        }
        if (fileSizeInBytes >= thresholdInBytes) {
            log.debug("File '{}' is of size {}, greater or equals to redirect thresholdInBytes {}. Sending redirect",
                    resource.getRepoPath().getId(), fileSizeInBytes, thresholdInBytes);
            return true;
        }
        log.debug("File '{}' is of size {}, less than redirect thresholdInBytes {}. Serving stream instead of redirect",
                resource.getRepoPath().getId(), fileSizeInBytes, thresholdInBytes);
        return false;
    }
}
