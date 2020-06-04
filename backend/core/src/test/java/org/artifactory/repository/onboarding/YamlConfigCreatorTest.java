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

package org.artifactory.repository.onboarding;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.repo.ProxyDescriptor;
import org.artifactory.repo.onboarding.YamlConfigCreator;
import org.easymock.EasyMock;
import org.testng.annotations.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.List;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.fest.assertions.Assertions.assertThat;
import static org.spockframework.util.Assert.fail;
import static org.testng.Assert.assertTrue;

/**
 * Unit test for Yaml bootstrap creator and ConfigurationYamlModel model
 *
 * @author nadavy
 */
public class YamlConfigCreatorTest {

    private String baseUrl = "https://mycomp.arti.co";
    private String proxyKey = "proxy-default";
    private String proxyHost = "https://proxy.mycomp.io";
    private int proxyPort = 443;
    private String proxyUserName = "admin";
    private String proxyPassword = "password";

    @Test
    public void testYamlBootstrapCreator() {
        try {
            // test creation of bootstrap model based on configuration to a temp file
            File temp = File.createTempFile("temp-file-name", ".tmp");
            YamlConfigCreator yamlConfigCreator = new YamlConfigCreator();
            yamlConfigCreator.saveBootstrapYaml(getRepoTypeList(), getConfigDescriptorMock(), temp);
            // parse and test yaml to model output
            Yaml yaml = new Yaml();
            try (Reader reader = new InputStreamReader(new FileInputStream(temp), Charsets.UTF_8)) {
                ConfigurationYamlModel configurationYamlModel = yaml.loadAs(reader, ConfigurationYamlModel.class);
                // assert all default repo types are in yaml file
                assertTrue(configurationYamlModel.OnboardingConfiguration.repoTypes.containsAll(getRepoTypeList()),
                        "Not all repo types included in the yaml file");
                // assert base url is in yaml file
                assertThat(configurationYamlModel.GeneralConfiguration.baseUrl).isEqualTo(baseUrl);
                // assert empty default license is in yaml file
                String defaultLicenseKeyString = "Enter your license key";
                assertThat(configurationYamlModel.GeneralConfiguration.licenseKey).isEqualTo(defaultLicenseKeyString);
                // assert default proxy configurations are in yaml file
                assertProxy(configurationYamlModel.GeneralConfiguration.proxies);
            }
        } catch (IOException e) {
            fail("Can't create temp file");
            e.printStackTrace();
        }
    }

    private void assertProxy(List<ProxyConfigurationYamlModel> proxies) {
        // only 1 proxy
        assertTrue(proxies.size() == 1, "Test should have only 1 proxy");
        ProxyConfigurationYamlModel proxy = proxies.get(0);
        assertTrue(proxy.key.equals(proxyKey), "");
        assertThat(proxy.host).isEqualTo(proxyHost);
        assertThat(proxy.port).isEqualTo(proxyPort);
        assertThat(proxy.userName).isEqualTo(proxyUserName);
        assertThat(proxy.password).isEqualTo(proxyPassword);

    }

    private MutableCentralConfigDescriptor getConfigDescriptorMock() {
        ProxyDescriptor proxyDescriptor = EasyMock.createMock(ProxyDescriptor.class);
        expect(proxyDescriptor.getKey()).andReturn(proxyKey).anyTimes();
        expect(proxyDescriptor.getHost()).andReturn(proxyHost).anyTimes();
        expect(proxyDescriptor.getPort()).andReturn(proxyPort).anyTimes();
        expect(proxyDescriptor.getUsername()).andReturn(proxyUserName).anyTimes();
        expect(proxyDescriptor.getPassword()).andReturn(proxyPassword).anyTimes();
        expect(proxyDescriptor.isDefaultProxy()).andReturn(true).anyTimes();
        MutableCentralConfigDescriptor configDescriptor = EasyMock.createMock(MutableCentralConfigDescriptor.class);
        List<ProxyDescriptor> proxyDescriptors = Lists.newArrayList();
        proxyDescriptors.add(proxyDescriptor);
        expect(configDescriptor.getProxies()).andReturn(proxyDescriptors).anyTimes();
        expect(configDescriptor.getUrlBase()).andReturn(baseUrl).anyTimes();
        replay(configDescriptor, proxyDescriptor);
        return configDescriptor;
    }

    private List<String> getRepoTypeList() {
        List<String> repoList = Lists.newArrayList();
        repoList.add("bower");
        repoList.add("maven");
        return repoList;
    }

}
