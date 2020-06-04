package org.artifactory.backup;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.CoreAddons;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.mail.MailService;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.util.EmailException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * @author batelt
 */
public class BackupServiceImplTest {
    private static final Logger log = LoggerFactory.getLogger(BackupServiceImplTest.class);
    private List<String> emailAddresses = Arrays.asList("a@a.com", "b@b.com", "c@c.com");
    private BasicStatusHolder statusHolder;

    private BackupServiceImpl backupService;

    @Mock
    private AddonsManager addonsManagerMock;
    @Mock
    private MailService mailService;
    @Mock
    private CentralConfigService centralConfigService;
    @Mock
    private CentralConfigDescriptor configDescriptor;

    @BeforeMethod
    private void setup() {
        MockitoAnnotations.initMocks(this);
        backupService = new BackupServiceImpl(centralConfigService, null, mailService, addonsManagerMock, null, null,
                null);
        mockCoreAddons(addonsManagerMock);
        when(centralConfigService.getDescriptor()).thenReturn(configDescriptor);
        when(configDescriptor.getServerUrlForEmail()).thenReturn("URLXYZ");
        statusHolder = new BasicStatusHolder();
        statusHolder.error("status", log);
    }

    @Test(description = "RTFACT-18957")
    public void testSendMailToMalformedAdminMailAddress() throws Exception {
        doThrow(new EmailException("Test message: b@b.com is Malformed")).when(mailService)
                .sendMail(eq(new String[]{"b@b.com"}), any(), any());
        backupService.sendBackupErrorNotification("not_important", statusHolder);
        verifySendBackupErrorMailCalled();
    }

    @Test(description = "RTFACT-18957")
    public void testSendMailToAdmins() throws Exception {
        mockCoreAddons(addonsManagerMock);
        doNothing().when(mailService).sendMail(any(), any(), any());
        backupService.sendBackupErrorNotification("not_important", statusHolder);
        verifySendBackupErrorMailCalled();
    }

    private void mockCoreAddons(AddonsManager addonsManagerMock) {
        CoreAddons coreAddonsMock = mock(CoreAddons.class);
        when(addonsManagerMock.addonByType(CoreAddons.class)).thenReturn(coreAddonsMock);
        when(coreAddonsMock.getUsersForBackupNotifications()).thenReturn(emailAddresses);
    }

    private void verifySendBackupErrorMailCalled() {
        verify(mailService, times(emailAddresses.size())).sendMail(any(), any(), any());
        emailAddresses.forEach(emailAddress -> verify(mailService, times(1))
                .sendMail(eq(new String[]{"b@b.com"}), any(), any()));
    }
}