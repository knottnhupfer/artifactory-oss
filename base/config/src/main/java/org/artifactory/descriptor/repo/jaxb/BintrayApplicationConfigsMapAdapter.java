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

package org.artifactory.descriptor.repo.jaxb;

import com.google.common.collect.Maps;
import org.artifactory.descriptor.Descriptor;
import org.artifactory.descriptor.repo.BintrayApplicationConfig;
import org.jfrog.client.util.PathUtils;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Dan Feldman
 */
public class BintrayApplicationConfigsMapAdapter
        extends XmlAdapter<BintrayApplicationConfigsMapAdapter.Wrapper, Map<String, BintrayApplicationConfig>> {

    @Override
    public Map<String, BintrayApplicationConfig> unmarshal(Wrapper wrapper) throws Exception {
        Map<String, BintrayApplicationConfig> bintrayAppConfigsMap = Maps.newLinkedHashMap();
        List<String> duplicateRepos = wrapper.getList().stream()
                .map(appConfig -> bintrayAppConfigsMap.put(appConfig.getKey(), appConfig))
                .filter(duplicateAppConfig -> duplicateAppConfig != null)
                .map(BintrayApplicationConfig::getKey)
                .collect(Collectors.toList());

        if (!duplicateRepos.isEmpty()) {
            //Throw an error since jaxb swallows exceptions
            throw new Error("Duplicate Bintray OAUth Application in configuration: "
                    + PathUtils.collectionToDelimitedString(duplicateRepos) + ".");
        }
        return bintrayAppConfigsMap;
    }

    @Override
    public Wrapper marshal(Map<String, BintrayApplicationConfig> map) throws Exception {
        return new Wrapper(map);
    }

    @XmlType(name = "BintrayApplicationsType", namespace = Descriptor.NS)
    public static class Wrapper {
        @XmlElement(name = "bintrayApplication", required = true, namespace = Descriptor.NS)
        private List<BintrayApplicationConfig> list = new ArrayList<>();

        public Wrapper() {
        }

        public Wrapper(Map<String, BintrayApplicationConfig> map) {
            list.addAll(map.values().stream().collect(Collectors.toList()));
        }

        public List<BintrayApplicationConfig> getList() {
            return list;
        }
    }
}
