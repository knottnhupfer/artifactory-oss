package org.artifactory.layout;

import org.artifactory.descriptor.config.CentralConfigDescriptorImpl;
import org.artifactory.descriptor.trashcan.TrashcanConfigDescriptor;
import org.artifactory.repo.service.trash.TrashServiceImpl;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author Shay Bagants
 */
public class TrashcanConfigurationInterceptorTest {

    @Test(dataProvider = "provideValidRetentionDays")
    public void testOnBeforeSave(int retentionDays) {
        TrashServiceImpl trashService = new TrashServiceImpl();
        TrashcanConfigurationInterceptor interceptor = new TrashcanConfigurationInterceptor(trashService);
        CentralConfigDescriptorImpl centralConfigDescriptor = new CentralConfigDescriptorImpl();
        configureTrashcan(retentionDays, centralConfigDescriptor);

        interceptor.onBeforeSave(centralConfigDescriptor);
        Assert.assertNotNull(centralConfigDescriptor.getTrashcanConfig());
    }

    @DataProvider
    public static Object[][] provideValidRetentionDays() {
        return new Object[][]{
                {14},
                {22},
                {400},
                {500},
                {1000},
                {Days.daysBetween(LocalDate.now().minusDays(300), LocalDate.now()).getDays()},
                {Days.daysBetween(LocalDate.now().minusDays(900), LocalDate.now()).getDays()},
                {Days.daysBetween(LocalDate.now().minusYears(5), LocalDate.now()).getDays()}
        };
    }

    @Test(dataProvider = "provideInvalidRetentionDays", expectedExceptions = IllegalArgumentException.class)
    public void testOnBeforeSaveInvalidRetention(int retentionDays) {
        TrashServiceImpl trashService = new TrashServiceImpl();
        TrashcanConfigurationInterceptor interceptor = new TrashcanConfigurationInterceptor(trashService);
        CentralConfigDescriptorImpl centralConfigDescriptor = new CentralConfigDescriptorImpl();
        configureTrashcan(retentionDays, centralConfigDescriptor);

        interceptor.onBeforeSave(centralConfigDescriptor);
        Assert.assertNotNull(centralConfigDescriptor.getTrashcanConfig());
    }

    @DataProvider
    public static Object[][] provideInvalidRetentionDays() {
        return new Object[][]{
                {Days.daysBetween(LocalDate.now().minusYears(22), LocalDate.now()).getDays()},
                {Days.daysBetween(LocalDate.now().minusYears(30), LocalDate.now()).getDays()},
                {Days.daysBetween(LocalDate.now().minusYears(50), LocalDate.now()).getDays()},
        };
    }

    private void configureTrashcan(int retentionDays, CentralConfigDescriptorImpl centralConfigDescriptor) {
        TrashcanConfigDescriptor trashcanConfigDescriptor = new TrashcanConfigDescriptor();
        trashcanConfigDescriptor.setEnabled(true);
        trashcanConfigDescriptor.setRetentionPeriodDays(retentionDays);
        centralConfigDescriptor.setTrashcanConfig(trashcanConfigDescriptor);
    }
}