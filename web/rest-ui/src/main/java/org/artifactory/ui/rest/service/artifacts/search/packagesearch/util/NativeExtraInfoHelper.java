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

package org.artifactory.ui.rest.service.artifacts.search.packagesearch.util;

import com.google.common.collect.Lists;
import org.artifactory.api.properties.PropertiesService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.aql.AqlService;
import org.artifactory.aql.api.domain.sensitive.AqlApiItem;
import org.artifactory.aql.api.internal.AqlBase;
import org.artifactory.aql.result.AqlLazyResult;
import org.artifactory.aql.result.rows.AqlItem;
import org.artifactory.aql.result.rows.AqlLazyObjectResultStreamer;
import org.artifactory.aql.result.rows.FileInfoWithStatisticsItemRow;
import org.artifactory.md.Properties;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.ui.rest.model.artifacts.search.NativeSummaryExtraInfoModel;
import org.artifactory.ui.rest.model.artifacts.search.packagesearch.search.AqlUISearchModel;
import org.artifactory.ui.rest.service.artifacts.search.versionsearch.NativeSearchControls;
import org.artifactory.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static org.artifactory.ui.rest.service.artifacts.search.packagesearch.util.PackageNativeConstants.ARTIFACTORY_LIC;

/**
 * @author Inbar Tal
 */
@Component
public class NativeExtraInfoHelper {

    private static final Logger log = LoggerFactory.getLogger(NativeExtraInfoHelper.class);

    private PropertiesService propertiesService;
    private AuthorizationService authorizationService;
    private PackageNativeSearchHelper packageNativeSearchHelper;
    private AqlService aqlService;

    @Autowired
    public NativeExtraInfoHelper(PropertiesService propertiesService,
            AuthorizationService authorizationService,
            PackageNativeSearchHelper packageNativeSearchHelper, AqlService aqlService) {
        this.propertiesService = propertiesService;
        this.authorizationService = authorizationService;
        this.packageNativeSearchHelper = packageNativeSearchHelper;
        this.aqlService = aqlService;
    }

    public NativeSummaryExtraInfoModel getSummaryExtraInfo(String path, List<AqlUISearchModel> searches) {
        int totalDownloads = getTotalDownloads(searches);
        Properties properties = propertiesService.getProperties(RepoPathFactory.create(path));
        Set<String> licenses = properties.get(ARTIFACTORY_LIC);
        String license = "";
        if (CollectionUtils.notNullOrEmpty(licenses) && licenses.iterator().hasNext()) {
            license = licenses.iterator().next();
        }
        return new NativeSummaryExtraInfoModel(license, totalDownloads);
    }

    public int getTotalDownloads(List<AqlUISearchModel> searches) {
        NativeSearchControls searchControls = NativeSearchControls.builder().searches(searches).build();
        AqlBase query = packageNativeSearchHelper.buildQuery(searchControls, false,
                Lists.newArrayList(AqlApiItem.statistic().downloads()), null);

        if (log.isDebugEnabled()) {
            log.debug("strategies resolved to query: {}", query.toNative(0));
        }

        long timer = System.currentTimeMillis();
        int totalDownloads = 0;

        try (AqlLazyResult<AqlItem> results = aqlService.executeQueryLazy(query)) {
            log.trace("Search took {} milliseconds", System.currentTimeMillis() - timer);

            AqlLazyObjectResultStreamer<FileInfoWithStatisticsItemRow> streamer = new AqlLazyObjectResultStreamer<>(
                    results, FileInfoWithStatisticsItemRow.class);

            FileInfoWithStatisticsItemRow row;
            while ((row = streamer.getRow()) != null) {
                if (authorizationService.canRead(row.getRepoPath())) {
                    totalDownloads += row.getStatDownloaded();
                }
            }
        } catch (Exception e) {
            log.debug(e.getMessage());
        }
        return totalDownloads;
    }
}
