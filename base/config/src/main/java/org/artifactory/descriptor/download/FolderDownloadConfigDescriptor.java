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

package org.artifactory.descriptor.download;

import org.artifactory.descriptor.Descriptor;
import org.jfrog.common.config.diff.GenerateDiffFunction;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Descriptor for the system message
 *
 * @author Dan Feldman
 */
@XmlType(name = "FolderDownloadConfigType", propOrder = {"enabled", "enabledForAnonymous", "maxDownloadSizeMb", "maxFiles",
        "maxConcurrentRequests"}, namespace = Descriptor.NS)
@GenerateDiffFunction
public class FolderDownloadConfigDescriptor implements Descriptor {

    @XmlElement
    private boolean enabled = false;

    @XmlElement
    private boolean enabledForAnonymous = false;

    @XmlElement
    private int maxDownloadSizeMb = 1024; //1GB

    @XmlElement
    private long maxFiles = 5000;

    @XmlElement
    private int maxConcurrentRequests = 10;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabledForAnonymous() {
        return enabledForAnonymous;
    }

    public void setEnabledForAnonymous(boolean enabledForAnonymous) {
        this.enabledForAnonymous = enabledForAnonymous;
    }

    public int getMaxDownloadSizeMb() {
        return maxDownloadSizeMb;
    }

    public void setMaxDownloadSizeMb(int maxDownloadSizeMb) {
        this.maxDownloadSizeMb = maxDownloadSizeMb;
    }

    public long getMaxFiles() {
        return maxFiles;
    }

    public void setMaxFiles(long maxFiles) {
        this.maxFiles = maxFiles;
    }

    public int getMaxConcurrentRequests() {
        return maxConcurrentRequests;
    }

    public void setMaxConcurrentRequests(int maxConcurrentRequests) {
        this.maxConcurrentRequests = maxConcurrentRequests;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FolderDownloadConfigDescriptor)) {
            return false;
        }

        FolderDownloadConfigDescriptor that = (FolderDownloadConfigDescriptor) o;

        if (isEnabled() != that.isEnabled()) {
            return false;
        }
        if (isEnabledForAnonymous() != that.isEnabledForAnonymous()) {
            return false;
        }
        if (getMaxDownloadSizeMb() != that.getMaxDownloadSizeMb()) {
            return false;
        }
        if (getMaxFiles() != that.getMaxFiles()) {
            return false;
        }
        return getMaxConcurrentRequests() == that.getMaxConcurrentRequests();

    }

    @Override
    public int hashCode() {
        int result = (isEnabled() ? 1 : 0);
        result = 31 * result + getMaxDownloadSizeMb();
        result = 31 * result + (int) (getMaxFiles() ^ (getMaxFiles() >>> 32));
        result = 31 * result + getMaxConcurrentRequests();
        return result;
    }
}
