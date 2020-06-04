package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes.fetch;

import org.artifactory.api.rest.common.model.continues.ContinueResult;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.api.rest.common.model.continues.FetchFunction;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes.RepositoryType;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes.RestTreeNode;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes.TreeFilter;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes.repo.RepositoryNode;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Omri Ziv
 */
public class RootFetchByRepositoryTypeTest extends RootFetchStrategyTest {

    @Test
    public void getByRepositoryType() {
        createMocks();
        TreeFilter treeFilter = new TreeFilter();
        RootFetchByRepositoryType rootFetchByPackageType = new RootFetchByRepositoryType(treeFilter, RestTreeNode.getRepoOrder());
        List<FetchFunction<RepositoryNode>> fetchFunctions = rootFetchByPackageType.getFetchFunctions();
        assertEquals(8, fetchFunctions.size());
        assertEquals(22L, fetchFunctions.get(4).getOriginalSize().longValue()); //local

        ContinueResult<? extends RestModel> continueResult1 = fetchFunctions.get(4).getFunction().apply(0, 30);
        assertEquals(22, continueResult1.getData().size());
    }

    @Test
    public void getByRepositoryTypeLimited() {
        createMocks();
        TreeFilter treeFilter = new TreeFilter();
        RootFetchByRepositoryType rootFetchByPackageType = new RootFetchByRepositoryType(treeFilter, RestTreeNode.getRepoOrder());
        List<FetchFunction<RepositoryNode>> fetchFunctions = rootFetchByPackageType.getFetchFunctions();
        assertEquals(8, fetchFunctions.size());
        assertEquals(0, fetchFunctions.get(3).getOriginalSize().longValue()); // distribution
        assertEquals(22, fetchFunctions.get(4).getOriginalSize().longValue()); // local
        assertEquals(0, fetchFunctions.get(5).getOriginalSize().longValue()); // remote
        assertEquals(0, fetchFunctions.get(6).getOriginalSize().longValue()); // cache
        assertEquals(0, fetchFunctions.get(7).getOriginalSize().longValue()); //virtual
        ContinueResult<? extends RepositoryNode> continueResult1 = fetchFunctions.get(4).getFunction().apply(0, 10);
        assertEquals(10, continueResult1.getData().size());
    }

    @Test
    public void getByRepositoryTypeFilteredByType() {
        createMocksWithLocalAndVirtual();
        TreeFilter treeFilter = new TreeFilter();
        treeFilter.setRepositoryTypes(Collections.singletonList(RepositoryType.VIRTUAL));
        RootFetchByRepositoryType rootFetchByPackageType = new RootFetchByRepositoryType(treeFilter, RestTreeNode.getRepoOrder());
        List<FetchFunction<RepositoryNode>> fetchFunctions = rootFetchByPackageType.getFetchFunctions();
        assertEquals(4, fetchFunctions.size());
        assertEquals(8, fetchFunctions.get(3).getOriginalSize().longValue());

        ContinueResult<RepositoryNode> continueResult1 = fetchFunctions.get(3).getFunction().apply(0, 30);
        assertEquals(8, continueResult1.getData().size());
    }

    @Test
    public void getByRepositoryTypeFilteredByTypeAndPackageType() {
        createMocksWithLocalAndVirtual();
        TreeFilter treeFilter = new TreeFilter();
        treeFilter.setRepositoryTypes(Collections.singletonList(RepositoryType.VIRTUAL));
        treeFilter.setPackageTypes(Collections.singletonList(RepoType.Maven));
        RootFetchByRepositoryType rootFetchByPackageType = new RootFetchByRepositoryType(treeFilter, RestTreeNode.getRepoOrder());
        List<FetchFunction<RepositoryNode>> fetchFunctions = rootFetchByPackageType.getFetchFunctions();
        assertEquals(4, fetchFunctions.size());
        assertEquals(8, fetchFunctions.get(3).getOriginalSize().longValue()); // virtual

        ContinueResult<RepositoryNode> continueResult1 = fetchFunctions.get(3).getFunction().apply(0, 30);
        assertEquals(3, continueResult1.getData().size());
    }


    @Test
    public void getByRepositoryTypeFilteredByTypeAndPackageTypeFindNone() {
        createMocksWithLocalAndVirtual();
        TreeFilter treeFilter = new TreeFilter();
        treeFilter.setPackageTypes(Collections.singletonList(RepoType.NuGet));
        RootFetchByRepositoryType rootFetchByPackageType = new RootFetchByRepositoryType(treeFilter, RestTreeNode.getRepoOrder());
        List<FetchFunction<RepositoryNode>> fetchFunctions = rootFetchByPackageType.getFetchFunctions();
        assertEquals(8, fetchFunctions.size());
        assertEquals(22, fetchFunctions.get(4).getOriginalSize().longValue()); // local
        assertEquals(0, fetchFunctions.get(6).getOriginalSize().longValue()); // remote
        assertEquals(8, fetchFunctions.get(7).getOriginalSize().longValue()); // virtual

        ContinueResult<RepositoryNode> continueResult1 = fetchFunctions.get(4).getFunction().apply(0, 30);
        assertEquals(0, continueResult1.getData().size());
        ContinueResult<RepositoryNode> continueResult2 = fetchFunctions.get(6).getFunction().apply(0, 30);
        assertEquals(0, continueResult2.getData().size());
        ContinueResult<RepositoryNode> continueResult3 = fetchFunctions.get(7).getFunction().apply(0, 30);
        assertEquals(0, continueResult3.getData().size());
    }


    @Test
    public void getByRepositoryTypeFilteredByKey() {
        createMocksWithLocalAndVirtual();
        TreeFilter treeFilter = new TreeFilter();
        treeFilter.setByRepoKey("001");
        RootFetchByRepositoryType rootFetchByPackageType = new RootFetchByRepositoryType(treeFilter, RestTreeNode.getRepoOrder());
        List<FetchFunction<RepositoryNode>> fetchFunctions = rootFetchByPackageType.getFetchFunctions();
        assertEquals(8, fetchFunctions.size());
        assertEquals(0, fetchFunctions.get(3).getOriginalSize().longValue()); // distribution
        assertEquals(22, fetchFunctions.get(4).getOriginalSize().longValue()); // local
        assertEquals(0, fetchFunctions.get(5).getOriginalSize().longValue()); // remote
        assertEquals(0, fetchFunctions.get(6).getOriginalSize().longValue()); // remote
        assertEquals(8, fetchFunctions.get(7).getOriginalSize().longValue()); // virtual

        ContinueResult<RepositoryNode> continueResult1 = fetchFunctions.get(4).getFunction().apply(0, 30);
        assertEquals(3, continueResult1.getData().size());
        ContinueResult<RepositoryNode> continueResult3 = fetchFunctions.get(6).getFunction().apply(0, 30);
        assertEquals(0, continueResult3.getData().size());
    }
}
