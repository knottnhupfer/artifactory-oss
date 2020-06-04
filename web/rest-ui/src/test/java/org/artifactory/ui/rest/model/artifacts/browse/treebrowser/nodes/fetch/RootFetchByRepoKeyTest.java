package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes.fetch;

import org.artifactory.api.rest.common.model.continues.ContinueResult;
import org.artifactory.api.rest.common.model.continues.FetchFunction;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes.TreeFilter;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes.repo.RepositoryNode;
import org.testng.annotations.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Omri Ziv
 */
public class RootFetchByRepoKeyTest extends RootFetchStrategyTest {

    @Test
    public void testByKey() {
        createMocks();
        TreeFilter treeFilter = new TreeFilter();
        RootFetchByRepoKey rootFetchByRepoKey = new RootFetchByRepoKey(treeFilter);
        List<FetchFunction<RepositoryNode>> fetchFunctions = rootFetchByRepoKey.getFetchFunctions();
        assertEquals(6, fetchFunctions.size());
        assertEquals(5L, fetchFunctions.get(3).getOriginalSize().longValue());
        assertEquals(10L ,fetchFunctions.get(4).getOriginalSize().longValue());

        ContinueResult<RepositoryNode> continueResult1 = fetchFunctions.get(3).getFunction().apply(0, 20);
        assertEquals(5, continueResult1.getData().size());
        ContinueResult<RepositoryNode> continueResult2 = fetchFunctions.get(4).getFunction().apply(0, 20);
        assertEquals(10, continueResult2.getData().size());
    }

}
