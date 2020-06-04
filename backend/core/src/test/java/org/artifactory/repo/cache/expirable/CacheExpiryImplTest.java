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

package org.artifactory.repo.cache.expirable;

import ch.qos.logback.classic.Level;
import com.google.common.collect.Maps;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.repo.LocalCacheRepo;
import org.artifactory.repo.local.IsLocalGenerated;
import org.artifactory.spring.ArtifactoryApplicationContext;
import org.artifactory.test.TestUtils;
import org.easymock.EasyMock;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.stream.Stream;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Noam Y. Tenne
 */
public class CacheExpiryImplTest {

    @Test(expectedExceptions = NullPointerException.class)
    public void testInitWithNullContext() {
        new CacheExpiryImpl().init();
    }

    @Test
    public void testInitWithNullBeanName() {
        ArtifactoryApplicationContext context = EasyMock.createMock(ArtifactoryApplicationContext.class);
        EasyMock.expect(context.beansForType(CacheExpiryChecker.class))
                .andReturn(Maps.newHashMap()).times(2);

        EasyMock.replay(context);
        CacheExpiryImpl cacheExpiry = new CacheExpiryImpl();
        cacheExpiry.setApplicationContext(context);
        cacheExpiry.init();
        EasyMock.verify(context);
    }

    @Test
    public void testIsExpirableWithNullPath() {
        assertFalse(new CacheExpiryImpl().isExpirable(null, null, null), "Null path should never be expirable.");
        assertFalse(new CacheExpiryImpl().isLocalGenerated(null, null, null), "Null path should never be allowed.");
    }

    @Test
    public void testIsExpirableWithValidPathAndNoExpirables() {
        assertFalse(new CacheExpiryImpl().isExpirable(null, null, "afdafasdf"),
                "Nothing should be expirable when no expiry criteria is set.");
        assertFalse(new CacheExpiryImpl().isLocalGenerated(null, null, "afdafasdf"),
                "Nothing should be expirable when no expiry criteria is set.");
    }

    @Test
    public void testIsNotExpirable() {
        LocalCacheRepo localCacheRepo = EasyMock.createMock(LocalCacheRepo.class);
        CacheExpiryChecker rootExpirable = EasyMock.createMock(CacheExpiryChecker.class);
        CacheExpiryChecker denyingExpirable = EasyMock.createMock(CacheExpiryChecker.class);
        EasyMock.expect(denyingExpirable.isExpirable(EasyMock.anyObject(RepoType.class), EasyMock.anyObject(),
                EasyMock.anyObject())).andReturn(false).times(1);
        EasyMock.expect(rootExpirable.isExpirable(EasyMock.anyObject(RepoType.class), EasyMock.anyObject(),
                EasyMock.anyObject())).andReturn(false).times(1);

        Map<String, CacheExpiryChecker> expirableHashMap = Maps.newHashMap();
        expirableHashMap.put("root", rootExpirable);
        expirableHashMap.put("denying", denyingExpirable);

        ArtifactoryApplicationContext context = EasyMock.createMock(ArtifactoryApplicationContext.class);
        EasyMock.expect(context.beansForType(CacheExpiryChecker.class)).andReturn(expirableHashMap).times(2);

        EasyMock.replay(localCacheRepo, rootExpirable, denyingExpirable, context);
        CacheExpiryImpl cacheExpiry = new CacheExpiryImpl();
        cacheExpiry.setApplicationContext(context);
        cacheExpiry.setBeanName("impl");

        cacheExpiry.init();
        assertFalse(cacheExpiry.isExpirable(RepoType.Maven, "root", "asdfasdf"), "Path should not be expirable.");
        EasyMock.verify(localCacheRepo, rootExpirable, denyingExpirable, context);
    }

    @Test
    public void testIsExpirable() {
        LocalCacheRepo localCacheRepo = EasyMock.createMock(LocalCacheRepo.class);
        CacheExpiryChecker rootExpirable = EasyMock.createMock(CacheExpiryChecker.class);
        CacheExpiryChecker acceptingExpirable = EasyMock.createMock(CacheExpiryChecker.class);
        EasyMock.expect(acceptingExpirable.isExpirable(EasyMock.anyObject(RepoType.class), EasyMock.anyObject(),
                EasyMock.anyObject())).andReturn(true).times(1);
        EasyMock.expect(rootExpirable.isExpirable(EasyMock.anyObject(RepoType.class), EasyMock.anyObject(),
                EasyMock.anyObject())).andReturn(false).anyTimes();

        Map<String, CacheExpiryChecker> expirableHashMap = Maps.newHashMap();
        expirableHashMap.put("root", rootExpirable);
        expirableHashMap.put("accepting", acceptingExpirable);

        ArtifactoryApplicationContext context = EasyMock.createMock(ArtifactoryApplicationContext.class);
        EasyMock.expect(context.beansForType(CacheExpiryChecker.class)).andReturn(expirableHashMap).times(2);

        EasyMock.replay(localCacheRepo, rootExpirable, acceptingExpirable, context);
        CacheExpiryImpl cacheExpiry = new CacheExpiryImpl();
        cacheExpiry.setApplicationContext(context);
        cacheExpiry.setBeanName("impl");

        cacheExpiry.init();
        assertTrue(cacheExpiry.isExpirable(RepoType.Maven, "root", "asdfasdf"), "Path should be expirable.");
        EasyMock.verify(localCacheRepo, rootExpirable, acceptingExpirable, context);
    }

    @Test
    public void testIsLocalGenerated() {
        TestUtils.setLoggingLevel("org.artifactory.repo.cache.expirable", Level.TRACE);

        IsLocalGenerated reject = (repoType, key, path) -> false;
        IsLocalGenerated nameJunk = (repoType, key, path) -> path.equalsIgnoreCase("junk");
        IsLocalGenerated nexus = (repoType, key, path) -> repoType.isMavenGroup() &&
                path.equalsIgnoreCase("NEXUS_INDEX_PREFIX");
        IsLocalGenerated nexusInMyRepo = (repoType, key, path) -> repoType.isMavenGroup() && key.equals("maven") &&
                path.equalsIgnoreCase("NEXUS_INDEX_PREFIX");

        Assert.assertFalse(CacheExpiryImpl
                .isMatchAnyLocalGenerated(Stream.of(reject, nexus), RepoType.Maven, "maven",
                        "aFile"));
        Assert.assertTrue(CacheExpiryImpl
                .isMatchAnyLocalGenerated(Stream.of(reject, nexus), RepoType.Maven, "maven",
                        "NEXUS_INDEX_PREFIX"));
        Assert.assertTrue(CacheExpiryImpl
                .isMatchAnyLocalGenerated(Stream.of(reject, nexusInMyRepo), RepoType.Maven,
                        "maven", "NEXUS_INDEX_PREFIX"));
        Assert.assertTrue(CacheExpiryImpl
                .isMatchAnyLocalGenerated(Stream.of(reject, nexusInMyRepo), RepoType.SBT,
                        "maven", "NEXUS_INDEX_PREFIX"));
        Assert.assertFalse(CacheExpiryImpl
                .isMatchAnyLocalGenerated(Stream.of(reject, nexusInMyRepo), RepoType.SBT,
                        "sbt", "NEXUS_INDEX_PREFIX"));
        Assert.assertTrue(CacheExpiryImpl
                .isMatchAnyLocalGenerated(Stream.of(reject, nameJunk), RepoType.Maven,
                        "maven", "junk"));
        Assert.assertTrue(CacheExpiryImpl.isMatchAnyLocalGenerated(
                Stream.of(reject, reject, reject, reject, reject, reject, reject, reject, reject, reject, nameJunk,
                        nexus), RepoType.Maven, "maven", "junk"));
        Assert.assertFalse(CacheExpiryImpl.isMatchAnyLocalGenerated(Stream.empty(), RepoType.Maven, "maven", "junk"));

        TestUtils.setLoggingLevel("org.artifactory.repo.cache.expirable", Level.INFO);
    }
}