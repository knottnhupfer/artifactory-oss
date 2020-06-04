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
import TOOLTIP from '../../../../constants/artifact_tooltip.constant';

// Injectables:
let $q, $scope, $state, $stateParams, JFrogGridFactory, PropertySetsDao, PropertyFormModal, Property, PropertySet, uiGridConstants, JFrogModal, ArtifactoryModelSaver;

export class AdminConfigurationPropertySetFormController {
    constructor(_$stateParams_, _$scope_, _PropertySetsDao_, _$state_, _JFrogGridFactory_, _PropertyFormModal_, ArtifactoryState, _$q_, _Property_, _PropertySet_, _uiGridConstants_, _JFrogModal_, _ArtifactoryModelSaver_) {
        $scope = _$scope_;
    	$state = _$state_;
        $stateParams = _$stateParams_;
        Property = _Property_;
        PropertySet = _PropertySet_;
        JFrogModal = _JFrogModal_;
        ArtifactoryModelSaver = _ArtifactoryModelSaver_.createInstance(this,['propertySet']);;

    	this.isNew = !$stateParams.propertySetName;
    	PropertySetsDao = _PropertySetsDao_;
        PropertyFormModal = _PropertyFormModal_;
        JFrogGridFactory = _JFrogGridFactory_;
        $q = _$q_;
        uiGridConstants = _uiGridConstants_;

        this.TOOLTIP = TOOLTIP.admin.configuration.propertySetsForm;
        this._createGrid();
        this._initPropertySet();
        ArtifactoryState.setState('prevState', $state.current);
    }

    _initPropertySet() {
        let promise;
        if (this.isNew) {
            promise = $q.when();
        }
        else {
            promise = PropertySetsDao.get({name: $stateParams.propertySetName}).$promise;
        }
        promise.then((propertySet) => {
            this.propertySet = new PropertySet(propertySet);
            ArtifactoryModelSaver.save();
            this.gridOptions.setGridData(this.propertySet.properties)
        });
    }

    _createGrid() {
        this.gridOptions = JFrogGridFactory.getGridInstance($scope)
                .setColumns(this.getColumns())
                .setRowTemplate('default')
                .setMultiSelect()
                .setButtons(this._getActions())
                .setBatchActions(this._getBatchActions());
    }

    save() {

        if (this.savePending) return;

        this.savePending = true;

        let whenSaved = this.isNew ? PropertySetsDao.save(this.propertySet) : PropertySetsDao.update(this.propertySet);
        whenSaved.$promise.then(() => {
            ArtifactoryModelSaver.save();
            this._end()
            this.savePending = false;
        }).catch(()=>this.savePending = false);
    }

	cancel() {
        this._end();
    }

    _end() {
        $state.go('^.property_sets');
    }

    editProperty(property) {
        // (Adam) Don't take the actual property object because it's different after filtering the GRID
        // Instead, we find the property in the original propertySet
        property = this.propertySet.getPropertyByName(property.name);
        this._launchPropertyEditor(property, false);
    }

    newProperty(e) {
        e.preventDefault();
        let property = new Property();
        this._launchPropertyEditor(property, true);
    }

    _launchPropertyEditor(property, isNew) {
        new PropertyFormModal(this.propertySet, property, isNew).launch()
        .then(() => {
            if (isNew) {
                this.propertySet.addProperty(property);
            }
            // (Adam) Must reset the data, because of the filter
            this.gridOptions.setGridData(this.propertySet.properties);
        });
    }

    _doDeleteProperty(property) {
        this.propertySet.removeProperty(property.name);
    }

    deleteProperty(property) {
        JFrogModal.confirm(`Are you sure you want to delete the property '${property.name}?'`)
            .then(() => {
                this._doDeleteProperty(property);
                this.gridOptions.setGridData(this.propertySet.properties);
            });
    }

    deleteSelectedProperties() {
        let selectedRows = this.gridOptions.api.selection.getSelectedGridRows();
        JFrogModal.confirm(`Are you sure you want to delete ${selectedRows.length} properties?`)
            .then(() => {
                selectedRows.forEach((row) => this._doDeleteProperty(row.entity));
                this.gridOptions.setGridData(this.propertySet.properties);
            });
    }

    getColumns() {
        return [
            {
                field: "name",
                name: "Property Name",
                displayName: "Property Name",
                sort: {
                    direction: uiGridConstants.ASC
                },
                cellTemplate: `
                    <div class="ui-grid-cell-contents">
                        <a  href=""
                            ng-click="grid.appScope.PropertySetForm.editProperty(row.entity)"
                            class="jf-link text-center ui-grid-cell-contents">{{row.entity.name}}</a>
                    </div>`
            },
            {
                name: 'Value Type',
                displayName: 'Value Type',
                field: "propertyType",
                cellTemplate: `<div class="ui-grid-cell-contents">{{ row.entity.getDisplayType() }}</div>`
            },
            {
                field: "predefinedValues",
                name: "Predefined Values",
                displayName: "Predefined Values",
                cellTemplate: `
                    <div style="padding-left: 10px;  white-space: nowrap; overflow-x: auto;">
                        <div class="item" ng-repeat="value in row.entity.predefinedValues">
                            {{value.value}}<span ng-if="value.defaultValue"> (default)</span>
                        </div>
                    </div>
                `
            }
        ]
    }

    _getActions() {
        return [
            {
                icon: 'icon icon-clear',
                tooltip: 'Delete',
                callback: propertySet => this.deleteProperty(propertySet)
            }
        ];
    }

    _getBatchActions() {
        return [
            {
                icon: 'clear',
                name: 'Delete',
                callback: () => this.deleteSelectedProperties()
            },
        ]
    }
}

