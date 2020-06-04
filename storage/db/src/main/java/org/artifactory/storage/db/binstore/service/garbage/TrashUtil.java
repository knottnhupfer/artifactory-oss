package org.artifactory.storage.db.binstore.service.garbage;

import org.artifactory.storage.GCCandidate;

import static org.artifactory.repo.trash.TrashService.TRASH_KEY;

public class TrashUtil {

    public static boolean isTrashItem(GCCandidate gcCandidate) {
        return gcCandidate.getRepoPath() != null && TRASH_KEY.equals(gcCandidate.getRepoPath().getRepoKey());
    }
}
