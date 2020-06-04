package org.artifactory.repo.cleanup;

import org.artifactory.common.ConstantValues;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.repo.virtual.VirtualRepo;
import org.artifactory.sapi.search.*;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

@Component
public class PomVirtualCacheCleaner implements VirtualCacheCleaner {
    private static final Logger log = LoggerFactory.getLogger(PomVirtualCacheCleaner.class);

    private VfsQueryService vfsQueryService;

    private InternalRepositoryService repositoryService;

    @Autowired
    public PomVirtualCacheCleaner(VfsQueryService vfsQueryService, InternalRepositoryService repositoryService) {
        this.vfsQueryService = vfsQueryService;
        this.repositoryService = repositoryService;
    }

    @Override
    public long cleanCache(@Nonnull VirtualRepo virtualRepo) {
        int maxAgeMinutes = ConstantValues.virtualCleanupMaxAgeHours
                .getInt(); // old bug - value is used as minutes instead of hours
        if (maxAgeMinutes < 0) {
            log.debug("Cleanup of virtual caches is disabled");
            return 0L;
        }
        long expiryTime = new DateTime().minusMinutes(maxAgeMinutes).getMillis();
        VfsQuery query = vfsQueryService.createQuery().expectedResult(VfsQueryResultType.FILE)
                .name(ConstantValues.virtualCleanupNamePattern.getString()).comp(VfsComparatorType.CONTAINS)
                .setSingleRepoKey(virtualRepo.getKey())
                .prop("created")// old bug - should be modified instead of created
                .comp(VfsComparatorType.LOWER_THAN_EQUAL)
                .val(expiryTime);
        VfsQueryResult queryResult = query.execute(Integer.MAX_VALUE);
        if (queryResult.getCount() > 0) {
            log.debug("Found {} cached files in {}", queryResult.getCount(), virtualRepo.getKey());
        }
        for (VfsQueryRow result : queryResult.getAllRows()) {
            log.trace("Undeploying old cached file {}", result.getItem().getName());
            repositoryService.undeploy(result.getItem().getRepoPath());
        }
        return queryResult.getCount();
    }

    @Override
    public boolean shouldRun(VirtualRepo virtualRepo) {
        return true; // old bug - should run only for virtual repos with pom files in cache (gradle, ivy, maven)
    }
}
