package org.artifactory.ui.rest.model.continuous.translators;

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.rest.common.model.continues.ContinueIndexPage;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes.TreeFilter;
import org.artifactory.ui.rest.model.continuous.dtos.ContinueTreeDto;
import org.artifactory.util.CollectionUtils;

public class ContinueTreeTranslator {


    public static TreeFilter toContinueTreeFilter(ContinueTreeDto continueTreeDto) {
        ContinueIndexPage continueIndexPage = ContinuePageTranslator.toContinueIndex(continueTreeDto);
        TreeFilter treeFilter = new TreeFilter(continueIndexPage);
        if (StringUtils.isNotBlank(continueTreeDto.getByRepoKey())) {
            treeFilter.setByRepoKey(continueTreeDto.getByRepoKey());
        }
        if (CollectionUtils.notNullOrEmpty(continueTreeDto.getPackageTypes())) {
            treeFilter.setPackageTypes(continueTreeDto.getPackageTypes());
        }
        if (CollectionUtils.notNullOrEmpty(continueTreeDto.getRepositoryKeys())) {
            treeFilter.setRepositoryKeys(continueTreeDto.getRepositoryKeys());
        }
        if (CollectionUtils.notNullOrEmpty(continueTreeDto.getRepositoryTypes())) {
            treeFilter.setRepositoryTypes(continueTreeDto.getRepositoryTypes());
        }
        if (continueTreeDto.getSortBy() != null) {
            treeFilter.setSortBy(continueTreeDto.getSortBy());
        }
        return treeFilter;
    }

}
