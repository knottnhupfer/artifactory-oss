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

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.rest.common.model.continues.ContinueResult;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes.fetch.RootFetchByPackageType;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes.fetch.RootFetchByRepoKey;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes.fetch.RootFetchByRepositoryType;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes.fetch.RootFetchStrategy;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes.repo.RepositoryNode;
import org.artifactory.ui.rest.model.continuous.dtos.ContinueTreeDto;
import org.artifactory.ui.rest.model.continuous.translators.ContinueTreeTranslator;
import org.artifactory.util.CollectionUtils;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.annotate.JsonUnwrapped;
import org.jfrog.common.StreamSupportUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the root node of the tree browser. It contains all the repository nodes.
 *
 * @author Chen Keinan
 * @author Omri Ziv
 */
@JsonTypeName("root")
class RootNode implements RestTreeNode {

    @JsonUnwrapped
    private ContinueTreeDto continueTreeDto;

    @JsonIgnore
    private static final Logger log = LoggerFactory.getLogger(RootNode.class);

    @Override
    public ContinueResult<? extends RestModel> fetchItemTypeData(boolean isCompact) {
        //Add a tree node for each file repository and local cache repository
        TreeFilter treeFilter = ContinueTreeTranslator.toContinueTreeFilter(continueTreeDto);
        RootFetchStrategy rootFetchStrategy = chooseStrategy(treeFilter);
        printFilter();
        return fetch(rootFetchStrategy);
    }

    private void printFilter() {
        if (!log.isDebugEnabled()) {
            return;
        }
        if (CollectionUtils.notNullOrEmpty(continueTreeDto.getRepositoryTypes())) {
            log.debug("Fetching repos only of types {}", continueTreeDto.getRepositoryTypes());
        }
        if (CollectionUtils.notNullOrEmpty(continueTreeDto.getRepositoryKeys())) {
            log.debug("Fetching only favorite repos {}", continueTreeDto.getRepositoryKeys());
        }
        if (CollectionUtils.notNullOrEmpty(continueTreeDto.getPackageTypes())) {
            log.debug("Fetching repos only of package types {}", continueTreeDto.getPackageTypes());
        }
        if (StringUtils.isNotBlank(continueTreeDto.getByRepoKey())) {
            log.debug("Fetching repos only matches {}", continueTreeDto.getByRepoKey());
        }
    }

    @Override
    public boolean isArchiveExpendRequest() {
        return false;
    }

    private ContinueResult<RepositoryNode> fetch(RootFetchStrategy rootFetchStrategy) {
        ContinueResult<RepositoryNode> result = new ContinueResult<>();
        boolean needToFetch = true;
        while(needToFetch) {
            result.merge(rootFetchStrategy.fetchItems());
            needToFetch = !isMustIncludeFulfilled(result);
        }
        log.debug("{} repos fetched with continue state '{}'", result.getData().size(), result.getContinueState());
        return result;
    }

    private boolean isMustIncludeFulfilled(ContinueResult<RepositoryNode> continueResult) {
        if (StringUtils.isNotBlank(continueTreeDto.getMustInclude()) && continueResult.getContinueState() != null) {
            return StreamSupportUtils.stream(continueResult.getData())
                    .anyMatch(repositoryNode -> repositoryNode.getText().equals(continueTreeDto.getMustInclude()));
        }
        return true;
    }


    private RootFetchStrategy chooseStrategy(TreeFilter treeFilter) {
        TreeFilter.SortBy sortBy = treeFilter.getSortBy();
        RootFetchStrategy rootFetchStrategy;
        switch(sortBy) {
            case PACKAGE_TYPE:
                rootFetchStrategy = new RootFetchByPackageType(treeFilter);
                log.debug("Fetching repos order by package type.");
                break;
            case REPO_KEY:
                rootFetchStrategy = new RootFetchByRepoKey(treeFilter);
                log.debug("Fetching repos order by repo key.");
                break;
            case REPO_TYPE:
            default:
                rootFetchStrategy = new RootFetchByRepositoryType(treeFilter, RestTreeNode.getRepoOrder());
                log.debug("Fetching repos order by repo type.");
                break;
        }
        return rootFetchStrategy;
    }

    public ContinueTreeDto getContinueTreeDto() {
        return continueTreeDto;
    }

    public void setContinueTreeDto(ContinueTreeDto continueTreeDto) {
        this.continueTreeDto = continueTreeDto;
    }
}