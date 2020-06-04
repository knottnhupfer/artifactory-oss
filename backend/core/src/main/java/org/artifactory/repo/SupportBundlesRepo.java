package org.artifactory.repo;

import org.artifactory.descriptor.repo.SupportBundleRepoDescriptor;
import org.artifactory.repo.db.DbLocalRepo;
import org.artifactory.repo.service.InternalRepositoryService;

/**
 * @author Tamir Hadad
 */
public class SupportBundlesRepo extends DbLocalRepo<SupportBundleRepoDescriptor> {

    public SupportBundlesRepo(SupportBundleRepoDescriptor descriptor,
            InternalRepositoryService repositoryService,
            DbLocalRepo<SupportBundleRepoDescriptor> oldLocalRepo) {
        super(descriptor, repositoryService, oldLocalRepo);
    }
}
