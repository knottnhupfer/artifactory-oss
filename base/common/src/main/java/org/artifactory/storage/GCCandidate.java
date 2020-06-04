package org.artifactory.storage;

import lombok.Value;
import org.artifactory.repo.RepoPath;

/**
 * @author dudim
 */
@Value
public class GCCandidate {
    private RepoPath repoPath;
    private final String sha1;
    private final String sha2;
    private final String md5;
    private final long length;
}
