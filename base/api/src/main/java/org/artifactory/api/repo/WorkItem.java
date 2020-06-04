package org.artifactory.api.repo;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author dudim
 */
public abstract class WorkItem {

    private List<WorkItem> identicalWorkItems = new ArrayList<>();

    /**
     * This key will be used by the locking map that synchronizes all work items
     * it **MUST** match the equals() and hashcode() relations of this work item.
     */
    @Nonnull
    public abstract String getUniqueKey();

    public List<WorkItem> getIdenticalWorkItems() {
        return identicalWorkItems;
    }

    public void addIdenticalWorkItems(List<? extends WorkItem> combinedContext) {
        identicalWorkItems.addAll(combinedContext);
    }

    public abstract boolean equals(Object other);

    public abstract int hashCode();
}
