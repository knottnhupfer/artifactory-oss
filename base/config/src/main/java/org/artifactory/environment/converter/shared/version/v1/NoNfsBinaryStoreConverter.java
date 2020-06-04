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

package org.artifactory.environment.converter.shared.version.v1;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.environment.BinaryStoreProperties;
import org.artifactory.version.CompoundVersionDetails;
import org.jfrog.storage.binstore.config.BinaryProviderConfigBuilder;
import org.jfrog.storage.binstore.config.ConfigurableBinaryProviderManager;
import org.jfrog.storage.binstore.config.model.*;
import org.jfrog.storage.binstore.ifc.BinaryProviderConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.*;
import java.util.Optional;
import java.util.Set;

/**
 * @author Gidi Shabat
 */
public class NoNfsBinaryStoreConverter extends NoNfsBasicEnvironmentConverter {
    private static final Logger log = LoggerFactory.getLogger(NoNfsBinaryStoreConverter.class);

    @Override
    public boolean isInterested(ArtifactoryHome home, CompoundVersionDetails source, CompoundVersionDetails target) {
        return isUpgradeTo5x(source, target);
    }

    @Override
    public void doConvert(ArtifactoryHome artifactoryHome, File clusterHomeDir) {
        if (clusterHomeDir == null) {
            // None HA environment therefore load the local storage.properties and local binarystore.xml files
            File storagePropertiesFile = new File(artifactoryHome.getEtcDir(), "storage.properties");
            File binaryStoreXmlFile = new File(artifactoryHome.getEtcDir(), "binarystore.xml");
            // Now convert both files into the final binarystore.xml
            mergeAndConvertToBinaryStoreXml(artifactoryHome, storagePropertiesFile, artifactoryHome.getDataDir(),
                    binaryStoreXmlFile);
        } else {
            // TODO: [by fsi] when reaching here the db.properties should have been done and copied to local etc,
            // TODO: and also binarystore.xml if it exists in ha-etc. Conclusion the code above (only local manipulation) is good.
            // HA environment therefore load the cluster home storage.properties and cluster home binarystore.xml files
            safeCopyRelativeFile(clusterHomeDir, artifactoryHome.getBinaryStoreXmlFile());
            File storagePropertiesFile = new File(clusterHomeDir, "ha-etc/storage.properties");
            File dataDir = new File(clusterHomeDir, "ha-data");
            File binaryStoreXmlFile = new File(clusterHomeDir, "ha-etc/binarystore.xml");
            // Now convert both files into the final binarystore.xml
            mergeAndConvertToBinaryStoreXml(artifactoryHome, storagePropertiesFile, dataDir, binaryStoreXmlFile);
        }

    }

    @Override
    protected void doAssertConversionPreconditions(ArtifactoryHome artifactoryHome, File clusterHomeDir) {
        if (clusterHomeDir == null) {
            assertFilePermissions(new File(artifactoryHome.getEtcDir(), "storage.properties"));
            assertFilePermissions(new File(artifactoryHome.getEtcDir(), "binarystore.xml"));
        } else {
            assertFilePermissions(new File(clusterHomeDir, "ha-etc/storage.properties"));
            assertFilePermissions(new File(clusterHomeDir, "ha-etc/binarystore.xml"));
        }
        assertTargetFilePermissions(artifactoryHome.getBinaryStoreXmlFile());
    }

    private void mergeAndConvertToBinaryStoreXml(ArtifactoryHome artifactoryHome, File storagePropertiesFile,
            File dataDir, File binaryStoreXmlFile) {
        if (!storagePropertiesFile.exists()) {
            // No storage.properties file assuming already converted to binarystore.xml
            return;
        }
        log.info("Starting environment conversion: storage.properties -> binarystore.xml");
        try {
            // Load the storage.properties
            BinaryStoreProperties binaryStoreProperties = new BinaryStoreProperties(storagePropertiesFile,
                    dataDir.getAbsolutePath(), artifactoryHome.getSecurityDir().getAbsolutePath());
            BinaryProviderConfig storageConfig = binaryStoreProperties.toDefaultValues();
            // Load the binary providers chain
            ChainMetaData selectedChain;
            if (binaryStoreXmlFile.exists()) {
                // Create binary provider according to the The new generation config
                Config config = BinaryProviderConfigBuilder.loadConfig(new FileInputStream(binaryStoreXmlFile));
                selectedChain = ConfigurableBinaryProviderManager.buildByConfig(config, storageConfig, Optional.empty());
            } else {
                // Create binary provider using to the The old generation properties
                selectedChain = ConfigurableBinaryProviderManager.buildByStorageProperties(storageConfig);
            }
            // Now we have the runtime chain which is already merged with the storage.properties (by the default values)
            Config config = new Config();
            config.getChains().add(selectedChain);
            config.setVersion("v1");
            // In order to generate clean binarystore.xml file we need to remove the default values from the configuration
            // and split between the chain the the providers.
            removeDefaultParams(config);
            splitBetweenChainAndProviders(config);
            String template = selectedChain.getTemplate();
            if (StringUtils.isNotBlank(template) && !"user-chain".equals(template)) {
                selectedChain.setProviderMetaData(null);
            }
            if ("user-chain".equals(template)) {
                selectedChain.setTemplate(null);
            }
            // Ok finally we have clean binarystore xml and now we will convert it to string
            // and copy it to the local binarystore.xml
            String content = convertXmlToString(config);
            FileUtils.writeStringToFile(artifactoryHome.getBinaryStoreXmlFile(), content);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create binarystore.xml file", e);
        } catch (JAXBException e) {
            throw new RuntimeException("Failed to create binarystore.xml file, could not read storage config file ", e);
        }
        log.info("Finished environment conversion: storage.properties -> binarystore.xml");
    }

    private void splitBetweenChainAndProviders(Config config) {
        ChainMetaData chainMetaData = config.getChains().get(0);
        splitBetweenChainAndProviders(config, chainMetaData.getProviderMetaData());
    }

    private void splitBetweenChainAndProviders(Config config, ProviderMetaData providerMetaData) {
        if (providerMetaData == null) {
            return;
        }
        ProviderMetaData clone = new ProviderMetaData(providerMetaData);
        config.getProviderMetaDatas().add(clone);
        providerMetaData.getParams().clear();
        providerMetaData.getProperties().clear();
        splitBetweenChainAndProviders(config, providerMetaData.getProviderMetaData());
        providerMetaData.getSubProviderMetaDataList().forEach(a -> splitBetweenChainAndProviders(config, a));
    }

    private void removeDefaultParams(Config config) {
        ChainMetaData chainMetaData = config.getChains().get(0);
        removeDefaultParams(chainMetaData.getProviderMetaData());
    }

    private void removeDefaultParams(ProviderMetaData providerMetaData) {
        if (providerMetaData == null) {
            return;
        }
        cleanDefaultParams(providerMetaData.getParams(), providerMetaData.getType());
        removeDefaultParams(providerMetaData.getProviderMetaData());
        providerMetaData.getSubProviderMetaDataList().forEach(this::removeDefaultParams);
    }

    private void cleanDefaultParams(Set<Param> params, String providerSignature) {
        params.removeIf(a -> BinaryStoreProperties.isDefault(a, providerSignature));
    }

    private String convertXmlToString(Config config) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Config.class,
                    ChainMetaData.class, ProviderMetaData.class, Param.class, Property.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            StringWriter sw = new StringWriter();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
            jaxbMarshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            PrintWriter pw = new PrintWriter(sw);
            jaxbMarshaller.marshal(config, pw);
            return sw.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert Storage properties to binarystore.xml", e);
        }
    }

}