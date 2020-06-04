/*
 *
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2018 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.artifactory.rest.common.model.storagesummary;

import org.artifactory.rest.common.model.BaseModel;
import org.artifactory.storage.*;

import java.util.List;

/**
 * @author Chen Keinan
 */
public class StorageSummaryModel extends BaseModel implements StorageSummary {

    StorageSummaryImpl storageSummary;

    /**
     * serialization .ctr
     */
    public StorageSummaryModel() {
    }

    public StorageSummaryModel(StorageSummaryInfo storageSummaryInfo) {
        storageSummary = new StorageSummaryImpl(storageSummaryInfo);
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

    public StorageSummaryImpl getStorageSummary() {
        return storageSummary;
    }
}
