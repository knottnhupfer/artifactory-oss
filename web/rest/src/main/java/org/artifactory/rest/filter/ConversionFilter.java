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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.http.HttpStatus;
import org.artifactory.common.ConstantValues;
import org.artifactory.rest.common.exception.RestException;
import org.artifactory.storage.db.servers.service.ArtifactoryServersCommonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author Gal Ben Ami
 */
@Component
public class ConversionFilter implements ContainerRequestFilter {
    private final static Logger log = LoggerFactory.getLogger(ConversionFilter.class);

    @Autowired
    private ArtifactoryServersCommonService artifactoryServersCommonService;

    private LoadingCache<String, Boolean> cache = CacheBuilder.newBuilder()
            .maximumSize(1)
            .expireAfterAccess(ConstantValues.blockOnConversionCacheTimeoutInMillis.getLong(), TimeUnit.MILLISECONDS)
            .build(new CacheLoader<String, Boolean>() {
                @Override
                public Boolean load(String s) {
                    return isConversionRunning();
                }
            });


    @Override
    public void filter(ContainerRequestContext containerRequest) {
        try {
            doFilter(containerRequest, cache.get("isConversionRunning"));
        } catch (ExecutionException e) {
            log.error("Error in getting conversion state: ", e);
        }

    }

    private void doFilter(ContainerRequestContext containerRequest, boolean isConversionRunning) {
        if (isConversionRunning) {
            log.debug("Rejected execution of method while upgrading: " + containerRequest.getUriInfo().getPath());
            throw new RestException(HttpStatus.SC_SERVICE_UNAVAILABLE, "Can not execute this action on upgrade");
        }
    }

    private Boolean isConversionRunning() {
        return artifactoryServersCommonService.isConversionRunning();
    }
}

