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
import searchDescriptor from './search_descriptor';

class searchQueryMakerController {
    constructor($q, $scope,$stateParams,$timeout) {

        this.$q = $q;
        this.$stateParams = $stateParams;
        this.$timeout = $timeout;
        this.$scope = $scope;

        this.whenGotPropertiesData = $q.defer();
        this.whenInitiated = $q.defer();

        this.orderIndex = 0;
        this.initWatchers();
    }

    initWatchers() {
        this.$scope.$watch('queryMaker.searchType',(newVal,oldVal) => {
            if (newVal === 'package') return;
            this.queryDescriptor = this.compileQueryDescriptor(_.find(searchDescriptor.searchTypes,(type)=>type.searchTypeName.toLowerCase() === newVal.toLowerCase()));
            this.refreshAvailableCriteria();
            if (!_.isEqual(this.$stateParams.searchType,this.searchType)) this.initQuery();
            else if (!_.isEmpty(this.query)) this.initFromURLQuery();
            else {
                _.extend(this.query, this.queryDescriptor.staticPayload);
                if (this.query.search === 'property') {
                    this.initQuery();
                }
                this.whenInitiated.resolve();
            }

            this.focusField = false;
            this.$timeout(()=>this.focusField = true);
        });
        this.$scope.$watch('queryMaker.repoList',(newVal,oldVal) => {
            if (!newVal.length) return;
            this.whenInitiated.promise.then(()=>{
                this.selectionRepoList = _.map(newVal, (repo)=>{
                    return {
                        text: repo.repoKey,
                        iconClass: repo._iconClass,
                        isSelected: this.inititiatedByURL && this.query.selectedRepositories ? this.query.selectedRepositories.indexOf(repo.repoKey) !== -1 : false
                    }
                })
                if (this.inititiatedByURL) delete this.inititiatedByURL;
            })
        });
        this.$scope.$watch('queryMaker.propertiesData',(newVal,oldVal) => {
            if (newVal) {
                this.transformedPropertiesData = _.cloneDeep(newVal);
                this.transformedPropertiesData.forEach((prop)=>{
                    if (prop.propertyType === 'MULTI_SELECT') {
                        prop.values = _.map(prop.values,(val)=>{
                            return {
                                text: val,
                                isSelected: false
                            }
                        })
                    }
                })
                this.propertySetKeys = _.sortBy(_.map(this.transformedPropertiesData,'key'));
                this.whenGotPropertiesData.resolve();
            }
        });
    }

    initQuery() {
        for (var key in this.query) delete this.query[key];

        this.queryDescriptor.searchCriteria.forEach((criterion) => {
            if (criterion.multi && !criterion.copy) {
                this.query[criterion.field] = [];
                if (criterion.default || criterion.visible) this.query[criterion.field].push({key: '', values: []});
            }
        })

        _.extend(this.query, this.queryDescriptor.staticPayload);

        this.whenInitiated.resolve();
    }

    initFromURLQuery() {
//        console.log(this.query);
        this.whenGotPropertiesData.promise.then(()=>{
            for (var key in this.query) {
                if (key === 'search') continue;
                let criterion = _.find(this.queryDescriptor.searchCriteria,{field: key});
                if (criterion &&  criterion.multi && this.query.search === 'property') {
                    criterion.visible = false;
                    if (this.query[key]) {
                        for (let i in this.query[key]) {
                            if (i>0) {
                                let newCriterion = this._createMultiCriterionCopy(criterion);
                                this.queryDescriptor.searchCriteria.push(newCriterion)
                            }
                            else {
                                criterion.visible = true;
                            }

                            if (key === 'propertySetKeyValues') {
                                let keyVal = this.query[key][i];
                                let propData = _.find(this.transformedPropertiesData,{key: keyVal.key});
                                if (propData.propertyType === 'MULTI_SELECT') {
                                    keyVal.values.forEach((val) => {
                                        let selectionObj = _.find(propData.values,{text: val});
                                        if (selectionObj) selectionObj.isSelected = true;
                                    })
                                }
                            }
                        }
                    }
                }
                else {
                    if (criterion && !criterion.default && !criterion.multi) criterion.visible = true;
                }
            }

            _.extend(this.query, this.queryDescriptor.staticPayload);

            this.inititiatedByURL = true;
            this.refreshAvailableCriteria();
            this.$timeout(()=>{
                if (this.canSearch()) this.search()
            });

            this.whenInitiated.resolve();
        });
    }

    compileQueryDescriptor(descriptor) {
        descriptor = _.cloneDeep(descriptor);
        for (let i in descriptor.searchCriteria) {
            let criterion = descriptor.searchCriteria[i];

            if (_.isString(criterion)) {
                let global = _.find(searchDescriptor.globalSearchCriteria, {id: criterion});
                descriptor.searchCriteria[i] = _.cloneDeep(global);
            }
            else if (criterion.multi) {
                criterion.index = 0;
                criterion.nextIndex = criterion.default || criterion.visible ? 1 : 0;
                if (criterion.default) {
                    criterion.default = false;
                    criterion.visible = true;
                }
            }
            descriptor.searchCriteria[i].order = this.orderIndex;
            this.orderIndex++;
        }
        return descriptor;
    }

    refreshAvailableCriteria() {
        this.availableCriteria = this.queryDescriptor ? _.filter(this.queryDescriptor.searchCriteria,(criterion) => (!criterion.default && !criterion.visible) || (criterion.multi && !criterion.copy)) : [];
    }

    onRepoSelectionChange(criterion) {
        let selectedRepos = _.filter(this.selectionRepoList, (repo) => repo.isSelected);
        this.query[criterion.field] = _.map(selectedRepos,'text');
    }

    onAddCriteria() {

        if (this.criterionToAdd.id === 'limitRepo') {
            this.openLimitDropDown = true;
        }
        else {
            this.openLimitDropDown = false;
        }

        if (this.criterionToAdd.multi) {
            let newCriterion = this._createMultiCriterionCopy(this.criterionToAdd);
            this.queryDescriptor.searchCriteria.push(newCriterion)
            if (!this.query[newCriterion.field]) this.query[newCriterion.field] = [];
            this.query[newCriterion.field].push({key: '', values: []});

            newCriterion.order = this.orderIndex;

            newCriterion.autofocus = true;
            this.$timeout(()=>{newCriterion.autofocus = false},500);

            this.orderIndex++;

        }
        else {
            this.criterionToAdd.order = this.orderIndex;
            this.orderIndex++;

            this.criterionToAdd.visible = true;

            let tempCrit = this.criterionToAdd;
            tempCrit.autofocus = true;
            this.$timeout(()=>{tempCrit.autofocus = false},500);

            if (this.criterionToAdd.type === 'boolean') {
                this.query[this.criterionToAdd.field] = true;
            }
            else if (this.criterionToAdd.id === 'limitRepo') {
                this.query['selectedRepositories'] = [];
            }
        }
        this.criterionToAdd = null;
        this.refreshAvailableCriteria();
    }

    _createMultiCriterionCopy(orig) {
        let newCriterion = _.cloneDeep(orig);
        newCriterion.default = false;
        newCriterion.visible = true;
        newCriterion.copy = true;
        newCriterion.index = orig.nextIndex;
        orig.nextIndex++;
        delete newCriterion.nextIndex;
        return newCriterion;
    }

    clearMultiValPropSetSelectedValues(criterion) {
        if (criterion.multi && criterion.type === 'keyValSet' && this.query[criterion.field][criterion.index]) {
            let propSet = this.getPropertySetByKey(this.query[criterion.field][criterion.index].key);
            if (propSet && propSet.propertyType === 'MULTI_SELECT') {
                propSet.values.forEach((val)=>{val.isSelected=false});
            }
        }
    }
    removeCriterion(criterion) {
        this.clearMultiValPropSetSelectedValues(criterion);
        if (criterion.multi) {

            if (criterion.copy) {
                let criterionIndex = _.indexOf(this.queryDescriptor.searchCriteria,criterion);
                this.queryDescriptor.searchCriteria.splice(criterionIndex,1);
            }
            else criterion.visible = false;

            this.queryDescriptor.searchCriteria.forEach((crit)=>{
                if (crit.field === criterion.field && crit.multi && crit.copy && crit.index > criterion.index) {
                    crit.index--;
                }
                else if (crit.field === criterion.field && crit.multi && !crit.copy) {
                    crit.nextIndex--;
                }
            })
            if (this.query[criterion.field]) this.query[criterion.field].splice(criterion.index,1);

        }
        else {
            criterion.visible = false;
            delete this.query[criterion.field];
            if (criterion.id === 'limitRepo') {
                _.filter(this.selectionRepoList, (repo) => repo.isSelected).forEach((selRepo)=>{
                    selRepo.isSelected=false
                })
                delete this.query['selectedRepositories'];
            }

        }
        this.refreshAvailablePropertySetKeys();
        this.refreshAvailableCriteria();
    }

    search() {
        if (!this.canSearch()) return;
        this.$timeout(()=>this.parentController.search(), 50);
    }

    getPropertySetByKey(key) {
        return _.find(this.transformedPropertiesData, {key: key});
    }
    onMultiSelectPropertyChange(criterion) {
        let keyVal = this.query[criterion.field][criterion.index];
        let values = this.getPropertySetByKey(keyVal.key).values;

        keyVal.values = _.map(_.filter(values, (val) => val.isSelected),'text');

    }

    onChangePropertySetKey(criterion) {
        let keyVal = this.query[criterion.field][criterion.index];
        keyVal.values = [];

        this.refreshAvailablePropertySetKeys();

    }

    refreshAvailablePropertySetKeys() {
        this.propertySetKeys = _.sortBy(_.map(this.transformedPropertiesData,'key'));
        if (!this.query.propertySetKeyValues || !this.query.propertySetKeyValues.length) return;
        else {
            this.query.propertySetKeyValues.forEach((keyVal)=>{
                if (keyVal.key) {
                    let propType = this.getPropertySetByKey(keyVal.key).propertyType;
                    if (propType === 'MULTI_SELECT') {
                        let i = this.propertySetKeys.indexOf(keyVal.key);
                        if (i !== -1) {
                            this.propertySetKeys.splice(i,1);
                        }
                    }
                }
            })
        }
    }

    canSearch() {
        return this.form && this.form.$valid && this.isQueryValid();
    }
    isQueryValid() {
        if (this.searchType === 'property') {
            if ((!this.query.propertyKeyValues || !this.query.propertyKeyValues.length) &&
                (!this.query.propertySetKeyValues || !this.query.propertySetKeyValues.length)) {
                return false;
            }
            else if (this.query.propertySetKeyValues && this.query.propertySetKeyValues.length) {
                let nullValFound = false;
                for (let i in this.query.propertySetKeyValues) {
                    if (!this.query.propertySetKeyValues[i].key) {
                        nullValFound=true;
                        break;
                    }
                }
                if (nullValFound) return false;
            }
        }
        else if (this.searchType === 'archive') {
            if (!this.query.name && !this.query.path) {
                return false;
            }
        }

        return true;
    }

    clear() {
        let refreshProps = false;
        this.queryDescriptor.searchCriteria.forEach((criterion) => {
            this.clearMultiValPropSetSelectedValues(criterion);

            if (criterion.default || criterion.visible) {
                if (criterion.type === 'string') {
                    this.query[criterion.field] = '';
                }
                else if (criterion.type === 'boolean') {
                    this.query[criterion.field] = false;
                }
                else if (criterion.type === 'array') {
                    this.query[criterion.field] = [];
                    _.filter(this.selectionRepoList, (repo) => repo.isSelected).forEach((selRepo)=>{
                        selRepo.isSelected=false
                    })
                }
                else if (criterion.type === 'keyVal' || criterion.type === 'keyValSet') {
                    this.query[criterion.field][criterion.index] = {key: '', values: []};
                    refreshProps = true;
                }
            }
        });
        if (refreshProps) this.refreshAvailablePropertySetKeys();
    }
}

export function searchQueryMaker() {
    return {
        restrict: 'E',
        scope: {
            searchType: '=',
            query: '=',
            repoList: '=',
            propertiesData: '=',
            parentController: '='
        },
        controller: searchQueryMakerController,
        controllerAs: 'queryMaker',
        bindToController: true,
        templateUrl: 'states/search/search_query_maker.html'
    }
}