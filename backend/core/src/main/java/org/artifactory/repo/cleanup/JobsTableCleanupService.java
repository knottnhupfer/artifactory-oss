package org.artifactory.repo.cleanup;

import org.artifactory.spring.ReloadableBean;

/**
 * @author Yoaz Menda
 */
public interface JobsTableCleanupService extends ReloadableBean {
    void clean();
}
