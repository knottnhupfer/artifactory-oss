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

package org.artifactory.api.config;

import lombok.NoArgsConstructor;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.crypto.CryptoHelper;
import org.artifactory.descriptor.mail.MailServerDescriptor;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * The descriptor of the mail server configuration
 *
 * @author Lior Gur
 */
@NoArgsConstructor
public class MailServerModel {

    private boolean enabled;
    private String host;
    private int port;
    private String username;
    private String password;
    private String from;
    @JsonProperty("subject_prefix")
    private String subjectPrefix;
    @JsonProperty("artifactory_url")
    private String artifactoryUrl;
    private boolean tls;
    private boolean ssl;

    public MailServerModel(MailServerDescriptor descriptor) {
        if (descriptor == null) {
            return;
        }
        this.enabled = descriptor.isEnabled();
        this.host = descriptor.getHost();
        this.port = descriptor.getPort();
        this.username = descriptor.getUsername();
        this.password = descriptor.getPassword();
        this.from = descriptor.getFrom();
        this.subjectPrefix = descriptor.getSubjectPrefix();
        this.artifactoryUrl = descriptor.getArtifactoryUrl();
        this.tls = descriptor.isTls();
        this.ssl = descriptor.isSsl();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return CryptoHelper.decryptIfNeeded(ArtifactoryHome.get(),password);
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getSubjectPrefix() {
        return subjectPrefix;
    }

    public void setSubjectPrefix(String subjectPrefix) {
        this.subjectPrefix = subjectPrefix;
    }

    public boolean isTls() {
        return tls;
    }

    public void setTls(boolean tls) {
        this.tls = tls;
    }

    public boolean isSsl() {
        return ssl;
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    /**
     * Artifactory URL that will be used <b>exclusively<b/> in <b>EMAILS ONLY!<b/>
     *
     * @return The Artifactory URL.
     */
    public String getArtifactoryUrl() {
        return artifactoryUrl;
    }

    public void setArtifactoryUrl(String artifactoryUrl) {
        this.artifactoryUrl = artifactoryUrl;
    }
}
