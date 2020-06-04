package org.artifactory.release.bundle;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.artifactory.api.release.bundle.ReleaseBundleSearchFilter;
import org.artifactory.api.release.bundle.ReleaseBundleSearchService;
import org.artifactory.api.release.bundle.ReleaseBundleService;
import org.artifactory.api.rest.distribution.bundle.models.BundleVersion;
import org.artifactory.api.rest.distribution.bundle.models.BundleVersionUtils;
import org.artifactory.api.rest.distribution.bundle.models.BundleVersionsResponse;
import org.artifactory.api.rest.distribution.bundle.models.BundlesResponse;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.aql.AqlService;
import org.artifactory.aql.api.domain.sensitive.AqlApiReleaseBundle;
import org.artifactory.aql.api.internal.AqlApiDynamicFieldsDomains.AqlApiComparator;
import org.artifactory.aql.api.internal.AqlBase;
import org.artifactory.aql.result.rows.AqlReleaseBundle;
import org.artifactory.bundle.BundleType;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.storage.db.bundle.dao.ArtifactBundlesDao;
import org.artifactory.storage.db.bundle.model.DBArtifactsBundle;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.InternalServerErrorException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import static org.artifactory.aql.api.domain.sensitive.AqlApiReleaseBundle.*;

/**
 * @author Lior Gur
 */
@Service
public class ReleaseBundleSearchServiceImpl implements ReleaseBundleSearchService {
    private static final String FETCH_BUNDLES_ERR = "Failed to fetch bundles ";

    private static final Logger log = LoggerFactory.getLogger(ReleaseBundleSearchServiceImpl.class);

    @Autowired
    private ArtifactBundlesDao artifactsBundleDao;

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private AqlService aqlService;

    @Autowired
    ReleaseBundleService releaseBundleService;


    public BundlesResponse getFilteredBundles(ReleaseBundleSearchFilter filter) {
        BundlesResponse bundlesResponse;
        try {
            List<DBArtifactsBundle> artifactsBundles = Lists.newArrayList();
            if (authorizationService.isAdmin() || authorizationService.canRead(RepoPathFactory.create(releaseBundleService.getDefaultStoringRepo()))){
                filter.setDaoLimit(filter.getLimit());
                artifactsBundles = artifactsBundleDao.getFilteredBundlesLastVersion(filter);

            } else {
                while (artifactsBundles.size() < filter.getLimit()) {
                    List<DBArtifactsBundle> dbBundles = fetchBundlesFromDb(filter, artifactsBundles);
                    List<DBArtifactsBundle> permissionFilteredBundles = filterByPermissions(filter, dbBundles);
                    artifactsBundles.addAll(permissionFilteredBundles);

                    //Stop loop when sql return less results then limit size
                    if (dbBundles.size() < filter.getDaoLimit()) {
                        break;
                    }
                }
            }
            artifactsBundles.stream().limit(filter.getLimit()).collect(Collectors.toList());
            bundlesResponse = populateBundlesResponse(artifactsBundles);
        } catch (SQLException e) {
            log.error("Failed to fetch bundles ", e);
            throw new InternalServerErrorException(e);
        }
        return bundlesResponse;
    }

    private List<DBArtifactsBundle> filterByPermissions(ReleaseBundleSearchFilter filter,
            List<DBArtifactsBundle> dbFilteredBundles) {
        return dbFilteredBundles.stream()
                .filter(bundle -> authorizationService.canRead(RepoPathFactory.create(bundle.getStoringRepo(), bundle.getName() + "/" + bundle.getVersion())))
                .limit(filter.getLimit())
                .collect(Collectors.toList());
    }

    private List<DBArtifactsBundle> fetchBundlesFromDb(ReleaseBundleSearchFilter filter,
            List<DBArtifactsBundle> artifactsBundles) throws SQLException {
        filter.setOffset(artifactsBundles.size());
        return artifactsBundleDao.getFilteredBundlesLastVersion(filter);
    }

    @Override
    public BundleVersionsResponse getBundleVersions(String bundleName, BundleType bundleType) {
        BundleVersionsResponse bundlesResponse = null;
        try {
            List<DBArtifactsBundle> artifactsBundles = artifactsBundleDao.getArtifactsBundles(bundleName, bundleType);
            bundlesResponse = populateBundleVersionsResponse(artifactsBundles);
            BundleVersionUtils.sort(bundlesResponse.getVersions());
            bundlesResponse.setVersions(bundlesResponse.getVersions());
        } catch (SQLException e) {
            log.error("Failed to fetch bundles ", e);
        }
        return bundlesResponse;
    }

    public BundleVersionsResponse getFilterBundleVersions(ReleaseBundleSearchFilter filter) {
        List<AqlReleaseBundle> bundleVersions = Lists.newArrayList();
        if (authorizationService.isAdmin()) {
            filter.setLimit(filter.getLimit());
            bundleVersions = createAqlFilterBundleVersions(filter);
        } else {
            while (bundleVersions.size() <= filter.getLimit()) {
                List<AqlReleaseBundle> aqlBundleVersionsBatch = createAqlFilterBundleVersions(filter);
                List<AqlReleaseBundle> permissionFilterVersions = filterVersionsByPermissions(filter, aqlBundleVersionsBatch);
                bundleVersions.addAll(permissionFilterVersions);
                //Stop loop when sql return less results then limit size
                if (aqlBundleVersionsBatch.size() < filter.getDaoLimit()) {
                    break;
                }
            }
        }
        return populateBundleVersionsResponseAql(bundleVersions.stream().limit(filter.getLimit()).collect(Collectors.toList()));
    }

    private List<AqlReleaseBundle> filterVersionsByPermissions(ReleaseBundleSearchFilter filter,
            List<AqlReleaseBundle> aqlBundleVersionsBatch) {

        return aqlBundleVersionsBatch.stream()
                            .filter(bundle -> authorizationService.canRead(RepoPathFactory.create(bundle.getStoringRepo(), bundle.getReleaseName() + "/" + bundle.getReleaseVersion()))) //todo replace with Inbar Code
                            .limit(filter.getDaoLimit())
                            .collect(Collectors.toList());


    }

    private List<AqlReleaseBundle> createAqlFilterBundleVersions(ReleaseBundleSearchFilter filter) {
        CriteriaClause<AqlApiReleaseBundle> nameEquals = null;
        AqlBase.OrClause<AqlApiReleaseBundle> versions = null;
        AqlBase.CriteriaClause<AqlApiReleaseBundle> before = null;
        AqlBase.CriteriaClause<AqlApiReleaseBundle> after = null;
        if (StringUtils.isNotBlank(filter.getName())) {
            nameEquals = name().equal(filter.getName());
        }
        if (filter.getVersions() != null) {
            versions = AqlBase.or();
            for (String version : filter.getVersions()) {
                versions.append(version().equal(version));
            }
        }
        if (filter.getAfter() != 0) {
            after = created().greaterEquals(filter.getAfter());
        }
        if (filter.getBefore() != 0) {
            before = created().lessEquals(filter.getBefore());
        }
        AqlApiReleaseBundle aql = create().include(AqlApiReleaseBundle.version(),AqlApiReleaseBundle.created(),AqlApiReleaseBundle.status())
                .filter(AqlBase.and(
                        AqlApiReleaseBundle.bundleType().equal(filter.getBundleType().name()),
                        nameEquals,
                        after,
                        before,
                        versions))
                .addSortElement(sortResultsBy(filter))
                .desc();

        if (filter.getDirection().equals("asc")) {
            aql.asc();
        }
        return aqlService.executeQueryEager(aql).getResults();
    }

    private static AqlApiComparator<AqlApiReleaseBundle> sortResultsBy(ReleaseBundleSearchFilter filter) {

        switch (filter.getOrderBy()) {
            case "name":
                return version();
            case "date_created":
                return created();
            case "version":
                return version();
            default:
                return created();
        }
    }

    private BundlesResponse populateBundlesResponse(List<DBArtifactsBundle> artifactsBundles) {
        BundlesResponse bundlesResponse = new BundlesResponse();
        artifactsBundles.forEach(bundle -> {
            BundleVersion bundleVersion = populateBundleVersion(bundle);
            bundlesResponse.add(bundle.getName(), bundleVersion);
        });
        return bundlesResponse;
    }

    private BundleVersionsResponse populateBundleVersionsResponse(List<DBArtifactsBundle> artifactsBundles) {
        BundleVersionsResponse bundleVersionsResponse = new BundleVersionsResponse();
        artifactsBundles.stream()
                .filter(bundle ->
                        authorizationService.canRead(
                                getBundleRepoPath(bundle.getStoringRepo(), bundle.getName(), bundle.getVersion())))
                .forEach(bundle -> {
                    BundleVersion bundleVersion = populateBundleVersion(bundle);
                    bundleVersionsResponse.add(bundleVersion);
                });
        return bundleVersionsResponse;
    }

    private BundleVersion populateBundleVersion(DBArtifactsBundle bundle) {
        BundleVersion bundleVersion = new BundleVersion();
        if (bundle.getStatus() != null) {
            bundleVersion.setStatus(bundle.getStatus().name());
        }
        bundleVersion.setVersion(bundle.getVersion());
        bundleVersion.setCreated(ISODateTimeFormat.dateTime().withZoneUTC().print(bundle.getDateCreated().getMillis()));
        bundleVersion.setStoringRepo(bundle.getStoringRepo());
        return bundleVersion;
    }

    private BundleVersionsResponse populateBundleVersionsResponseAql(List<AqlReleaseBundle> artifactsBundles) {
        BundleVersionsResponse bundleVersionsResponse = new BundleVersionsResponse();
        artifactsBundles.forEach(bundle -> {
            BundleVersion bundleVersion = populateBundleVersionAql(bundle);
            bundleVersionsResponse.add(bundleVersion);
        });
        return bundleVersionsResponse;
    }

    private BundleVersion populateBundleVersionAql(AqlReleaseBundle bundle) {
        BundleVersion bundleVersion = new BundleVersion();
        if (bundle.getReleaseStatus() != null) {
            bundleVersion.setStatus(bundle.getReleaseStatus());
        }
        bundleVersion.setVersion(bundle.getReleaseVersion());
        bundleVersion
                .setCreated(ISODateTimeFormat.dateTime().withZoneUTC().print(bundle.getReleaseCreated().getTime()));
        bundleVersion.setStoringRepo(bundle.getStoringRepo());

        return bundleVersion;
    }


    public BundlesResponse getBundles(BundleType bundleType) {
        BundlesResponse bundlesResponse = null;
        try {
            List<DBArtifactsBundle> bundles = artifactsBundleDao.getAllArtifactsBundles(bundleType);
            bundlesResponse = populateBundlesResponseAndFilterByPermissions(bundles);
        } catch (SQLException e) {
            log.error(FETCH_BUNDLES_ERR, e);
        }
        return bundlesResponse;
    }

    private BundlesResponse populateBundlesResponseAndFilterByPermissions(List<DBArtifactsBundle> artifactsBundles) {
        BundlesResponse bundlesResponse = new BundlesResponse();
        artifactsBundles.stream()
                .filter(bundle ->
                        authorizationService.canRead(
                                getBundleRepoPath(bundle.getStoringRepo(), bundle.getName(), bundle.getVersion())))
                .forEach(bundle -> {
                    BundleVersion bundleVersion = populateBundleVersion(bundle);
                    bundlesResponse.add(bundle.getName(), bundleVersion);
                });
        return bundlesResponse;
    }

    private RepoPath getBundleRepoPath(String repo, String name, String version) {
        return RepoPathFactory.create(repo, name + "/" + version);
    }
}
