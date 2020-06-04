/*
 *
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2018 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.artifactory.ui.rest.model.admin.advanced.systemlogs;

import org.artifactory.rest.common.model.BaseModel;

import java.util.Date;

/**
 * @author Lior Hasson
 */
public class SystemLogData extends BaseModel {
    private Date lastUpdateModified;
    private Date lastUpdateLabel;
    private String logContent;
    private long fileSize;

    public long getFileSize() {
        return fileSize;
    }

    public Date getLastUpdateModified() {
        return lastUpdateModified;
    }

    public Date getLastUpdateLabel() {
        return lastUpdateLabel;
    }

    public String getLogContent() {
        return logContent;
    }

    public void setLastUpdateModified(Date lastUpdateModified) {
        this.lastUpdateModified = lastUpdateModified;
    }

    public void setLastUpdateLabel(Date lastUpdateLabel) {
        this.lastUpdateLabel = lastUpdateLabel;
    }

    public void setLogContent(String logContent) {
        this.logContent = logContent;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
}
