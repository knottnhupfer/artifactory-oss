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

package org.artifactory.api.rest.replication;

import org.artifactory.api.rest.restmodel.IModel;

import java.io.Serializable;

/**
 * @author mamo
 */
public class ReplicationConfigRequest implements Serializable, IModel {

    //local/remote
    private Boolean enabled;
    private String cronExp;
    private Boolean syncDeletes;
    private Boolean syncProperties;
    private String pathPrefix;
    private Boolean checkBinaryExistenceInFilestore;

    //local only
    private String url;
    private Boolean enableEventReplication;
    private String username;
    private String password;
    private String proxy;
    private Integer socketTimeoutMillis;
    private Boolean syncStatistics;

    public Boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getCronExp() {
        return cronExp;
    }

    public void setCronExp(String cronExp) {
        this.cronExp = cronExp;
    }

    public Boolean getSyncDeletes() {
        return syncDeletes;
    }

    public void setSyncDeletes(Boolean syncDeletes) {
        this.syncDeletes = syncDeletes;
    }

    public Boolean getSyncProperties() {
        return syncProperties;
    }

    public void setSyncProperties(Boolean syncProperties) {
        this.syncProperties = syncProperties;
    }

    public Boolean getSyncStatistics() {
        return syncStatistics;
    }

    public void setSyncStatistics(Boolean syncStatistics) {
        this.syncStatistics = syncStatistics;
    }

    public String getPathPrefix() {
        return pathPrefix;
    }

    public void setPathPrefix(String pathPrefix) {
        this.pathPrefix = pathPrefix;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Boolean isCheckBinaryExistenceInFilestore() {
        return checkBinaryExistenceInFilestore;
    }

    public void setCheckBinaryExistenceInFilestore(Boolean checkBinaryExistenceInFilestore) {
        this.checkBinaryExistenceInFilestore = checkBinaryExistenceInFilestore;
    }

    public Boolean getEnableEventReplication() {
        return enableEventReplication;
    }

    public void setEnableEventReplication(Boolean enableEventReplication) {
        this.enableEventReplication = enableEventReplication;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getProxy() {
        return proxy;
    }

    public void setProxy(String proxy) {
        this.proxy = proxy;
    }

    public Integer getSocketTimeoutMillis() {
        return socketTimeoutMillis;
    }

    public void setSocketTimeoutMillis(Integer socketTimeoutMillis) {
        this.socketTimeoutMillis = socketTimeoutMillis;
    }
}
