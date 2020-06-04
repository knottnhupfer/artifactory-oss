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

package org.artifactory.descriptor.repo;

import org.artifactory.descriptor.Descriptor;
import org.jfrog.common.config.diff.GenerateDiffFunction;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

/**
 * @author Fred Simon
 */
@XmlType(name = "LocalRepoType",
        propOrder = {"snapshotVersionBehavior", "checksumPolicyType", "calculateYumMetadata", "yumRootDepth",
                "yumGroupFileNames", "debianTrivialLayout", "enableFileListsIndexing", "optionalIndexCompressionFormats"},
        namespace = Descriptor.NS)
@GenerateDiffFunction
public class LocalRepoDescriptor extends RealRepoDescriptor {

    @XmlElement(defaultValue = "unique", required = false)
    private SnapshotVersionBehavior snapshotVersionBehavior = SnapshotVersionBehavior.UNIQUE;

    @XmlElement(name = "localRepoChecksumPolicyType", defaultValue = "client-checksums", required = false)
    private LocalRepoChecksumPolicyType checksumPolicyType = LocalRepoChecksumPolicyType.CLIENT;

    private boolean calculateYumMetadata;

    private boolean enableFileListsIndexing;

    private int yumRootDepth;

    private String yumGroupFileNames;

    private boolean debianTrivialLayout = false;

    @XmlElementWrapper(name = "optionalIndexCompressionFormats")
    @XmlElement(name = "debianFormat", type = String.class)
    private List<String> optionalIndexCompressionFormats;

    public SnapshotVersionBehavior getSnapshotVersionBehavior() {
        return snapshotVersionBehavior;
    }

    public void setSnapshotVersionBehavior(SnapshotVersionBehavior snapshotVersionBehavior) {
        this.snapshotVersionBehavior = snapshotVersionBehavior;
    }

    @Override
    public boolean isLocal() {
        return true;
    }

    @Override
    public boolean isCache() {
        return false;
    }

    public LocalRepoChecksumPolicyType getChecksumPolicyType() {
        return checksumPolicyType;
    }

    public void setChecksumPolicyType(LocalRepoChecksumPolicyType checksumPolicyType) {
        this.checksumPolicyType = checksumPolicyType;
    }

    public boolean isCalculateYumMetadata() {
        return calculateYumMetadata;
    }

    public void setCalculateYumMetadata(boolean calculateYumMetadata) {
        this.calculateYumMetadata = calculateYumMetadata;
    }

    public boolean isEnableFileListsIndexing() {
        return enableFileListsIndexing;
    }

    public void setEnableFileListsIndexing(boolean enableFileListsIndexing) {
        this.enableFileListsIndexing = enableFileListsIndexing;
    }

    public int getYumRootDepth() {
        return yumRootDepth;
    }

    public void setYumRootDepth(int yumRootDepth) {
        this.yumRootDepth = yumRootDepth;
    }

    public String getYumGroupFileNames() {
        return yumGroupFileNames;
    }

    public void setYumGroupFileNames(String yumGroupFileNames) {
        this.yumGroupFileNames = yumGroupFileNames;
    }

    public boolean isDebianTrivialLayout() {
        return debianTrivialLayout;
    }

    public void setDebianTrivialLayout(boolean debianTrivialLayout) {
        this.debianTrivialLayout = debianTrivialLayout;
    }

    public void setOptionalIndexCompressionFormats(List<String> optionalIndexCompressionFormats) {
        this.optionalIndexCompressionFormats = optionalIndexCompressionFormats;
    }

    public List<String> getOptionalIndexCompressionFormats() {
        return this.optionalIndexCompressionFormats;
    }
}