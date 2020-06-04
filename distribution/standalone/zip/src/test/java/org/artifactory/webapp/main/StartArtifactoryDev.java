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

package org.artifactory.webapp.main;

import ch.qos.logback.classic.util.ContextInitializer;
import org.apache.catalina.Context;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.valves.RemoteIpValve;
import org.apache.catalina.valves.rewrite.RewriteValve;
import org.apache.commons.lang3.StringUtils;
import org.apache.coyote.AbstractProtocol;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.webapp.WebappUtils;
import org.jfrog.common.ResourceUtils;
import org.jfrog.common.platform.test.helpers.RouterProcess;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Starts an Artifactory OSS instance with Tomcat embedded.
 *
 * @author yoavl
 */
public class StartArtifactoryDev {
    static final String ACCESS_HOME_SYS_PROP = "jfrog.access.home";
    static final String ACCESS_DEBUG_PORT_PROP = "jfrog.access.debug.port";
    static final String ACCESS_DEFAULT_DEBUG_PORT = "6006";
    static final int ROUTER_INBOUND_PORT = 8081;
    static final int ROUTER_OUTBOUND_PORT = 8080;

    public static void main(String... args) throws IOException {
        // set default dev logback configuration
        System.setProperty(ContextInitializer.CONFIG_FILE_PROPERTY,
                ResourceUtils.getResourceAsFile("/dev/etc/logback.xml").getAbsolutePath());

        File devArtHome = getArtifactoryDevHome(args);

        File devEtcDir = WebappUtils.getDevEtcFolder();
        WebappUtils.updateMimetypes(devEtcDir);
        WebappUtils.copyNewerDevResources(devEtcDir, devArtHome, true);

        setSystemProperties(devArtHome);

        startAccessProcess(devArtHome);

        Tomcat tomcat = startTomcat(devArtHome);
        tomcat.getServer().await();
    }

    static Tomcat startTomcat(File devArtHome) {
        return startTomcat(devArtHome, 8080);
    }

    static Tomcat startTomcatWithRouter(File devArtHome) {
        return startTomcat(devArtHome, 8082);
    }

    static Tomcat startTomcat(File devArtHome, int port) {
        Tomcat tomcat = null;
        try {
            tomcat = new Tomcat();
            tomcat.setBaseDir(devArtHome + "/tomcat");
            // must serve root for the rewrite to process that.

            String webappsAbsolutePath = WebappUtils.getWebappRoot(devArtHome, false, true).getAbsolutePath();
            Path rootDir = Paths.get(webappsAbsolutePath ,"ROOT");
            Files.createDirectories(rootDir);

            tomcat.addContext(tomcat.getHost(),"", rootDir.toString());
            Context context = tomcat
                    .addWebapp("/artifactory", webappsAbsolutePath);
            File tomcatResources = WebappUtils.getTomcatResources();
            ((StandardContext) context).setDefaultWebXml(new File(tomcatResources, "web.xml").getAbsolutePath());
            ((StandardContext) context)
                    .setDefaultContextXml(new File(tomcatResources, "context.xml").getAbsolutePath());
            context.setConfigFile(new File(tomcatResources, "artifactory.xml").toURI().toURL());
            tomcat.setPort(Integer.parseInt(System.getProperty("server.port", port + "")));
            ((AbstractProtocol) tomcat.getConnector().getProtocolHandler()).setSendReasonPhrase(false);

            tomcat.getConnector().setProperty("relaxedPathChars", "[]");
            tomcat.getConnector().setProperty("relaxedQueryChars", "[]");

            // add Valves
            addRemoteIPValve(tomcat);
            addRewriteValve(tomcat);

            tomcat.start();
            return tomcat;
        } catch (Exception e) {
            System.err.println("Could not start the Tomcat server: " + e);
            if (tomcat != null) {
                try {
                    tomcat.stop();
                } catch (Exception e1) {
                    System.err.println("Unable to stop the Tomcat server: " + e1);
                }
            }
            throw new RuntimeException(e);
        }
    }

    static void addRemoteIPValve(Tomcat tomcat) {
        RemoteIpValve valve = new RemoteIpValve();
        valve.setProtocolHeader("X-Forwarded-Proto");

        tomcat.getEngine().getPipeline().addValve(valve);
    }

    static void addRewriteValve(Tomcat tomcat) throws Exception {
        String rules =
                " RewriteRule ^/v2/(.*)$ /artifactory/v2/$1 [L]\n"+
                        "";
        RewriteValve valve = new RewriteValve();
        tomcat.getEngine().getPipeline().addValve(valve);

        valve.setConfiguration(rules);
    }

    static void setSystemProperties(File devArtHome) {
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty(ConstantValues.dev.getPropertyName(), "true");
        System.setProperty(ConstantValues.pluginScriptsRefreshIntervalSecs.getPropertyName(), "3");
        System.setProperty("logback.configurationFile", new File(devArtHome + "/etc/logback.xml").getAbsolutePath());
        if (System.getProperty("artifactory.addons.disabled") == null) {
            System.setProperty("artifactory.addons.disabled", "aol");
        }
        System.setProperty("staging.mode", "true"); // license checks against staging store
    }

    static File getArtifactoryDevHome(String[] args) throws IOException {
        String homeProperty = System.getProperty("artifactory.home");
        String prefix = args.length == 0 ? ".." : args[0];
        File devArtHome = new File(
                homeProperty != null ? homeProperty : prefix + "/devenv/.artifactory").getCanonicalFile();
        if (!devArtHome.exists() && !devArtHome.mkdirs()) {
            throw new RuntimeException("Failed to create home dir: " + devArtHome.getAbsolutePath());
        }
        System.setProperty(ArtifactoryHome.SYS_PROP, devArtHome.getAbsolutePath());
        return devArtHome;
    }

    static void startAccessProcess() throws IOException {
        startAccessProcess(0, false, false, null, true, false);
    }

    static void startAccessProcess(File devArtHome) throws IOException {
        startAccessProcess(0, false, false, devArtHome, true, false);
    }

    static void startAccessProcess(int port, boolean bundled, boolean waitForAccess) throws IOException {
        startAccessProcess(port, bundled, waitForAccess, null, true, false);
    }

    static AccessProcess startAccessProcess(int port, boolean bundled, boolean waitForAccess, File devArtHome, boolean tls, boolean withRouter) throws IOException {
        if (!Boolean.getBoolean("access.process.skip")) {
            // start Access server. re-use existing service is detected on the same port
            // the process will register a shutdown hook so we will let the JVM kill it
            File accessDevHome = getAccessDevHome();
            AccessProcess.AccessProcessConfig accessConfig = new AccessProcess.AccessProcessConfig(accessDevHome)
                    .port(port).bundled(bundled).artHome(devArtHome)
                    .tlsEnabled(tls);
            if(withRouter) {
                accessConfig.contextPath("/access");
                accessConfig.router(true);
                accessConfig.routerPort(ROUTER_OUTBOUND_PORT);
            }
            if (StringUtils.isNotBlank(System.getProperty(ACCESS_DEBUG_PORT_PROP))) {
                accessConfig = accessConfig.debug(Integer.valueOf(System.getProperty(ACCESS_DEBUG_PORT_PROP)), false);
            }
            AccessProcess accessProcess = new AccessProcess(accessConfig);
            if (waitForAccess) {
                accessProcess.startAndWait();
            } else {
                accessProcess.start();
            }
            System.setProperty(ConstantValues.accessClientServerUrlOverride.getPropertyName(), accessProcess.getAccessUrl());
            return accessProcess;
        }
        return null;
    }

    static void startMetadataApiProcess(int mdsPort, int accessPort, String artiHomePath)
            throws IOException {
        MetadataServerProcess metadataServerProcess = new MetadataServerProcess(mdsPort, accessPort, artiHomePath);
        metadataServerProcess.start();
    }

    static void startRouterProcess(AccessProcess accessProcess, File artDev)
            throws IOException {
        RouterProcess.RouterProcessConfig routerProcessConfigBuilder = RouterProcess.RouterProcessConfig.builder()
                .accessUrl(accessProcess.getAccessUrl())
                .outboundPort(ROUTER_OUTBOUND_PORT)
                .inboundPort(ROUTER_INBOUND_PORT)
                .home(new File(artDev, ".router").getAbsolutePath())
                .serviceRegistryGrpcPort(accessProcess.getGrpcPort())
                .build();
        RouterProcess routerProcess = new ArtRouterProcess(routerProcessConfigBuilder);
        routerProcess.start();
        System.setProperty("JFROG_ROUTER_URL", "http://localhost:" + ROUTER_OUTBOUND_PORT);
        System.setProperty(ConstantValues.accessClientServerUrlOverride.getPropertyName(), "http://localhost:" + ROUTER_OUTBOUND_PORT + "/access");
    }

    private static File getAccessDevHome() {
        String homeProperty = System.getProperty(ACCESS_HOME_SYS_PROP);
        File devHome;
        if (homeProperty != null) {
            devHome = new File(homeProperty).getAbsoluteFile();
        } else {
            String artHomeProperty = System.getProperty(ArtifactoryHome.SYS_PROP);
            if (artHomeProperty != null) {
                devHome = new File(new File(artHomeProperty), "access"); //embedded home
            } else {
                devHome = new File(WebappUtils.getArtifactoryDevenv(), ".jfrog-access");
            }
        }
        if (!devHome.exists() && !devHome.mkdirs()) {
            throw new RuntimeException("Failed to create home dir: " + devHome.getAbsolutePath());
        }
        System.setProperty(ACCESS_HOME_SYS_PROP, devHome.getAbsolutePath());
        return devHome;
    }

}
