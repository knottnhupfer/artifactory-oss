package org.artifactory.storage.jobs.migration.buildinfo;

import org.artifactory.api.repo.WorkItem;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * @author Yuval Reches
 */
public class BuildInfoCalculationWorkItem extends WorkItem {

    public static final String BUILD_INFO_CALCULATION_KEY_PREFIX = "build-info-lock-";
    private final long buildId;
    private final BuildInfoMigrationJobDelegate delegate;

    public BuildInfoCalculationWorkItem(long buildId, BuildInfoMigrationJobDelegate delegate) {
        this.buildId = buildId;
        this.delegate = delegate;
    }

    public long getBuildId() {
        return buildId;
    }

    public BuildInfoMigrationJobDelegate getDelegate() {
        return delegate;
    }

    @Nonnull
    @Override
    public String getUniqueKey() {
        return BUILD_INFO_CALCULATION_KEY_PREFIX + buildId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BuildInfoCalculationWorkItem)) {
            return false;
        }
        BuildInfoCalculationWorkItem workItem = (BuildInfoCalculationWorkItem) o;
        return Objects.equals(getBuildId(), workItem.getBuildId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBuildId());
    }
}
