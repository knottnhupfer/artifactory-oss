package org.artifactory.properties;

import org.artifactory.md.Properties;
import org.artifactory.model.xstream.fs.PropertiesImpl;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

/**
 * @author dudim
 */
public class PropertiesServiceImplTest {

    @Test
    public void testSetPropertiesWithInternalPropertiesNotInvokeInterceptors() {
        PropertiesServiceImpl propertiesServiceSpy = spy(new PropertiesServiceImpl());

        Mockito.doReturn(true).when(propertiesServiceSpy).setMetadataProperties(any(),any());
        RepoPath repoPath = RepoPathFactory.create("repo-example", "");
        Properties properties = new PropertiesImpl();
        propertiesServiceSpy.setProperties(repoPath, properties, true);

        verify(propertiesServiceSpy, times(1)).setMetadataProperties(any(), any());
        verify(propertiesServiceSpy, times(0)).setArtifactProperties(any(), any());
        verify(propertiesServiceSpy, times(0)).goThroughBeforeCreateProperties(any(), any(), any(), any());
        verify(propertiesServiceSpy, times(0)).goThroughAfterCreateProperties(any(), any(), any());
    }
}