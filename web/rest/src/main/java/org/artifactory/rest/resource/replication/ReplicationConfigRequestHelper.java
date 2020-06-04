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

package org.artifactory.rest.resource.replication;

import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.rest.replication.ReplicationConfigRequest;
import org.artifactory.descriptor.replication.LocalReplicationDescriptor;
import org.artifactory.descriptor.replication.RemoteReplicationDescriptor;
import org.artifactory.descriptor.replication.ReplicationBaseDescriptor;
import org.artifactory.descriptor.repo.ProxyDescriptor;
import org.artifactory.rest.exception.BadRequestException;
import org.artifactory.rest.exception.ForbiddenWebAppException;
import org.quartz.CronExpression;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * @author mamo
 */
public abstract class ReplicationConfigRequestHelper {

    public static void fillBaseReplicationDescriptor(ReplicationConfigRequest replicationRequest,
            ReplicationBaseDescriptor newReplication) {
        if (replicationRequest.isEnabled() != null) {
            newReplication.setEnabled(replicationRequest.isEnabled());
        }
        if (replicationRequest.getCronExp() != null) {
            newReplication.setCronExp(replicationRequest.getCronExp());
        }
        if (replicationRequest.getSyncDeletes() != null) {
            newReplication.setSyncDeletes(replicationRequest.getSyncDeletes());
        }
        if (replicationRequest.getSyncProperties() != null) {
            newReplication.setSyncProperties(replicationRequest.getSyncProperties());
        }
        if (replicationRequest.isCheckBinaryExistenceInFilestore() != null) {
            newReplication.setCheckBinaryExistenceInFilestore(replicationRequest.isCheckBinaryExistenceInFilestore());
        }
        if (replicationRequest.getPathPrefix() != null) {
            newReplication.setPathPrefix(replicationRequest.getPathPrefix());
        }
        if (replicationRequest.getEnableEventReplication() != null && replicationRequest.getEnableEventReplication()) {
            if (!ContextHelper.get().beanForType(AddonsManager.class).isHaLicensed() &&
                    newReplication instanceof RemoteReplicationDescriptor) {
                throw new ForbiddenWebAppException("Event based pull replication is only available with an Enterprise license.");
            }
            newReplication.setEnableEventReplication(true);
        } else {
            newReplication.setEnableEventReplication(false);
        }
    }

    public static void fillLocalReplicationDescriptor(ReplicationConfigRequest replicationRequest,
            LocalReplicationDescriptor newReplication) {
        fillBaseReplicationDescriptor(replicationRequest, newReplication);
        if (replicationRequest.getUrl() != null) {
            newReplication.setUrl(replicationRequest.getUrl());
        }
        if (replicationRequest.getEnableEventReplication() != null) {
            newReplication.setEnableEventReplication(replicationRequest.getEnableEventReplication());
        }
        if (replicationRequest.getUsername() != null) {
            newReplication.setUsername(replicationRequest.getUsername());
        }
        if (replicationRequest.getPassword() != null) {
            newReplication.setPassword(replicationRequest.getPassword());
        }
        if (replicationRequest.getSyncStatistics() != null) {
            newReplication.setSyncStatistics(replicationRequest.getSyncStatistics());
        }
        final String proxy = replicationRequest.getProxy();
        if (proxy != null) {
            try {
                CentralConfigService centralConfigService = ContextHelper.get().beanForType(CentralConfigService.class);
                List<ProxyDescriptor> proxies = centralConfigService.getDescriptor().getProxies();
                ProxyDescriptor proxyDescriptor = proxies.stream()
                        .filter(Objects::nonNull)
                        .filter(input -> proxy.equals(input.getKey()))
                        .findFirst().orElse(null);
                newReplication.setProxy(proxyDescriptor);
            } catch (NoSuchElementException e) {
                throw new BadRequestException("Could not find proxy");
            }
        }
        if (replicationRequest.getSocketTimeoutMillis() != null) {
            newReplication.setSocketTimeoutMillis(replicationRequest.getSocketTimeoutMillis());
        }
    }

    public static void fillRemoteReplicationDescriptor(ReplicationConfigRequest replicationRequest,
            RemoteReplicationDescriptor newReplication) {
        if (replicationRequest.getEnableEventReplication() != null) {
            newReplication.setEnableEventReplication(replicationRequest.getEnableEventReplication());
        }
    }

    public static void verifyBaseReplicationRequest(ReplicationBaseDescriptor replication) {
        if (StringUtils.isBlank(replication.getCronExp())) {
            throw new BadRequestException("cronExp is required");
        }
        if (!CronExpression.isValidExpression(replication.getCronExp())) {
            throw new BadRequestException("Invalid cronExp");
        }
    }

    public static void verifyLocalReplicationRequest(LocalReplicationDescriptor replication) {
        if (StringUtils.isBlank(replication.getUrl())) {
            throw new BadRequestException("url is required");
        }
        try {
            new URL(replication.getUrl());
        } catch (MalformedURLException e) {
            throw new BadRequestException("Invalid url [" + e.getMessage() + "]");
        }
        verifyBaseReplicationRequest(replication);
        if (StringUtils.isBlank(replication.getUsername())) {
            throw new BadRequestException("username is required");
        }
    }
}
