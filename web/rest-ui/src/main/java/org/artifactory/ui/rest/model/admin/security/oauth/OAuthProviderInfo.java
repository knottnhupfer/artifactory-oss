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

package org.artifactory.ui.rest.model.admin.security.oauth;

import org.artifactory.api.rest.restmodel.IModel;

/**
 * @author Gidi Shabat
 */
public class OAuthProviderInfo implements IModel {
    private String displayName;
    private String type;
    private String[] mandatoryFields;
    private String[] fieldHolders;
    private String[] fieldsValues;


    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String[] getMandatoryFields() {
        return mandatoryFields;
    }

    public void setMandatoryFields(String[] mandatoryFields) {
        this.mandatoryFields = mandatoryFields;
    }

    public String[] getFieldHolders() {
        return fieldHolders;
    }

    public void setFieldHolders(String[] fieldHolders) {
        this.fieldHolders = fieldHolders;
    }

    public String[] getFieldsValues() {
        return fieldsValues;
    }

    public void setFieldsValues(String[] fieldsValues) {
        this.fieldsValues = fieldsValues;
    }
}

