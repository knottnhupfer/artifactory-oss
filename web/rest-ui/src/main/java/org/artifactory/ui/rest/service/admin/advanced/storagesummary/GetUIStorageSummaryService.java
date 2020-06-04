package org.artifactory.ui.rest.service.admin.advanced.storagesummary;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.CoreAddons;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.storage.StorageService;
import org.artifactory.storage.StorageSummaryInfo;
import org.artifactory.ui.rest.model.storage.StorageSummaryUIModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Inbar Tal
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetUIStorageSummaryService implements RestService {

    @Autowired
    private StorageService storageService;

    @Autowired
    private AddonsManager addonsManager;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        StorageSummaryInfo info = storageService.getStorageSummaryInfoFromCache();
        StorageSummaryUIModel model = new StorageSummaryUIModel(info);
        CoreAddons coreAddons = addonsManager.addonByType(CoreAddons.class);
        if (coreAddons.isAol() && !coreAddons.isDashboardUser()) {
            model.getFileStoreSummary().setStorageDirectory("Artifactory Cloud");
        }
        response.iModel(model);
    }
}
