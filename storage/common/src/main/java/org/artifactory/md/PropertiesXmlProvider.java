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

package org.artifactory.md;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import java.util.Iterator;
import java.util.Map;

/**
 * @author freds
 */
public class PropertiesXmlProvider extends XStreamMetadataProvider<PropertiesInfo, MutablePropertiesInfo> {
    private static final String COLON_PLACE_HOLDER = "_colonPlaceHolder_";

    public PropertiesXmlProvider() {
        super(PropertiesInfo.ROOT);
    }

    @Override
    public MutablePropertiesInfo fromXml(String xmlData) {
        // In order to avoid a case where invalid character : is made part of an xml element name, we replace it with
        // a place holder and after the conversion to propertiesInfo we replace it back
        String propertiesStr = xmlData.replaceAll("(<[^>]+)(:)([^>]*>)", "$1" + COLON_PLACE_HOLDER + "$3");
        MutablePropertiesInfo propertiesInfo = (MutablePropertiesInfo) getXstream().fromXML(propertiesStr);

        if (propertiesStr.contains(COLON_PLACE_HOLDER)) {
            Multimap<String, String> propertiesToModify = LinkedHashMultimap.create();
            for (Iterator<Map.Entry<String, String>> it = propertiesInfo.entries().iterator(); it.hasNext(); ) {
                Map.Entry<String, String> entry = it.next();
                if (entry.getKey().contains(COLON_PLACE_HOLDER)) {
                    propertiesToModify.put(entry.getKey().replaceAll(COLON_PLACE_HOLDER, ":"), entry.getValue());
                    it.remove();
                }
            }
            propertiesToModify.entries().forEach(entry -> propertiesInfo.put(entry.getKey(), entry.getValue()));
        }
        return propertiesInfo;
    }

    @Override
    public String toXml(PropertiesInfo metadata) {
        return getXstream().toXML(metadata);
    }
}
