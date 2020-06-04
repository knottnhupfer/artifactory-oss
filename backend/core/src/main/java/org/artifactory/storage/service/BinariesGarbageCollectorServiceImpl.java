package org.artifactory.storage.service;

import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.SecurityService;
import org.artifactory.repo.trash.TrashService;
import org.artifactory.storage.binstore.service.BinariesGarbageCollectorService;
import org.artifactory.storage.binstore.service.GCProviderWrapper;
import org.artifactory.storage.binstore.service.GarbageCollectorStrategy;
import org.artifactory.storage.binstore.service.InternalBinaryService;
import org.artifactory.storage.db.DbService;
import org.artifactory.storage.db.binstore.service.garbage.GarbageCollector;
import org.artifactory.storage.db.binstore.service.garbage.TrashAndBinariesGCProvider;
import org.jfrog.storage.DbType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Uriah Levy
 */
@Service
public class BinariesGarbageCollectorServiceImpl implements BinariesGarbageCollectorService {
    private static final Logger log = LoggerFactory.getLogger(BinariesGarbageCollectorServiceImpl.class);

    private RepositoryService repoService;
    private TrashService trashService;
    private InternalBinaryService binaryService;
    private SecurityService securityService;
    private DbType databaseType;

    @Autowired
    public BinariesGarbageCollectorServiceImpl(RepositoryService repoService, TrashService trashService,
            InternalBinaryService binaryService, SecurityService securityService, DbService dbService) {
        this.repoService = repoService;
        this.trashService = trashService;
        this.binaryService = binaryService;
        this.securityService = securityService;
        databaseType = dbService.getDatabaseType();
    }

    @Override
    public void startGCByStrategy(GarbageCollectorStrategy strategy) {
        GCProviderWrapper gcProvider =
                new GCProviderWrapper(new TrashAndBinariesGCProvider(repoService, trashService, binaryService));
        GarbageCollector garbageCollector = new GarbageCollector(gcProvider, securityService, binaryService, databaseType);
        garbageCollector.run();
    }
}
