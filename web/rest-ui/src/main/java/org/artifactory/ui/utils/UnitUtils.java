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

package org.artifactory.ui.utils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.model.Model;
import org.artifactory.api.artifact.*;
import org.artifactory.api.maven.MavenArtifactInfo;
import org.artifactory.api.maven.MavenService;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.maven.MavenModelUtils;
import org.artifactory.mime.NamingUtils;
import org.artifactory.ui.rest.model.artifacts.deploy.UploadArtifactInfo;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.jfrog.client.util.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * @author Chen Keinan
 */
public class UnitUtils {

    private static final Logger log = LoggerFactory.getLogger(UnitUtils.class);

    /**
     * get maven artifact info from jar
     *
     * @param pomFromJar - pom content
     * @param file  - file
     * @return maven artifact info
     */
    public static UnitInfo getMavenArtifactInfo(File file, String pomFromJar) {
        if (pomFromJar == null) {
            if (file.getName().endsWith(".jar")) {
                return MavenModelUtils.artifactInfoFromFile(file);
            } else {
                return null;
            }
        }
        MavenArtifactInfo mavenArtifactInfo = null;
        if (StringUtils.isNotBlank(pomFromJar)) {
            InputStream pomInputStream = IOUtils.toInputStream(pomFromJar);
            try {
                mavenArtifactInfo = MavenModelUtils.mavenModelToArtifactInfo(pomInputStream);
            } catch (IOException | XmlPullParserException e) {
                log.error(e.toString());
            }
            if (mavenArtifactInfo != null) {
                mavenArtifactInfo.setType(PathUtils.getExtension(file.getName()));
                return mavenArtifactInfo;
            }
        }
        return null;
    }

    public static String getPomContent(File file, MavenArtifactInfo mavenArtifactInfo) {
        String pom = MavenModelUtils.getPomFileAsStringFromJar(file);
        if(StringUtils.isBlank(pom)){
            Model mavenModel = MavenModelUtils.toMavenModel(mavenArtifactInfo);
            pom = MavenModelUtils.mavenModelToString(mavenModel);
        }

        return pom;
    }

    /**
     * return unit info , 1st check if maven artifact info , if not return basic Artifact info
     *
     * @param file - current file
     * @param uploadArtifactInfo -
     * @param mavenService -
     * @return artifact info
     */
    public static UploadArtifactInfo getUnitInfo(File file, UploadArtifactInfo uploadArtifactInfo,
                                                 MavenService mavenService, RepoType repoType) {
        UnitInfo artifactInfo = null;
        uploadArtifactInfo.cleanData();
        if (file.getName().endsWith(".pom")) {
            MavenArtifactInfo pomInfo = getArtifactInfoFromPom(file, true);
            String pomFromPom = UnitUtils.getPomContent(file, pomInfo);
            uploadArtifactInfo.setUnitConfigFileContent(pomFromPom);
            artifactInfo = pomInfo;
        }
        if (file.getName().endsWith(".deb")) {
            artifactInfo = new DebianArtifactInfo(file.getName());
        }
        if (file.getName().endsWith(".box")) {
            artifactInfo = new VagrantArtifactInfo(file.getName());
        }
        //if Maven
        if (NamingUtils.isJarVariant(file.getName())) {
            MavenArtifactInfo mavenArtifactInfo = mavenService.getMavenArtifactInfo(file);
            String pomFromJar = UnitUtils.getPomContent(file, mavenArtifactInfo);
            uploadArtifactInfo.setUnitConfigFileContent(pomFromJar);
            artifactInfo = getMavenArtifactInfo(file, pomFromJar);
        }
        if (RepoType.CRAN.equals(repoType)) {
            artifactInfo = new CranUnitInfo();
        }
        if(artifactInfo == null){
            artifactInfo = new ArtifactInfo(file.getName());
        }

        uploadArtifactInfo.setUnitInfo(artifactInfo);

        return uploadArtifactInfo;
    }

    /**
     * get artifact info from pom
     *
     * @param file - current file
     * @return artifact info
     */
    private static MavenArtifactInfo getArtifactInfoFromPom(File file, boolean isPom) {
        MavenArtifactInfo mavenArtifactInfo = null;
        try {
            InputStream pomInputStream = new FileInputStream(file);
            mavenArtifactInfo = MavenModelUtils.mavenModelToArtifactInfo(pomInputStream);
            if (isPom) {
                mavenArtifactInfo.setType("pom");
            }
        } catch (FileNotFoundException | XmlPullParserException e) {
            log.error(e.toString());
        } catch (IOException e) {
            log.error(e.toString());
        }
        return mavenArtifactInfo;
    }
}
