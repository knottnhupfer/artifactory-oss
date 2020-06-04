package org.artifactory.storage.db.fs.service;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.artifactory.repo.RepoPath;
import org.springframework.context.ApplicationEvent;

import java.util.Map;

/**
 * @author Uriah Levy
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class UpdateArtifactStatsEvent extends ApplicationEvent {

    private Map<RepoPath, Long> pathToDownloadCount;

    UpdateArtifactStatsEvent(Object source, Map<RepoPath, Long> pathToDownloadCount) {
        super(source);
        this.pathToDownloadCount = pathToDownloadCount;
    }
}
