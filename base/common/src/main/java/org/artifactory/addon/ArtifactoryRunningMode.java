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

package org.artifactory.addon;

/**
 * @author gidis
 * Represent the actualArtifactory mode which can be one of the following OSS,PRO,HA,AOL
 */
public enum ArtifactoryRunningMode {
    OSS, PRO, HA, CONAN, JCR, AOL_JCR;

    public static ArtifactoryRunningMode fromString(String val) {
        // Artifactory always have its running mode so throw exception if fail to find proper enum.
        return valueOf(val.toUpperCase());
    }

    public boolean isHa() {
        return HA == this;
    }

    public boolean isOss() {
        return OSS == this;
    }

    public boolean isConan() {
        return CONAN == this;
    }

    public boolean isJCR() {
        return JCR == this;
    }

    public boolean isJcrAol() {
        return AOL_JCR == this;
    }

    public boolean isJcrOrJcrAol() {
        return isJCR() || isJcrAol();
    }

    public boolean isPro() {
        return PRO == this;
    }

    public static boolean sameMode(ArtifactoryRunningMode... runningModes) {
        ArtifactoryRunningMode last = runningModes[0];
        for (ArtifactoryRunningMode runningMode : runningModes) {
            if (runningMode != last) {
                return false;
            }
        }
        return true;
    }
}
