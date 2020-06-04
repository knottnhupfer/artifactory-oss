package org.artifactory.storage.db.build.service;

import org.artifactory.build.BuildRun;
import org.artifactory.storage.build.service.BuildStoreService;
import org.artifactory.storage.db.build.entity.BuildEntity;

/**
 * @author Dan Feldman
 */
public interface InternalBuildStoreService extends BuildStoreService {

    BuildEntity getBuildEntity(BuildRun buildRun);
}
