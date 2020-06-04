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

package org.artifactory.addon.nuget;

import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.common.ConstantValues;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.jfrog.client.util.PathUtils;

import java.util.List;

/**
 * Shared authentication util for the NuGet Resource and the NuGet anonymous auth interceptor.
 *
 * @author Dan Feldman
 */
public class NuGetAuthUtil {

    public static boolean repoAllowsAnonymousRootGet(String repoKey) {
        boolean allow = false;
        String allowRootGet = ConstantValues.nuGetAllowRootGetWithAnon.getString();
        if ("true".equalsIgnoreCase(allowRootGet) || "all".equalsIgnoreCase(allowRootGet)) {
            allow = true;
        } else {
            List<String> repoKeys = PathUtils.delimitedListToStringList(allowRootGet, ",");
            for (String key : repoKeys) {
                if (key.contains("*")) {
                    if (repoKey.matches(key)) {
                        allow = true;
                    }
                } else if (key.equals(repoKey)) {
                    allow = true;
                }
            }
        }
        return allow && !forceAuthIsNeededForRepo(repoKey);
    }

    public static boolean forceAuthIsNeededForRepo(String repoKey) {
        //Check for nuget force auth on repo descriptor
        boolean forceAuth = false;
        //Need both due to RTFACT-4891
        RepositoryService repoService = ContextHelper.get().beanForType(RepositoryService.class);
        RepoDescriptor descriptor = repoService.repoDescriptorByKey(repoKey);
        RepoDescriptor virtual = repoService.virtualRepoDescriptorByKey(repoKey);
        if (descriptor != null) {
            forceAuth = descriptor.isForceNugetAuthentication();
        } else if (virtual != null) {
            forceAuth = virtual.isForceNugetAuthentication();
        }

        //Fallback to force auth system property if not set on descriptor
        if (!forceAuth && ConstantValues.nuGetRequireAuthentication.isSet()) {
            String requireAuth = ConstantValues.nuGetRequireAuthentication.getString();
            if ("true".equalsIgnoreCase(requireAuth) || "all".equalsIgnoreCase(requireAuth)) {
                forceAuth = true;
            } else {
                List<String> repoKeys = PathUtils.delimitedListToStringList(requireAuth, ",");
                for (String key : repoKeys) {
                    if (key.contains("*")) {
                        if (repoKey.matches(key)) {
                            forceAuth = true;
                            break;
                        }
                    } else if (key.equals(repoKey)) {
                        forceAuth = true;
                        break;
                    }
                }
            }
        }
        return forceAuth;
    }
}
