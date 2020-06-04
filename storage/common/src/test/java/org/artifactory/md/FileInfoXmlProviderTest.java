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

import com.google.common.collect.Sets;
import org.artifactory.checksum.ChecksumInfo;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.fs.FileInfo;
import org.artifactory.model.common.RepoPathImpl;
import org.artifactory.model.xstream.fs.FileAdditionalInfo;
import org.artifactory.model.xstream.fs.FileInfoImpl;
import org.jfrog.common.ResourceUtils;
import org.springframework.test.util.XmlExpectationsHelper;
import org.testng.annotations.Test;

import java.util.Set;

import static org.testng.Assert.assertEquals;

/**
 * Unit test for {@link FileInfoXmlProvider}.
 *
 * @author Yossi Shaul
 */
@Test
public class FileInfoXmlProviderTest {

    public void testFromXml() throws Exception {
        FileInfo fileInfo = createFileInfo();
        String xml = new FileInfoXmlProvider().toXml(fileInfo);
        String expected = ResourceUtils.getResourceAsString("/artifactory-file.xml");
        new XmlExpectationsHelper().assertXmlEqual(expected, xml);
    }

    public void testToXml() {
        String xml = ResourceUtils.getResourceAsString("/artifactory-file.xml");
        FileInfo fi = new FileInfoXmlProvider().fromXml(xml);
        assertEquals(fi, createFileInfo());
    }

    public void toAndFromEquals() {
        FileInfoXmlProvider provider = new FileInfoXmlProvider();
        FileInfoImpl fileInfo = createFileInfo();
        assertEquals(provider.fromXml(provider.toXml(fileInfo)), fileInfo);
    }

    private FileInfoImpl createFileInfo() {
        FileAdditionalInfo additional = new FileAdditionalInfo();
        ChecksumInfo sha1 = new ChecksumInfo(ChecksumType.sha1, "da39a3ee5e6b4b0d3255bfef95601890afd80709",
                "da39a3ee5e6b4b0d3255bfef95601890afd80709");
        ChecksumInfo md5 = new ChecksumInfo(ChecksumType.md5, "d41d8cd98f00b204e9800998ecf8427e",
                "d41d8cd98f00b204e9800998ecf8427e");
        Set<ChecksumInfo> checksums = Sets.newHashSet(sha1, md5);
        additional.setChecksums(checksums);

        FileInfoImpl fileInfo = new FileInfoImpl(
                new RepoPathImpl("libs-release-local", "aspectjweaver/aspectjweaver/1.5.3/aspectjweaver-1.5.3.jar"));
        fileInfo.setAdditionalInfo(additional);
        fileInfo.setSize(456);
        fileInfo.setMimeType("application/java-archive");
        fileInfo.setCreated(1316590989115L);
        fileInfo.setCreatedBy("admin");
        fileInfo.setLastModified(1316591065519L);
        fileInfo.setModifiedBy("admin");
        additional.setLastUpdated(1316591065520L);
        return fileInfo;
    }

}