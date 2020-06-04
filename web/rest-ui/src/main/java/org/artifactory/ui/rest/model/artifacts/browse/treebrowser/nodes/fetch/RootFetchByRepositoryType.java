package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes.fetch;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import org.artifactory.api.rest.common.model.continues.ContinueResult;
import org.artifactory.descriptor.repo.LocalCacheRepoDescriptor;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.descriptor.repo.VirtualRepoDescriptor;
import org.artifactory.descriptor.repo.distribution.DistributionRepoDescriptor;
import org.artifactory.api.rest.common.model.continues.FetchFunction;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes.RepositoryType;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes.TreeFilter;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes.repo.RepositoryNode;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes.repo.VirtualRemoteRepositoryNode;
import org.artifactory.util.CollectionUtils;
import org.glassfish.jersey.internal.guava.Lists;
import org.jfrog.common.StreamSupportUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.artifactory.repo.RepoDetailsType.*;
import static org.artifactory.repo.RepoPath.REMOTE_CACHE_SUFFIX;
import static org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes.RepositoryType.CACHED;

/**
 * @author Omri Ziv
 */
public class RootFetchByRepositoryType extends RootFetchStrategy {

    private static final Logger log = LoggerFactory.getLogger(RootFetchByRepositoryType.class);

    private Collection<RepositoryType> repoOrder;
    private Map<RepositoryType, FetchFunction<RepositoryNode>> repoDetailsTypeFetchFunctionMap = ImmutableMap.of
            (RepositoryType.DISTRIBUTION, new FetchFunction<>(this::getDistributionRepoNodes,
                    (long) repositoryService.getDistributionRepoDescriptors().size()),
                    RepositoryType.LOCAL, new FetchFunction<>(this::getLocalRepoNodes,
                    (long) repositoryService.getLocalRepoDescriptorsIncludingBuildInfo().size()),
                    CACHED, new FetchFunction<>(this::getCachedRepoNodes,
                    (long) repositoryService.getCachedRepoDescriptors().size()),
                    RepositoryType.REMOTE, new FetchFunction<>(this::getRemoteRepoNodes,
                    (long) repositoryService.getRemoteRepoDescriptors().size()),
                    RepositoryType.VIRTUAL, new FetchFunction<>(this::getVirtualRepoNodes,
                    (long) repositoryService.getVirtualRepoDescriptors().size()));

    public RootFetchByRepositoryType(TreeFilter treeFilter, Collection<RepositoryType> repoOrder) {
        super(treeFilter);
        this.repoOrder = repoOrder;
    }

    @Override
    protected List<FetchFunction<RepositoryNode>> getFetchFunctions() {

        List<FetchFunction<RepositoryNode>> superFunctions = super.getFetchFunctions();
        List<RepositoryType> repositoryTypes = treeFilter.getRepositoryTypes();
        boolean emptyRepos = CollectionUtils.isNullOrEmpty(repositoryTypes);
        List<FetchFunction<RepositoryNode>> fetchFunctions = StreamSupportUtils.stream(this.repoOrder)
                .filter(repoType -> emptyRepos || repositoryTypes.contains(repoType))
                .map(repoDetailsTypeFetchFunctionMap::get)
                .collect(Collectors.toList());

        List<FetchFunction<RepositoryNode>> allFunctions = Lists.newArrayList(Iterables.concat(superFunctions, fetchFunctions));
        log.debug("{} Candidates Fetch Function", allFunctions.size());
        return allFunctions;

    }

    private ContinueResult<RepositoryNode> getDistributionRepoNodes(Integer skip, Integer limit) {
        List<DistributionRepoDescriptor> originalSortedDistributionDescriptors =
                sortAndGet(repositoryService.getDistributionRepoDescriptors());

        List<DistributionRepoDescriptor> distributionDescriptors =
                StreamSupportUtils.stream(originalSortedDistributionDescriptors)
                        .skip(skip)
                        .filter(this::accept)
                        .limit(limit)
                        .collect(Collectors.toList());

        int lastIndex = getLastIndex(originalSortedDistributionDescriptors, distributionDescriptors, limit);
        return new ContinueResult<>(lastIndex, getDistributionNodes(distributionDescriptors));
    }

    private ContinueResult<RepositoryNode> getLocalRepoNodes(Integer skip, Integer limit) {
        List<LocalRepoDescriptor> originalSortedLocalRepos =
                sortAndGet(repositoryService.getLocalRepoDescriptorsIncludingBuildInfo());

        List<LocalRepoDescriptor> localRepoDescriptors =
                StreamSupportUtils.stream(originalSortedLocalRepos)
                        .skip(skip)
                        .filter(this::accept)
                        .limit(limit)
                        .collect(Collectors.toList());
        int lastIndex = getLastIndex(originalSortedLocalRepos, localRepoDescriptors, limit);
        return new ContinueResult<>(lastIndex, getLocalNodes(localRepoDescriptors));
    }

    private ContinueResult<RepositoryNode> getCachedRepoNodes(Integer skip, Integer limit) {
        List<LocalCacheRepoDescriptor> originalSortedLocalRepos =
                sortAndGet(repositoryService.getCachedRepoDescriptors());

        List<LocalCacheRepoDescriptor> localRCachedepoDescriptors =
                StreamSupportUtils.stream(originalSortedLocalRepos)
                        .skip(skip)
                        .filter(this::accept)
                        .limit(limit)
                        .collect(Collectors.toList());
        int lastIndex = getLastIndex(originalSortedLocalRepos, localRCachedepoDescriptors, limit);
        return new ContinueResult<>(lastIndex, getLocalNodes(localRCachedepoDescriptors));
    }

    private ContinueResult<RepositoryNode> getRemoteRepoNodes(Integer skip, Integer limit) {
        List<RemoteRepoDescriptor> originalSortedRemoteDescriptors =
                sortAndGet(repositoryService.getRemoteRepoDescriptors());

        List<RemoteRepoDescriptor> remoteRepoDescriptors =
                StreamSupportUtils.stream(originalSortedRemoteDescriptors)
                        .skip(skip)
                        .filter(this::accept)
                        .limit(limit)
                        .collect(Collectors.toList());
        int lastIndex = getLastIndex(originalSortedRemoteDescriptors, remoteRepoDescriptors, limit);
        return new ContinueResult<>(lastIndex, getRemoteNodes(remoteRepoDescriptors));
    }

    /**
     * add virtual repo nodes to repo list
     */
    private ContinueResult<RepositoryNode> getVirtualRepoNodes(Integer skip, Integer limit) {
        List<VirtualRepoDescriptor> originalSortedVirtualDescriptors =
                sortAndGet(repositoryService.getVirtualRepoDescriptors());

        List<VirtualRepoDescriptor> virtualDescriptors =
                StreamSupportUtils.stream(originalSortedVirtualDescriptors)
                        .skip(skip)
                        .filter(this::accept)
                        .limit(limit)
                        .collect(Collectors.toList());
        int lastIndex = getLastIndex(originalSortedVirtualDescriptors, virtualDescriptors, limit);
        return new ContinueResult<>(lastIndex, getVirtualNodes(virtualDescriptors));
    }


    private List<RepositoryNode> getLocalNodes(List<? extends LocalRepoDescriptor> repos) {
        return StreamSupportUtils.stream(repos)
                .map(repo -> {
                    String repoType =
                            repo.getKey().endsWith(REMOTE_CACHE_SUFFIX) ? CACHED.getTypeNameLoweCase() : LOCAL.typeNameLowercase();
                    return new RepositoryNode(repo.getKey(), repo.getType(), repoType);
                })
                .collect(Collectors.toList());
    }

    private List<RepositoryNode> getRemoteNodes(List<RemoteRepoDescriptor> repos) {
        return StreamSupportUtils.stream(repos)
                .map(repo -> new VirtualRemoteRepositoryNode(repo.getKey(), repo.getType(),
                        REMOTE.typeNameLowercase(), false))
                .collect(Collectors.toList());
    }

    private List<RepositoryNode> getVirtualNodes(List<VirtualRepoDescriptor> repos) {
        return StreamSupportUtils.stream(repos)
                .map(repo -> new VirtualRemoteRepositoryNode(repo.getKey(), repo.getType(),
                        VIRTUAL.typeNameLowercase(), repo.getDefaultDeploymentRepo() != null))
                .collect(Collectors.toList());
    }

    private List<RepositoryNode> getDistributionNodes(List<DistributionRepoDescriptor> repos) {
        return StreamSupportUtils.stream(repos)
                .map(repo -> new RepositoryNode(repo.getKey(), repo.getType(), DISTRIBUTION.typeNameLowercase()))
                .collect(Collectors.toList());
    }
}
