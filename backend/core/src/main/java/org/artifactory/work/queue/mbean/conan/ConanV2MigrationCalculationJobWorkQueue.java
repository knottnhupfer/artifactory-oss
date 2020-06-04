package org.artifactory.work.queue.mbean.conan;

import org.artifactory.work.queue.mbean.WorkQueueMBean;

/**
 * @author Yuval Reches
 */
public class ConanV2MigrationCalculationJobWorkQueue implements ConanV2MigrationCalculationJobWorkQueueMBean {
    private WorkQueueMBean workQueueMBean;

    public ConanV2MigrationCalculationJobWorkQueue(WorkQueueMBean workQueueMBean) {
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
