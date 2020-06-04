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

package org.artifactory.ui.rest.model.admin.security.ldap;

import org.artifactory.descriptor.security.ldap.LdapSetting;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.model.RestSpecialFields;
import org.artifactory.rest.common.service.IgnoreSpecialFields;
import org.artifactory.rest.common.util.JsonUtil;
import org.codehaus.jackson.map.annotate.JsonFilter;
import org.jfrog.common.config.diff.DiffIgnore;

/**
 * @author chen Keinan
 */
@JsonFilter("exclude fields")
@IgnoreSpecialFields({"autoCreateUser", "enabled", "emailAttribute"})
public class LdapSettingModel extends LdapSetting implements RestModel, RestSpecialFields {

    @DiffIgnore
    private boolean isView;
    private String testUsername;
    private String testPassword;

    LdapSettingModel() {
    }

    public LdapSettingModel(LdapSetting ldapSetting, boolean isView) {
        if (ldapSetting != null) {
            super.setKey(ldapSetting.getKey());
            super.setLdapUrl(ldapSetting.getLdapUrl());
            if (isView) {
                this.isView = true;
            } else {
                super.setEnabled(ldapSetting.isEnabled());
                super.setAutoCreateUser(ldapSetting.isAutoCreateUser());
                super.setEmailAttribute(ldapSetting.getEmailAttribute());
                super.setSearch(ldapSetting.getSearch());
                super.setUserDnPattern(ldapSetting.getUserDnPattern());
                super.setLdapPoisoningProtection(ldapSetting.getLdapPoisoningProtection());
                super.setAllowUserToAccessProfile(ldapSetting.isAllowUserToAccessProfile());
            }
        }
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToStringIgnoreSpecialFields(this);
    }

    @Override
    public boolean ignoreSpecialFields() {
        return isView;
    }


    public String getTestUsername() {
        return testUsername;
    }

    public void setTestUsername(String testUsername) {
        this.testUsername = testUsername;
    }

    public String getTestPassword() {
        return testPassword;
    }

    public void setTestPassword(String testPassword) {
        this.testPassword = testPassword;
    }

}
