package org.artifactory.ui.rest.service.admin.advanced.storagesummary;

import org.apache.http.HttpStatus;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Inbar Tal
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class RefreshStorageSummaryService implements RestService {

    @Autowired
    private StorageService storageService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        storageService.calculateStorageSummaryAsync();
        response.responseCode(HttpStatus.SC_ACCEPTED)
                .info("Calculating storage summary scheduled to run successfully");
    }
}
