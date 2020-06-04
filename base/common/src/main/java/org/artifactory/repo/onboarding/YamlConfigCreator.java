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

package org.artifactory.repo.onboarding;

import com.google.common.base.Strings;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.repo.ProxyDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Creates a bootstrap yaml file using given repo types list and a config descriptor
 *
 * @author nadavy
 */
public class YamlConfigCreator {
    private static final Logger log = LoggerFactory.getLogger(YamlConfigCreator.class);

    private static final String BASE_URL_PLACEHOLDER = "{baseurl-placeholder}";
    private static final String PROXY_PLACEHOLDER = "{proxies-placeholder}";
    private static final String REPOSITORY_PLACEHOLDER = "{repotypes-placeholder}";
    private static final String YAML_TEMPLATE_FILE = "/templates/artifactory.config.template.yml";
    private static final String EMPTY_PROXY = "#proxies :\n" +
            "  # -  key : \"proxy1\"\n" +
            "  #    host : \"https://proxy.mycomp.io\"\n" +
            "  #    port : 443\n" +
            "  #    userName : \"admin\"\n" +
            "  #    password : \"password\"\n" +
            "  #    defaultProxy : true\n" +
            "  # -  key : \"proxy2\"\n" +
            "  #    ...\n";
    private static final String EMPTY_BASE_URL = "  #baseUrl : \"https://mycomp.arti.co\"\n";

    private MutableCentralConfigDescriptor configDescriptor;
    private List<String> repoTypeList;

    public void saveBootstrapYaml(List<String> repoTypeList, MutableCentralConfigDescriptor configDescriptor,
            File yamlOutputFile) throws IOException {
        this.configDescriptor = configDescriptor;
        this.repoTypeList = repoTypeList;
        try (InputStream inputStream = getClass().getResourceAsStream(YAML_TEMPLATE_FILE)) {
            List<String> yamlStringList = IOUtils.readLines(inputStream, StandardCharsets.UTF_8);
            String yamlOutputString = yamlStringList.stream()
                    .map(this::handleYamlLine)
                    .collect(Collectors.joining());
            log.info("Saving bootstrap settings to {}", yamlOutputFile.getName());
            FileUtils.writeStringToFile(yamlOutputFile, yamlOutputString);
        }
    }

    private String handleYamlLine(String line) {
        if (line.contains(BASE_URL_PLACEHOLDER)) {
            return getYamlBaseUrl();
        }
        if (line.contains(PROXY_PLACEHOLDER)) {
            return getYamlProxy();
        }
        if (line.contains(REPOSITORY_PLACEHOLDER)) {
            return getYamlRepositories();
        }
        return line + "\n";
    }

    private String getYamlBaseUrl() {
        String baseUrl = configDescriptor.getUrlBase();
        if (Strings.isNullOrEmpty(baseUrl)) {
            baseUrl = EMPTY_BASE_URL;
        } else {
            baseUrl = "  baseUrl : \"" + baseUrl + "\"\n";
        }
        return baseUrl;
    }

    private String getYamlProxy() {
        List<ProxyDescriptor> proxiesDescriptors = configDescriptor.getProxies();
        StringBuilder proxies = new StringBuilder();
        if (proxiesDescriptors == null) {
            proxies = new StringBuilder(EMPTY_PROXY);
        } else {
            proxies.append("  proxies : \n");
            for (ProxyDescriptor proxyDescriptor : proxiesDescriptors) {
                proxies.append("  - key : \"").append(proxyDescriptor.getKey()).append("\"\n").append("    host : \"")
                        .append(proxyDescriptor.getHost()).append("\"\n").append("    port : ")
                        .append(proxyDescriptor.getPort()).append("\n")
                        .append(createPropertyIfNotNullOrEmpty("userName", proxyDescriptor.getUsername()))
                        .append(createPropertyIfNotNullOrEmpty("password", proxyDescriptor.getPassword()))
                        .append("    defaultProxy : ").append(proxyDescriptor.isDefaultProxy()).append("\n");
            }
        }
        return proxies.toString();
    }

    private String createPropertyIfNotNullOrEmpty(String property, String value) {
        return Strings.isNullOrEmpty(value) ? "" : "    " + property + " : \"" + value + "\"\n";
    }

    private String getYamlRepositories() {
        StringBuilder repos = new StringBuilder();
        for (String repoType : repoTypeList) {
            repos.append("  - ").append(repoType).append("\n");
        }
        return repos.toString();
    }

}
