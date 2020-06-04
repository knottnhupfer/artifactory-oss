package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tree;

import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.artifactory.api.jackson.JacksonReader;
import org.artifactory.api.rest.common.model.continues.ContinueResult;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.fs.ItemInfo;
import org.artifactory.model.common.RepoPathImpl;
import org.artifactory.model.xstream.fs.FileInfoImpl;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.service.ArtifactoryRestResponse;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes.JunctionNode;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes.archive.ArchiveEntriesTree;
import org.artifactory.ui.utils.MockArtifactoryArchiveEntriesTree;
import org.codehaus.jackson.JsonNode;
import org.mockito.Mockito;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.StreamingOutput;
import java.io.*;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.apache.http.HttpStatus.SC_OK;
import static org.artifactory.repo.RepoDetailsType.LOCAL_REPO;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

/**
 * @author Alexei Vainshtein
 */
public class BrowseTreeNodesServiceTest extends MockArtifactoryArchiveEntriesTree {

    private File archive;

    @BeforeClass
    public void setup() throws IOException {
        super.setup();
        archive = new File(System.getProperty("java.io.tmpdir"), "test.zip");
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(archive));
        ZipEntry entry1 = new ZipEntry(Paths.get("folder", "mytext.txt").toString());
        ZipEntry entry2 = new ZipEntry(Paths.get("folder", "text.jpg").toString());
        ZipEntry entry3 = new ZipEntry("test.txt");
        ZipEntry entry4 = new ZipEntry("one.txt");
        ZipEntry entry5 = new ZipEntry("two.txt");
        out.putNextEntry(entry1);
        out.putNextEntry(entry2);
        out.putNextEntry(entry3);
        out.putNextEntry(entry4);
        out.putNextEntry(entry5);
        out.closeEntry();
        out.close();
    }

    @AfterClass
    public void destroy() {
        archive.delete();
    }

    @Test
    public void testExecuteAsArchive() throws IOException {
        prepareMock("test.zip");
        InputStream ARCHIVE_STREAM = new FileInputStream(archive);
        when(repoService.archiveInputStream(any())).thenReturn(new ZipArchiveInputStream(ARCHIVE_STREAM));
        ArtifactoryRestResponse response = new ArtifactoryRestResponse();
        BrowseTreeNodesService browseTreeNodesService = new BrowseTreeNodesService();
        assertTrue(browseTreeNodesService.isArchiveRequest(artifactoryRestRequest));
        browseTreeNodesService.execute(artifactoryRestRequest, response);
        assertTrue(response.getIModel() instanceof StreamingOutput);
        assertEquals(response.buildResponse().getStatus(), SC_OK);
    }

    @Test
    public void testExecuteNonArchiveRequest() {
        LocalRepoDescriptor repoDescriptor = new LocalRepoDescriptor();
        repoDescriptor.setType(RepoType.Generic);
        prepareMock("test.txt");
        when(context.getRepositoryService()).thenReturn(repoService);
        when(repoService.repoDescriptorByKey(any())).thenReturn(repoDescriptor);
        ArtifactoryRestResponse response = new ArtifactoryRestResponse();
        BrowseTreeNodesService browseTreeNodesService = new BrowseTreeNodesService();
        assertFalse(browseTreeNodesService.isArchiveRequest(artifactoryRestRequest));
        browseTreeNodesService.execute(artifactoryRestRequest, response);
        assertTrue(response.getIModel() instanceof ContinueResult);
        assertEquals(response.buildResponse().getStatus(), SC_OK);
    }

    private void prepareMock(String fileName) {
        ItemInfo fileInfo = new FileInfoImpl(new RepoPathImpl("test", fileName));
        JunctionNode junctionNode = new JunctionNode();
        junctionNode.setRepoKey("test");
        junctionNode.setRepoType(LOCAL_REPO);
        junctionNode.setPath(fileName);
        when(repoService.getItemInfo(any())).thenReturn(fileInfo);
        when(artifactoryRestRequest.getImodel()).thenReturn(junctionNode);
        Request request = Mockito.mock(Request.class);
        when(artifactoryRestRequest.getRequest()).thenReturn(request);
        when(request.getMethod()).thenReturn(HttpMethod.POST);
    }

    @Test
    public void testWrite() throws IOException {
        OutputStream output = new OutputStream() {
            private StringBuilder string = new StringBuilder();

            @Override
            public void write(int x) {
                this.string.append((char) x);
            }

            public String toString() {
                return this.string.toString();
            }
        };
        InputStream ARCHIVE_STREAM = new FileInputStream(archive);
        when(repoService.archiveInputStream(any())).thenReturn(new ZipArchiveInputStream(ARCHIVE_STREAM));
        BrowseTreeNodesService browseTreeNodesService = new BrowseTreeNodesService();
        ContinueResult<? extends RestModel> continueResult = new ArchiveEntriesTree().buildChildren("test", "test.zip");
        browseTreeNodesService.write(continueResult, output);
        String streamResult = output.toString();
        JsonNode jsonNode = JacksonReader.bytesAsTree(streamResult.getBytes());
        assertEquals(jsonNode.size(), 2);
        JsonNode data = jsonNode.get("data");
        assertEquals(data.size(), 4);
        assertEquals(data.get(0).get("children").size(), 2);
        assertTrue(data.get(0).get("folder").asBoolean());}
}