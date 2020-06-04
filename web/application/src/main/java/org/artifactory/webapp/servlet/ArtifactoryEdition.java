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

package org.artifactory.webapp.servlet;

import org.artifactory.common.ArtifactoryHome;


/**
 * Enum of the various Artifactory editions.
 *
 * @author Yossi Shaul
 */
public enum ArtifactoryEdition {
    oss, conanCE, pro, aol, aolJCR, ha, jcr;

    public static ArtifactoryEdition detect() {
        return detect(null);
    }

    /**
     * Detects the edition based on configuration and available add-on implementations.
     *
     * @param artifactoryHome Running Artifactory home
     * @return The running Artifactory edition
     */
    public static ArtifactoryEdition detect(ArtifactoryHome artifactoryHome) {

        if (isJcrAolEdition()) {
            return aolJCR;
        }
        if (isAolEdition()) {
            return aol;
        }
        if (artifactoryHome != null && artifactoryHome.isHaConfigured() && classExists("org.artifactory.addon.ha.HaAddonImpl")) {
            return ha;
        }
        if (classExists("org.artifactory.addon.LicenseProvider")) {
            return pro;
        }
        if (classExists("org.artifactory.addon.conan.rest.ConanResource")) {
            return conanCE;
        }
        if (isJcrEdition()) {
            return jcr;
        }
        return oss;
    }

    private static boolean isAolEdition() {
        return classExists("org.artifactory.addon.aol.webapp.AolCoreAddons");
    }

    public static boolean isJcrAolEdition() {
        return isAolEdition() && isJcrEdition();
    }

    public static boolean isJcrEdition() {
        return classExists("org.artifactory.addon.jcr.JcrRepoValidationInterceptor") &&
                classExists("org.artifactory.addon.docker.rest.DockerResource")
                && classExists("org.artifactory.addon.helm.rest.HelmResource");
    }

    public static boolean classExists(String clazz) {
        try {
            Class.forName(clazz);
            return true;
        } catch (Exception e) {
            return false;
        }
    }


}
