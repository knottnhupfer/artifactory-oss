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

package org.artifactory.features;

import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.artifactory.api.config.VersionInfo;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.version.ArtifactoryVersion;
import org.artifactory.version.ArtifactoryVersionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;



/**
 * Version features descriptor
 *
 * @author Michael Pasternak
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class VersionFeatures {

    transient private static final Logger log = LoggerFactory.getLogger(VersionFeatures.class);

    transient public static final VersionInfo v4_1 = new VersionInfo("4.1");
    transient public static final VersionInfo v4_2 = new VersionInfo("4.2");
    transient public static final String FEATURES_CONFIG_PATH = "/features/";

    @XmlElementWrapper(name = "features")
    @XmlElement(name = "feature", required = false)
    private List<VersionFeature> features;

    /**
     * serialization .ctr
     */
    protected VersionFeatures() {
        features = new LinkedList<>();
    }

    /**
     * Builds {@link VersionFeatures}
     *
     * @param versionFeatures a collection of {@link VersionFeature}
     */
    public VersionFeatures(VersionFeature... versionFeatures) {
        this();
        for(VersionFeature versionFeature : versionFeatures) {
            features.add(versionFeature);
        }
    }

    /**
     * Builds master VersionFeatures container by
     * merging other {@link VersionFeatures} into this VersionFeatures
     *
     * @param versionFeatures a collection of {@link VersionFeatures}
     */
    public VersionFeatures(VersionFeatures... versionFeatures) {
        this();
        for(VersionFeatures versionFeature : versionFeatures) {
            features.addAll(versionFeature.getFeatures());
        }
    }

    /**
     * Loads {@link VersionFeatures} from the resource
     *
     * @param configPath a path to resource with {@link VersionFeatures}
     */
    public VersionFeatures(String configPath) {
        this();
        VersionFeatures resourceConfig = getResourceConfig(configPath);
        if (resourceConfig != null) {
            List<VersionFeature> features = resourceConfig.getFeatures();
            if (features != null) {
                this.features.addAll(features);
            } else {
                log.debug("No features were loaded from \"{}\"", configPath);
            }
        } else {
            log.debug("No resourceConfig were loaded from \"{}\"", configPath);
        }
    }

    /**
     * Returns features available for the given version
     *
     * @param version {@link VersionInfo} to check from (inclusive)
     *
     * @return List<VersionFeature>
     */
    public List<VersionFeature> getFeaturesByVersion(VersionInfo version) {
        ArtifactoryVersion artifactoryVersion;
        if (version == null) {
            log.error("Artifactory version was not provided or cannot be parsed");
            return Lists.newLinkedList();
        }
        if ((artifactoryVersion = ArtifactoryVersionProvider.get(version.getVersion(), Long.parseLong(version.getRevision()))) == null) {
            log.debug("Target Artifactory version cannot be parsed, assuming that the target server version {} is newer than the current version.", version.getVersion());
            artifactoryVersion = ArtifactoryVersion.getCurrent();
        }

        ArtifactoryVersion finalArtifactoryVersion = artifactoryVersion;
        return features.stream()
                .filter(feature -> isFeatureAvailableInVersion(finalArtifactoryVersion, feature))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Compares a feature version (derived from smartrepo-features.xml) with Artifactory version.
     *
     * @param artifactoryVersion The version we wish to check feature availability in
     * @param feature The feature we compare with a specific version
     * @return TRUE if the version is the same or greater than the feature version
     * FALSE if one of the versions cannot be converted to ArtifactoryVersion enum, or if the feature version is newer
     */
    private boolean isFeatureAvailableInVersion(ArtifactoryVersion artifactoryVersion, VersionFeature feature) {
        ArtifactoryVersion featureVersion = ArtifactoryVersionProvider.get(feature.getAvailableFrom().getVersion(), Long.parseLong(feature.getAvailableFrom().getRevision()));
        if (featureVersion == null) {
            log.error("Feature '{}' version '{}' cannot be parsed", feature.getName(), feature.getAvailableFrom());
            return false;
        }
        log.debug("Comparing feature {} in version {} with Artifactory version {}",
                feature.getName(), featureVersion, artifactoryVersion);
        return featureVersion.beforeOrEqual(artifactoryVersion);
    }

    /**
     * @return all {@link VersionFeature} held by this container
     */
    public List<VersionFeature> getFeatures() {
        return features;
    }

    /**
     * Fetches features resource config
     *
     * @param configStream a stream from config resource
     *
     * @return {@link VersionFeatures} implementator
     */
    private VersionFeatures getResourceConfig(InputStream configStream) {
        VersionFeatures versionFeatures = null;
        JAXBContext jaxbContext = null;
        Unmarshaller jaxbUnmarshaller = null;
        String errorMsg = "Loading VersionFeatures has failed";
        try {
            jaxbContext = JAXBContext.newInstance(VersionFeatures.class);
            jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            versionFeatures = (VersionFeatures) jaxbUnmarshaller.unmarshal(configStream);
        } catch (JAXBException e) {
            log.error(errorMsg, e);
        } finally {
            IOUtils.closeQuietly(configStream);
        }
        if (versionFeatures == null) {
            log.error(errorMsg);
        }
        return versionFeatures;
    }

    /**
     * Fetches features resource config
     *
     * @param configPath the name of specific "features" config resource
     *
     * @return {@link VersionFeatures} implementator
     */
    private VersionFeatures getResourceConfig(String configPath) {
        String errorMsg = "Cannot read VersionFeatures config \"" + configPath + "\"";
        log.debug("Loading SmartRepoVersionFeatures from \"{}\"", configPath);
        try {
            URL resource = ArtifactoryHome.class.getResource(FEATURES_CONFIG_PATH + configPath);
            if (resource != null) {
                InputStream configStream = resource.openStream();
                return getResourceConfig(configStream);
            } else {
                throw new IllegalStateException(errorMsg);
            }
        } catch (IOException e) {
            log.error(errorMsg, e);
            throw new IllegalStateException(errorMsg, e);
        }
    }
}
