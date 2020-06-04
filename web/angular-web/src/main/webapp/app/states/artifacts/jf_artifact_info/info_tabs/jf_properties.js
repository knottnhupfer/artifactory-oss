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
import EVENTS from '../../../../constants/artifacts_events.constants';
import KEYS from '../../../../constants/keys.constants';
import TOOLTIP from '../../../../constants/artifact_tooltip.constant';

class jfPropertiesController {
    constructor($q, $scope, JFrogGridFactory, ArtifactPropertyDao, JFrogEventBus, JFrogModal,
                PredefineDao, RepoPropertySetDao, JFrogNotifications, $timeout, User, GoogleAnalytics) {

        this.propertyGridOption = {};
        this.$timeout = $timeout;
        this.$q = $q;
        this.user = User;
        this.artifactoryGridFactory = JFrogGridFactory;
        this.GoogleAnalytics = GoogleAnalytics;
        this.artifactPropertyDao = ArtifactPropertyDao.getInstance();
        this.predefineDao = PredefineDao.getInstance();
        this.repoPropertySetDao = RepoPropertySetDao.getInstance();
        this.modal = JFrogModal;
        this.$scope = $scope;
        this.artifactoryNotifications = JFrogNotifications;
        this.propertyTypeKeys = KEYS.PROPERTY_TYPE;
        this.propertyType = 'Property';
        this.propertiesOptions = [];
        this.repoPropertyRecursive = {recursive: false};
        this.TOOLTIP = TOOLTIP.artifacts.browse;

        JFrogEventBus.registerOnScope($scope, EVENTS.TAB_NODE_CHANGED, (node) => {
            this.currentNode = node;
            this.clearFields();
            this._getPropertiesData();
        });

        /**
         *  config selectize inputs
         *  **/
        this.propertySetMultiValuesConfig = {
            sortField: 'text',
            maxItems: null,
            plugins: ['remove_button']
        }
        this.propertySetSingleValueConfig = {
            sortField: 'text',
            maxItems: 1
        }
        this.propertySetAnyValueConfig = {
            sortField: 'text',
            maxItems: 1,
            create: true,
            createOnBlur: true,
            persist: true
        }
    }

    $onInit() {
        this._createGrid();
        this._getPropertiesData();
        this._createModalScope();
    }

    /**
     * delete Selected properties by batch
     * **/
    deleteSelectedProperties(recursive) {
        let self = this;
        let rowSelection = this.propertyGridOption.api.selection;
        let selectedProperties = rowSelection.getSelectedRows();
        let confirmMessage = 'Are you sure you wish to delete ' + selectedProperties.length;
        confirmMessage += selectedProperties.length > 1 ? ' properties?' : ' property?';

        this.modal.confirm(confirmMessage)
            .then(() => {
                rowSelection.clearSelectedRows();
                let propertiesToDelete = selectedProperties.map(property => {
                    return {
                        name: property.name,
                        path: self.currentNode.data.path,
                        repoKey: self.currentNode.data.repoKey,
                        recursive: recursive
                    }
                });
                this.artifactPropertyDao.deleteBatch({properties:propertiesToDelete}).$promise.then(()=> {
                    this._getPropertiesData();
                })
            });
    }

    /**
     * delete single proerty
     * ***/
    deleteSingleProperty(row, recursive) {

        let json ={properties:[
            {
                name: row.name,
                path: this.currentNode.data.path,
                repoKey: this.currentNode.data.repoKey,
                recursive: recursive
            }
        ]
    }

        this.modal.confirm('Are you sure you wish to delete this property?')
            .then(() => {
                this.artifactPropertyDao.deleteBatch(json).$promise.then(()=> {
                        this._getPropertiesData();
                    })
            });
    }

    clearFields() {
        if (this.repoPropertySetSelected) {
            if (this.repoPropertySetSelected.parent) {
                delete this.repoPropertySetSelected.parent;
            }
            if (this.repoPropertySetSelected.property) {
                delete this.repoPropertySetSelected.property;
            }
            if (this.repoPropertySetSelected.value) {
                delete this.repoPropertySetSelected.value;
            }
        }
    }

    isSelected(propertyType) {
        return this.propertyType == propertyType;
    }

    setProperty(propertyType) {
        this.propertyType = propertyType;
    }

    /**
     * add Property Set to list
     * **/
    addPropertySet() {
        this._trackAddPropertyEvent('Add property set');
        if (this.repoPropertySetSelected) {
            this._savePropertySetValues(this.repoPropertySetSelected);
        }
        this.repoPropertySetSelected = '';
        this.propertyValuesOptions = [];
    }

    /**
     * add single property to list
     * **/
    addProperty() {
        this._trackAddPropertyEvent('Add property');
        let objProperty = this._createNewRepoObject(this.repoPropertySelected.name)
        delete objProperty.text;
        delete objProperty.value;
        this._savePropertyValues(objProperty);

        this.repoPropertySelected.name = '';
        this.repoPropertySelected.value = '';
    }

    _trackAddPropertyEvent(type) {
        this.GoogleAnalytics.trackEvent('Artifacts' , 'Tab - Property' , type , null , this.currentNode.data.repoPkgType , this.currentNode.data.repoType);
    }

    /**
     * pouplited values to input propertyValuesOptions
     *
     * **/
    getPropertySetValues() {
        if (this.repoPropertySetSelected) {
            this.predefineDao.get({
                name: this.repoPropertySetSelected.parent.name + "." + this.repoPropertySetSelected.property.name,
                path: this.currentNode.data.path,
                repoKey: this.currentNode.data.repoKey,
                recursive: this.recursive
            }).$promise.then((predefineValues)=> {

                    this._getPropertySetPreDefinedValues(predefineValues);
                });
        }
    }

    isCurrentPropertyType(type) {
        if (!this.repoPropertySetSelected && type === 'ANY_VALUE') {
            return true;
        }
        else if (this.repoPropertySetSelected) {
            if(!this.repoPropertySetSelected.propertyType && type === 'ANY_VALUE') {
                return true;
            }
            return this.propertyTypeKeys[this.repoPropertySetSelected.propertyType] === this.propertyTypeKeys[type]
        }
    }

    setModalData(selectedProperty, predefineValues) {
        this.modalScope.property = selectedProperty;
        this.modalScope.property.predefineValues = predefineValues ? predefineValues.predefinedValues : null;
        this.modalScope.property.selectedValues = [];
        this.modalScope.property.modalTitle = "Add New '" + selectedProperty.property.name + "' Property";
        this.modalScope.save = (property) =>this._savePropertySetValues(property);
        this._propertyFormModal();
    }


    editSelectedProperty(row) {

        let selectedProperty = row;

        this.artifactPropertyDao.get({
            name: selectedProperty.name,
            path: this.currentNode.data.path,
            repoKey: this.currentNode.data.repoKey

        }).$promise.then((currentProperty)=> {
                    //console.log('currentProperty=',currentProperty);
                this.modalScope.property = currentProperty;
                this.modalScope.property.selectedValues = this.modalScope.property.propertyType !== 'MULTI_SELECT' && _.isArray(this.modalScope.property.selectedValues) ? this.modalScope.property.selectedValues.join(';') : this.modalScope.property.selectedValues;
                this.modalScope.selectizeConfig = {
                    create: currentProperty.propertyType === 'ANY_VALUE',
                    maxItems: 1
                };
                this.modalScope.property.options = currentProperty.predefineValues && currentProperty.predefineValues.slice(0);
//                this.modalScope.property.multiValue = currentProperty.propertyType && currentProperty.propertyType === 'MULTI_SELECT';
                this.modalScope.property.modalTitle = "Edit '" + selectedProperty.name + "' Property";
                this.modalScope.property.name = selectedProperty.name;
                this.modalScope.save = (property) =>this._updatePropertySetValues(property)
                this._propertyFormModal();

            });
    }

    /**
     * build defulat template proerty
     * **/
    _createNewRepoObject(repoName) {
        return {
            multiValue: false,
            property: {name: repoName},
            text: repoName,
            value: repoName
        }
    }

    /**
     * popluted grid data and property Set list name
     * **/
    _getPropertiesData() {
        this.user.canAnnotate(this.currentNode.data.repoKey, this.currentNode.data.path).then((response) => {
            this.canAnnotate = response.data;
        });
        this.artifactPropertyDao.query({
            path: this.currentNode.data.path,
            repoKey: this.currentNode.data.repoKey
        }).$promise.then((properties) => {

                this.properties = properties.artifactProperties ? properties.artifactProperties.map(this._formatToArray) : [];
                this._createDisplayValues();
                this.propertyGridOption.setGridData(this.properties);

                this._getPropertySetData();
            });
    }

    _getPropertySetData() {
        this.repoPropertySetDao.query({
            path: this.currentNode.data.path,
            repoKey: this.currentNode.data.repoKey
        }).$promise.then((_propertyOptionList)=> {
                let propertyOptionList = [];
                _propertyOptionList.forEach((propertyOption)=> {
                    propertyOption.value = propertyOption.property.name;
                    propertyOption.text = propertyOption.property.name;
                    propertyOptionList.push(propertyOption);
                });
                this.propertiesOptions = propertyOptionList;

            });
    }

    _createModalScope() {
        this.modalScope = this.$scope.$new();
        this.modalScope.closeModal = () => this.modalInstance.close();
    }

    _createGrid() {
        this.propertyGridOption = this.artifactoryGridFactory.getGridInstance(this.$scope)
                .setColumns(this._getColumns())
                .setRowTemplate('default')
                .setMultiSelect()
                .setButtons(this._getActions())
                .setBatchActions(this._getBatchActions());
    }

    _getActions() {
        return [
            {
                icon: 'icon icon-clear',
                tooltip: 'Delete',
                callback: row => this.deleteSingleProperty(row, false),
                visibleWhen: () => this.canAnnotate && (this.currentNode.data.type == 'folder' || this.currentNode.data.type == 'repository')
            },
            {
                icon: 'icon icon-delete-versions',
                tooltip: 'Delete Recursively',
                callback: row => this.deleteSingleProperty(row, true),
                visibleWhen: () => this.canAnnotate && (this.currentNode.data.type == 'folder' || this.currentNode.data.type == 'repository')
            }
        ];
    }

    _getBatchActions() {
        return [
            {
                icon: 'clear',
                name: 'Delete',
                callback: () => this.deleteSelectedProperties(false),
                visibleWhen: () => this.canAnnotate && this.currentNode && this.currentNode.data && this.currentNode.data.getRoot().repoType !== 'virtual'
            },
            {
                icon: 'delete-recursive',
                name: 'Delete Recursively',
                callback: () => this.deleteSelectedProperties(true),
                visibleWhen: () => this.canAnnotate && (this.currentNode.data.type == 'folder' || this.currentNode.data.type == 'repository') && this.currentNode.data.getRoot().repoType !== 'virtual'
            }
        ]
    }

    _propertyFormModal() {
        this.modalInstance = this.modal.launchModal("property_modal", this.modalScope, (this.modalScope.property.propertyType != 'MULTI_SELECT' ? 'sm' : 'lg'));
    }

    _savePropertyValues(property) {
        if (this.repoPropertySelected.value.indexOf(';') >= 0) {
            property.selectedValues = _.filter(this.repoPropertySelected.value.split(';'), (val) => !!val
        )
            ;
        }
        else {
            property.selectedValues = [];
            property.selectedValues.push(this.repoPropertySelected.value);
        }
        this.artifactPropertyDao.save({
            path: this.currentNode.data.path,
            repoKey: this.currentNode.data.repoKey,
            recursive: this.repoPropertyRecursive.recursive
        }, property).$promise.then(()=> {
                this._getPropertiesData();
            });
    }

    _savePropertySetValues(property) {
        if (property.propertyType==='MULTI_SELECT') {
            this._addValuesToMulti(property, this.repoPropertySetSelected.value);
        }
        else {
            property.selectedValues = this.repoPropertySetSelected.value;
        }

        if (!property.multiValue && !_.isArray(property.selectedValues)) {
            let selectedValuesToArray = angular.copy(property.selectedValues);
            property.selectedValues = [];
            property.selectedValues.push(selectedValuesToArray);

        }

        this.artifactPropertyDao.save({
            path: this.currentNode.data.path,
            repoKey: this.currentNode.data.repoKey,
            recursive: this.repoPropertyRecursive.recursive
        }, property).$promise.then(()=> {
                    this._getPropertiesData();
                });
    }

    _addValuesToMulti(property, addedValues) {
        //console.log(property);
        let theProperty = _.findWhere(this.properties, {name: property.parent.name+'.'+property.property.name});
        if (theProperty) {
            property.selectedValues = theProperty.value.concat(addedValues);
        }
        else {
            property.selectedValues = addedValues;
        }
    }


    _updatePropertySetValues(property) {
        if (property.selectedValues.indexOf(';') >= 0) property.selectedValues = _.filter(property.selectedValues.split(';'), (val) => !!val
    )
        ;
        if (!property.multiValue && !_.isArray(property.selectedValues)) {
            let selectedValuesToArray = angular.copy(property.selectedValues);
            property.selectedValues = [];
            property.selectedValues.push(selectedValuesToArray);

        }
        //console.log(property);
        this.artifactPropertyDao.update({
            path: this.currentNode.data.path,
            repoKey: this.currentNode.data.repoKey,
            recursive: this.repoPropertyRecursive.recursive
        }, property).$promise.then(()=> {
                    this._getPropertiesData();
                    this.modalInstance.close();
                });
    }

    _getPropertySetPreDefinedValues(predefineValues) {
        this.propertyValuesOptions = [];
        predefineValues.predefinedValues.forEach((preValue)=> {
            this.propertyValuesOptions.push(this._createNewRepoObject(preValue));
            this.repoPropertySetSelected.value = [];
        });
        this.repoPropertySetSelected.value = predefineValues.selectedValues;
    }

    _getColumns() {

        let cellTemplate = `<div class="grid-items-container gridcell-content-text"
                                 id="{{row.uid}}">
                                <div class="item" 
                                     ng-if="row.entity.value.length>1" 
                                     ng-repeat="col in row.entity.value track by $index">{{col}}
                                </div>
                                <a class="gridcell-showall jf-link" 
                                   ng-if="row.entity.value.length>1 && grid.options.htmlIsOverflowing(row.uid) && !grid.options.lastHtmlElementOverflowing(row.uid)" 
                                   href 
                                   ng-click="grid.options.showAll(row.entity.value,row.entity.name,col)"> (See All)</a>
                                <a class="gridcell-showall jf-link" 
                                   ng-if="row.entity.value.length>1 && grid.options.lastHtmlElementOverflowing(row.uid)" 
                                   href 
                                   ng-click="grid.options.showAll(row.entity.value,row.entity.name,col)"> (See List)</a>
                                <div class="ui-grid-cell-contents" ng-if="row.entity.value.length==1">
                                    <span ng-if="!row.entity.displayValues[0]">{{row.entity.value[0]}}</span>
                                    <span ng-if="row.entity.displayValues[0]" ng-bind-html="row.entity.displayValues[0]"></span>
                                </div>
                            </div>`;

        let keyCellTemplate = '<div ng-if="!grid.appScope.jfProperties.canAnnotate" class="ui-grid-cell-contents">{{row.entity.name}}</div>' +
                '<div ng-if="grid.appScope.jfProperties.canAnnotate" class="ui-grid-cell-contents"><a href="" class="jf-link" ng-click="grid.appScope.jfProperties.editSelectedProperty(row.entity)">{{row.entity.name}}</a></div>'

        return [
            {
                name: "Property",
                displayName: "Property",
                field: "name",
                cellTemplate: keyCellTemplate
            },
            {
                name: "Value(s)",
                displayName: "Value(s)",
                field: "value",
                cellTemplate: cellTemplate
            }
        ]
    }

    _formatToArray(list) {
        return {name: list.name, value: _.trimRight(list.value.toString(), ';').split(';')};
    }

    _createDisplayValues() {
        var urlRegex = /^https?:\/\/[a-zA-Z0-9]+(\.)?(:[0-9]+)?.+?(?=\s|$|"|'|>|<)/;
        _.map(this.properties,(prop) => {
            let displayValues = [];
            for (let i = 0; i<prop.value.length; i++) {
                let val = prop.value[i];
                if (val.match(urlRegex)) {
                    displayValues[i] = `<a href="${val}" target="_blank">${val}</a>`;
                }
                else displayValues[i] = undefined;
            }
            prop.displayValues = displayValues;
        });
    }

    isInVirtual() {
        return this.currentNode && this.currentNode.data && this.currentNode.data.getRoot().repoType === 'virtual';
    }

}
export function jfProperties() {
    return {
        restrict: 'EA',
        controller: jfPropertiesController,
        scope: {
            currentNode: '='
        },
        controllerAs: 'jfProperties',
        bindToController: true,
        templateUrl: 'states/artifacts/jf_artifact_info/info_tabs/jf_properties.html'
    }
}