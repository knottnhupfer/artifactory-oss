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

package org.artifactory.ui.rest.service.artifacts.search.packagesearch;

import com.google.common.collect.Lists;
import org.apache.http.HttpStatus;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.xray.XrayAddon;
import org.artifactory.addon.xray.XrayArtifactsSummary;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.aql.AqlService;
import org.artifactory.aql.api.domain.sensitive.AqlApiItem;
import org.artifactory.aql.api.internal.AqlBase;
import org.artifactory.aql.result.AqlLazyResult;
import org.artifactory.aql.result.rows.AqlItem;
import org.artifactory.aql.result.rows.AqlLazyObjectResultStreamer;
import org.artifactory.aql.result.rows.FileInfoWithStatisticsItemRow;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.search.NativeExtraInfoModel;
import org.artifactory.ui.rest.model.artifacts.search.PackageNativeXraySummaryModel;
import org.artifactory.ui.rest.model.artifacts.search.packagesearch.search.AqlUISearchModel;
import org.artifactory.ui.rest.service.artifacts.search.NativeDockerTotalDownloadSearchService;
import org.artifactory.ui.rest.service.artifacts.search.packagesearch.util.PackageNativeModelHandler;
import org.artifactory.ui.rest.service.artifacts.search.packagesearch.util.PackageNativeSearchHelper;
import org.artifactory.ui.rest.service.artifacts.search.versionsearch.NativeSearchControls;
import org.artifactory.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.artifactory.ui.rest.service.artifacts.search.packagesearch.util.PackageNativeRestConstants.TYPE;

/**
 * @author Lior Gur
 * @author Inbar Tal
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class VersionNativeExtraInfoService implements RestService {

    private static final Logger log = LoggerFactory.getLogger(NativeDockerTotalDownloadSearchService.class);

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private PackageNativeSearchHelper packageNativeSearchHelper;

    @Autowired
    private AddonsManager addonsManager;

    @Autowired
    private AqlService aqlService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        NativeSearchControls searchControls = NativeSearchControls.builder()
                .type(request.getPathParamByKey(TYPE))
                .searches(request.getModels())
                .build();

        if (!isValidRequest(searchControls)) {
            log.debug("Request should contain package name and package version");
            response.responseCode(HttpStatus.SC_BAD_REQUEST);
            response.error("Package name or package version is missing");
            return;
        }
        response.iModel(searchAndUpdateResults(searchControls));
    }

    private boolean isValidRequest(NativeSearchControls searchControls) {
        PackageNativeModelHandler modelHandler = searchControls.getModelHandler();
        String namePropKey = modelHandler.getNamePropKey();
        String versionPropKey = modelHandler.getVersionPropKey();
        List<AqlUISearchModel> searchModels = searchControls.getSearches();
        if (searchModels.stream().noneMatch(model -> namePropKey.equals(model.getId()))) {
            return false;
        }
        return searchModels.stream().anyMatch(model -> versionPropKey.equals(model.getId()));
    }

    private NativeExtraInfoModel searchAndUpdateResults(NativeSearchControls searchControls) {
        NativeExtraInfoModel nativeExtraInfoModel = new NativeExtraInfoModel();
        List<AqlUISearchModel> searches = searchControls.getSearches();
        PackageNativeModelHandler modelHandler = searchControls.getModelHandler();

        AqlBase query = packageNativeSearchHelper.buildQuery(searchControls, true,
                Lists.newArrayList(AqlApiItem.statistic().downloads()), modelHandler.getVersionExtraInfoPropKeys());

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
            List<RepoPath> repoPaths = Lists.newArrayList();
            String sha2 = null;
            while ((row = streamer.getRow()) != null) {
                if (authorizationService.canRead(row.getRepoPath())) {
                    totalDownloads += row.getStatDownloaded();
                    repoPaths.add(row.getRepoPath());
                    sha2 = row.getSha2();
                    nativeExtraInfoModel.addKeywords(row.getPropVal());
                }
            }

            XrayAddon xrayAddon = addonsManager.addonByType(XrayAddon.class);
            if (xrayAddon.isXrayEnabled()) {
                String version = extractVersion(searches, modelHandler);
                PackageNativeXraySummaryModel xraySummaryModel = addXraySummary(xrayAddon, repoPaths, version,
                        searchControls.getType(), sha2);
                nativeExtraInfoModel.setXrayStatus(xraySummaryModel.getXrayStatus());
                nativeExtraInfoModel.setDetailsUrl(xraySummaryModel.getDetailsUrl());
            }
            nativeExtraInfoModel.setTotalDownloads(totalDownloads);
        } catch (Exception e) {
            log.debug("Failed to get results for ", e);
        }
        return nativeExtraInfoModel;
    }

    private String extractVersion(List<AqlUISearchModel> searches, PackageNativeModelHandler modelHandler) {
        return searches.stream()
                        .filter(r -> r.getId().equals(modelHandler.getVersionPropKey()))
                        .map(AqlUISearchModel::getValues)
                        .filter(CollectionUtils::notNullOrEmpty)
                        .map(r -> r.get(0))
                        .findFirst().orElse(null);
    }

    private PackageNativeXraySummaryModel addXraySummary(XrayAddon xrayAddon, List<RepoPath> repoPaths, String version,
            String type, String sha2) {
        PackageNativeXraySummaryModel xraySummaryModel = new PackageNativeXraySummaryModel(version);
        if (CollectionUtils.notNullOrEmpty(repoPaths)) {
            XrayArtifactsSummary artifactXraySummary = xrayAddon
                    .getArtifactXraySummary(sha2, repoPaths, version, type, false);
            xraySummaryModel.addXraySummary(artifactXraySummary);

        }
        return xraySummaryModel;
    }
}