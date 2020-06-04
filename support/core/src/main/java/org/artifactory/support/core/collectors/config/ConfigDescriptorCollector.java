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

package org.artifactory.support.core.collectors.config;

import com.google.common.base.Strings;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.support.config.descriptor.ConfigDescriptorConfiguration;
import org.jfrog.support.common.core.AbstractGenericContentCollector;
import org.jfrog.support.common.core.exceptions.BundleConfigurationException;
import org.jfrog.support.common.utils.StringBuilderWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Config descriptor collector
 *
 * @author Michael Pasternak
 */
public class ConfigDescriptorCollector extends AbstractGenericContentCollector<ConfigDescriptorConfiguration> {
    private static final Logger log = LoggerFactory.getLogger(ConfigDescriptorCollector.class);
    private static final String[] TO_SCRUB = {
            "\\s*+<password>.+</password>",
            "\\s*+<keyStorePassword>.+</keyStorePassword>",
            "\\s*+<passphrase>.+</passphrase>",
            "\\s*+<gpgPassPhrase>.+</gpgPassPhrase>",
            "\\s*+<refreshToken>.+</refreshToken>",
            "\\s*+<secret>.+</secret>",
            "\\s*+<adminToken>.+</adminToken>",
            "\\s*+<managerDn>.+</managerDn>",
            "\\s*+<managerPassword>.+</managerPassword>"};

    private CentralConfigService centralConfigService;

    public ConfigDescriptorCollector(CentralConfigService centralConfigService) {
        super("conf");
        this.centralConfigService = centralConfigService;
    }

    /**
     * Produces content and returns it wrapped with {@link StringBuilderWrapper}
     *
     * @param configuration the runtime configuration
     * @return {@link StringBuilderWrapper}
     */
    @Override
    protected StringBuilderWrapper doProduceContent(ConfigDescriptorConfiguration configuration) {
        String centralConfigDescriptor = getDescriptorData(configuration);
        if (!Strings.isNullOrEmpty(centralConfigDescriptor)) {
            return new StringBuilderWrapper(centralConfigDescriptor);
        }
        return failure();
    }

    /**
     * Performs filtering on returned data
     *
     * @param configuration {@link ConfigDescriptorConfiguration}
     * @return {@link CentralConfigDescriptor}
     */
    private String getDescriptorData(ConfigDescriptorConfiguration configuration) {
        String configDescriptor = centralConfigService.getConfigXml();

        if (!configuration.isHideUserDetails()) {
            return configDescriptor;
        }
        return Arrays.stream(TO_SCRUB)
                .reduce(configDescriptor, (descriptor, scrub) -> descriptor.replaceAll(scrub, ""));
    }

    @Override
    public Logger getLog() {
        return log;
    }

    /**
     * Makes sure configuration is valid
     *
     * @param configuration configuration to check
     * @throws BundleConfigurationException if configuration is invalid
     */
    @Override
    protected void doEnsureConfiguration(ConfigDescriptorConfiguration configuration)
            throws BundleConfigurationException {
    }

    /**
     * @return The filename to be used
     */
    @Override
    protected String getFileName() {
        return "artifactory.config.xml";
    }
}
