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

package org.artifactory.ui.rest.service.admin.advanced;

import org.artifactory.rest.common.service.admin.advance.sumologic.*;
import org.artifactory.ui.rest.service.admin.advanced.configDescriptor.GetConfigDescriptorService;
import org.artifactory.ui.rest.service.admin.advanced.configDescriptor.UpdateConfigDescriptorService;
import org.artifactory.ui.rest.service.admin.advanced.maintenance.*;
import org.artifactory.ui.rest.service.admin.advanced.replication.GetGlobalReplicationsConfigService;
import org.artifactory.ui.rest.service.admin.advanced.replication.UpdateGlobalReplicationsConfigService;
import org.artifactory.ui.rest.service.admin.advanced.securitydescriptor.GetSecurityDescriptorService;
import org.artifactory.ui.rest.service.admin.advanced.securitydescriptor.UpdateSecurityDescriptorService;
import org.artifactory.ui.rest.service.admin.advanced.storage.GetBinaryProvidersInfoService;
import org.artifactory.ui.rest.service.admin.advanced.storagesummary.GetRefreshStatusService;
import org.artifactory.ui.rest.service.admin.advanced.storagesummary.GetUIStorageSummaryService;
import org.artifactory.ui.rest.service.admin.advanced.storagesummary.RefreshStorageSummaryService;
import org.artifactory.ui.rest.service.admin.advanced.support.*;
import org.artifactory.ui.rest.service.admin.advanced.systeminfo.GetSystemInfoService;
import org.artifactory.ui.rest.service.admin.advanced.systemlogs.GetSysLogDataService;
import org.artifactory.ui.rest.service.admin.advanced.systemlogs.GetSysLogDownloadLinkService;
import org.artifactory.ui.rest.service.admin.advanced.systemlogs.GetSysLogsInitializeService;
import org.springframework.beans.factory.annotation.Lookup;

/**
 * @author Chen Keinan
 */
public abstract class AdvancedServiceFactory {

    // storage summary service
    @Lookup
    public abstract GetUIStorageSummaryService getStorageSummaryService();
    // refresh storage summary service
    @Lookup
    public abstract RefreshStorageSummaryService refreshStorageSummaryService();
    // get refresh status service
    @Lookup
    public abstract GetRefreshStatusService getRefreshStatusService();
    // system info service
    @Lookup
    public abstract GetSystemInfoService getSystemInfoService();
    // config descriptor service
    @Lookup
    public abstract UpdateConfigDescriptorService updateConfigDescriptorService();

    @Lookup
    public abstract GetConfigDescriptorService getConfigDescriptorService();
    // security descriptor service
    @Lookup
    public abstract UpdateSecurityDescriptorService updateSecurityConfigService();

    @Lookup
    public abstract GetBinaryProvidersInfoService getBinaryProvidersInfoService();

    @Lookup
    public abstract GetSecurityDescriptorService getSecurityDescriptorService();

    @Lookup
    public abstract CleanUnusedCachedService cleanUnusedCached();

    @Lookup
    public abstract CleanupVirtualRepoService cleanupVirtualRepo();

    @Lookup
    public abstract GarbageCollectionService garbageCollection();

    @Lookup
    public abstract SaveMaintenanceService saveMaintenance();

    @Lookup
    public abstract PruneUnReferenceDataService pruneUnReferenceData();

    @Lookup
    public abstract CompressInternalDataService compressInternalData();

    @Lookup
    public abstract GetMaintenanceService getMaintenance();

    @Lookup
    public abstract GetSysLogDataService getSystemLogData();

    @Lookup
    public abstract GetSysLogsInitializeService getSystemLogsInitialize();

    @Lookup
    public abstract GetSysLogDownloadLinkService getSystemLogDownloadLink();

    @Lookup
    public abstract SupportServiceGenerateBundle<BundleConfigurationWrapper> getSupportServiceGenerateBundle();

    @Lookup
    public abstract SupportServiceDownloadBundle<String> getSupportServiceDownloadBundle();

    @Lookup
    public abstract SupportServiceListBundles getSupportServiceListBundles();

    @Lookup
    public abstract SupportServiceDeleteBundle getSupportServiceDeleteBundle();

    @Lookup
    public abstract GetGlobalReplicationsConfigService getGlobalReplicationConfig();

    @Lookup
    public abstract UpdateGlobalReplicationsConfigService updateGlobalReplicationConfig();

    @Lookup
    public abstract RegisterSumoLogicApplicationService registerSumoLogicApplicationService();

    @Lookup
    public abstract GetSumoLogicConfigService getSumoLogicConfigService();

    @Lookup
    public abstract RefreshSumoLogicTokenService refreshSumoLogicTokenService();

    @Lookup
    public abstract UpdateSumoLogicConfigService updateSumoLogicConfigService();

    @Lookup
    public abstract UpdateSumoLogicProxyService updateSumoLogicProxyService();

    @Lookup
    public abstract ResetSumoLogicApplicationService resetSumoLogicApplicationService();
}
