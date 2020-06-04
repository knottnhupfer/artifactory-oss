package org.artifactory.work.queue.mbean.debian;

import org.artifactory.work.queue.mbean.WorkQueueMBean;

/**
 * @author dudim
 */
public class DebianVirtualWorkQueue implements DebianVirtualWorkQueueMBean {
    private WorkQueueMBean workQueueMBean;

    public DebianVirtualWorkQueue(WorkQueueMBean workQueueMBean) {
        this.workQueueMBean = workQueueMBean;
    }

    @Override
    public int getQueueSize() {
        return workQueueMBean.getQueueSize();
    }

    @Override
    public int getNumberOfWorkers() {
        return workQueueMBean.getNumberOfWorkers();
    }

    @Override
    public int getMaxNumberOfWorkers() {
        return workQueueMBean.getMaxNumberOfWorkers();
    }

    @Override
    public String getName() {
        return workQueueMBean.getName();
    }
}
