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

package org.artifactory.aql.result.rows.populate;

import org.artifactory.aql.AqlException;
import org.artifactory.aql.model.AqlRepoProvider;
import org.artifactory.aql.model.DomainSensitiveField;
import org.artifactory.aql.result.rows.RowResult;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;

import java.util.List;
import java.util.stream.Collectors;

import static org.artifactory.aql.model.AqlPhysicalFieldEnum.*;

/**
 * @author Yinon Avraham
 */
public class ItemVirtualReposPopulator implements FieldResultPopulator {

    @Override
    public void populate(RowPopulationContext populationContext, DomainSensitiveField field) {
        RowResult row = populationContext.getRow();
        String repo = (String) row.get(new DomainSensitiveField(itemRepo, field.getSubDomains()));
        String path = (String) row.get(new DomainSensitiveField(itemPath, field.getSubDomains()));
        String name = (String) row.get(new DomainSensitiveField(itemName, field.getSubDomains()));
        if (repo != null && path != null && name != null) {
            RepoPath repoPath = RepoPathFactory.create(repo, path + "/" + name);
            String[] virtualRepoKeys = getVirtualRepoKeysForRepoPath(populationContext, repoPath);
            row.put(field, virtualRepoKeys);
        } else if (repo != null || path != null || name != null) { // at least one is present - this row is an item
            throw new AqlException("Unhandled field; item repo, path & name fields are required: " + field);
        }
    }

    private String[] getVirtualRepoKeysForRepoPath(RowPopulationContext populationContext, RepoPath repoPath) {
        String repoKey = repoPath.getRepoKey();
        AqlRepoProvider repoProvider = populationContext.getRepoProvider();
        List<String> virtualRepoKeys = populationContext.getVirtualRepoKeysContainingRepo(repoKey);
        List<String> filteredVirtualRepoKeys = virtualRepoKeys.stream()
                .map(virtualRepoKey -> RepoPathFactory.create(virtualRepoKey, repoPath.getPath()))
                .filter(repoProvider::isRepoPathAccepted)
                .map(RepoPath::getRepoKey)
                .sorted() // Return a predictable order
                .collect(Collectors.toList());
        return filteredVirtualRepoKeys.toArray(new String[filteredVirtualRepoKeys.size()]);
    }
}
