package org.artifactory.repo.virtual;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.LayoutsCoreAddon;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.VirtualRepoDescriptor;
import org.artifactory.fs.RepoResource;
import org.artifactory.mime.MimeType;
import org.artifactory.model.common.RepoPathImpl;
import org.artifactory.model.xstream.fs.FileInfoImpl;
import org.artifactory.repo.RealRepo;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.request.ArtifactoryRequest;
import org.artifactory.request.DownloadRequestContext;
import org.artifactory.resource.FileResource;
import org.artifactory.resource.UnfoundRepoResource;
import org.artifactory.resource.UnfoundRepoResourceReason;
import org.artifactory.spring.InternalArtifactoryContext;
import org.artifactory.spring.InternalContextHelper;
import org.artifactory.util.RepoLayoutUtils;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@PrepareForTest({ContextHelper.class, InternalContextHelper.class/*, NamingUtils.class*/})
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.parsers.*", "org.xml.*", "javax.security.*"})
public class VirtualRepoDownloadStrategyTest extends PowerMockTestCase {

    @Mock
    private DownloadRequestContext context;
    @Mock
    private InternalArtifactoryContext internalArtifactoryContext;
    @Mock
    private AddonsManager addonsManager;
    @Mock
    private CentralConfigService centralConfigService;
    @Mock
    private VirtualRepo virtualRepo;
    @Mock
    private VirtualRepoDescriptor virtualRepoDescriptor;
    @Mock
    private InternalRepositoryService internalRepositoryService;
    @Mock
    private LayoutsCoreAddon layoutsCoreAddon;
    @Mock
    private ArtifactoryRequest artifactoryRequest;

    private RepoPath repoPath1, repoPath2;
    private RealRepo repo1, repo2;
    private FileInfoImpl fileInfo1, fileInfo2;

    @BeforeClass
    public void setup() {
        MockitoAnnotations.initMocks(this);
        repoPath1 = new RepoPathImpl("repo1", "/mysql/connector-java/" +
                "5.1.7-SNAPSHOT/connector-java-5.1.7-20190219.131101-4.jar");
        repoPath2 = new RepoPathImpl("repo2", "/mysql/connector-java/" +
                "5.1.7-SNAPSHOT/connector-java-5.1.7-20190225.231101-5.jar");
    }

    @BeforeMethod
    public void beforeMethod() {
        PowerMockito.mockStatic(ContextHelper.class);
        when(ContextHelper.get())
                .thenReturn(internalArtifactoryContext);
        PowerMockito.mockStatic(InternalContextHelper.class);
        when(InternalContextHelper.get())
                .thenReturn(internalArtifactoryContext);
        when(internalArtifactoryContext.getCentralConfig())
                .thenReturn(centralConfigService);
        when(internalArtifactoryContext.beanForType(InternalRepositoryService .class))
                .thenReturn(internalRepositoryService);
        when(internalArtifactoryContext.beanForType(AddonsManager.class))
                .thenReturn(addonsManager);
        when(addonsManager.addonByType(LayoutsCoreAddon.class))
                .thenReturn(layoutsCoreAddon);
        when(virtualRepo.getDescriptor())
                .thenReturn(virtualRepoDescriptor);
        when(virtualRepoDescriptor.getRepoLayout())
                .thenReturn(RepoLayoutUtils.MAVEN_2_DEFAULT);
        when(context.getRequest())
                .thenReturn(artifactoryRequest);
        when(layoutsCoreAddon.translateArtifactPath(any(), any(), anyString()))
                .thenAnswer(invocation -> invocation.getArgument(2));

        repo1 = mock(RealRepo.class);
        repo2 = mock(RealRepo.class);

        fileInfo1 = new FileInfoImpl(repoPath1);
        fileInfo1.setMimeType(MimeType.javaArchive);
        fileInfo2 = new FileInfoImpl(repoPath2);
        fileInfo2.setMimeType(MimeType.javaArchive);
        when(repo1.getKey())
                .thenReturn(repoPath1.getRepoKey());
        when(repo2.getKey())
                .thenReturn(repoPath2.getRepoKey());
        when(repo1.isHandleSnapshots())
                .thenReturn(true);
        when(repo2.isHandleSnapshots())
                .thenReturn(true);
        when(repo1.getDescriptor())
                .thenReturn(createLocalDescriptor(repoPath1.getRepoKey()));
        when(repo2.getDescriptor())
                .thenReturn(createLocalDescriptor(repoPath2.getRepoKey()));

    }

    @Test
    public void processUniqueSnapshot() throws IOException {

        VirtualRepoDownloadStrategy tester = new VirtualRepoDownloadStrategy(virtualRepo);

        when(context.getResourcePath())
                .thenReturn(repoPath1.getPath());
        when(artifactoryRequest.getRepoKey())
                .thenReturn(repoPath1.getRepoKey());

        when(repo1.getInfo(context))
                .thenReturn(new FileResource(fileInfo1));
        when(repo2.getInfo(context))
                .thenReturn(new FileResource(fileInfo2));
        RepoResource repoResource = tester.processSnapshot(context, repoPath1, Arrays.asList(repo1, repo2));
        assertEquals(repoResource.getRepoPath().getRepoKey(), repoPath1.getRepoKey());
        verify(repo2, times(0)).getInfo(any());

    }

    @Test
    public void processUniqueSnapshotNotFoundFirst() throws IOException {

        VirtualRepoDownloadStrategy tester = new VirtualRepoDownloadStrategy(virtualRepo);

        when(context.getResourcePath())
                .thenReturn(repoPath2.getPath());
        when(artifactoryRequest.getRepoKey())
                .thenReturn(repoPath2.getRepoKey());
        when(repo1.getInfo(context))
                .thenReturn(new UnfoundRepoResource(repoPath1, "",
                        UnfoundRepoResourceReason.Reason.PROPERTY_MISMATCH));
        when(repo2.getInfo(context))
                .thenReturn(new FileResource(fileInfo2));
        RepoResource repoResource = tester.processSnapshot(context, repoPath2, Arrays.asList(repo1, repo2));

        assertEquals(repoResource.getRepoPath().getRepoKey(), repoPath2.getRepoKey());
        verify(repo2, times(1)).getInfo(any());

    }

    private LocalRepoDescriptor createLocalDescriptor(String repoKey) {
        LocalRepoDescriptor descriptor = new LocalRepoDescriptor();
        descriptor.setKey(repoKey);
        descriptor.setRepoLayout(RepoLayoutUtils.MAVEN_2_DEFAULT);
        return descriptor;
    }

}
