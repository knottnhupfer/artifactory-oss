package org.artifactory.ui.rest.model.continuous.translators;

import org.artifactory.api.rest.common.model.continues.ContinueIndexPage;
import org.artifactory.api.security.SearchStringPermissionFilter;
import org.artifactory.ui.rest.model.continuous.dtos.ContinuePermissionDto;

public class SearchStringTranslator {

    private SearchStringTranslator() {

    }

    public static SearchStringPermissionFilter toSearchStringFilter(ContinuePermissionDto continuePermissionDto) {
        ContinueIndexPage continueIndexPage = ContinuePageTranslator.toContinueIndex(continuePermissionDto);
        SearchStringPermissionFilter searchStringPermissionFilter = new SearchStringPermissionFilter(continueIndexPage);
        searchStringPermissionFilter.setSearchStr(continuePermissionDto.getSearchStr());
        return searchStringPermissionFilter;
    }

}
