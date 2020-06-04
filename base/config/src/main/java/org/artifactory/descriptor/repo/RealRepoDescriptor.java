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
import org.artifactory.descriptor.property.PropertySet;
import org.artifactory.util.AlreadyExistsException;
import org.artifactory.util.DoesNotExistException;
import org.jfrog.common.config.diff.DiffAtomic;
import org.jfrog.common.config.diff.DiffElement;
import org.jfrog.common.config.diff.DiffReference;
import org.jfrog.common.config.diff.GenerateDiffFunction;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Fred Simon
 */
@XmlType(name = "RealRepoType", propOrder = {"blackedOut", "handleReleases", "handleSnapshots", "maxUniqueSnapshots",
        "maxUniqueTags", "blockPushingSchema1", "suppressPomConsistencyChecks", "propertySets", "archiveBrowsingEnabled", "xrayConfig",
        "downloadRedirectConfig"},
        namespace = Descriptor.NS)
@GenerateDiffFunction
public abstract class RealRepoDescriptor extends RepoBaseDescriptor {

    @XmlElement(defaultValue = "false", required = false)
    private boolean blackedOut;

    @XmlElement(defaultValue = "true", required = false)
    private boolean handleReleases = true;

    @XmlElement(defaultValue = "true", required = false)
    private boolean handleSnapshots = true;

    @XmlElement(defaultValue = "0", required = false)
    private int maxUniqueSnapshots;

    @XmlElement(defaultValue = "0", required = false)
    private int maxUniqueTags;

    @XmlElement(defaultValue = "true", required = false)
    private boolean blockPushingSchema1 = true;

    @XmlElement(defaultValue = "true", required = false)
    private boolean suppressPomConsistencyChecks = true;

    @XmlElement(defaultValue = "false", required = false)
    private boolean archiveBrowsingEnabled;

    @XmlElement(name = "xray", required = false)
    @DiffElement(name = "xray")
    private XrayRepoConfig xrayConfig;

    @XmlElement(name = "downloadRedirect", required = false)
    @DiffElement(name = "downloadRedirect")
    private DownloadRedirectRepoConfig downloadRedirectConfig;

    @XmlIDREF
    @XmlElementWrapper(name = "propertySets")
    @XmlElement(name = "propertySetRef", type = PropertySet.class, required = false)
    @DiffAtomic
    @DiffReference
    private List<PropertySet> propertySets = new ArrayList<>();

    public boolean isHandleReleases() {
        return handleReleases;
    }

    public void setHandleReleases(boolean handleReleases) {
        this.handleReleases = handleReleases;
    }

    public boolean isHandleSnapshots() {
        return handleSnapshots;
    }

    public void setHandleSnapshots(boolean handleSnapshots) {
        this.handleSnapshots = handleSnapshots;
    }


    public boolean isBlackedOut() {
        return blackedOut;
    }

    public void setBlackedOut(boolean blackedOut) {
        this.blackedOut = blackedOut;
    }

    public int getMaxUniqueSnapshots() {
        return maxUniqueSnapshots;
    }

    public void setMaxUniqueSnapshots(int maxUniqueSnapshots) {
        this.maxUniqueSnapshots = maxUniqueSnapshots;
    }

    public int getMaxUniqueTags() {
        return maxUniqueTags;
    }

    public void setMaxUniqueTags(int maxUniqueTags) {
        this.maxUniqueTags = maxUniqueTags;
    }

    public boolean isBlockPushingSchema1() {
        return blockPushingSchema1;
    }

    public void setBlockPushingSchema1(boolean blockPushingSchema1) {
        this.blockPushingSchema1 = blockPushingSchema1;
    }

    public boolean isSuppressPomConsistencyChecks() {
        return suppressPomConsistencyChecks;
    }

    public void setSuppressPomConsistencyChecks(boolean suppressPomConsistencyChecks) {
        this.suppressPomConsistencyChecks = suppressPomConsistencyChecks;
    }

    public List<PropertySet> getPropertySets() {
        return propertySets;
    }

    public void setPropertySets(List<PropertySet> propertySets) {
        this.propertySets = Optional.ofNullable(propertySets).orElseGet(ArrayList::new);
    }

    public boolean isPropertySetExists(String propertySetName) {
        return getPropertySet(propertySetName) != null;
    }

    public void addPropertySet(PropertySet propertySet) {
        String propertySetName = propertySet.getName();
        if (isPropertySetExists(propertySetName)) {
            throw new AlreadyExistsException("Property set " + propertySetName + " already exists");
        }
        propertySets.add(propertySet);
    }

    public void updatePropertySet(PropertySet propertySet) {
        int index = propertySets.indexOf(propertySet);
        if (index == -1) {
            throw new DoesNotExistException("Property set " + propertySet.getName() + " does not exist");
        }
        propertySets.set(index, propertySet);
    }

    public PropertySet removePropertySet(String propertySetName) {
        PropertySet propertySet = getPropertySet(propertySetName);
        if (propertySet == null) {
            return null;
        }
        //Remove the property set from the property sets list
        while (propertySets.contains(propertySet)) {
            boolean removed = propertySets.remove(propertySet);
            //Sanity check
            if (!removed) {
                throw new RuntimeException("Property set: '" + propertySet.getName() + "' could be removed from " +
                        "repository: '" + getKey() + "'");
            }
        }
        return propertySet;
    }

    public PropertySet getPropertySet(String propertySetName) {
        for (PropertySet propertySet : propertySets) {
            if (propertySet.getName().equals(propertySetName)) {
                return propertySet;
            }
        }
        return null;
    }

    public boolean isArchiveBrowsingEnabled() {
        return archiveBrowsingEnabled;
    }

    public void setArchiveBrowsingEnabled(boolean archiveBrowsingEnabled) {
        this.archiveBrowsingEnabled = archiveBrowsingEnabled;
    }

    public boolean isXrayIndex() {
        return xrayConfig != null && xrayConfig.isEnabled();
    }

    public void setXrayIndex(boolean xrayIndex) {
        if (xrayConfig == null) {
            xrayConfig = new XrayRepoConfig();
        }
        xrayConfig.setEnabled(xrayIndex);
    }

    public XrayRepoConfig getXrayConfig() {
        return xrayConfig;
    }

    public void setXrayConfig(XrayRepoConfig xrayConfig) {
        this.xrayConfig = xrayConfig;
    }

    public boolean isDownloadRedirect() {
        return downloadRedirectConfig != null && downloadRedirectConfig.isEnabled();
    }

    public void setDownloadRedirect(boolean enableRedirect) {
        if (downloadRedirectConfig == null) {
            downloadRedirectConfig = new DownloadRedirectRepoConfig();
        }
        downloadRedirectConfig.setEnabled(enableRedirect);
    }

    public DownloadRedirectRepoConfig getDownloadRedirectConfig() {
        return downloadRedirectConfig;
    }

    public void setDownloadRedirectConfig(DownloadRedirectRepoConfig downloadRedirect) {
        this.downloadRedirectConfig = downloadRedirect;
    }
    @Override
    public boolean isReal() {
        return true;
    }

    public abstract boolean isLocal();

    public abstract boolean isCache();
}