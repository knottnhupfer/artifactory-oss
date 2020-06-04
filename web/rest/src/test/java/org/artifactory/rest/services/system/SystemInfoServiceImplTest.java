package org.artifactory.rest.services.system;

import com.sun.management.OperatingSystemMXBean;
import com.sun.management.UnixOperatingSystemMXBean;
import org.artifactory.rest.resource.system.SystemInfo;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.management.ManagementFactory;

/**
 * @author Yoaz Menda
 */
import static org.testng.Assert.*;

public class SystemInfoServiceImplTest {

    private SystemInfoService systemServiceInfo;

    @BeforeMethod
    public void setup() {
        systemServiceInfo = new SystemInfoServiceImpl();
    }

    private static OperatingSystemMXBean mbean =
            (com.sun.management.OperatingSystemMXBean)
                    ManagementFactory.getOperatingSystemMXBean();

    @Test
    public void testGetSystemInfo() {
        SystemInfo systemInfo = systemServiceInfo.getSystemInfo();
        assertNotNull(systemInfo);
        assertTrue(systemInfo.getCommittedVirtualMemorySize() > Long.MIN_VALUE);
        assertTrue(systemInfo.getFreeSwapSpaceSize() > Long.MIN_VALUE);
        assertTrue(systemInfo.getTotalSwapSpaceSize() > Long.MIN_VALUE);
        assertTrue(systemInfo.getProcessCpuTime() > Long.MIN_VALUE);
        assertTrue(systemInfo.getFreePhysicalMemorySize() > Long.MIN_VALUE);
        assertTrue(systemInfo.getTotalPhysicalMemorySize() > Long.MIN_VALUE);
        assertTrue(systemInfo.getSystemCpuLoad() >= Long.MIN_VALUE);
        assertTrue(systemInfo.getProcessCpuLoad() >= Long.MIN_VALUE);
        if(mbean instanceof UnixOperatingSystemMXBean) {
            assertTrue(systemInfo.getOpenFileDescriptorCount() > Long.MIN_VALUE);
            assertTrue(systemInfo.getMaxFileDescriptorCount() > Long.MIN_VALUE);
        }
        assertTrue(systemInfo.getNumberOfCores()  > Integer.MIN_VALUE);
        assertTrue(systemInfo.getHeapMemoryUsage()  > Long.MIN_VALUE);
        assertTrue(systemInfo.getNoneHeapMemoryUsage()  > Long.MIN_VALUE);
        assertTrue(systemInfo.getNoneHeapMemoryMax()  > Long.MIN_VALUE);
        assertTrue(systemInfo.getThreadCount()  > Integer.MIN_VALUE);
        assertTrue(systemInfo.getHeapMemoryMax()  > Long.MIN_VALUE);
        assertTrue(systemInfo.getJvmUpTime()  > Long.MIN_VALUE);
    }
}