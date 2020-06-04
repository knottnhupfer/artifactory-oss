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

import org.artifactory.descriptor.security.ldap.group.LdapGroupSetting;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.model.RestSpecialFields;
import org.artifactory.rest.common.service.IgnoreSpecialFields;
import org.artifactory.rest.common.util.JsonUtil;
import org.codehaus.jackson.map.annotate.JsonFilter;
import org.jfrog.common.config.diff.DiffIgnore;

/**
 * @author Chen Keinan
 */
@JsonFilter("exclude fields")
@IgnoreSpecialFields({"descriptionAttribute", "subTree", "groupMemberAttribute", "groupNameAttribute", "groupBaseDn", "filter", "enabled"})
public class LdapGroupModel extends LdapGroupSetting implements RestModel, RestSpecialFields {
    @DiffIgnore
    private boolean isView;

    public LdapGroupModel() {
    }

    public LdapGroupModel(LdapGroupSetting ldapGroupSetting, boolean isView) {
        if (ldapGroupSetting != null) {
            super.setName(ldapGroupSetting.getName());
            super.setStrategy(ldapGroupSetting.getStrategy());
            super.setEnabledLdap(ldapGroupSetting.getEnabledLdap());
            if (isView) {
                this.isView = isView;
            } else {
                super.setDescriptionAttribute(ldapGroupSetting.getDescriptionAttribute());
                super.setFilter(ldapGroupSetting.getFilter());
                super.setGroupBaseDn(ldapGroupSetting.getGroupBaseDn());
                super.setGroupMemberAttribute(ldapGroupSetting.getGroupMemberAttribute());
                super.setGroupNameAttribute(ldapGroupSetting.getGroupNameAttribute());
                super.setSubTree(ldapGroupSetting.isSubTree());
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(this.getClass().getSuperclass().getSimpleName().equals(o.getClass().getSimpleName()))) {
            return false;
        }
        LdapGroupSetting that = (LdapGroupSetting) o;
        return this.getName().equals(that.getName());
    }

    @Override
    public int hashCode() {
        return this.getName().hashCode();
    }
}
