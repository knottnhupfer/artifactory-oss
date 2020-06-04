package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.artifactory.api.rest.common.model.continues.ContinueIndexPage;
import org.artifactory.descriptor.repo.RepoType;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.List;


/**
 * @author Omri Ziv
 */
@Data
@AllArgsConstructor
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class TreeFilter extends ContinueIndexPage {

    List<RepoType> packageTypes;
    List<RepositoryType> repositoryTypes;
    List<String> repositoryKeys;
    SortBy sortBy = SortBy.REPO_TYPE;
    String byRepoKey;

    public TreeFilter() {
        super();
    }

    public TreeFilter(TreeFilter orig) {
        super(orig);
        this.packageTypes = orig.packageTypes;
        this.repositoryTypes = orig.repositoryTypes;
        this.repositoryKeys = orig.repositoryKeys;
        this.sortBy = orig.sortBy;
        this.byRepoKey = orig.byRepoKey;
    }

    public TreeFilter(ContinueIndexPage orig) {
        super(orig);
    }

    public enum SortBy {
        REPO_TYPE, PACKAGE_TYPE, REPO_KEY
    }

}
