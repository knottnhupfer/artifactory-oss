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

package org.artifactory.webapp;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang.SystemUtils;
import org.apache.tomcat.util.descriptor.web.WebXml;
import org.apache.tomcat.util.descriptor.web.WebXmlParser;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.jfrog.client.util.PathUtils;
import org.jfrog.common.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

import java.io.*;
import java.nio.charset.Charset;

/**
 * @author Fred Simon on 9/12/16.
 */
public abstract class WebappUtils {

    private static final String MASTER_KEY = "0c1a1554553d487466687b339cd85f3d";

    private static final Logger log = LoggerFactory.getLogger(WebappUtils.class);

    private static File[] rootLocations;
    private static final String WEB_INF_LOCATION = "WEB-INF" + File.separator + "lib";

    static {
        File currentFolder = new File(".").getAbsoluteFile();
        File userDir = SystemUtils.getUserDir().getAbsoluteFile();
        rootLocations = new File[]{
                currentFolder,
                currentFolder.getParentFile(),
                currentFolder.getParentFile().getParentFile(),
                currentFolder.getParentFile().getParentFile().getParentFile(),
                currentFolder.getParentFile().getParentFile().getParentFile().getParentFile(),
                userDir,
                userDir.getParentFile(),
                // TODO: [by fsi] use good naming standards like ~/projects to look into
                new File(userDir, "projects")
        };
    }

    public static File getArtifactoryDevenv() {
        String[] folderNames = new String[]{"devenv", "artifactory-devenv"};

        return find("Artifactory Devenv root", folderNames);
    }

    public static File getWebappRoot(File artHome, boolean integTest, boolean addArtifactoryHomeParam) throws IOException {
        // Find out if Pro or OSS version by using classpath contains Pro license manager
        boolean isPro = ResourceUtils.resourceExists("/org/artifactory/addon/LicenseProvider.class");
        boolean isJenkins = System.getenv("BUILD_NUMBER") != null;

        String toFindDescription;
        String[] folderNames;
        if (!integTest) {
            // Use only source code for UI dev
            toFindDescription = "Webapp Source Code root";
            folderNames = new String[]{
                    "artifactory-oss/web/war/src/main/webapp"
            };
        } else if (isPro) {
            toFindDescription = "Webapp target root";
            folderNames = new String[]{
                    "artifactory-pro/pro/war/target/artifactory",
                    "pro/war/target/artifactory",
                    "itest-pro/target/webapp",
                    "itest-online/target/webapp",
                    "webapp",
            };
        } else {
            toFindDescription = "Webapp Source OSS root";
            folderNames = new String[]{
                    "artifactory-oss/web/war/src/main/webapp",
                    "web/war/src/main/webapp",
                    "itest/target/webapp",
                    "war/target/artifactory-conan-ce"
            };
        }
        File targetWebappRoot = find(toFindDescription, folderNames);
        // Copy the correct web.xml dev-env file only if not running in Jenkins (there the one in target is used)
        //if (!isJenkins) {
            File webXmlFile;
            if (isPro) {
                webXmlFile = find("Pro web.xml file", new String[]{
                        "pro/war/src/main/webapp/WEB-INF/web.xml",
                        "artifactory-pro/pro/war/src/main/webapp/WEB-INF/web.xml",
                        "itest-pro/target/webapp/WEB-INF/web.xml"
                });
            } else {
                webXmlFile = find("OSS web.xml file", new String[]{
                        "web/war/src/main/webconf/WEB-INF/web.xml",
                        "artifactory-oss/web/war/src/main/webconf/WEB-INF/web.xml",
                        "itest/target/webapp/WEB-INF/web.xml",
                        "war/src/main/webapp/WEB-INF/web.xml"
                });
            }
            updateWebXml(artHome, addArtifactoryHomeParam, targetWebappRoot, webXmlFile);
        //}

        if (integTest) {
            if (artHome == null) {
                throw new IllegalArgumentException(
                        "Cannot create webapp folder in unit test without an artifactory home folder");
            }
            File webapp = new File(artHome, "webapp");
            IOFileFilter fileFilter = new WebappCopyFileFilter(targetWebappRoot, webapp);
            fileFilter = FileFilterUtils.makeSVNAware(fileFilter);
            FileUtils.copyDirectory(targetWebappRoot, webapp, fileFilter, true);
            //File origWebXml = new File(webapp, "WEB-INF/web.xml");
            //updateWebXml(artHome, addArtifactoryHomeParam, webapp, origWebXml);
            return webapp;
        }

        return targetWebappRoot;
    }

    private static void updateWebXml(File artHome, boolean addArtifactoryHomeParam, File targetWebappRoot,
            File webXmlFile) throws IOException {
        File destFile = new File(targetWebappRoot, "WEB-INF/web.xml");
        if (addArtifactoryHomeParam) {
            customizeWebXml(webXmlFile, destFile, artHome);
        } else {
            if (!webXmlFile.equals(destFile)) {
                FileUtils.copyFile(webXmlFile, destFile);
            }
        }
    }

    private static File customizeWebXml(File origWebXmlFile, File destWebXmlFile, File artHome) throws IOException {
        WebXmlParser parser = new WebXmlParser(false, false, true);
        WebXml webXml = new WebXml();
        InputStream in = new FileInputStream(origWebXmlFile);
        InputSource inputSource = new InputSource(new InputStreamReader(in));
        parser.parseWebXml(inputSource, webXml, false);
        webXml.addContextParam(ArtifactoryHome.SYS_PROP, artHome.getAbsolutePath());
        webXml.addContextParam("logbackRefreshInterval", "50");    // shorten the log refresh interval for testing
        String webXmlString = webXml.toXml();
        FileUtils.write(destWebXmlFile, webXmlString, Charset.forName("UTF-8"));
        return destWebXmlFile;
    }

    public static File getDevEtcFolder() {
        String[] folderNames = new String[]{
                "artifactory-oss/distribution/standalone/zip/src/test/resources/dev/etc",
                "distribution/standalone/zip/src/test/resources/dev/etc"
        };
        return find("Dev etc folder", folderNames);
    }

    public static File getAccessStandaloneJar() {
        String[] folderNames = new String[] {
                "access/server/application/target/access-application-4.14.x-SNAPSHOT-standalone.jar",
                "target/access-application-standalone.jar",
                "artifactory-pro/itest/target/access-application-standalone.jar",
                "artifactory-pro/itest-pro/target/access-application-standalone.jar"
        };
        // TODO: If not found please hint to run mvn -Pitestalone on itest module
        return find("Access Standalone Jar", folderNames);
    }

    public static File getMetadataApiExecutable(boolean macOS) {
        String[] filePaths;
        String os = macOS ? "darwin" : "linux";
        filePaths = new String[]{
                "artifactory-pro/itest/target/test-resources/metadata/bin/metadata-" + os + "-amd64",
                "artifactory-pro/itest-pro/target/test-resources/metadata/bin/metadata-" + os + "-amd64",
                "target/test-resources/metadata/bin/metadata-" + os + "-amd64"
        };
        return find("Metadata API Executable", filePaths);
    }

    public static File getRouterExecutable(boolean macOS) {
        String[] filePaths;
        String os = macOS ? "darwin" : "linux";
        filePaths = new String[]{
                "artifactory-pro/itest/target/router/bin/jf-router-" + os + "-amd64",
                "artifactory-pro/itest-pro/target/router/bin/jf-router-" + os + "-amd64",
                "target/router-extract/router/bin/jf-router-" + os + "-amd64",
                "artifactory-oss/distribution/standalone/base/target/router-extract/router/bin/jf-router-" + os + "-amd64"
        };
        return find("Router API Executable", filePaths);
    }

    public static File getTomcatResources() {
        String[] folderNames = new String[]{"artifactory-oss/distribution/standalone/zip/src/main/install/misc/tomcat"};
        return find("Tomcat resources folder", folderNames);
    }

    private static File find(String toFind, String[] folderNames) {
        for (String folderName : folderNames) {
            for (File rootLocation : rootLocations) {
                File file = new File(rootLocation, folderName);
                if (file.exists()) {
                    log.info("Found {} under {}", toFind, file.getAbsolutePath());
                    return file;
                }
            }
        }

        StringBuilder lookedInto = new StringBuilder("Trying to find " + toFind + " under:\n");
        for (File rootLocation : rootLocations) {
            for (String folderName : folderNames) {
                lookedInto.append(new File(rootLocation, folderName).getAbsolutePath()).append("\n");
            }
        }
        throw new RuntimeException(lookedInto.toString() + "FAILED!");
    }

    public static void updateMimetypes(File devEtcDir) {
        File defaultMimeTypes = ResourceUtils.getResourceAsFile("/META-INF/default/mimetypes.xml");
        File devMimeTypes = new File(devEtcDir, "mimetypes.xml");
        if (!devMimeTypes.exists() || defaultMimeTypes.lastModified() > devMimeTypes.lastModified()) {
            // override developer mimetypes file with newer default mimetypes file
            try {
                FileUtils.copyFile(defaultMimeTypes, devMimeTypes);
            } catch (IOException e) {
                System.err.println("Failed to copy default mime types file: " + e.getMessage());
            }
        }
    }

    /**
     * Copy newer files from the standalone dir to the working artifactory home dir
     *
     * @param isMaster -> secondary nodes get config from the database when they start
     */
    public static File copyNewerDevResources(File devEtcDir, File artHome, boolean isMaster) throws IOException {
        File etcDir = new File(artHome, "etc");
        IOFileFilter fileFilter = new EtcCopyFileFilter(devEtcDir, etcDir, isMaster);
        fileFilter = FileFilterUtils.makeSVNAware(fileFilter);
        FileUtils.copyDirectory(devEtcDir, etcDir, fileFilter, true);
        updateDefaultMimetypes(etcDir);
        deleteHaProps(etcDir);

        /**
         * If the bootstrap already exists, it means it's not the first startup, so don't keep the original config file
         * or the etc folder will flood with bootstrap files
         */
        if (isMaster) {
            if (new File(etcDir, ArtifactoryHome.ARTIFACTORY_CONFIG_BOOTSTRAP_FILE).exists()) {
                new File(etcDir, ArtifactoryHome.ARTIFACTORY_CONFIG_FILE).delete();
            }
        }
        copyMasterKey(etcDir);
        return etcDir;
    }

    private static void copyMasterKey(File etcDir) throws IOException {
        File securityDir = new File(etcDir, ArtifactoryHome.SECURITY_DIR_NAME);
        if (!securityDir.exists()) {
            securityDir.mkdir();
        }
        File masterKeyFile = new File(securityDir, "master.key");
        if (!masterKeyFile.exists()) {
            java.nio.file.Files.write(masterKeyFile.toPath(), MASTER_KEY.getBytes());
        }
    }

    private static void deleteHaProps(File homeEtcDir) throws IOException {
        if (!Boolean.parseBoolean(System.getProperty(ConstantValues.devHa.getPropertyName()))) {
            File haProps = new File(homeEtcDir, "artifactory.ha.properties");
            if (haProps.exists()) {
                FileUtils.forceDelete(haProps);
            }
        }
    }

    private static void updateDefaultMimetypes(File devEtcDir) {
        File defaultMimeTypes = ResourceUtils.getResourceAsFile("/META-INF/default/mimetypes.xml");
        File devMimeTypes = new File(devEtcDir, "mimetypes.xml");
        if (!devMimeTypes.exists() || defaultMimeTypes.lastModified() > devMimeTypes.lastModified()) {
            // override developer mimetypes file with newer default mimetypes file
            try {
                FileUtils.copyFile(defaultMimeTypes, devMimeTypes);
            } catch (IOException e) {
                System.err.println("Failed to copy default mime types file: " + e.getMessage());
            }
        }
    }

    private static class EtcCopyFileFilter extends AbstractFileFilter {
        private final File srcDir;
        private final File destDir;
        private final boolean isMasterNode;

        EtcCopyFileFilter(File srcDir, File destDir, boolean isMasterNode) {
            this.srcDir = srcDir;
            this.destDir = destDir;
            this.isMasterNode = isMasterNode;
        }

        @Override
        public boolean accept(File srcFile) {
            if (srcFile.isDirectory()) {
                return true;    // don't exclude directories
            }
            // Jetty.xml always true
            String srcFileName = srcFile.getName();
            if ("jetty.xml".equals(srcFileName)) {
                return true;
            }
            String relativePath = PathUtils.getRelativePath(srcDir.getAbsolutePath(), srcFile.getAbsolutePath());
            File destFile = new File(destDir, relativePath);
            if (!destFile.exists() || srcFile.lastModified() > destFile.lastModified()) {
                if (isMasterNode) {
                    return true;
                } else if (srcFileName.equalsIgnoreCase("artifactory.config.xml")
                        || srcFileName.equalsIgnoreCase("artifactory.system.properties")
                        || srcFileName.equalsIgnoreCase("mimetypes.xml")) {
                    // These are config files that should be fetched from db by nodes
                    return false;
                }
                return true;
            }
            return false;
        }
    }

    private static class WebappCopyFileFilter extends AbstractFileFilter {
        private final File srcDir;
        private final File destDir;

        WebappCopyFileFilter(File srcDir, File destDir) {
            this.srcDir = srcDir;
            this.destDir = destDir;
        }

        @Override
        public boolean accept(File srcFile) {
            if (srcFile.isDirectory()) {
                if (srcFile.getAbsolutePath().endsWith(WEB_INF_LOCATION)) {
                    // WEB-INF lib no copy
                    return false;
                }
                if (srcFile.getAbsolutePath().endsWith("webapp")) {
                    // webapp UI not needed in integration tests
                    return false;
                }
                return true;    // don't exclude directories
            }
            if (srcFile.getName().endsWith(".jar")) {
                if (srcFile.getParentFile().getAbsolutePath().endsWith(WEB_INF_LOCATION)) {
                    // WEB-INF lib no copy
                    return false;
                }
            }
            String relativePath = PathUtils.getRelativePath(srcDir.getAbsolutePath(), srcFile.getAbsolutePath());
            File destFile = new File(destDir, relativePath);
            if (!destFile.exists() || srcFile.lastModified() > destFile.lastModified()) {
                return true;
            }
            return false;
        }
    }
}