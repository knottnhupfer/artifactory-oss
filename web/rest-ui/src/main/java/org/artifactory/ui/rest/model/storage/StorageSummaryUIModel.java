package org.artifactory.ui.rest.model.storage;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.artifactory.rest.common.model.BaseModel;
import org.artifactory.storage.*;

import java.util.List;

/**
 * @author Inbar Tal
 */
@Getter
@NoArgsConstructor
public class StorageSummaryUIModel extends BaseModel implements StorageSummary {

    private StorageSummaryImpl storageSummary;
    private long lastUpdatedCache;

    public StorageSummaryUIModel(StorageSummaryInfo storageSummaryInfo) {
        this.storageSummary = new StorageSummaryImpl(storageSummaryInfo);
        this.lastUpdatedCache = storageSummaryInfo.getLastUpdated();
    }

    @Override
    public BinariesSummary getBinariesSummary() {
        return storageSummary.getBinariesSummary();
    }

    @Override
    public void setBinariesSummary(BinariesSummary binariesSummary) {
        storageSummary.setBinariesSummary(binariesSummary);
    }

    @Override
    public FileStoreSummary getFileStoreSummary() {
        return storageSummary.getFileStoreSummary();
    }

    @Override
    public void setFileStoreSummary(FileStoreSummary fileStoreSummary) {
        storageSummary.setFileStoreSummary(fileStoreSummary);
    }

    @Override
    public List<RepositorySummary> getRepositoriesSummaryList() {
        return storageSummary.getRepositoriesSummaryList();
    }

    @Override
    public void setRepositoriesSummaryList(List<RepositorySummary> repositoriesSummaryList) {
        storageSummary.setRepositoriesSummaryList(repositoriesSummaryList);
    }
}
