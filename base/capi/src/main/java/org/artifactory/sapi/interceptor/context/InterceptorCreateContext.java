package org.artifactory.sapi.interceptor.context;

import org.artifactory.repo.RepoPath;

/**
 * A context to pass when performing item create.
 * Used to set alternate item path in case item was moved during the interceptor logic.
 *
 * @author Yuval Reches
 */
public class InterceptorCreateContext {

    private RepoPath alternateRepoPath;

    public InterceptorCreateContext() {
        // empty ctor
    }

    public RepoPath getAlternateRepoPath() {
        return alternateRepoPath;
    }

    public void setAlternateRepoPath(RepoPath alternateRepoPath) {
        this.alternateRepoPath = alternateRepoPath;
    }
}
