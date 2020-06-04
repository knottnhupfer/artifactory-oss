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
// modal
let $rootScope;
let JFrogModal;
let Property, PropertySet;
class PropertyFormModal {
    constructor(propertySet, property, isNew) {
        this.scope = $rootScope.$new();
        this.scope.PropertyForm = this;
        this.isNew = isNew;
        this.originalProperty = property;
        this.property = angular.copy(this.originalProperty);
        this.propertySet = propertySet;
        this.propertyTypes = Property.propertyTypes;
    }

    launch() {
        this.modalInstance = JFrogModal.launchModal('property_form_modal', this.scope)
        return this.modalInstance.result;
    }

    save() {
        angular.copy(this.property, this.originalProperty);
        this.modalInstance.close();
    }

    cancel() {
        this.modalInstance.dismiss();
    }

    isPropertyUnique(propertyName) {
        return propertyName === this.originalProperty.name || !this.propertySet.getPropertyByName(propertyName);
    }

    isPredefinedValuesValid() {
        if (this.property.propertyType === 'ANY_VALUE') return true; // Any Value allows no predefined values
        else return this.property.predefinedValues.length; // Other types must have predefined values
    }

    isDefaultValuesValid(propertyType) {
        if (propertyType === 'MULTI_SELECT') return true;
        return this.property.getDefaultValues().length < 2;
    }

    invalidateType() {
        // By changing the property we use in ui-validate-watch, we force the validation on propertyType to run again
        this.propertyTypeWatch = this.propertyTypeWatch || 0;
        this.propertyTypeWatch++;
    }

    getPredefinedValuesStr() {
        // This is for watching the propertyType value
        return JSON.stringify(this.property.predefinedValues);
    }

    removeValue(value) {
        _.remove(this.property.predefinedValues, value);
        this.invalidateType();
    }

    addValue() {
        this.newValue = $('#newPredefinedValueName').val();
        this.errorMessage = null;

        if (this._isValueEmpty(this.newValue)) {
            this.errorMessage = "Must input value";
        }
        else if (!this._isValueUnique(this.newValue)) {
            this.errorMessage = "Value already exists";
        }
        else {
            this.property.addPredefinedValue(this.newValue);
            this.newValue = null;
            $('#newPredefinedValueName').val('');
            this.invalidateType();
        }
    }

    _isValueEmpty(text) {
        return _.isEmpty(text);
    }
    _isValueUnique(text) {
        return !this.property.getPredefinedValue(text);
    }
}

export function PropertyFormModalFactory(_$rootScope_, _JFrogModal_, _Property_, _PropertySet_) {
    Property = _Property_;
    PropertySet = _PropertySet_;
    $rootScope = _$rootScope_;
    JFrogModal = _JFrogModal_;
    return PropertyFormModal;
}