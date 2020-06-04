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

package org.artifactory.ui.rest.service.artifacts.search;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.xray.XrayAddon;
import org.artifactory.addon.xray.XrayArtifactsSummary;
import org.artifactory.aql.AqlService;
import org.artifactory.aql.api.domain.sensitive.AqlApiItem;
import org.artifactory.aql.api.internal.AqlBase;
import org.artifactory.aql.model.AqlComparatorEnum;
import org.artifactory.aql.result.rows.AqlBaseFullRowImpl;
import org.artifactory.aql.result.rows.AqlItem;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.search.PackageNativeTotalDownloadsModel;
import org.artifactory.ui.rest.model.artifacts.search.PackageNativeXraySummaryModel;
import org.artifactory.ui.rest.model.artifacts.search.packagesearch.search.AqlUISearchModel;
import org.artifactory.ui.rest.service.artifacts.search.versionsearch.NativeSearchControls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.artifactory.aql.util.AqlUtils.aggregateRowByPermission;
import static org.artifactory.ui.rest.service.artifacts.search.packagesearch.util.NativeDockerSearchHelper.buildVersionItemQuery;

/**
 * @author ortalh
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class NativeDockerTotalDownloadSearchService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(NativeDockerTotalDownloadSearchService.class);

    private static final String PACKAGE_NAME = "packageName";
    private static final String VERSION_NAME = "versionName";
    private static final String REPO_NAME = "repoKey";

    @Autowired
    private AqlService aqlService;

    @Autowired
    private AddonsManager addonsManager;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        NativeSearchControls searchControls = NativeSearchControls.builder()
                .searches(Lists.newArrayList())
                .type("docker")
                .packageName(request.getQueryParamByKey(PACKAGE_NAME))
                .versionName(request.getQueryParamByKey(VERSION_NAME))
                .repoName(request.getPathParamByKey(REPO_NAME))
                .build();

        String packageName = searchControls.getPackageName();
        if (StringUtils.isEmpty(packageName)) {
            response.error("Package name is missing");
            response.responseCode(HttpStatus.SC_BAD_REQUEST);
            log.error("Package name is missing");
            return;
        }
        String versionName = searchControls.getVersionName();
        List<String> packageValues = new ArrayList<>();
        packageValues.add(packageName);
        List<AqlUISearchModel> searches = searchControls.getSearches();
        searches.add(new AqlUISearchModel("pkg", AqlComparatorEnum.matches, packageValues));
        if (StringUtils.isNotEmpty(versionName)) {
            List<String> versionValues = new ArrayList<>();
            versionValues.add(versionName);
            searches.add(new AqlUISearchModel("version", AqlComparatorEnum.matches, versionValues));
        }
        String repoName = searchControls.getRepoName();
        if (StringUtils.isNotEmpty(repoName)) {
            List<String> repoValues = new ArrayList<>();
            repoValues.add(repoName);
            searches.add(new AqlUISearchModel("repo", AqlComparatorEnum.matches, repoValues));
        }
        PackageNativeTotalDownloadsModel model = search(searchControls);
        response.iModel(model);
    }

    public PackageNativeTotalDownloadsModel search(NativeSearchControls searchControls) {
        AqlBase.PropertyResultFilterClause<AqlApiItem> aqlApiElement = AqlApiItem
                .propertyResultFilter(AqlApiItem.property().key().equal("docker.manifest"));
        AqlApiItem query = buildVersionItemQuery(searchControls, true, aqlApiElement);
        if (log.isDebugEnabled()) {
            log.debug("strategies resolved to query: {}", query.toNative(0));
        }
        return executeSearch(query, searchControls.getPackageName(), searchControls.getVersionName());
    }

    private PackageNativeTotalDownloadsModel executeSearch(/*AqlDomainEnum domain,*/ AqlBase query, String packageName, String versionName) {
        PackageNativeTotalDownloadsModel packageNativeTotalDownloadsModel = new PackageNativeTotalDownloadsModel();
        long timer = System.currentTimeMillis();
        List<AqlItem> queryResults = aqlService.executeQueryEager(query).getResults();
        queryResults = aggregateRowByPermission(queryResults);
        addXrayExtraInfo(versionName, packageNativeTotalDownloadsModel, queryResults);
        int totalDownloads = aggregateDownloadsResultsPackage(queryResults);
        log.debug("Search total downloads for package named {} in {} milliseconds total {}", packageName,
                System.currentTimeMillis() - timer, totalDownloads);
        packageNativeTotalDownloadsModel.setTotalDownloads(totalDownloads);
        return packageNativeTotalDownloadsModel;
    }

    private void addXrayExtraInfo(String version, PackageNativeTotalDownloadsModel dockerExtraInfoModel,
            List<AqlItem> queryResults) {
        XrayAddon xrayAddon = addonsManager.addonByType(XrayAddon.class);
        if (!StringUtils.isEmpty(version) && xrayAddon.isXrayEnabled()) {
            for (AqlItem aqlItem : queryResults) {
                RepoPath repoPath = RepoPathFactory
                        .create(aqlItem.getRepo(), aqlItem.getPath() + RepoPath.PATH_SEPARATOR + aqlItem.getName());
                PackageNativeXraySummaryModel xraySummaryModel = addXraySummary(xrayAddon, repoPath, version,
                        aqlItem.getSha2());
                dockerExtraInfoModel.setXrayStatus(xraySummaryModel.getXrayStatus());
                dockerExtraInfoModel.setDetailsUrl(xraySummaryModel.getDetailsUrl());
            }
        }
    }

    private int aggregateDownloadsResultsPackage(List<AqlItem> queryResults) {
        return queryResults.stream().map(aql -> ((AqlBaseFullRowImpl)aql).getDownloads()).reduce(0, (a, b) -> a + b);
        //return queryResults.stream().mapToInt(AqlBaseFullRowImpl::getDownloads).sum();
    }

    private PackageNativeXraySummaryModel addXraySummary(XrayAddon xrayAddon, RepoPath repoPaths, String version,
            String sha2) {
        PackageNativeXraySummaryModel xraySummaryModel = new PackageNativeXraySummaryModel(version);
        if (repoPaths != null) {
            XrayArtifactsSummary artifactXraySummary = xrayAddon.getArtifactXraySummary(sha2,
                    Lists.newArrayList(repoPaths), version, RepoType.Docker.getType(), false);
            xraySummaryModel.addXraySummary(artifactXraySummary);
        }
        return xraySummaryModel;
    }
}
