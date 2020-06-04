package org.artifactory.support.core.collectors.configfiles;

import com.google.common.collect.Lists;
import org.artifactory.common.ArtifactoryHome;
import org.jfrog.support.common.core.collectors.ConfigFilesCollector;

import java.io.File;
import java.util.List;

/**
 * @author Tamir Hadad
 */
public class ArtifactoryConfigFileCollector extends ConfigFilesCollector {

    private ArtifactoryHome home;

    public ArtifactoryConfigFileCollector(ArtifactoryHome home) {
        super();
        this.home = home;
    }

    @Override
    protected List<File> getConfigFiles() {
        List<File> configFiles = Lists.newArrayList();
        configFiles.add(home.getArtifactoryConfigBootstrapFile());
        configFiles.add(home.getArtifactoryConfigFile());
        configFiles.add(home.getArtifactoryConfigImportFile());
        configFiles.add(home.getArtifactoryConfigLatestFile());
        configFiles.add(home.getArtifactoryConfigNewBootstrapFile());
        configFiles.add(home.getLogbackConfig());
        configFiles.add(home.getMimeTypesFile());
        configFiles.add(home.getDBPropertiesFile());
        configFiles.add(home.getBinaryStoreXmlFile());
        configFiles.add(home.getArtifactorySystemPropertiesFile());

        return configFiles;
    }

    @Override
    protected String[] getScrubStrings() {
        return new String[]{"password=", "<password>", "<refreshToken>", "<secret>",
                "<managerPassword>", "<passphrase>", "<gpgPassPhrase>", "<keyStorePassword>", "<identity>", "<credential>", "artifactory.bintray.system"};
    }
}
