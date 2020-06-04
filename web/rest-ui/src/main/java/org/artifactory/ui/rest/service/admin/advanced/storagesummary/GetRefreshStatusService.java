package org.artifactory.ui.rest.service.admin.advanced.storagesummary;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.ha.HaCommonAddon;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.storage.RefreshStatusIModel;
import org.jfrog.storage.common.ConflictGuard;
import org.jfrog.storage.common.ConflictsGuard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.artifactory.storage.service.StorageServiceImpl.STORAGE_SUMMARY_KEY;
import static org.artifactory.storage.service.StorageServiceImpl.STORAGE_SUMMARY_MAP;

/**
 * @author Inbar Tal
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetRefreshStatusService implements RestService {

    @Autowired
    private AddonsManager addonsManager;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        ConflictsGuard<Object> conflictsGuard = addonsManager.addonByType(HaCommonAddon.class)
                .getConflictsGuard(STORAGE_SUMMARY_MAP);
        ConflictGuard lock = conflictsGuard.getLock(STORAGE_SUMMARY_KEY);

        RefreshStatusIModel refreshStatusModel = new RefreshStatusIModel(lock.isLocked());
        response.iModel(refreshStatusModel);
    }
}
