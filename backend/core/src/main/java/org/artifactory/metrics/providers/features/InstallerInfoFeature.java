/*
 *
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2019 JFrog Ltd.
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

package org.artifactory.metrics.providers.features;

import org.artifactory.api.callhome.FeatureGroup;
import org.artifactory.api.jackson.JacksonReader;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.metrics.model.FeatureUsage;
import org.artifactory.metrics.model.IntegrationsProductUsage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * This class represent the repositories feature group of the CallHome feature
 * @author shivaramr
 */
@Component
public class InstallerInfoFeature implements CallHomeFeature {
    private static final Logger log = LoggerFactory.getLogger(InstallerInfoFeature.class);

    @Override
    public FeatureGroup getFeature() {
        File etcDir = ArtifactoryHome.get().getEtcDir();
        String installerInfoFile = etcDir + "/info/installer-info.json";


        FeatureGroup installerInfoFeature = null;
        IntegrationsProductUsage installerInfoProduct = null;
        try (FileInputStream in = new FileInputStream(new File(installerInfoFile))) {
            installerInfoProduct = JacksonReader.streamAsClass(in, IntegrationsProductUsage.class);
        } catch (IOException e) {
            log.debug("Failed parsing installer json: ", e);
        }

        if (installerInfoProduct != null) {
            String productId = installerInfoProduct.getProductId();
            if (productId != null && !productId.isEmpty()) {
                List<FeatureUsage> featureUsageList = installerInfoProduct.getFeatures();

                if (featureUsageList == null || featureUsageList.isEmpty()) {
                    return null;
                }
                FeatureGroup currentFeature = new FeatureGroup(productId);
                for (final FeatureUsage featureUsage : featureUsageList) {
                    FeatureGroup installer = new FeatureGroup(featureUsage.getFeatureId());
                    installer.setFeatureAttribute(featureUsage.getAttributes());
                    currentFeature.addFeature(installer);
                }

                installerInfoFeature = new FeatureGroup("Installer");

                installerInfoFeature.addFeature(currentFeature);
            }
        }

        return installerInfoFeature;
    }
}
