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

import com.github.zafarkhaja.semver.Version;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.xray.XrayAddon;
import org.artifactory.addon.xray.XrayArtifactsSummary;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.aql.model.AqlComparatorEnum;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.search.PackageNativeXraySummaryModel;
import org.artifactory.ui.rest.model.artifacts.search.SearchResult;
import org.artifactory.ui.rest.model.artifacts.search.VersionsNativeModel;
import org.artifactory.ui.rest.model.artifacts.search.packagesearch.result.PackageNativeSearchResult;
import org.artifactory.ui.rest.model.artifacts.search.packagesearch.search.AqlUISearchModel;
import org.artifactory.ui.rest.model.artifacts.search.versionsearch.result.VersionNativeModel;
import org.artifactory.ui.rest.service.artifacts.search.packagesearch.util.NativeExtraInfoHelper;
import org.artifactory.ui.rest.service.artifacts.search.packagesearch.util.PackageNativeModelHandler;
import org.artifactory.ui.rest.service.artifacts.search.packagesearch.util.PackageNativeSearchHelper;
import org.artifactory.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static org.artifactory.ui.rest.service.artifacts.search.packagesearch.util.PackageNativeConstants.ORDER_DESC;
import static org.artifactory.ui.rest.service.artifacts.search.packagesearch.util.PackageNativeConstants.SORT_BY_LAST_MODIFIED;
import static org.artifactory.ui.rest.service.artifacts.search.packagesearch.util.PackageNativeRestConstants.*;

/**
 * @author Inbar Tal
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class VersionNativeListSearchService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(VersionNativeListSearchService.class);

    private PackageNativeSearchHelper packageNativeSearchHelper;
    private AddonsManager addonsManager;
    private RepositoryService repositoryService;
    private NativeExtraInfoHelper nativeExtraInfoHelper;

    @Autowired
    public VersionNativeListSearchService(PackageNativeSearchHelper packageNativeSearchHelper,
            AddonsManager addonsManager, RepositoryService repositoryService,
            NativeExtraInfoHelper nativeExtraInfoHelper) {
        this.packageNativeSearchHelper = packageNativeSearchHelper;
        this.addonsManager = addonsManager;
        this.repositoryService = repositoryService;
        this.nativeExtraInfoHelper = nativeExtraInfoHelper;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        NativeSearchControls searchControls = NativeSearchControls.builder()
                .searches(request.getModels())
                .type(request.getPathParamByKey(TYPE))
                .sort(request.getQueryParamByKey(SORT_BY))
                .order(request.getQueryParamByKey(ORDER))
                .limit(request.getQueryParamByKey(LIMIT))
                .from(request.getQueryParamByKey(FROM))
                .withXray(Boolean.TRUE.toString().equalsIgnoreCase(request.getQueryParamByKey(WITH_XRAY)))
                .build();

        if (CollectionUtils.isNullOrEmpty(searchControls.getSearches())) {
            List<VersionNativeModel> results = Lists.newArrayList();
            response.iModel(new VersionsNativeModel(results));
            return;
        }
        VersionsNativeModel model = search(searchControls);
        response.iModel(model);
    }

    private VersionsNativeModel search(NativeSearchControls searchControls) {
        PackageNativeModelHandler modelHandler = searchControls.getModelHandler();

        SearchResult<PackageNativeSearchResult> searchResults = packageNativeSearchHelper
                .search(searchControls, modelHandler.getVersionPropKeys());

        List<VersionNativeModel> nativeResults = createNativeResults(searchResults, modelHandler);
        List<VersionNativeModel> sortedResults = sortResults(nativeResults, searchControls.getSort(),
                searchControls.getOrder());
        if (searchControls.limit() < sortedResults.size()) {
            sortedResults = sortedResults.subList(0, searchControls.limit());
        }
        VersionsNativeModel versionsNativeModel = new VersionsNativeModel(sortedResults);
        if (searchControls.isWithXray()) {
            getXraySummary(searchControls, searchResults, sortedResults, versionsNativeModel);
        }
        return versionsNativeModel;
    }

    private void getXraySummary(NativeSearchControls searchControls,
            SearchResult<PackageNativeSearchResult> searchResults, List<VersionNativeModel> sortedResults,
            VersionsNativeModel versionsNativeModel) {
        List<String> versions = sortedResults.stream()
                .map(VersionNativeModel::getName)
                .collect(Collectors.toList());
        try {
            log.debug("Adding xray violations summary and total downloads for {}:{}", searchControls.getPackageName(),
                    searchControls.getVersionName());
            addXrayViolations(searchControls, searchResults, versionsNativeModel, versions);
            addTotalDownloadsToXray(searchControls, versionsNativeModel);
        } catch (IllegalStateException e) {
            versionsNativeModel.setErrorStatus(e.getMessage());
            versionsNativeModel.setResults(null);
            log.trace("", e);
        }
    }

    private void addTotalDownloadsToXray(NativeSearchControls searchControls, VersionsNativeModel versionsNativeModel) {
        List<AqlUISearchModel> searches = searchControls.getSearches();
        String versionPropKey = searchControls.getModelHandler().getVersionPropKey();
        versionsNativeModel.getResults().forEach(version -> {
            AqlUISearchModel versionSearch = new AqlUISearchModel(versionPropKey, AqlComparatorEnum.matches,
                    Lists.newArrayList(version.getName()));
            searches.add(versionSearch);
            int totalDownloads = nativeExtraInfoHelper.getTotalDownloads(searches);
            version.getXrayViolations().setTotalDownloads(totalDownloads);
            searches.remove(versionSearch);
        });
    }

    private void addXrayViolations(NativeSearchControls searchControls,
            SearchResult<PackageNativeSearchResult> searchResults, VersionsNativeModel versionsNativeModel,
            List<String> versions) {
        if (addonsManager.addonByType(XrayAddon.class).isXrayEnabled()) {
            Map<String, VersionNativeModel> versionsMap = versionsNativeModel.getResults().stream()
                    .filter(r -> versions.contains(r.getName()))
                    .collect(Collectors.toMap(VersionNativeModel::getName, r -> r));
            addXrayExtraInfo(versionsMap, searchResults.getResults(), searchControls.getModelHandler(), searchControls.getType());
        }
    }

    private List<VersionNativeModel> createNativeResults(SearchResult<PackageNativeSearchResult> searchResults,
            PackageNativeModelHandler modelHandler) {
        Collection<PackageNativeSearchResult> packageSearchResults = searchResults.getResults();
        Map<String, VersionNativeModel> versionNativeResults = Maps.newHashMap();

        for (PackageNativeSearchResult packageResult : packageSearchResults) {
            String versionName = modelHandler.getPackageVersion(packageResult);
            VersionNativeModel currentVersion = versionNativeResults.get(versionName);
            mergeResults(versionNativeResults, versionName, packageResult, currentVersion, modelHandler);
        }
        return new ArrayList<>(versionNativeResults.values());
    }

    private void addXrayExtraInfo(Map<String, VersionNativeModel> versionsMap,
            Collection<PackageNativeSearchResult> queryResults, PackageNativeModelHandler modelHandler,
            String repoType) {
        XrayAddon xrayAddon = addonsManager.addonByType(XrayAddon.class);
        Map<String/*version*/, List<RepoPath>> versionPaths = Maps.newHashMap();
        for (PackageNativeSearchResult item : queryResults) {
            String version = modelHandler.getPackageVersion(item);
            if (versionsMap.keySet().contains(version)) {
                RepoPath repoPath = RepoPathFactory.create(modelHandler.getPath(item.getRepoPath()));
                versionPaths.computeIfAbsent(version, r -> Lists.newArrayList()).add(repoPath);
            }

        }
        versionPaths.forEach((version, repoPaths) -> {
            String sha2 = repositoryService.getFileInfo(repoPaths.get(0)).getSha2();
            PackageNativeXraySummaryModel xraySummaryModel = addXraySummary(xrayAddon, repoPaths, version,
                    sha2, repoType);
            versionsMap.get(version).setXrayViolations(xraySummaryModel);
        });
    }

    private PackageNativeXraySummaryModel addXraySummary(XrayAddon xrayAddon, List<RepoPath> repoPaths, String version,
            String sha2, String repoType) {
        PackageNativeXraySummaryModel xraySummaryModel = new PackageNativeXraySummaryModel(version);
        if (repoPaths != null) {
            XrayArtifactsSummary artifactXraySummary = xrayAddon
                    .getArtifactXraySummary(sha2, repoPaths, version, repoType, true);
            if (artifactXraySummary.getErrorStatus() != null) {
                throw new IllegalStateException(artifactXraySummary.getErrorStatus());
            }
            xraySummaryModel.addXraySummary(artifactXraySummary);
        }
        return xraySummaryModel;
    }

    private void mergeResults(Map<String, VersionNativeModel> versionNativeResults, String versionName,
            PackageNativeSearchResult versionResult, VersionNativeModel currentVersion,
            PackageNativeModelHandler modelHandler) {
        if (currentVersion != null) {
            modelHandler.mergeVersionFields(currentVersion, versionResult);
        } else {
            currentVersion = new VersionNativeModel();
            modelHandler.mergeVersionFields(currentVersion, versionResult);
            versionNativeResults.put(versionName, currentVersion);
        }
    }

    private List<VersionNativeModel> sortResults(List<VersionNativeModel> nativeResults, String sort,
            String orderType) {
        if (SORT_BY_LAST_MODIFIED.equals(sort)) {
            return sortByLastModified(nativeResults, orderType);
        }
        return sortByVersion(nativeResults, orderType);
    }

    private List<VersionNativeModel> sortByLastModified(List<VersionNativeModel> nativeResults, String orderType) {
        Comparator<VersionNativeModel> comparator = Comparator.comparing(VersionNativeModel::getLastModified)
                .thenComparing(VersionNativeModel::getName);

        if (ORDER_DESC.equals(orderType)) {
            comparator = comparator.reversed();
        }
        return nativeResults.stream().sorted(comparator).collect(Collectors.toList());
    }

    private List<VersionNativeModel> sortByVersion(List<VersionNativeModel> nativeResults, String orderType) {
        Comparator<VersionNativeModel> comparator = new CompareByVersion();

        if (ORDER_DESC.equals(orderType)) {
            comparator = comparator.reversed();
        }
        return nativeResults.stream().sorted(comparator).collect(Collectors.toList());
    }

    private static class CompareByVersion implements Comparator<VersionNativeModel> {
        @Override
        public int compare(VersionNativeModel ver1, VersionNativeModel ver2) {
            return Version.BUILD_AWARE_ORDER.compare(Version.valueOf(ver1.getName()),
                    Version.valueOf(ver2.getName()));
        }
    }
}
