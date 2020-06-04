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

package org.artifactory.ui.rest.service.artifacts.search.versionsearch;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.xray.XrayAddon;
import org.artifactory.addon.xray.XrayArtifactsSummary;
import org.artifactory.aql.AqlService;
import org.artifactory.aql.api.domain.sensitive.AqlApiItem;
import org.artifactory.aql.api.internal.AqlBase;
import org.artifactory.aql.model.AqlComparatorEnum;
import org.artifactory.aql.result.rows.AqlBaseFullRowImpl;
import org.artifactory.aql.result.rows.AqlItem;
import org.artifactory.aql.util.AqlUtils;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.search.PackageNativeXraySummaryModel;
import org.artifactory.ui.rest.model.artifacts.search.VersionsDockerNativeModel;
import org.artifactory.ui.rest.model.artifacts.search.packagesearch.search.AqlUISearchModel;
import org.artifactory.ui.rest.model.artifacts.search.versionsearch.result.VersionDockerNativeModel;
import org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.docker.DockerV2ManifestBaseService;
import org.artifactory.ui.rest.service.artifacts.search.NativeDockerTotalDownloadSearchService;
import org.jfrog.security.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.artifactory.ui.rest.service.artifacts.search.packagesearch.util.NativeDockerSearchHelper.buildVersionItemQuery;

/**
 * @author ortalh
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class VersionNativeDockerListSearchService extends DockerV2ManifestBaseService implements RestService {

    private static final Logger log = LoggerFactory.getLogger(VersionNativeDockerListSearchService.class);
    private static final String TYPE = "type";
    private static final String PACKAGE_NAME = "packageName";
    private static final String SORT_BY = "sort_by";
    private static final String ORDER = "order";
    private static final String LIMIT = "limit";
    private static final String FROM = "from";
    private static final String WITH_XRAY = "with_xray";

    @Autowired
    private AqlService aqlService;

    @Autowired
    private AddonsManager addonsManager;

    @Autowired
    private NativeDockerTotalDownloadSearchService dockerTotalDownloadSearchService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        NativeSearchControls searchControls = NativeSearchControls.builder()
                .searches((List<AqlUISearchModel>) request.getModels())
                .type(request.getPathParamByKey(TYPE))
                .packageName(request.getQueryParamByKey(PACKAGE_NAME))
                .sort(request.getQueryParamByKey(SORT_BY))
                .order(request.getQueryParamByKey(ORDER))
                .limit(request.getQueryParamByKey(LIMIT))
                .from(request.getQueryParamByKey(FROM))
                .withXray(Boolean.TRUE.toString().equalsIgnoreCase(request.getQueryParamByKey(WITH_XRAY)))
                .build();

        if (StringUtils.isEmpty(searchControls.getPackageName())) {
            response.error("Package name is missing");
            response.responseCode(400);
            log.error("Package name is missing");
            return;
        }

        List<String> values = Lists.newArrayList(searchControls.getPackageName());

        searchControls.getSearches().add(new AqlUISearchModel("pkg", AqlComparatorEnum.matches, values));
        VersionsDockerNativeModel model = search(searchControls);
        response.iModel(model);
    }

    public VersionsDockerNativeModel search(NativeSearchControls searchControls) {
        AqlBase.PropertyResultFilterClause<AqlApiItem> firstFilter = AqlApiItem
                .propertyResultFilter(AqlApiItem.property().key().equal("docker.manifest"));
        AqlBase.PropertyResultFilterClause<AqlApiItem> secondFilter = AqlApiItem
                .propertyResultFilter(AqlBase.or(AqlApiItem.property().key().equal("docker.manifest.digest"),
                        AqlApiItem.property().key().equal("sha256")));

        AqlApiItem aqlApiItem = buildVersionItemQuery(searchControls, true, firstFilter);
        AqlApiItem aqlApiItemWithDigest = buildVersionItemQuery(searchControls, true, secondFilter);
        if (log.isDebugEnabled()) {
            log.debug("strategies resolved to query: {}", aqlApiItem.toNative(0));
            log.debug("strategies resolved to query: {}", aqlApiItemWithDigest.toNative(0));
        }
        return executeSearch(searchControls, aqlApiItem, aqlApiItemWithDigest);
    }

    private VersionsDockerNativeModel executeSearch(NativeSearchControls searchControls, AqlApiItem query,
            AqlApiItem queryWithDigest) {
        long timer = System.currentTimeMillis();
        List<AqlItem> queryResults = aqlService.executeQueryEager(query).getResults();
        Map<Long, String> resultsWithDigest = Maps.newHashMap();
        aqlService.executeQueryEager(queryWithDigest).getResults().stream()
                .filter(item -> isNotBlank(((AqlBaseFullRowImpl) item).getValue()))
                .forEach(item -> resultsWithDigest
                        .putIfAbsent(item.getNodeId(), ((AqlBaseFullRowImpl) item).getValue()));

        List<AqlItem> resultsByPath = AqlUtils.aggregateRowByPermission(queryResults);

        VersionsDockerNativeModel results = reduceAggregatedResultRows(searchControls.getPackageName(), resultsByPath,
                resultsWithDigest);

        if (searchControls.isWithXray()) {
            XrayAddon xrayAddon = addonsManager.addonByType(XrayAddon.class);
            if (xrayAddon.isXrayEnabled()) {
                Map<Pair<String, String>, VersionDockerNativeModel> resultsMap = results.getVersions().stream()
                        .limit(searchControls.limit())
                        .collect(Collectors.toMap(e -> new Pair<>(e.getName(), e.getRepoKey()), e -> e));
                for (AqlItem resultByPath : resultsByPath) {
                    String version = ((AqlBaseFullRowImpl) resultByPath).getValue();
                    PackageNativeXraySummaryModel xrayViolations = addXrayViolations(xrayAddon, resultByPath, version);
                    if (xrayViolations.getErrorStatus() != null) {
                        results.setErrorStatus(xrayViolations.getErrorStatus());
                        results.setVersions(null);
                        break;
                    }
                    Pair<String, String> versionAndRepo = new Pair<>(version, resultByPath.getRepo());
                    VersionDockerNativeModel versionDockerNativeModel = resultsMap.get(versionAndRepo);
                    if (versionDockerNativeModel != null) {
                        versionDockerNativeModel.setXrayViolations(xrayViolations);
                    } else {
                        log.warn("Can't add xray summary to Docker package viewer request for {}", versionAndRepo);
                    }
                }
            }
        }

        log.trace("Search found {} results in {} milliseconds", results.getResultsCount(),
                System.currentTimeMillis() - timer);
        return results;
    }

    //TODO [by shayb]: This is a quick fix for the 5.10 release, we must move the docker-logic from here, this should be generic code
    private VersionsDockerNativeModel reduceAggregatedResultRows(String packageName, List<AqlItem> resultsByPath,
            Map<Long, String> resultsWithDigest) {
        VersionsDockerNativeModel result = new VersionsDockerNativeModel();
        result.setLastModified(new Date(0));
        List<VersionDockerNativeModel> versions = Lists.newArrayList();
        resultsByPath.forEach(item -> {
            String name = ((AqlBaseFullRowImpl) item).getValue();
            String repoKey = item.getRepo();
            Date lastModified = item.getModified();
            String digest = resultsWithDigest.get(item.getNodeId());
            String manifestDigest = getPackageIdFromDigestProperty(digest);
            VersionDockerNativeModel nativeResult = new VersionDockerNativeModel(name, manifestDigest, repoKey,
                    lastModified);
            versions.add(nativeResult);
            if (result.getLastModified().before(nativeResult.getLastModified())) {
                result.setLastModified(nativeResult.getLastModified());
            }
        });
        result.setPackageName(packageName);
        result.setResultsCount(versions.size());
        result.setVersions(versions);
        return result;
    }

    private PackageNativeXraySummaryModel addXrayViolations(XrayAddon xrayAddon,
            AqlItem resultByPath, String version) {
        PackageNativeXraySummaryModel xraySummaryModel = new PackageNativeXraySummaryModel(version);
        RepoPath repoPath = RepoPathFactory.create(resultByPath.getRepo(),
                resultByPath.getPath() + RepoPath.PATH_SEPARATOR + resultByPath.getName());
        XrayArtifactsSummary artifactXraySummary = xrayAddon
                .getArtifactXraySummary(resultByPath.getSha2(), Lists.newArrayList(repoPath), version,
                        RepoType.Docker.getType(), true);
        xraySummaryModel.setErrorStatus(artifactXraySummary.getErrorStatus());
        xraySummaryModel.addXraySummary(artifactXraySummary);
        xraySummaryModel.setTotalDownloads(((AqlBaseFullRowImpl) resultByPath).getDownloads());
        return xraySummaryModel;
    }
}
