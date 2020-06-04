/*
 *
 * Copyright 2016 JFrog Ltd. All rights reserved.
 * JFROG PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package org.artifactory.work.queue.mbean.debian;

import org.artifactory.work.queue.mbean.WorkQueueMBean;

/**
 * @author Nadav Yogev
 */
public class DebianCacheCoordinatesWorkQueue implements DebianCacheCoordinatesWorkQueueMBean {
    private WorkQueueMBean workQueueMBean;

    public DebianCacheCoordinatesWorkQueue(WorkQueueMBean workQueueMBean) {
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
