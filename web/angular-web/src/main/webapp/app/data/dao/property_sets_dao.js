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
export function PropertySetsDao(ArtifactoryDaoFactory, RESOURCE) {
    return ArtifactoryDaoFactory()
    	.setPath(RESOURCE.PROPERTY_SETS + "/:action/:name")
        .setCustomActions({
            'get': {
                params: {name: '@name'}
            },
            'update': {
                params: {name: '@name'}
            },
            'delete': {
                method: 'POST',
                params: {action: 'deletePropertySet'}
            },
                'query': {
                    isArray: true,
                    params: {name: '@name', isRepoForm: '@isRepoForm'}
                }
            })
    	.getInstance();
}


export class Property {
    constructor(data) {
        data = data || {};
        data.propertyType = data.propertyType || "ANY_VALUE";

        data.predefinedValues = data.predefinedValues || [];
        angular.extend(this, data);
    }
    getDisplayType() {
        let type = Property.propertyTypesMap[this.propertyType];
        return type ? type.text : null;
    }
    getDefaultValues() {
        return _.where(this.predefinedValues, {defaultValue: true});
    }

    getPredefinedValue(value) {
        return _.findWhere(this.predefinedValues, {value: value});
    }
    addPredefinedValue(newValue) {
        this.predefinedValues.push({value: newValue, defaultValue: false});
    }
}

// Create an array and map of types for easy access
let anyValue = {value: 'ANY_VALUE', text: 'Any Value'};
let singleSelect = {value: 'SINGLE_SELECT', text: 'Single Select'};
let multiSelect = {value: 'MULTI_SELECT', text: 'Multi Select'};

Property.propertyTypes = [anyValue, singleSelect, multiSelect];
Property.propertyTypesMap = {
    ANY_VALUE: anyValue,
    SINGLE_SELECT: singleSelect,
    MULTI_SELECT: multiSelect
};

export function PropertyFactory() {
    return Property;
}

export class PropertySet {
    constructor(data) {
        data = data || {};
        data.properties = data.properties || [];
        angular.extend(this, data);
        this.properties = this.properties.map((property) => new Property(property));
    }
    getPropertyByName(propertyName) {
        return _.findWhere(this.properties, {name: propertyName});
    }
    addProperty(property) {
        this.properties.push(property);
    }
    removeProperty(propertyName) {
        _.remove(this.properties, {name: propertyName});
    }
}
export function PropertySetFactory() {
    return PropertySet;
}
