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
/*
 * Additional contributors:
 *    JFrog Ltd.
 */

package org.artifactory.maven.index.creator;

import com.google.common.base.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.maven.index.ArtifactContext;
import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.creator.MavenPluginArtifactInfoIndexCreator;
import org.artifactory.storage.fs.tree.file.JavaIOFileAdapter;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.PlexusConfigurationException;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author yoavl
 */
public class VfsMavenPluginArtifactInfoIndexCreator extends MavenPluginArtifactInfoIndexCreator {
    private static final Logger log = LoggerFactory.getLogger(VfsMavenPluginArtifactInfoIndexCreator.class);

    private static final String MAVEN_PLUGIN_PACKAGING = "maven-plugin";

    @Override
    public void populateArtifactInfo(ArtifactContext ac) {
        File artifact = ac.getArtifact();

        ArtifactInfo ai = ac.getArtifactInfo();

        // we need the file to perform these checks, and those may be only JARs
        if (artifact != null && MAVEN_PLUGIN_PACKAGING.equals(ai.packaging) && artifact.getName().endsWith(".jar")) {
            // TODO: recheck, is the following true? "Maven plugins and Maven Archetypes can be only JARs?"

            // 1st, check for maven plugin
            checkMavenPlugin(ai, (JavaIOFileAdapter) artifact);
        }
    }

    private void checkMavenPlugin(ArtifactInfo ai, JavaIOFileAdapter artifact) {
        ZipInputStream zis = null;
        InputStream is = null;
        try {
            is = artifact.getStream();
            zis = new ZipInputStream(is);
            ZipEntry currentEntry;
            while ((currentEntry = zis.getNextEntry()) != null) {
                if (currentEntry.getName().equals("META-INF/maven/plugin.xml")) {
                    parsePluginDetails(ai, zis);
                    break;
                }
            }
        } catch (Exception e) {
            log.info("Failed to parsing Maven plugin " + artifact.getAbsolutePath(), e.getMessage());
            log.debug("Failed to parsing Maven plugin " + artifact.getAbsolutePath(), e);
        } finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(zis);
        }
    }

    private void parsePluginDetails(ArtifactInfo ai, ZipInputStream zis)
            throws XmlPullParserException, IOException, PlexusConfigurationException {
        PlexusConfiguration plexusConfig =
                new XmlPlexusConfiguration(Xpp3DomBuilder.build(new InputStreamReader(zis, Charsets.UTF_8)));

        ai.prefix = plexusConfig.getChild("goalPrefix").getValue();
        ai.goals = new ArrayList<>();
        PlexusConfiguration[] mojoConfigs = plexusConfig.getChild("mojos").getChildren("mojo");
        for (PlexusConfiguration mojoConfig : mojoConfigs) {
            ai.goals.add(mojoConfig.getChild("goal").getValue());
        }
    }
}