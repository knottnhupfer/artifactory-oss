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

package org.artifactory.util;

import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.repo.ReverseProxyDescriptor;
import org.artifactory.descriptor.repo.ReverseProxyMethod;
import org.artifactory.descriptor.repo.WebServerType;

/**
 * @author Matan Gottlieb
 */
public class ConfReverseProxyHelper {

    public static ReverseProxyDescriptor getReverseProxyDescriptor() {
        ArtifactoryContext artifactoryContext = ContextHelper.get();
        if(artifactoryContext == null){
            return null;
        }
        CentralConfigDescriptor res = artifactoryContext.getCentralConfig().getDescriptor();
        if (res==null) {
            return null;
        }
        return res.getCurrentReverseProxy();
    }

    /**
     * Default Docker repository path prefix - enabled.
     * unless we do have explicit method we will default to REPOPATHPREFIX
     */
    public static ReverseProxyMethod getReverseProxyMethod() {
        ReverseProxyMethod defaultMethod = ReverseProxyMethod.SUBDOMAIN;
        ReverseProxyDescriptor currentReverseProxy = getReverseProxyDescriptor();
        if (currentReverseProxy == null) {
            return defaultMethod;
        }
        ReverseProxyMethod res = currentReverseProxy.getDockerReverseProxyMethod();
        if (ReverseProxyMethod.NOVALUE.equals(defaultMethod)) {
            return defaultMethod;
        }
        return res;
    }

    static  WebServerType getReverseProxyType() {
        WebServerType defaultMethod = WebServerType.DIRECT;
        ReverseProxyDescriptor currentReverseProxy = getReverseProxyDescriptor();
        if (currentReverseProxy == null) {
            return defaultMethod;
        }
        return currentReverseProxy.getWebServerType();
    }

}
