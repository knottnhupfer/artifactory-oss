package org.artifactory.storage.binstore.service;

import com.google.common.collect.ImmutableList;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.property.ArtifactorySystemProperties;
import org.artifactory.storage.GCCandidate;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.testng.annotations.Test;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author dudim
 */
@Test
public class GCProviderWrapperTest extends ArtifactoryHomeBoundTest {

    public void testErrorCount() {
        ArtifactorySystemProperties artifactoryProperties = ArtifactoryHome.get().getArtifactoryProperties();
        artifactoryProperties.setProperty(ConstantValues.gcFailCountThreshold.getPropertyName(), "3");
        GCProvider gcProviderMock = mock(GCProvider.class);

        when(gcProviderMock.getBatch()).thenReturn(ImmutableList.of(
                new GCCandidate(null, "isEnabled=true", null, null, 0)));

        when(gcProviderMock.getAction()).thenReturn((o, o2) -> {
            throw new RuntimeException("increasing error count");
        });

        GCProviderWrapper gcProviderWrapper = new GCProviderWrapper(gcProviderMock);
        assertEquals(gcProviderWrapper.getBatch().get(0).getSha1(), "isEnabled=true");

        gcProviderWrapper.getAction()
                .accept(new GCCandidate(null, null, null, null, 0), new GarbageCollectorInfo());
        assertEquals(gcProviderWrapper.getBatch().get(0).getSha1(), "isEnabled=true");

        gcProviderWrapper.getAction()
                .accept(new GCCandidate(null, null, null, null, 0), new GarbageCollectorInfo());
        assertEquals(gcProviderWrapper.getBatch().get(0).getSha1(), "isEnabled=true");

        gcProviderWrapper.getAction()
                .accept(new GCCandidate(null, null, null, null, 0), new GarbageCollectorInfo());
        assertTrue(gcProviderWrapper.getBatch().isEmpty());
    }
}