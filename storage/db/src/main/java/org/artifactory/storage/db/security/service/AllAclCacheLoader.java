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

import com.google.common.collect.Lists;
import org.artifactory.api.bintray.Repo;
import org.artifactory.security.*;
import org.jfrog.common.StreamSupportUtils;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Loads All ACLs from Access mapped to first characters of permission target
 *
 * @author Omri Ziv
 */
public class AllAclCacheLoader implements Callable<AllAclCacheLoader.AllAclCacheItem> {

    // Used to get entities from Access
    private final Supplier<Collection<BuildAcl>> findAllBuilds;
    // Used to get entities from Access
    private final Supplier<Collection<RepoAcl>> findAllRepos;
    // Used to get entities from Access
    private final Supplier<Collection<ReleaseBundleAcl>> findAllReleaseBundles;

    public AllAclCacheLoader(
            Supplier<Collection<BuildAcl>> findAllBuilds,
            Supplier<Collection<RepoAcl>> findAllRepos,
            Supplier<Collection<ReleaseBundleAcl>> findAllReleaseBundles) {
        this.findAllBuilds = findAllBuilds;
        this.findAllRepos = findAllRepos;
        this.findAllReleaseBundles = findAllReleaseBundles;
    }

    /**
     * gets and updates all AclCaches mapped to first characters from DB, when call by db promotion
     * This call creates user and group ACL cache mappers
     * key: user/group name
     * value: map of user/group repoKeys of given user/group to respected entity ACL (Access Control List)
     * ACL will still have all of their ACE (Access Control Entries)
     *
     * @return up-to-date AllAclCacheItem
     */

    @Override
    public AllAclCacheItem call() {

        Collection<BuildAcl> aclBuildPermissionTargetMap = findAllBuilds.get();
        Collection<RepoAcl> aclRepoPermissionTargetMap = findAllRepos.get();
        Collection<ReleaseBundleAcl> aclReleaseBundlePermissionTargetMap =
                findAllReleaseBundles.get();

        Map<Character, List<PermissionTargetAcls>> charMap = getCharacterListMap(aclBuildPermissionTargetMap,
                aclRepoPermissionTargetMap, aclReleaseBundlePermissionTargetMap);

        Map<Character, List<PermissionTargetAcls>> reverseCharMap = toReverse(charMap);
        return new AllAclCacheItem(charMap, reverseCharMap);
    }

    private Map<Character, List<PermissionTargetAcls>> toReverse(Map<Character, List<PermissionTargetAcls>> charMap) {
        Map<Character, List<PermissionTargetAcls>> reversedMap = new TreeMap<>(Collections.reverseOrder());
        reversedMap.putAll(charMap);
        return StreamSupportUtils.mapEntriesStream(reversedMap)
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> Lists.reverse(entry.getValue())));
    }

    private Map<Character, List<PermissionTargetAcls>> getCharacterListMap(
            Collection<BuildAcl> aclBuildPermissionTargetMap,
            Collection<RepoAcl> aclRepoPermissionTargetMap,
            Collection<ReleaseBundleAcl> aclReleaseBundlePermissionTargetMap) {
        Map<String, PermissionTargetAcls> map = new TreeMap<>();

        // Add all acl build permission targets.
        StreamSupportUtils.stream(aclBuildPermissionTargetMap)
                .forEach(stringAclEntry -> {
                    String permissionTargetName = stringAclEntry.getPermissionTarget().getName();
                    if (!map.containsKey(permissionTargetName)) {
                        map.put(permissionTargetName, new PermissionTargetAcls(permissionTargetName));
                    }
                    map.get(permissionTargetName).setBuildAcl((BuildAcl) stringAclEntry);
                });

        // Add all acl repo permission targets.
        StreamSupportUtils.stream(aclRepoPermissionTargetMap)
                .forEach(stringAclEntry -> {
                    String permissionTargetName = stringAclEntry.getPermissionTarget().getName();
                    if (!map.containsKey(permissionTargetName)) {
                        map.put(permissionTargetName, new PermissionTargetAcls(permissionTargetName));
                    }
                    map.get(permissionTargetName).setRepoAcl(stringAclEntry);
                });

        // Add all acl release bundles permission targets.
        StreamSupportUtils.stream(aclReleaseBundlePermissionTargetMap)
                .forEach(stringAclEntry -> {
                    String permissionTargetName = stringAclEntry.getPermissionTarget().getName();
                    if (!map.containsKey(permissionTargetName)) {
                        map.put(permissionTargetName, new PermissionTargetAcls(permissionTargetName));
                    }
                    map.get(permissionTargetName).setReleaseBundleAcl(stringAclEntry);
                });

        return StreamSupportUtils.stream(map.values())
                .sorted(PermissionTargetAcls::compareToIgnoreCase)
                .collect(Collectors.groupingBy(permissionTargetAcls ->
                        permissionTargetAcls.getPermissionTargetName().toLowerCase().charAt(0),
                        TreeMap::new, Collectors.toList()));
    }

    public static class AllAclCacheItem implements BasicCacheModel {

        private final Map<Character, List<PermissionTargetAcls>> aclInfosMap;
        private final Map<Character, List<PermissionTargetAcls>> reverseAclInfosMap;

        private long version;

        AllAclCacheItem(Map<Character, List<PermissionTargetAcls>> aclInfosMap,
                Map<Character, List<PermissionTargetAcls>> reverseAclInfosMap) {
            this.aclInfosMap = aclInfosMap;
            this.reverseAclInfosMap = reverseAclInfosMap;
        }

        @Override
        public long getVersion() {
            return version;
        }

        @Override
        public void setVersion(long version) {
            this.version = version;
        }

        @Override
        public void destroy() {
            // nop
        }

        public Map<Character, List<PermissionTargetAcls>> getAclInfosMap() {
            return aclInfosMap;
        }

        public Map<Character, List<PermissionTargetAcls>> getReverseAclInfosMap() {
            return reverseAclInfosMap;
        }

    }
}