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

package org.artifactory.version.converter.v208;

import org.artifactory.convert.XmlConverterTest;
import org.artifactory.util.IdUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;

import static org.testng.Assert.*;

/**
 * @author Noam Shemesh
 */
public class AddReplicationKeyTest extends XmlConverterTest {

    private static final String CONFIG_XML_WITHOUT_REPLICATION = "/config/test/config.2.0.8.without_replication.xml";
    private static final String CONFIG_XML_WITH_SINGLE_LOCAL_REPLICATION = "/config/test/config.2.0.8.with_single_local.xml";
    private static final String CONFIG_XML_WITH_SINGLE_REMOTE_REPLICATION = "/config/test/config.2.0.8.with_single_remote.xml";
    private static final String CONFIG_XML_WITH_MULTIPLE_LOCAL_REPLICATIONS = "/config/test/config.2.0.8.with_multi_local.xml";
    private static final String CONFIG_XML_WITH_MULTIPLE_REMOTE_REPLICATIONS = "/config/test/config.2.0.8.with_multi_remote.xml";
    private static final String CONFIG_XML_WITH_FULL_DATA = "/config/test/config.2.0.8.with_full_data.xml";
    private static final String CONFIG_XML_WITH_DUPLICATE_KEY = "/config/test/config.2.0.8.with_duplicate_key.xml";

    private final AddReplicationKey converter = new AddReplicationKey();

    @Test
    public void convertWithoutPreviousData() throws Exception {
        Document document = convertXml(CONFIG_XML_WITHOUT_REPLICATION, converter);
        validateXml(document, true, true);
    }

    @Test
    public void convertWithSingleLocalData() throws Exception {
        Document document = convertXml(CONFIG_XML_WITH_SINGLE_LOCAL_REPLICATION, converter);
        validateXml(document, false, true);
    }

    @Test
    public void convertWithSingleRemoteData() throws Exception {
        Document document = convertXml(CONFIG_XML_WITH_SINGLE_REMOTE_REPLICATION, converter);
        validateXml(document, true, false);
    }

    @Test
    public void convertWithMultiLocalData() throws Exception {
        Document document = convertXml(CONFIG_XML_WITH_MULTIPLE_LOCAL_REPLICATIONS, converter);
        validateXml(document, false, true);
    }

    @Test
    public void convertWithMultiRemoteData() throws Exception {
        Document document = convertXml(CONFIG_XML_WITH_MULTIPLE_REMOTE_REPLICATIONS, converter);
        validateXml(document, true, false);
    }

    @Test
    public void convertWithFullData() throws Exception {
        Document document = convertXml(CONFIG_XML_WITH_FULL_DATA, converter);
        validateXml(document, false, false);
    }

    @Test
    public void convertWithDuplicateKey() throws Exception {
        Document document = convertXml(CONFIG_XML_WITH_DUPLICATE_KEY, converter);
        validateXml(document, false, false);
    }

    private void validateXml(Document document, boolean localEmpty, boolean remoteEmpty) {
        Element root = document.getRootElement();
        Namespace ns = root.getNamespace();
        Element localReplications = root.getChild("localReplications", ns);
        Element remoteReplications = root.getChild("remoteReplications", ns);
        Set<String> keys = new HashSet<>();
        if (localEmpty) {
            assertTrue(localReplications.getChildren().isEmpty());
        } else {
            localReplications.getChildren().stream()
                    .peek(elem -> assertTrue(keys.add(elem.getChildText("replicationKey", ns))))
                    .peek(elem -> assertEquals(elem.getChildText("replicationKey", ns),
                        IdUtils.createReplicationKey(elem.getChildText("repoKey", ns),
                                elem.getChildText("url", ns))))
                    .peek(elem -> assertNotNull(elem.getChildText("syncStatistics", ns)))
                    .forEach(elem -> assertNotNull(elem.getChildText("url", ns)));
        }
        if (remoteEmpty) {
            assertTrue(remoteReplications.getChildren().isEmpty());
        } else {
            remoteReplications.getChildren()
                    .stream()
                    .peek(elem -> assertTrue(keys.add(elem.getChildText("replicationKey", ns))))
                    .forEach(elem -> assertEquals(elem.getChildText("replicationKey", ns),
                            IdUtils.createReplicationKey(elem.getChildText("repoKey", ns), null))
            );
        }
    }
}