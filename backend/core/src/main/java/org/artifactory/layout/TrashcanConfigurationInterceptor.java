package org.artifactory.layout;

import org.artifactory.config.ConfigurationChangesInterceptor;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.trashcan.TrashcanConfigDescriptor;
import org.artifactory.repo.trash.TrashService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author Shay Bagants
 */
@Component
public class TrashcanConfigurationInterceptor implements ConfigurationChangesInterceptor {

    private TrashService trashService;

    @Autowired
    public TrashcanConfigurationInterceptor(TrashService trashService) {
        this.trashService = trashService;
    }

    @Override
    public void onBeforeSave(CentralConfigDescriptor newDescriptor) {
        TrashcanConfigDescriptor trashcanConfig = newDescriptor.getTrashcanConfig();
        if (trashcanConfig != null && trashcanConfig.isEnabled()) {
            int retentionPeriodDays = trashcanConfig.getRetentionPeriodDays();
            long valueFrom = System.currentTimeMillis() - TimeUnit.MILLISECONDS.convert(retentionPeriodDays, TimeUnit.DAYS);
            trashService.validateRetentionPeriodTimestamp(valueFrom);
        }
    }
}
