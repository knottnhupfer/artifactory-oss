package org.artifactory.api.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.artifactory.api.rest.common.model.continues.ContinueIndexPage;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchStringPermissionFilter extends ContinueIndexPage {

    String searchStr;

    public SearchStringPermissionFilter(ContinueIndexPage continueIndex) {
        super(continueIndex);
    }

    public SearchStringPermissionFilter(SearchStringPermissionFilter other) {
        super(other);
        this.searchStr = other.searchStr;
    }

}
