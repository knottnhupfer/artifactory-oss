package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes.fetch;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.CoreAddons;
import org.artifactory.addon.support.SupportAddon;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.rest.common.model.continues.ContinueResult;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.descriptor.repo.SupportBundleRepoDescriptor;
import org.artifactory.descriptor.repo.releasebundle.ReleaseBundlesRepoDescriptor;
import org.artifactory.repo.trash.TrashService;
import org.artifactory.security.ArtifactoryPermission;
import org.artifactory.api.rest.common.model.continues.FetchFunction;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes.RepositoryType;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes.TreeFilter;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes.repo.RepositoryNode;
import org.artifactory.api.rest.common.model.continues.util.PagingUtils;
import org.artifactory.util.CollectionUtils;
import org.jfrog.common.StreamSupportUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.artifactory.repo.RepoPath.REMOTE_CACHE_SUFFIX;

/**
 * @author Omri Ziv
 */
public abstract class RootFetchStrategy {

    TreeFilter treeFilter;
    RepositoryService repositoryService;
    private AuthorizationService authService;
    private String nextContinueState;
    private static final Logger log = LoggerFactory.getLogger(RootFetchStrategy.class);

    RootFetchStrategy(TreeFilter treeFilter) {
        this.treeFilter = treeFilter;
        ArtifactoryContext artifactoryContext = ContextHelper.get();
        this.repositoryService = artifactoryContext.getRepositoryService();
        this.authService = artifactoryContext.getAuthorizationService();
    }

    public ContinueResult<RepositoryNode> fetchItems() {
        log.debug("Fetching repository items");
        TreeFilter nextTreeFilter = new TreeFilter(treeFilter);
        if (StringUtils.isNotBlank(nextContinueState)) {
            nextTreeFilter.setContinueIndex(Long.parseLong(nextContinueState));
        }
        ContinueResult<RepositoryNode> result =
                PagingUtils.getPagingFromMultipleFunctions(nextTreeFilter, getFetchFunctions());
        log.debug("{} repositories fetched. continue state was set to: '{}'",
                result.getData().size(), result.getContinueState());
        nextContinueState = result.getContinueState();
        return result;
    }

    protected List<FetchFunction<RepositoryNode>> getFetchFunctions() {
        return new ArrayList<>(Arrays.asList(new FetchFunction<>(this::getReleaseBundlesRepoNodes,
                        (long) repositoryService.getReleaseBundlesRepoDescriptors().size()),
                new FetchFunction<>(this::getTrashRepo),
                new FetchFunction<>(this::getSupportBundles)));
    }

    static <T extends RepoDescriptor> int getLastIndexOfKeys(List<String> originalRepoKeys,
            List<T> filteredRepoDescriptor,
            int limit) {
        return CollectionUtils.notNullOrEmpty(filteredRepoDescriptor) && filteredRepoDescriptor.size() == limit
                ? originalRepoKeys.indexOf(filteredRepoDescriptor.get(filteredRepoDescriptor.size() - 1).getKey()) + 1
                : originalRepoKeys.size();
    }

    static <T extends RepoDescriptor> int getLastIndex(List<T> originalRepoDecriptors, List<T> filteredRepoDescriptor,
            int limit) {
        return CollectionUtils.notNullOrEmpty(filteredRepoDescriptor) && filteredRepoDescriptor.size() == limit
                ? originalRepoDecriptors.indexOf(filteredRepoDescriptor.get(filteredRepoDescriptor.size() - 1)) + 1
                : originalRepoDecriptors.size();
    }

    static <T extends RepoDescriptor> List<T> sortAndGet(List<T> original) {
        return StreamSupportUtils.stream(original)
                .sorted(new RootFetchStrategy.RepoComparator())
                .collect(Collectors.toList());
    }

    boolean accept(RepoDescriptor repoDescriptor) {
        return matchRepoFromList(repoDescriptor) &&
                matchRepoKey(repoDescriptor) &&
                matchPackageType(repoDescriptor) &&
                matchRepoType(repoDescriptor) &&
                (userCanReadAnywhereOnBuildRepo(authService, repoDescriptor)
                        || authService.userHasPermissionsOnRepositoryRoot(repoDescriptor.getKey()));
    }

    List<RepositoryNode> getNodes(List<RepoDescriptor> repos) {
        List<RepositoryNode> items = Lists.newArrayListWithCapacity(repos.size());
        repos.forEach(repo -> {
            String repoType = repo.getKey().endsWith(REMOTE_CACHE_SUFFIX) ? "cached"
                    : RepositoryType.byDescriptor(repo).getTypeNameLoweCase();
            RepositoryNode itemNodes = new RepositoryNode(repo.getKey(), repo.getType(), repoType);
            items.add(itemNodes);
        });
        return items;
    }

    private ContinueResult<RepositoryNode> getTrashRepo(int skip, int limit) {
        List<RepositoryNode> ans = Lists.newArrayList();
        if (skip == 0 && limit > 0 && authService.isAdmin()) {
            LocalRepoDescriptor trashDescriptor = repositoryService.localRepoDescriptorByKey(TrashService.TRASH_KEY);
            ans.add(new RepositoryNode(trashDescriptor.getKey(), trashDescriptor.getType(), "trash"));
        }
        return new ContinueResult<>(skip > 0 ? "0" : "1", ans);
    }

    private ContinueResult<RepositoryNode> getSupportBundles(int skip, int limit) {
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        boolean isAol = addonsManager.addonByType(CoreAddons.class).isAol();
        boolean isSupportAddonEnabled = addonsManager.addonByType(SupportAddon.class).isSupportAddonEnabled();
        List<RepositoryNode> ans = Lists.newArrayList();
        boolean containsInList = CollectionUtils.isNullOrEmpty(treeFilter.getPackageTypes())
                || treeFilter.getPackageTypes().contains(RepoType.Support);
        if (skip == 0 && limit > 0 && authService.isAdmin()
                && isSupportAddonEnabled && containsInList && !isAol) {
            LocalRepoDescriptor supportBundles = repositoryService
                    .localRepoDescriptorByKey(SupportBundleRepoDescriptor.SUPPORT_BUNDLE_REPO_NAME);
            ans.add(new RepositoryNode(supportBundles.getKey(), supportBundles.getType(), "supportBundles"));
        }
        return new ContinueResult<>(skip > 0 ? "0" : "1", ans);
    }

    private ContinueResult<RepositoryNode> getReleaseBundlesRepoNodes(int skip, int limit) {
        List<ReleaseBundlesRepoDescriptor> originalSorted =
                sortAndGet(repositoryService.getReleaseBundlesRepoDescriptors());

        List<ReleaseBundlesRepoDescriptor> bundlesRepoDescriptors = StreamSupportUtils
                .stream(originalSorted)
                .skip(skip)
                .filter(this::accept) //todo make sure we don't run accept always
                .limit(limit)
                .collect(Collectors.toList());
        int lastIndex = getLastIndex(originalSorted, bundlesRepoDescriptors, limit);
        return new ContinueResult<>(lastIndex, getReleaseBundlesNodes(bundlesRepoDescriptors));
    }

    private boolean matchRepoType(RepoDescriptor repoDescriptor) {
        return CollectionUtils.isNullOrEmpty(treeFilter.getRepositoryTypes()) ||
                treeFilter.getRepositoryTypes().contains(RepositoryType.byDescriptor(repoDescriptor));
    }

    private boolean matchPackageType(RepoDescriptor repoDescriptor) {
        return CollectionUtils.isNullOrEmpty(treeFilter.getPackageTypes()) ||
                treeFilter.getPackageTypes().contains(repoDescriptor.getType());
    }

    private boolean matchRepoKey(RepoDescriptor repoDescriptor) {
        return StringUtils.isBlank(treeFilter.getByRepoKey()) ||
                StringUtils.contains(repoDescriptor.getKey(), treeFilter.getByRepoKey());
    }

    private boolean matchRepoFromList(RepoDescriptor repoDescriptor) {
        return CollectionUtils.isNullOrEmpty(treeFilter.getRepositoryKeys()) ||
                treeFilter.getRepositoryKeys().contains(repoDescriptor.getKey());
    }


    /**
     * Special case for the build info repo, since its improbable for admins to give users permissions on its root.
     * We allow any user that has read anywhere on the repo to see it, given that individual folders and files are
     * still filtered by permissions.
     */
    private boolean userCanReadAnywhereOnBuildRepo(AuthorizationService authService, RepoDescriptor repository) {
        return RepoType.BuildInfo.equals(repository.getType()) &&
                authService.hasBuildPermission(ArtifactoryPermission.READ);
    }

    private List<RepositoryNode> getReleaseBundlesNodes(List<ReleaseBundlesRepoDescriptor> repos) {
        List<RepositoryNode> items = new ArrayList<>(repos.size());
        repos.forEach(repo -> items.add(new RepositoryNode(repo.getKey(), repo.getType(), "releaseBundles")));
        return items;
    }

    static class RepoComparator implements Comparator<RepoDescriptor> {

        @Override
        public int compare(RepoDescriptor descriptor1, RepoDescriptor descriptor2) {

            //Local repositories can be either ordinary or caches
            if (descriptor1 instanceof LocalRepoDescriptor) {
                boolean repo1IsCache = ((LocalRepoDescriptor) descriptor1).isCache();
                boolean repo2IsCache = ((LocalRepoDescriptor) descriptor2).isCache();

                //Cache repositories should appear in a higher priority
                if (repo1IsCache && !repo2IsCache) {
                    return 1;
                } else if (!repo1IsCache && repo2IsCache) {
                    return -1;
                }
            }
            return descriptor1.getKey().compareTo(descriptor2.getKey());
        }
    }

}
