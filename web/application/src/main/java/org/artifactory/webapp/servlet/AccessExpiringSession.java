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
package org.artifactory.webapp.servlet;

import com.google.common.collect.Maps;
import org.springframework.session.ExpiringSession;

import java.util.Map;
import java.util.Set;

/**
 * @author gidis
 */
public class AccessExpiringSession implements ExpiringSession {
    private final String token;
    private final long expirationTime;
    private final long creationTime;
    private long lastAccessedTime;
    private int maxInactiveIntervalInSeconds;
    private Map<String,Object> attributes= Maps.newHashMap();


    public AccessExpiringSession(String token) {
        this.token = token;
        creationTime=System.currentTimeMillis();
        expirationTime=creationTime+1000*60*2;
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }

    @Override
    public void setLastAccessedTime(long lastAccessedTime) {
        this.lastAccessedTime = lastAccessedTime;
    }

    @Override
    public long getLastAccessedTime() {
        return lastAccessedTime;
    }

    @Override
    public void setMaxInactiveIntervalInSeconds(int interval) {
        this.maxInactiveIntervalInSeconds = interval;
    }

    @Override
    public int getMaxInactiveIntervalInSeconds() {
        return maxInactiveIntervalInSeconds;
    }

    @Override
    public boolean isExpired() {
        return System.currentTimeMillis()>=expirationTime;
    }

    @Override
    public String getId() {
        return token;
    }

    @Override
    public <T> T getAttribute(String attributeName) {
        return (T) attributes.get(attributeName);
    }

    @Override
    public Set<String> getAttributeNames() {
        return attributes.keySet();
    }

    @Override
    public void setAttribute(String attributeName, Object attributeValue) {
        attributes.put(attributeName,attributeValue);
    }

    @Override
    public void removeAttribute(String attributeName) {
        attributes.remove(attributeName);
    }
}
