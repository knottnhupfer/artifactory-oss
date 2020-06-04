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
import CONFIG_MESSAGES from "../../../../constants/configuration_messages.constants";

let $timeout, Property, PropertySet, uiGridConstants;
export class AdminConfigurationPropertySetsController {

    constructor($scope, PropertySetsDao, JFrogGridFactory, ArtifactoryFeatures, _$timeout_, _Property_, _PropertySet_, JFrogModal, _uiGridConstants_) {
        $timeout = _$timeout_;
        this.propertySetsDao = PropertySetsDao;
        this.$scope = $scope;
        this.artifactoryGridFactory = JFrogGridFactory;
        this.modal = JFrogModal;
        this.features = ArtifactoryFeatures;
        Property = _Property_;
        PropertySet = _PropertySet_;
        uiGridConstants = _uiGridConstants_;
        this.propertySets = {};
        this.noSetsMessage = this.features.isJCR() ? CONFIG_MESSAGES.admin.configuration.propertySets.noSetsMessageJCR :
                                                     CONFIG_MESSAGES.admin.configuration.propertySets.noSetsMessage;
        this._createGrid();
        this._initPropertySets();
    }

    _initPropertySets() {
        this.propertySetsDao.query().$promise.then((propertySets)=> {
            this.propertySets = propertySets.map((propertySet) => new PropertySet(propertySet));
            this.gridOptions.setGridData(this.propertySets)
        });
    }

    _createGrid() {
        this.gridOptions = this.artifactoryGridFactory.getGridInstance(this.$scope)
                .setColumns(this.getColumns())
                .setRowTemplate('default')
                .setMultiSelect()
                .setButtons(this._getActions())
                .setBatchActions(this._getBatchActions());
    }


    deletePropertySet(propertySet) {
        this.modal.confirm(`Are you sure you want to delete the property set '${propertySet.name}?'`)
            .then(() => {
                let json = {propertySetNames:[propertySet.name]};
                this.propertySetsDao.delete(json).$promise
                    .then(()=>this._initPropertySets());
            });
    }

    deleteSelectedPropertySets() {
        //Get All selected users
        let selectedRows = this.gridOptions.api.selection.getSelectedGridRows();
        this.modal.confirm(`Are you sure you want to delete ${selectedRows.length} property sets?`)
            .then(() => {
                //Create an array of the selected propertySet names
                let names = selectedRows.map(row => row.entity.name);
                //Delete bulk of property sets
                this.propertySetsDao.delete({propertySetNames: names}).$promise
                        .then(()=>this._initPropertySets());
            });
    }

    getColumns() {
        return [
            {
                field: "name",
                name: "Property Set Name",
                displayName: "Property Set Name",
                sort: {
                    direction: uiGridConstants.ASC
                },
                cellTemplate: '<div class="ui-grid-cell-contents"><a ui-sref="^.property_sets.edit({propertySetName: row.entity.name})" class="jf-link text-center ui-grid-cell-contents">{{row.entity.name}}</a></div>'
            },
            {
                field: "propertiesCount",
                name: "Properties Count",
                displayName: "Properties Count"
            }
        ]
    }

    _getActions() {
        return [
            {
                icon: 'icon icon-clear',
                tooltip: 'Delete',
                callback: propertySet => this.deletePropertySet(propertySet)
            }
        ];
    }

    _getBatchActions() {
        return [
            {
                icon: 'clear',
                name: 'Delete',
                callback: () => this.deleteSelectedPropertySets()
            },
        ]
    }

}
