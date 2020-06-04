package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes.fetch;

import org.artifactory.api.rest.common.model.continues.ContinueResult;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.api.rest.common.model.continues.FetchFunction;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes.RepositoryType;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes.TreeFilter;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes.repo.RepositoryNode;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Omri Ziv
 */
public class RootFetchByPackageTypeTest extends RootFetchStrategyTest {


    @Test
    public void testByType() {
        createMocks();
        TreeFilter treeFilter = new TreeFilter();
        treeFilter.setPackageTypes(Arrays.asList(RepoType.Maven, RepoType.Docker));
        RootFetchByPackageType rootFetchByPackageType = new RootFetchByPackageType(treeFilter);
        List<FetchFunction<RepositoryNode>> fetchFunctions = rootFetchByPackageType.getFetchFunctions();
        assertEquals(5, fetchFunctions.size());
        assertEquals(5L, fetchFunctions.get(3).getOriginalSize().longValue());
        assertEquals(10L ,fetchFunctions.get(4).getOriginalSize().longValue());

        ContinueResult<? extends RestModel> continueResult1 = fetchFunctions.get(3).getFunction().apply(0, 20);
        assertEquals(5, continueResult1.getData().size());
        ContinueResult<? extends RestModel> continueResult2 = fetchFunctions.get(4).getFunction().apply(0, 20);
        assertEquals(10, continueResult2.getData().size(), 10);
    }

    @Test
    public void testByTypeNoFilter() {
        createMocks();

        TreeFilter treeFilter = new TreeFilter();
        RootFetchByPackageType rootFetchByPackageType = new RootFetchByPackageType(treeFilter);
        List<FetchFunction<RepositoryNode>> fetchFunctions = rootFetchByPackageType.getFetchFunctions();

        assertEquals(6, fetchFunctions.size());
        assertEquals(5L, fetchFunctions.get(3).getOriginalSize().longValue());
        assertEquals(10L ,fetchFunctions.get(4).getOriginalSize().longValue());
        assertEquals(7L, fetchFunctions.get(5).getOriginalSize().longValue());

        ContinueResult<RepositoryNode> continueResult1 = fetchFunctions.get(3).getFunction().apply(0, 20);
        assertEquals(5, continueResult1.getData().size());
        ContinueResult<RepositoryNode> continueResult2 = fetchFunctions.get(4).getFunction().apply(0, 20);
        assertEquals(10, continueResult2.getData().size());
        ContinueResult<RepositoryNode> continueResult3 = fetchFunctions.get(5).getFunction().apply(0, 20);
        assertEquals(7, continueResult3.getData().size());

    }

    @Test
    public void testByTypeIncludingLocalAndVirtual() {
        createMocksWithLocalAndVirtual();

        TreeFilter treeFilter = new TreeFilter();
        RootFetchByPackageType rootFetchByPackageType = new RootFetchByPackageType(treeFilter);
        List<FetchFunction<RepositoryNode>> fetchFunctions = rootFetchByPackageType.getFetchFunctions();

        assertEquals(6, fetchFunctions.size());
        assertEquals(9L, fetchFunctions.get(3).getOriginalSize().longValue());
        assertEquals(13L ,fetchFunctions.get(4).getOriginalSize().longValue());
        assertEquals(8L, fetchFunctions.get(5).getOriginalSize().longValue());

        ContinueResult<RepositoryNode> continueResult1 = fetchFunctions.get(3).getFunction().apply(0, 20);
        assertEquals(9, continueResult1.getData().size());
        ContinueResult<RepositoryNode> continueResult2 = fetchFunctions.get(4).getFunction().apply(0, 20);
        assertEquals(13, continueResult2.getData().size());
        ContinueResult<RepositoryNode> continueResult3 = fetchFunctions.get(5).getFunction().apply(0, 20);
        assertEquals(8, continueResult3.getData().size());

    }

    @Test
    public void testByTypeIncludingLocalAndVirtualCheckSkipAndLimit() {
        createMocksWithLocalAndVirtual();

        TreeFilter treeFilter = new TreeFilter();
        RootFetchByPackageType rootFetchByPackageType = new RootFetchByPackageType(treeFilter);
        List<FetchFunction<RepositoryNode>> fetchFunctions = rootFetchByPackageType.getFetchFunctions();

        assertEquals(6, fetchFunctions.size());
        assertEquals(9L, fetchFunctions.get(3).getOriginalSize().longValue());
        assertEquals(13L ,fetchFunctions.get(4).getOriginalSize().longValue());
        assertEquals(8L, fetchFunctions.get(5).getOriginalSize().longValue());

        ContinueResult<RepositoryNode> continueResult1 = fetchFunctions.get(3).getFunction().apply(2, 1);
        assertEquals(1, continueResult1.getData().size());
        ContinueResult<RepositoryNode> continueResult2 = fetchFunctions.get(4).getFunction().apply(0, 3);
        assertEquals(3, continueResult2.getData().size());
        ContinueResult<RepositoryNode> continueResult3 = fetchFunctions.get(5).getFunction().apply(0, 4);
        assertEquals(4, continueResult3.getData().size());
        ContinueResult<RepositoryNode> continueResult4 = fetchFunctions.get(5).getFunction().apply(3, 51);
        assertEquals(5, continueResult4.getData().size());

    }

    @Test
    public void testByTypeOnlyVirtualFilter() {
        createMocksWithLocalAndVirtual();

        TreeFilter treeFilter = new TreeFilter();
        treeFilter.setRepositoryTypes(Collections.singletonList(RepositoryType.VIRTUAL));
        RootFetchByPackageType rootFetchByPackageType = new RootFetchByPackageType(treeFilter);
        List<FetchFunction<RepositoryNode>> fetchFunctions = rootFetchByPackageType.getFetchFunctions();

        assertEquals(6, fetchFunctions.size());
        assertEquals(9L, fetchFunctions.get(3).getOriginalSize().longValue());
        assertEquals(13L ,fetchFunctions.get(4).getOriginalSize().longValue());
        assertEquals(8L, fetchFunctions.get(5).getOriginalSize().longValue());

        ContinueResult<RepositoryNode> continueResult1 = fetchFunctions.get(3).getFunction().apply(0, 20);
        assertEquals(4, continueResult1.getData().size());
        ContinueResult<RepositoryNode> continueResult2 = fetchFunctions.get(4).getFunction().apply(0, 20);
        assertEquals(3, continueResult2.getData().size());
        ContinueResult<RepositoryNode> continueResult3 = fetchFunctions.get(5).getFunction().apply(0, 20);
        assertEquals(1, continueResult3.getData().size());
    }

    @Test
    public void testByTypeOnlyRemoteExpectNone() {
        createMocksWithLocalAndVirtual();

        TreeFilter treeFilter = new TreeFilter();
        treeFilter.setRepositoryTypes(Collections.singletonList(RepositoryType.REMOTE));
        RootFetchByPackageType rootFetchByPackageType = new RootFetchByPackageType(treeFilter);
        List<FetchFunction<RepositoryNode>> fetchFunctions = rootFetchByPackageType.getFetchFunctions();

        assertEquals(6, fetchFunctions.size());
        assertEquals(9L, fetchFunctions.get(3).getOriginalSize().longValue());
        assertEquals(13L ,fetchFunctions.get(4).getOriginalSize().longValue());
        assertEquals(8L, fetchFunctions.get(5).getOriginalSize().longValue());

        ContinueResult<RepositoryNode> continueResult1 = fetchFunctions.get(3).getFunction().apply(0, 20);
        assertEquals(0, continueResult1.getData().size());
        ContinueResult<RepositoryNode> continueResult2 = fetchFunctions.get(4).getFunction().apply(0, 20);
        assertEquals(0, continueResult2.getData().size());
        ContinueResult<RepositoryNode> continueResult3 = fetchFunctions.get(5).getFunction().apply(0, 20);
        assertEquals(0, continueResult3.getData().size());
    }

    @Test
    public void testByTypeFilterByRepoKey() {
        createMocksWithLocalAndVirtual();

        TreeFilter treeFilter = new TreeFilter();
        treeFilter.setByRepoKey("00");
        RootFetchByPackageType rootFetchByPackageType = new RootFetchByPackageType(treeFilter);
        List<FetchFunction<RepositoryNode>> fetchFunctions = rootFetchByPackageType.getFetchFunctions();

        assertEquals(6, fetchFunctions.size());
        assertEquals(9L, fetchFunctions.get(3).getOriginalSize().longValue());
        assertEquals(13L ,fetchFunctions.get(4).getOriginalSize().longValue());
        assertEquals(8L, fetchFunctions.get(5).getOriginalSize().longValue());

        ContinueResult<RepositoryNode> continueResult1 = fetchFunctions.get(3).getFunction().apply(0, 20);
        assertEquals(9, continueResult1.getData().size());
        ContinueResult<RepositoryNode> continueResult2 = fetchFunctions.get(4).getFunction().apply(0, 20);
        assertEquals(12, continueResult2.getData().size());
        ContinueResult<RepositoryNode> continueResult3 = fetchFunctions.get(5).getFunction().apply(0, 20);
        assertEquals(8, continueResult3.getData().size());

        treeFilter.setByRepoKey("001");
        fetchFunctions = rootFetchByPackageType.getFetchFunctions();

        assertEquals(6, fetchFunctions.size());
        assertEquals(9L, fetchFunctions.get(3).getOriginalSize().longValue());
        assertEquals(13L ,fetchFunctions.get(4).getOriginalSize().longValue());
        assertEquals(8L, fetchFunctions.get(5).getOriginalSize().longValue());

        continueResult1 = fetchFunctions.get(3).getFunction().apply(0, 20);
        assertEquals(2, continueResult1.getData().size());
        continueResult2 = fetchFunctions.get(4).getFunction().apply(0, 20);
        assertEquals(2, continueResult2.getData().size());
        continueResult3 = fetchFunctions.get(5).getFunction().apply(0, 20);
        assertEquals(2, continueResult3.getData().size());
    }


}
