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

package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes;

import org.artifactory.api.rest.common.model.continues.ContinueResult;
import org.artifactory.common.ConstantValues;
import org.artifactory.rest.common.model.RestModel;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.jfrog.common.StreamSupportUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Chen Keinan
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = JunctionNode.class, name = "junction"),
        @JsonSubTypes.Type(value = RootNode.class, name = "root")})
public interface RestTreeNode extends RestModel {

    ContinueResult<? extends RestModel> fetchItemTypeData(boolean isCompact);
    boolean isArchiveExpendRequest();

    /**
     * Merge the user repo type order with the default order which is DISTRIBUTION,LOCAL,REMOTE,VIRTUAL;
     */
    static Collection<RepositoryType> getRepoOrder() {
        // Load the user order
        String[] userOrder = ConstantValues.orderTreeBrowserRepositoriesByType.getString().toLowerCase().split(",");
        List<String> userOrderList = new ArrayList<>(Arrays.asList(userOrder));
        List<RepositoryType> originalRepoTypes = new ArrayList<>(Arrays.asList(RepositoryType.LOCAL, RepositoryType.REMOTE,
                RepositoryType.CACHED, RepositoryType.DISTRIBUTION, RepositoryType.VIRTUAL));

        // Backward compatible - cached should provide in repo order. by default it will be after remote
        if (userOrderList.contains(RepositoryType.REMOTE.getTypeNameLoweCase()) &&
                !userOrderList.contains(RepositoryType.CACHED.getTypeNameLoweCase())) {
            int indexOfRemote = userOrderList.indexOf(RepositoryType.REMOTE.getTypeNameLoweCase());
            userOrderList.add(indexOfRemote + 1, RepositoryType.CACHED.getTypeNameLoweCase());
        }
        List<RepositoryType> sortedRepoTypes = StreamSupportUtils.stream(userOrderList)
                .map(RepositoryType::byNativeName)
                .collect(Collectors.toList());

        if(sortedRepoTypes.size() < originalRepoTypes.size()) {
            StreamSupportUtils.stream(originalRepoTypes)
                    .forEach(repositoryType -> {
                        if (!sortedRepoTypes.contains(repositoryType)) {
                            sortedRepoTypes.add(repositoryType);
                        }
                    });
        }
        return sortedRepoTypes;
    }

}