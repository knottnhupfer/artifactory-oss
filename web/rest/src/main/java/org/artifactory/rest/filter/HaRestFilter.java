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

package org.artifactory.rest.filter;

import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.ha.HaCommonAddon;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.rest.common.exception.HaNodePropagationException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;

/**
 * @author Shay Yaakov
 */
public class HaRestFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext containerRequest) {
        HaCommonAddon haAddon = ContextHelper.get().beanForType(AddonsManager.class).addonByType(HaCommonAddon.class);
        if (haAddon.isHaEnabled()) {
            String nodeId = containerRequest.getHeaderString(HaCommonAddon.ARTIFACTORY_NODE_ID);
            // decodedBasePath = request.getContextPath() + servletPath + "/"
            if (StringUtils.isNotBlank(nodeId) && containerRequest.getUriInfo().getBaseUri().toString().endsWith("/mc/")) {
                if (!StringUtils.equals(haAddon.getCurrentMemberServerId(), nodeId)) {
                    throw new HaNodePropagationException(containerRequest, nodeId);
                }
            }
        }
    }
}
