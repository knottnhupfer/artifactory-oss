package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes.fetch;

import com.google.common.collect.Iterables;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.api.rest.common.model.continues.ContinueResult;
import org.artifactory.api.rest.common.model.continues.FetchFunction;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes.TreeFilter;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes.repo.RepositoryNode;
import org.artifactory.util.CollectionUtils;
import org.glassfish.jersey.internal.guava.Lists;
import org.jfrog.common.StreamSupportUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Omri Ziv
 */
public class RootFetchByPackageType extends RootFetchStrategy {

    private static final Logger log = LoggerFactory.getLogger(RootFetchByPackageType.class);

    public RootFetchByPackageType(TreeFilter treeFilter) {
        super(treeFilter);
    }

    @Override
    protected List<FetchFunction<RepositoryNode>> getFetchFunctions() {
        List<FetchFunction<RepositoryNode>> superFunctions = super.getFetchFunctions();
        List<FetchFunction<RepositoryNode>> functionByPackageTypes =
                StreamSupportUtils.mapEntriesStream(repositoryService.getRepoDescriptorByPackageTypeCount())
                .filter(entry -> CollectionUtils.isNullOrEmpty(treeFilter.getPackageTypes()) ||
                        treeFilter.getPackageTypes().contains(entry.getKey()))
                .map(entry -> byType(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        List<FetchFunction<RepositoryNode>> allFunctions = Lists.newArrayList(Iterables.concat(superFunctions, functionByPackageTypes));
        log.debug("{} Candidates Fetch Function", allFunctions.size());
        return allFunctions;
    }

    private FetchFunction<RepositoryNode> byType(RepoType packageType, int size) {
        return new FetchFunction<>((skip, limit) -> {
            List<RepoDescriptor> sortedRepoDescriptors = repositoryService.getRepoDescriptorByPackageType(packageType);
            List<RepoDescriptor> repoDescriptorList = StreamSupportUtils.stream(sortedRepoDescriptors)
                    .skip(skip)
                    .filter(this::accept)
                    .limit(limit)
                    .collect(Collectors.toList());
            int lastIndex = getLastIndex(sortedRepoDescriptors, repoDescriptorList, limit);
             return new ContinueResult<>(lastIndex, getNodes(repoDescriptorList));
        }, (long) size);
    }


}
