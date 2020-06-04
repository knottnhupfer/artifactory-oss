package org.artifactory.rest.services.system;

import com.sun.management.OperatingSystemMXBean;
import com.sun.management.UnixOperatingSystemMXBean;
import org.artifactory.rest.resource.system.SystemInfo;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;

/**
 * @author Yoaz Menda
 */
@Service
public class SystemInfoServiceImpl implements SystemInfoService {
    private static OperatingSystemMXBean mbean =
            (com.sun.management.OperatingSystemMXBean)
                    ManagementFactory.getOperatingSystemMXBean();

    @Override
    public SystemInfo getSystemInfo() {
        SystemInfo systemInfo = new SystemInfo();
        systemInfo.setNumberOfCores(Runtime.getRuntime().availableProcessors());
        systemInfo.setFreeSwapSpaceSize(mbean.getFreeSwapSpaceSize());
        systemInfo.setCommittedVirtualMemorySize(mbean.getCommittedVirtualMemorySize());
        systemInfo.setTotalSwapSpaceSize(mbean.getTotalSwapSpaceSize());
        systemInfo.setProcessCpuTime(mbean.getProcessCpuTime());
        systemInfo.setFreePhysicalMemorySize(mbean.getFreePhysicalMemorySize());
        systemInfo.setTotalPhysicalMemorySize(mbean.getTotalPhysicalMemorySize());
        systemInfo.setSystemCpuLoad(mbean.getSystemCpuLoad());
        systemInfo.setProcessCpuLoad(mbean.getProcessCpuLoad());
        MemoryMXBean mem = ManagementFactory.getMemoryMXBean();
        systemInfo.setHeapMemoryUsage(mem.getHeapMemoryUsage().getUsed());
        systemInfo.setHeapMemoryMax(mem.getHeapMemoryUsage().getMax());
        systemInfo.setNoneHeapMemoryUsage(mem.getNonHeapMemoryUsage().getUsed());
        systemInfo.setNoneHeapMemoryMax(mem.getNonHeapMemoryUsage().getCommitted());
        if(mbean instanceof UnixOperatingSystemMXBean){
            systemInfo.setOpenFileDescriptorCount(((UnixOperatingSystemMXBean) mbean).getOpenFileDescriptorCount());
            systemInfo.setMaxFileDescriptorCount(((UnixOperatingSystemMXBean) mbean).getMaxFileDescriptorCount());
        }
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        systemInfo.setThreadCount(bean.getThreadCount());
        systemInfo.setJvmUpTime(ManagementFactory.getRuntimeMXBean().getUptime());
        return systemInfo;
    }
}
