package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes;

import org.artifactory.fs.ItemInfo;
import org.artifactory.model.common.RepoPathImpl;
import org.artifactory.model.xstream.fs.FileInfoImpl;
import org.artifactory.ui.utils.MockArtifactoryArchiveEntriesTree;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.artifactory.repo.RepoDetailsType.LOCAL_REPO;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

/**
 * @author Alexei Vainshtein
 */
public class RestTreeNodeTest extends MockArtifactoryArchiveEntriesTree {

    @Test
    public void testRootNodeIsArchiveExpendRequest() {
        RootNode rootNode = new RootNode();
        assertFalse(rootNode.isArchiveExpendRequest());
    }

    @Test(dataProvider = "providerArchiveJunction")
    public void testJunctionIsArchiveExpendRequest(String repo, String path, boolean result) {
        ItemInfo fileInfo = new FileInfoImpl(new RepoPathImpl(repo, path));
        JunctionNode junctionNode = new JunctionNode();
        junctionNode.setRepoKey(repo);
        junctionNode.setRepoType(LOCAL_REPO);
        junctionNode.setPath(path);
        when(repoService.getItemInfo(any())).thenReturn(fileInfo);
        assertEquals(junctionNode.isArchiveExpendRequest(), result);
    }

    @Test(dataProvider = "providerRootNode")
    public void testRootNodeIsArchiveExpendRequest(String repo, String path) {
        ItemInfo fileInfo = new FileInfoImpl(new RepoPathImpl(repo, path));
        RootNode rootNode = new RootNode();
        when(repoService.getItemInfo(any())).thenReturn(fileInfo);
        assertFalse(rootNode.isArchiveExpendRequest());
    }

    @DataProvider
    public static Object[][] providerArchiveJunction() {
        return new Object[][]{
                {"repo", "test.txt", false},
                {"libs", "test.tar.gz", true},
                {"libs", "a/b/test.gz", true},
                {"libs-repo", "a/b/test.tgz", true},
                {"libs-repo", "a/b/", false}
        };
    }

    @DataProvider
    public static Object[][] providerRootNode() {
        return new Object[][]{
                {"repo", "test.txt"},
                {"libs", "test.tar.gz"},
                {"libs", "a/b/test.gz"},
                {"libs-repo", "a/b/test.tgz"},
                {"libs-repo", "a/b/"}
        };
    }
}