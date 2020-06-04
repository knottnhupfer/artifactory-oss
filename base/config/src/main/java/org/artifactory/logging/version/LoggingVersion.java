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

package org.artifactory.logging.version;

import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.logging.version.v1.LogbackConfigSwapper;
import org.artifactory.logging.version.v10.LogbackAddEventLogConverter;
import org.artifactory.logging.version.v11.LogbackFilteredXrayTrafficConverter;
import org.artifactory.logging.version.v12.BinaryStoreLogsConverter;
import org.artifactory.logging.version.v12.LogbackAddBuildInfoMigrationLogsConverter;
import org.artifactory.logging.version.v13.ConanV2MigrationLogsConverter;
import org.artifactory.logging.version.v14.LogbackXrayTrafficConverter;
import org.artifactory.logging.version.v3.LogbackJFrogInfoConverter;
import org.artifactory.logging.version.v5.LogbackRemoveSupportLogConverter;
import org.artifactory.logging.version.v7.LogbackAddAccessServerLogsConverter;
import org.artifactory.logging.version.v8.LogbackBackTracePatternLayoutConverter;
import org.artifactory.logging.version.v9.LogbackAddMigrationLogsConverter;
import org.artifactory.version.ArtifactoryVersion;
import org.artifactory.version.ArtifactoryVersionProvider;
import org.artifactory.version.XmlConverterUtils;
import org.artifactory.version.converter.XmlConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Keeps track of the logging configuration versions
 *
 * @author Noam Y. Tenne
 */
public enum LoggingVersion {
    v1(ArtifactoryVersionProvider.v122rc0.get(), new LogbackConfigSwapper()),
    v2(ArtifactoryVersionProvider.v310.get(), null),
    v3(ArtifactoryVersionProvider.v340.get(), new LogbackJFrogInfoConverter()),
    v4(ArtifactoryVersionProvider.v422.get(), new LogbackConfigSwapper()),
    /**
     * Last commit really messed up the versions here... v5 already existed when he committed v4.
     * v4 was originally RTFACT-4550 --> art v2.5.0 and v5 was originally RTFACT-6766 --> art v3.3.0
     * SO to accommodate the last change up to Artifactory v4.4.0 the logback swapper conversion will happen,
     * v5 is now a dummy version that does not change logback up to v4.8.2
     * v6 is the current version which triggers remove support appender conversion
     */
    v5(ArtifactoryVersionProvider.v441.get(), new LogbackRemoveSupportLogConverter()),
    v6(ArtifactoryVersionProvider.v490.get()),
    v7(ArtifactoryVersionProvider.v500beta1.get(), new LogbackAddAccessServerLogsConverter()),
    v8(ArtifactoryVersionProvider.v540m001.get(), new LogbackBackTracePatternLayoutConverter()),
    v9(ArtifactoryVersionProvider.v550m001.get(), new LogbackAddMigrationLogsConverter()),
    v10(ArtifactoryVersionProvider.v583.get(),
            new LogbackAddEventLogConverter()),
    v11(ArtifactoryVersionProvider.v640m007.get(), new LogbackFilteredXrayTrafficConverter()),
    v12(ArtifactoryVersionProvider.v660m001.get(), new BinaryStoreLogsConverter(),
            new LogbackAddBuildInfoMigrationLogsConverter()),
    v13(ArtifactoryVersionProvider.v690m001.get(), new ConanV2MigrationLogsConverter()),
    v14(ArtifactoryVersionProvider.v6110m001.get(), new LogbackXrayTrafficConverter());

    private static final Logger log = LoggerFactory.getLogger(LoggingVersion.class);

    private ArtifactoryVersion version;
    private XmlConverter[] xmlConverters;

    /**
     * Main constructor
     *
     * @param from          Start version
     * @param xmlConverters XML converters required for the specified range
     */
    LoggingVersion(ArtifactoryVersion from, XmlConverter... xmlConverters) {
        this.version = from;
        this.xmlConverters = xmlConverters;
    }

    /**
     * Run the needed conversions
     *
     * @param srcEtcDir the directory in which resides the logback file to convert
     */
    public static void convert(ArtifactoryVersion version, File srcEtcDir, File targetEtcDir) throws IOException {
        // First create the list of converters to apply
        List<XmlConverter> converters = LoggingVersion.getEffectedXmlConverters(version);
        if (!converters.isEmpty()) {
            File logbackConfigFile = new File(srcEtcDir, ArtifactoryHome.LOGBACK_CONFIG_FILE_NAME);
            try {
                String result =
                        XmlConverterUtils.convert(converters, FileUtils.readFileToString(logbackConfigFile, "utf-8"));
                backupAndSaveLogback(result, targetEtcDir);
            } catch (IOException e) {
                log.error("Error occurred while converting logback config for conversion: {}.", e.getMessage());
                log.debug("Error occurred while converting logback config for conversion", e);
                throw e;
            }
        }
    }

    /**
     * Creates a backup of the existing logback configuration file and proceeds to save post-conversion content
     *
     * @param result Conversion result
     * @param etcDir directory to which to save the conversion result
     */
    public static void backupAndSaveLogback(String result, File etcDir) throws IOException {
        File logbackConfigFile = new File(etcDir, ArtifactoryHome.LOGBACK_CONFIG_FILE_NAME);
        if (logbackConfigFile.exists()) {
            File originalBackup = new File(etcDir, "logback.original.xml");
            FileUtils.copyFile(logbackConfigFile, originalBackup);
        }
        FileUtils.writeStringToFile(logbackConfigFile, result, "utf-8");
    }

    public static void convert(ArtifactoryVersion from, File path)
            throws IOException {
        boolean foundConversion = false;
        // All converters of versions above me needs to be executed in sequence
        LoggingVersion[] versions = LoggingVersion.values();
        for (LoggingVersion logingVersion : versions) {
            if (logingVersion.version.after(from)) {
                foundConversion = true;
                convert(from, path, path);
            }
        }
        // Write to log only if conversion has been executed
        if (foundConversion) {
            log.info("Ending database conversion from {}" ,from);
        }
    }

    public static List<XmlConverter> getEffectedXmlConverters(ArtifactoryVersion importedVersion) {
        List<XmlConverter> converters = Lists.newArrayList();
        for (LoggingVersion loggingVersion : values()) {
            if (loggingVersion.version.afterOrEqual(importedVersion) && loggingVersion.xmlConverters != null) {
                    converters.addAll(Lists.newArrayList(loggingVersion.xmlConverters));
            }
        }
        return converters;
    }
}