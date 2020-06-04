package org.artifactory.eula;

import org.apache.commons.io.IOUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.eula.EulaService;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.eula.EulaDescriptor;
import org.artifactory.eula.exceptions.EulaException;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

import static org.artifactory.addon.ArtifactoryRunningMode.AOL_JCR;
import static org.artifactory.addon.ArtifactoryRunningMode.JCR;

/**
 * @author Omri Ziv
 */
@Component
public class EulaServiceImpl implements EulaService {

    private static final Logger log = LoggerFactory.getLogger(EulaServiceImpl.class);
    private CentralConfigService centralConfigService;
    private AddonsManager addonsManager;
    private static final String EULA_FILE = "jcr-eula.html";

    @Autowired
    public EulaServiceImpl(CentralConfigService centralConfigService, AddonsManager addonsManager) {
        this.addonsManager = addonsManager;
        this.centralConfigService = centralConfigService;
    }

    @Override
    public boolean isRequired() {
        if (addonsManager.getArtifactoryRunningMode() == AOL_JCR) {
            return false;
        }

        if (addonsManager.getArtifactoryRunningMode() == JCR) {
            CentralConfigDescriptor centralConfigDescriptor = centralConfigService.getDescriptor();
            EulaDescriptor eulaConfig = centralConfigDescriptor.getEulaConfig();
            if (eulaConfig == null) {
                return true;
            } else {
                return !eulaConfig.isAccepted();
            }
        }
        return false;
    }

    @Override
    public void accept() {
        MutableCentralConfigDescriptor mutableDescriptor = centralConfigService.getMutableDescriptor();
        EulaDescriptor eulaConfig = mutableDescriptor.getEulaConfig();
        if (eulaConfig == null || !eulaConfig.isAccepted()) {
            eulaConfig = new EulaDescriptor();
        }
        eulaConfig.setAccepted(true);
        eulaConfig.setAcceptDate(ISODateTimeFormat.dateTime().print(System.currentTimeMillis()));

        mutableDescriptor.setEulaConfig(eulaConfig);
        centralConfigService.saveEditedDescriptorAndReload(mutableDescriptor);
    }

    @Override
    public byte[] getEulaFile(){
        try (InputStream eulaStream = fetchEulaStream()) {
            return IOUtils.toByteArray(eulaStream);
        } catch (IOException e) {
            log.error("Could not read Eula.");
            throw new EulaException("Could not read Eula.", e);
        }
    }

    private InputStream fetchEulaStream() {
        return this.getClass().getResourceAsStream("/META-INF/jcr/" + EULA_FILE);
    }

}
