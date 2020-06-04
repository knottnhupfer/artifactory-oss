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

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.artifactory.model.xstream.security.PrincipalPermissionImpl;
import org.artifactory.security.*;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Loads the ACLs from Access according to Acl type
 *
 * @author Yuval Reches
 * @author nadavy
 */
public class AclCacheLoader<T extends PermissionTarget> implements Callable<AclCacheLoader.AclCacheItem<T>> {

    // Used to get entities from Access
    private final Supplier<Collection<Acl<T>>> findAll;
    // Used to get repoKeys
    private Function<T, List<String>> getEntityList;

    // HashMap<User/Group name, Map<RepoKey, Set<AclInfo>>
    private static class PrincipalToPermissions<T extends PermissionTarget> extends HashMap<String, Map<String, Set<PrincipalPermission<T>>>> {
    }

    public AclCacheLoader(Supplier<Collection<Acl<T>>> findDownstream, Function<T, List<String>> getEntityList) {
        this.findAll = findDownstream;
        this.getEntityList = getEntityList;
    }

    /**
     * gets and updates AclCache from DB, when call by db promotion
     * This call creates user and group ACL cache mappers
     * key: user/group name
     * value: map of user/group repoKeys of given user/group to respected entity ACL (Access Control List)
     * ACL will still have all of their ACE (Access Control Entries)
     *
     * @return up-to-date AclCacheItem
     */
    @Override
    public AclCacheItem<T> call() {
        Collection<Acl<T>> allAcls = findAll.get();

        Map<String, Acl<T>> aclResultMap = Maps.newHashMapWithExpectedSize(allAcls.size());
        // user and group cache mappers. mapper key is username/group name
        // mapper value is a mapper of repoKeys to a set of AclInfos
        PrincipalToPermissions<T> userResultMap = new PrincipalToPermissions<>();
        PrincipalToPermissions<T> groupResultMap = new PrincipalToPermissions<>();

        for (Acl<T> acl : allAcls) {
            PermissionTarget permissionTarget = acl.getPermissionTarget();
            Set<AceInfo> dbAces = acl.getAces();

            for (AceInfo ace : dbAces) {
                addToMap(ace.isGroup() ? groupResultMap : userResultMap, acl, ace);
            }

            aclResultMap.put(permissionTarget.getName(), acl);
        }
        return new AclCacheItem<>(aclResultMap, userResultMap, groupResultMap);
    }

    private void addToMap(PrincipalToPermissions<T> resultMap, Acl<T> acl, AceInfo ace) {
        if (ace != null && ace.getPrincipal() != null) {
            // populate group result map with given ace
            addPermissionTargetToResultMap(resultMap, ace, acl);
        }
    }

    /**
     * Creates or add a user/group map to a aclInfo in AclCache user or group cache.
     *
     * @param resultMap group or user result map to add repokey/aclInfo to
     * @param aceInfo   the ace information
     * @param acl       aclInfo to add for value
     */
    private void addPermissionTargetToResultMap(PrincipalToPermissions<T> resultMap, AceInfo aceInfo, Acl<T> acl) {
        Map<String, Set<PrincipalPermission<T>>> entityMap = resultMap.computeIfAbsent(aceInfo.getPrincipal(), map -> Maps.newHashMap()); // The user/group
        List<String> entities = getEntityList.apply(acl.getPermissionTarget()); // The repoKeys
        entities.forEach(entity -> addEntityNameToMap(entityMap, entity, acl, aceInfo));
    }

    /**
     * Add repoKey/buildName to a user/group cache mapper, with an AclInfo
     *
     * @param map        specific user/group map of repo keys to aclInfos set
     * @param entityName entity(repoKey/buildName) to add
     * @param acl        aclInfo to add
     * @param aceInfo    the ace information
     */
    private void addEntityNameToMap(Map<String, Set<PrincipalPermission<T>>> map, String entityName, Acl<T> acl, AceInfo aceInfo) {
        Set<PrincipalPermission<T>> permissions = map.computeIfAbsent(entityName, (String info) -> Sets.newHashSet());
        PrincipalPermission<T> principalPermission = new PrincipalPermissionImpl<>(acl.getPermissionTarget(), aceInfo);
        permissions.add(principalPermission);
    }

    public static class AclCacheItem<T extends PermissionTarget> implements BasicCacheModel {
        // acl name to acl info.
        private final Map<String, Acl<T>> aclInfoMap;
        // Maps of user/group name to -> map of repoPath/buildName to aclInfo
        private final PrincipalToPermissions<T> userResultMap;
        private final PrincipalToPermissions<T> groupResultMap;
        private long version;

        AclCacheItem(Map<String, Acl<T>> aclInfoMap, PrincipalToPermissions<T> userResultMap,
                PrincipalToPermissions<T> groupResultMap) {
            this.aclInfoMap = aclInfoMap;
            this.userResultMap = userResultMap;
            this.groupResultMap = groupResultMap;
        }

        @Override
        public long getVersion() {
            return version;
        }

        @Override
        public void setVersion(long version) {
            this.version = version;
        }

        public Map<String, Acl<T>> getAclInfoMap() {
            return aclInfoMap;
        }

        public Map<String, Map<String, Set<PrincipalPermission<T>>>> getUserResultMap() {
            return userResultMap;
        }

        public Map<String, Map<String, Set<PrincipalPermission<T>>>> getGroupResultMap() {
            return groupResultMap;
        }

        @Override
        public void destroy() {

        }
    }
}