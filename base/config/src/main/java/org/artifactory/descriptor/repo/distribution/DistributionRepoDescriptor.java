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

package org.artifactory.descriptor.repo.distribution;

import com.google.common.collect.Lists;
import org.artifactory.descriptor.Descriptor;
import org.artifactory.descriptor.repo.BintrayApplicationConfig;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.ProxyDescriptor;
import org.artifactory.descriptor.repo.distribution.rule.DistributionRule;
import org.jfrog.common.config.diff.DiffAtomic;
import org.jfrog.common.config.diff.DiffReference;
import org.jfrog.common.config.diff.GenerateDiffFunction;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlType;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Dan Feldman
 */
@XmlType(name = "DistributionRepoType",
        propOrder = {"bintrayApplication", "rules", "proxy", "productName", "defaultNewRepoPrivate",
                "defaultNewRepoPremium", "defaultLicenses", "defaultVcsUrl", "whiteListedProperties",
                "gpgSign", "gpgPassPhrase"},
        namespace = Descriptor.NS)
@GenerateDiffFunction
public class DistributionRepoDescriptor extends LocalRepoDescriptor {

    @XmlIDREF
    @XmlElement(name = "bintrayApplicationRef")
    @DiffReference
    private BintrayApplicationConfig bintrayApplication;

    @XmlElement(name = "rule")
    @XmlElementWrapper(name = "rules")
    @DiffAtomic
    private List<DistributionRule> rules = Lists.newArrayList();

    @XmlIDREF
    @XmlElement(name = "proxyRef")
    @DiffReference
    private ProxyDescriptor proxy;

    @XmlElement
    private String productName;

    @XmlElement
    private boolean defaultNewRepoPrivate = true;

    @XmlElement
    private boolean defaultNewRepoPremium = true;

    @XmlElement(name = "license")
    @XmlElementWrapper(name = "defaultLicenses")
    private Set<String> defaultLicenses = new HashSet<>();

    @XmlElement
    private String defaultVcsUrl;

    @XmlElement(name = "property")
    @XmlElementWrapper(name = "whiteListedProperties")
    private Set<String> whiteListedProperties = new HashSet<>();

    @XmlElement
    private boolean gpgSign;

    @XmlElement
    private String gpgPassPhrase;

    @Override
    public boolean isHandleReleases() {
        return true;
    }

    @Override
    public void setHandleReleases(boolean handleReleases) {
        // nop
    }

    @Override
    public boolean isHandleSnapshots() {
        return false;
    }

    @Override
    public void setHandleSnapshots(boolean handleSnapshots) {
        // nop
    }

    public BintrayApplicationConfig getBintrayApplication() {
        return bintrayApplication;
    }

    public void setBintrayApplication(BintrayApplicationConfig bintrayApplication) {
        this.bintrayApplication = bintrayApplication;
    }

    public List<DistributionRule> getRules() {
        return rules;
    }

    public void setRules(List<DistributionRule> rules) {
        this.rules = rules;
    }

    public ProxyDescriptor getProxy() {
        return proxy;
    }

    public void setProxy(ProxyDescriptor proxy) {
        this.proxy = proxy;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public boolean isDefaultNewRepoPrivate() {
        return defaultNewRepoPrivate;
    }

    public void setDefaultNewRepoPrivate(boolean defaultNewRepoPrivate) {
        this.defaultNewRepoPrivate = defaultNewRepoPrivate;
    }

    public boolean isDefaultNewRepoPremium() {
        return defaultNewRepoPremium;
    }

    public void setDefaultNewRepoPremium(boolean defaultNewRepoPremium) {
        this.defaultNewRepoPremium = defaultNewRepoPremium;
    }

    public Set<String> getDefaultLicenses() {
        return defaultLicenses;
    }

    public void setDefaultLicenses(Set<String> defaultLicenses) {
        this.defaultLicenses = defaultLicenses;
    }


    public String getDefaultVcsUrl() {
        return defaultVcsUrl;
    }

    public void setDefaultVcsUrl(String defaultVcsUrl) {
        this.defaultVcsUrl = defaultVcsUrl;
    }

    public Set<String> getWhiteListedProperties() {
        return whiteListedProperties;
    }

    public void setWhiteListedProperties(Set<String> whiteListedProperties) {
        this.whiteListedProperties = whiteListedProperties;
    }

    public boolean isGpgSign() {
        return gpgSign;
    }

    public void setGpgSign(boolean gpgSign) {
        this.gpgSign = gpgSign;
    }

    public String getGpgPassPhrase() {
        return gpgPassPhrase;
    }

    public void setGpgPassPhrase(String gpgPassPhrase) {
        this.gpgPassPhrase = gpgPassPhrase;
    }

}
