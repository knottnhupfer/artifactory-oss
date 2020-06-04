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

package org.artifactory.maven.versioning;

import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.fs.MutableFileInfo;
import org.artifactory.maven.MavenMetaDataInfo;
import org.artifactory.model.common.RepoPathImpl;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;

/**
 * Tests {@link CreationDateMavenMetadataVersionComparator}.
 *
 * @author Yossi Shaul
 */
@Test
public class CreationDateMavenMetadataVersionComparatorTest {

    public void compare() {
        CreationDateMavenMetadataVersionComparator comparator = new CreationDateMavenMetadataVersionComparator();

        MutableFileInfo olderFileInfo = InfoFactoryHolder.get().createFileInfo(new RepoPathImpl("repo", "2.0"));
        olderFileInfo.setCreated(System.currentTimeMillis());
        MavenMetaDataInfo older = new MavenMetaDataInfo();
        older.setRepo(olderFileInfo.getRepoKey());
        older.setPath(olderFileInfo.getRelPath());
        older.setName(olderFileInfo.getName());
        older.setCreated(new Date(olderFileInfo.getCreated()));

        MutableFileInfo newerFileInfo = InfoFactoryHolder.get().createFileInfo(new RepoPathImpl("repo", "1.1"));
        newerFileInfo.setCreated(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(2));
        MavenMetaDataInfo newer = new MavenMetaDataInfo();
        newer.setRepo(newerFileInfo.getRepoKey());
        newer.setPath(newerFileInfo.getRelPath());
        newer.setName(newerFileInfo.getName());
        newer.setCreated(new Date(newerFileInfo.getCreated()));

        assertEquals(comparator.compare(older, newer), -1, "The comparison should be time based");

    }
}
