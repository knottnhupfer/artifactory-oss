package org.artifactory.resource;

import org.artifactory.fs.RepoResource;
import org.artifactory.repo.RepoPath;

public class BlockedRepoResource  implements RepoResource {

    private final RepoPath repoPath;
    private final String message;

    public BlockedRepoResource(RepoPath repoPath) {
        this.repoPath = repoPath;
        this.message = "Resource is blocked";
    }

    public BlockedRepoResource(RepoPath repoPath, String message) {
        this.repoPath = repoPath;
        this.message = message;
    }

    @Override
    public RepoPath getRepoPath() {
        return repoPath;
    }

    @Override
    public RepoPath getResponseRepoPath() {
        return null;
    }

    @Override
    public void setResponseRepoPath(RepoPath responsePath) {
    }

    @Override
    public RepoResourceInfo getInfo() {
        return null;
    }

    @Override
    public boolean isFound() {
        return true;
    }

    @Override
    public boolean isBlocked() {
        return true;
    }

    @Override
    public boolean isExactQueryMatch() {
        return false;
    }

    @Override
    public boolean isExpired() {
        return false;
    }

    @Override
    public boolean isMetadata() {
        return false;
    }

    @Override
    public long getSize() {
        return 0;
    }

    @Override
    public long getCacheAge() {
        return 0;
    }

    @Override
    public long getLastModified() {
        return 0;
    }

    @Override
    public String getEtag() {
        return null;
    }

    @Override
    public String getMimeType() {
        return null;
    }

    @Override
    public boolean isExpirable() {
        return false;
    }

    @Override
    public void expirable() {
    }

    public String getMessage() {
        return message;
    }

}
