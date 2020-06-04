package org.artifactory.api.repo;

import org.artifactory.descriptor.repo.HttpRepoDescriptor;

/**
 * Service used to discover capabilities of another artifactory
 *
 * @author michaelp
 */
public interface ResearchService {

    /**
     * Smart repo means:
     *  - Target is an Artifactory instance
     *  - It answered properly on its version api endpoint
     *  - It's license is valid (pro, ha, ent, aol
     */
    boolean isSmartRemote(HttpRepoDescriptor repoDescriptor);

    boolean isRepoConfiguredToSyncProperties(HttpRepoDescriptor repoDescriptor);

}
