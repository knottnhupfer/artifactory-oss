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

package org.artifactory.storage.db.security.service;

import com.google.common.collect.Ordering;
import edu.emory.mathcs.backport.java.util.Collections;
import org.artifactory.security.BuildAcl;
import org.artifactory.security.PermissionTargetAcls;
import org.artifactory.security.ReleaseBundleAcl;
import org.artifactory.security.RepoAcl;
import org.jfrog.common.StreamSupportUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.testng.Assert.assertTrue;

/**
 * UnitTest of AclCacheLoader, especially the call method
 * mock the dao to create AclCache
 *
 * @author Omri Ziv
 */
@Test
public class AllAclCacheLoaderTest extends AclCacheLoaderTest {

    private AllAclCacheLoader.AllAclCacheItem allAclCacheItem;

    private final Supplier<Collection<RepoAcl>> getRepoAcls = this::getDownstreamAllRepoAcls;
    private final Supplier<Collection<BuildAcl>> getBuildAcls = this::getDownstreamAllBuildAcls;
    private final Supplier<Collection<ReleaseBundleAcl>> getReleaseBundleAcls = Collections::emptyList;

    /**
     * Create DAO mocks, populate new AclCache and call AclCache.
     */
    @BeforeClass
    public void populateAclInfo() {

        AllAclCacheLoader cacheLoader = new AllAclCacheLoader(getBuildAcls, getRepoAcls, getReleaseBundleAcls);
        allAclCacheItem = cacheLoader.call();
    }

    /**
     * Assert the different AclCacheLoader caches- groups and users
     */
    public void testAclCacheLoader() {

        Map<Character, List<PermissionTargetAcls>> map = allAclCacheItem.getAclInfosMap();
        assertTrue(Ordering.natural().isOrdered(map.keySet()));
        List<String> premissionTargetNames = StreamSupportUtils.stream(map.get('r'))
                .map(PermissionTargetAcls::getPermissionTargetName)
                .collect(Collectors.toList());
        assertTrue(Ordering.natural().isOrdered(premissionTargetNames));

        Map<Character, List<PermissionTargetAcls>> reverseAclInfosMap = allAclCacheItem.getReverseAclInfosMap();
        assertTrue(Ordering.natural().reverse().isOrdered(reverseAclInfosMap.keySet()));
        premissionTargetNames = StreamSupportUtils.stream(reverseAclInfosMap.get('r'))
                .map(PermissionTargetAcls::getPermissionTargetName)
                .collect(Collectors.toList());
        assertTrue(Ordering.natural().reverse().isOrdered(premissionTargetNames));
    }

    private Collection<RepoAcl> getDownstreamAllRepoAcls() {
        Collection<RepoAcl> aclInfos = Lists.newArrayList();
        aclInfos.add(getAnyAcl());
        aclInfos.add(getRepo1and2Acl());
        aclInfos.add(getRepo2Acl());
        return aclInfos;
    }

    private Collection<BuildAcl> getDownstreamAllBuildAcls() {
        return Lists.newArrayList();
    }

}
