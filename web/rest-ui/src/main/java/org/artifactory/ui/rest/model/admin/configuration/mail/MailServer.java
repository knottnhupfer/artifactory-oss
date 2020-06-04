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

package org.artifactory.ui.rest.model.admin.configuration.mail;

import org.artifactory.descriptor.mail.MailServerDescriptor;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.util.JsonUtil;

/**
 * @author Chen Keinan
 */
public class MailServer extends MailServerDescriptor implements RestModel {

    private String testReceipt;

    public String getTestReceipt() {
        return testReceipt;
    }

    public void setTestReceipt(String testReceipt) {
        this.testReceipt = testReceipt;
    }

    public MailServer() {
        super.setEnabled(false);
    }

    public MailServer(MailServerDescriptor mailServerDescriptor) {
        super.setEnabled(mailServerDescriptor.isEnabled());
        super.setHost(mailServerDescriptor.getHost());
        super.setPassword(mailServerDescriptor.getPassword());
        super.setUsername(mailServerDescriptor.getUsername());
        super.setArtifactoryUrl(mailServerDescriptor.getArtifactoryUrl());
        super.setFrom(mailServerDescriptor.getFrom());
        super.setPort(mailServerDescriptor.getPort());
        super.setTls(mailServerDescriptor.isTls());
        super.setSsl(mailServerDescriptor.isSsl());
        super.setSubjectPrefix(mailServerDescriptor.getSubjectPrefix());
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
