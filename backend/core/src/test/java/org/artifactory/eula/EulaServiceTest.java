package org.artifactory.eula;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.ArtifactoryRunningMode;
import org.artifactory.addon.eula.EulaService;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.config.CentralConfigDescriptorImpl;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.eula.EulaDescriptor;
import org.joda.time.format.ISODateTimeFormat;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

public class EulaServiceTest {
    private EulaService eulaService;
    private MutableCentralConfigDescriptor descriptor;
    @Mock
    private CentralConfigService centralConfigService = mock(CentralConfigService.class);
    @Mock
    private AddonsManager addonsManager = mock(AddonsManager.class);

    @BeforeMethod
    public void beforeTest() {
        eulaService = new EulaServiceImpl(centralConfigService, addonsManager);
        descriptor = new CentralConfigDescriptorImpl();
        when(centralConfigService.getDescriptor()).thenReturn(descriptor);
        when(centralConfigService.getMutableDescriptor()).thenReturn(descriptor);
        when(addonsManager.getArtifactoryRunningMode()).thenReturn(ArtifactoryRunningMode.JCR);
    }

    @Test
    public void testAccept() {
        assertNull(descriptor.getEulaConfig());

        long startTime = System.currentTimeMillis();
        eulaService.accept();
        EulaDescriptor eulaConfig = descriptor.getEulaConfig();
        assertTrue(eulaConfig.isAccepted());
        long acceptDate = ISODateTimeFormat.dateTime().parseMillis(eulaConfig.getAcceptDate());
        assertTrue(acceptDate >= startTime && acceptDate < startTime + TimeUnit.MINUTES.toMillis(1));
        assertFalse(eulaService.isRequired());
    }

    @Test
    public void testAcceptWhileAlreadySigned() {
        EulaDescriptor eulaDescriptor = new EulaDescriptor();
        eulaDescriptor.setAccepted(true);
        eulaDescriptor.setAcceptDate("xyz");
        descriptor.setEulaConfig(eulaDescriptor);

        validateAcceptWorks();
    }

    @Test
    public void testAcceptWhileDescriptorHasEmptyEulaObject() {
        EulaDescriptor eulaDescriptor = new EulaDescriptor();
        descriptor.setEulaConfig(eulaDescriptor);

        validateAcceptWorks();
    }

    @Test
    public void testIsRequiredDefault() {
        assertTrue(eulaService.isRequired());
    }

    @Test
    public void testJcrAolIsNotRequiredDefault() {
        when(addonsManager.getArtifactoryRunningMode()).thenReturn(ArtifactoryRunningMode.AOL_JCR);
        assertFalse(eulaService.isRequired());
    }

    @Test
    public void testNonJcrEulaNotRequired() {
        Arrays.stream(ArtifactoryRunningMode.values())
                .filter(mode -> !mode.isJCR())
                .forEach(runningMode -> {
                    when(addonsManager.getArtifactoryRunningMode()).thenReturn(runningMode);
                    assertFalse(eulaService.isRequired());
                });
    }

    @Test
    public void testNotRequiredAfterAccept() {
        assertTrue(eulaService.isRequired());
        eulaService.accept();
        assertFalse(eulaService.isRequired());
    }

    @Test
    public void testGetEula() {
        byte[] fileArr = eulaService.getEulaFile();
        assertTrue(new String(fileArr).contains("READ CAREFULLY THE TERMS AND CONDITIONS"));
    }

    private void validateAcceptWorks() {
        long startTime = System.currentTimeMillis();
        eulaService.accept();
        EulaDescriptor eulaConfig = descriptor.getEulaConfig();
        assertTrue(eulaConfig.isAccepted());
        assertFalse(eulaService.isRequired());
        assertTrue(descriptor.getEulaConfig().isAccepted());
        long acceptDate = ISODateTimeFormat.dateTime().parseMillis(descriptor.getEulaConfig().getAcceptDate());
        assertTrue(acceptDate >= startTime && acceptDate < startTime + TimeUnit.MINUTES.toMillis(1));
    }
}
