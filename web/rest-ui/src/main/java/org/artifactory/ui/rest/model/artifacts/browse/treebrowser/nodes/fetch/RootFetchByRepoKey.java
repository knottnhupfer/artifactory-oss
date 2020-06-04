package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes.fetch;

import com.google.common.collect.Iterables;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.api.rest.common.model.continues.ContinueResult;
import org.artifactory.api.rest.common.model.continues.FetchFunction;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes.TreeFilter;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes.repo.RepositoryNode;
import org.glassfish.jersey.internal.guava.Lists;
import org.jfrog.common.StreamSupportUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Omri Ziv
 */
public class RootFetchByRepoKey extends RootFetchStrategy {

    private static final Logger log = LoggerFactory.getLogger(RootFetchByRepoKey.class);

    public RootFetchByRepoKey(TreeFilter treeFilter) {
        super(treeFilter);
    }

    @Override
    protected List<FetchFunction<RepositoryNode>> getFetchFunctions() {
        List<FetchFunction<RepositoryNode>> superFunctions = super.getFetchFunctions();

        List<FetchFunction<RepositoryNode>> functionsByFirstRepoChar =
                StreamSupportUtils.stream(repositoryService.getAllRepoKeysByFirstCharMap().keySet())
                .map(this::getFetchFunctionByFirstRepoChar)
                .collect(Collectors.toList());
        List<FetchFunction<RepositoryNode>> allFunctions = Lists.newArrayList(Iterables.concat(superFunctions, functionsByFirstRepoChar));
        log.debug("{} Candidates Fetch Function", allFunctions.size());
        return allFunctions;

    }

    private FetchFunction<RepositoryNode> getFetchFunctionByFirstRepoChar(Character c) {

        List<String> sortedRepoByChar = StreamSupportUtils.stream(repositoryService.getAllRepoKeysByFirstCharMap().get(c))
                .sorted().collect(Collectors.toList());

        return new FetchFunction<>((skip, limit) -> {
            List<RepoDescriptor> repoDescriptors = StreamSupportUtils.stream(sortedRepoByChar)
                    .skip(skip)
                    .map(repositoryService::repoDescriptorByKey)
                    .filter(this::accept)
                    .limit(limit)
                    .collect(Collectors.toList());
            int lastIndex = getLastIndexOfKeys(sortedRepoByChar, repoDescriptors, limit);

            return new ContinueResult<>(lastIndex, getNodes(repoDescriptors));
        }, (long) sortedRepoByChar.size());
    }
}
