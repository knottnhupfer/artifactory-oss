package org.artifactory.ui.rest.model.continuous.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes.RepositoryType;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes.TreeFilter;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContinueTreeDto extends ContinuePageDto {

    List<RepoType> packageTypes;
    List<RepositoryType> repositoryTypes;
    List<String> repositoryKeys;
    TreeFilter.SortBy sortBy = TreeFilter.SortBy.REPO_TYPE;
    String byRepoKey;

}
