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
class packageSearchController {
    constructor($scope, $state, $stateParams, ArtifactPackageSearchDao, $timeout, $q, ArtifactoryFeatures) {
        this.queryFields = [];

        this.$q = $q;
        this.$state = $state;
        this.$scope = $scope;
        this.$stateParams = $stateParams;
        this.$timeout = $timeout;
        this.artifactPackageSearchDao = ArtifactPackageSearchDao;


        this.features = ArtifactoryFeatures;

    }

    $onInit() {
        this.parentController.packageController = this;
        this.$scope.$watch('packageSearch.repoList',(newVal,oldVal) => {
            if (!newVal || !newVal.length) return;
            this.rawRepos = newVal;
            this.refreshRepoList();
        });
        this.init();
    }

    refreshRepoList() {
        this.selectionRepoList = _.map(this.getFilteredRepoList(this.rawRepos, this.selectedPackageType ? this.selectedPackageType.id : undefined), (repo)=>{
            return {
                text: repo.repoKey,
                iconClass: repo._iconClass,
                isSelected: false
            }
        })
    }

    init() {
        if (this.query.selectedPackageType) {
            this.selectedPackageType = this.query.selectedPackageType;

            this.onPackageTypeChange().then(()=>{
                if (this.query.packagePayload) {
                    let runSearch = false;
                    this.query.packagePayload.forEach((queryItem)=>{
                        if (this.rawQuery[queryItem.id]) {
                            this.rawQuery[queryItem.id].values = queryItem.values.join(',');
                            let queryField = _.find(this.queryFields,{id: queryItem.id});
                            if (queryField.id !== 'repo') runSearch = true;
                            if (queryField && !queryField.default) {
                                if (queryField.id !== 'repo') {
                                    queryField.visible = true;
                                }
                                else {
                                    let queryRepos = _.find(this.query.packagePayload,{id: 'repo'}).values;
                                    if (queryRepos.length) queryField.visible = true;
                                    else {
                                        let i = _.findIndex(this.query.packagePayload,{id: 'repo'})
                                        this.query.packagePayload.splice(i,1);
                                    }

                                    this.selectionRepoList.forEach((repo)=>{
                                        if (queryRepos.indexOf(repo.text) !== -1) {
                                            repo.isSelected = true;
                                        }
                                    })
                                }
                            }
                        }
                    })
                    if (this.query.selectedPackageType.id === 'dockerV1') {
                        this.queryFields.push({
                            id: 'dockerV1',
                            displayName: 'V1 Images',
                            visible: true
                        })
                    }
                    this.refreshAvailableCriteria();
                    this.onRepoSelectionChange();
                    if (runSearch) this.$timeout(()=>{
                        if (this.canSearch()) this.search();
                    })

                }
                else if (this.query.selectedPackageType.id === 'gavc'){
                    for (let key in this.rawQuery) {
                        this.rawQuery[key].values = this.query[key];
                        let queryField = _.find(this.queryFields,{id: key});
                        if (queryField && this.query[key] && !queryField.default) {
                            queryField.visible = true;
                        }
                    }
                    this.$timeout(()=>{
                        if (this.canSearch()) this.search();
                        else this.parentController.refreshGrid();
                    });
                }
                else this.parentController.refreshGrid();
            });
        }
        else {
            this.parentController.refreshGrid();
        }

        this.artifactPackageSearchDao.availablePackages().$promise.then((data)=> {
            data = _.filter(data, (packageType)=> {
                if (this.features.isJCR()) {
                    return packageType.id === 'helm';
                }
                return !packageType.id.startsWith('docker') && (!this.features.isOss() || packageType.id === "gavc");
            });
            let i = 0;
            _.forEach(data, () => {
                if (data[i].displayName === "GAVC") {
                    data[i].displayName = "Maven GAVC";
                }
                i++;
            });
            if (!this.features.isOss()) {
                data.unshift({
                    id: 'dockerV2',
                    icon: 'docker',
                    displayName: 'Docker'
                });
            }

            this.availablePackageTypes = _.sortBy(data, (pack) => {
                return pack.displayName;
            });

            if (this.features.isOss() || this.features.isJCR()) {
                this.parentController.availablePackageTypes = this.availablePackageTypes;
            }
        })
    }

    onPackageTypeChange(selectedPackageType) {
        var defer = this.$q.defer();

        if (selectedPackageType && selectedPackageType === this.selectedPackageType) {
            defer.resolve();
            return defer.promise;
        }

        this.rawQuery = {};
        this.$timeout(()=>{
            if (selectedPackageType) {
                this.selectedPackageType = selectedPackageType
            } else {
                this.parentController.selectedPackageType = this.selectedPackageType;
            }

            this.refreshRepoList();

            this.query.selectedPackageType = this.selectedPackageType;
            this.parentController.updateUrl();
            if (this.selectedPackageType.id === 'gavc') {
                let gavcFields = [
                    {id: 'groupID', displayName: 'Group ID', allowedComparators: '', default: true},
                    {id: 'artifactID', displayName: 'Artifact ID', allowedComparators: '', default: true},
                    {id: 'version', displayName: 'Version', allowedComparators: '', default: true},
                    {id: 'classifier', displayName: 'Classifier', allowedComparators: '', default: false},
                    {id: 'repo', displayName: '', allowedComparators: ''},
                ];
                this.queryFields = gavcFields;

                this.refreshAvailableCriteria();

                this.queryFields.forEach((field)=>{
                    this.rawQuery[field.id] = {comparator: field.allowedComparators[0]};
                });
                defer.resolve();
            }
            else {
                this.artifactPackageSearchDao.queryFields({},{packageType:this.selectedPackageType.id}).$promise.then((data)=>{
                    if (this.selectedPackageType.id === 'nuget') {
                        data = _.filter(data,(field)=>{
                            return field.id !== 'nugetTags' && field.id !== 'nugetDigest';
                        })
                    }
                    else if (this.selectedPackageType.id === 'dockerV2') {
                        data = _.filter(data,(field)=>{
                            return field.id !== 'dockerV2TagPath';
                        })
                    }
                    this.queryFields = data;
                    this.queryFields.forEach((field)=>{
                        field.default = field.visibleByDefault;
                        this.rawQuery[field.id] = {comparator: field.allowedComparators[0]};
                    });

                    this.refreshAvailableCriteria();

                    defer.resolve();
                });
            }
        });

        this.focusField = false;
        this.$timeout(()=>this.focusField = true);

        return defer.promise;
    }

    _transformQuery(rawQuery) {
        let transformed;
        if (this.selectedPackageType.id === 'gavc') {
            transformed = {};
            transformed.search = 'gavc';
            for (let key in rawQuery) {
                if (rawQuery[key].values) {
                    transformed[key] = rawQuery[key].values || '';
                }
            }
            if (this.selectedRepos) transformed.selectedRepositories = this.selectedRepos;
        }
        else {
            transformed = [];
            for (let key in rawQuery) {
                if (rawQuery[key].values) {
                    if (key !== 'repo') transformed.push({
                        id: key,
                        /*
                         comparator: rawQuery[key].comparator,
                         */
                        values: rawQuery[key].values.split(',')
                    })
                }
            }
            let repoField = _.find(this.queryFields,{id: 'repo'});

            if (repoField && repoField.visible) transformed.push({
                id: 'repo',
                values: this.selectedRepos || []
            })
        }



        return transformed;
    }

    canSearch() {
        let ret = false;
        if (this.rawQuery) {
            for (let key in this.rawQuery) {
                if (key !== 'repo' && this.rawQuery[key].values) {
                    ret = true;
                    break;
                }
            }
        }
        return ret;
    }

    search() {
        if (!this.canSearch()) return;

        let transformedQuery = this._transformQuery(this.rawQuery);
        for (var key in this.query) delete this.query[key];
        if (_.isArray(transformedQuery)) {
            this.query.packagePayload = transformedQuery;
        }
        else { //gavc
            _.extend(this.query,transformedQuery);
        }
        this.query.selectedPackageType = this.selectedPackageType;
        this.parentController.packageSearchColumns = this.getColumnsByPackage();
        this.parentController.refreshGrid().then(()=>{
            this.$timeout(()=>this.parentController.search());
        })

    }
    clear() {
        for (let key in this.rawQuery) {
            let field = this.rawQuery[key];
            if (field.values) delete field.values;
        }

        _.filter(this.selectionRepoList, (repo) => repo.isSelected).forEach((repoSel)=>{
            repoSel.isSelected = false;
        })
        delete this.selectedRepos;
    }

    getColumnsByPackage() {

        switch(this.selectedPackageType.id) {
            case 'gavc':
                return ['artifact','groupID','artifactID','version','classifier','repo','path','modified'];
                break;
            case 'dockerV1':
                return ['dockerV1Image*Image@','dockerV1Tag*Tag@','repo','modified'];
                break;
            case 'dockerV2':
                return ['dockerV2Image*Image@','dockerV2Tag*Tag@','repo','modified'];
                break;
            case 'nuget':
                return ['nugetPackageId*Package ID','nugetVersion*Version@','repo','path','modified'];
                break;
            case 'npm':
                return ['npmName*Package Name','npmVersion*Version@','npmScope*Scope@','repo','path','modified'];
                break;
            case 'puppet':
                return ['puppetName*Name','puppetVersion*Version@','repo','path','modified'];
                break;
            case 'bower':
                return ['bowerName*Package Name','bowerVersion*Version@','repo','path','modified'];
                break;
            case 'debian':
                return ['artifact','repo','path','debianDistribution*Distribution@','debianComponent*Component@','debianArchitecture*Architecture@','modified'];
                break;
            case 'pypi':
                return ['pypiName*Name','pypiVersion*Version@','repo','path','modified'];
                break;
            case 'gems':
                return ['gemName*Name','gemVersion*Version@','repo','path','modified'];
                break;
            case 'rpm':
                return ['rpmName*Name','rpmVersion*Version@','rpmArchitecture*Architecture@','repo','path','modified'];
                break;
            case 'vagrant':
                return ['vagrantName*Box Name','vagrantVersion*Box Version@','vagrantProvider*Box Provider@','repo','path','modified'];
                break;
            case 'conan':
                return ['conanName*Name@','conanVersion*Version@','conanUser*User@','conanChannel*Channel@','repo','modified'];
                break;
            default:
                return ['artifact','repo','path','modified'];
        }

    }

    refreshAvailableCriteria() {
        if (this.queryFields) {
            let criteria = _.filter(this.queryFields,(criterion) => !criterion.default && !criterion.visible);

            this.availableCriteria = _.map(criteria,(criterion) => {
                if (criterion.id === 'repo') {
                    criterion.displayName = 'Limit to Specific Repositories';
                }
                return criterion;
            })

            if (this.selectedPackageType.id === 'dockerV2') {
                this.availableCriteria.push({
                    id: 'dockerV1',
                    displayName: 'V1 Images'
                })
            }
        }
        else {
            this.availableCriteria = [];
        }
    }

    onAddCriteria() {
        if (this.criterionToAdd.id === 'dockerV1') {
            //            this.selectedPackageType = _.find(this.availablePackageTypes,{id: 'dockerV1'});
            this.selectedPackageType.id = this.criterionToAdd.id;
            this.onPackageTypeChange().then(()=>{
                this.queryFields.push({
                    id: 'dockerV1',
                    displayName: 'V1 Images',
                    visible: true
                })
            })
        }
        else if (this.criterionToAdd.id === 'repo') {
            this.openLimitDropDown=true;
            this.selectedRepos = [];
        }
        else {
            let tempCrit = this.criterionToAdd;
            tempCrit.autofocus = true
            this.$timeout(()=>{tempCrit.autofocus = false},500)
        }
        this.criterionToAdd.visible = true;
        this.criterionToAdd = null;
        this.refreshAvailableCriteria();
    }
    removeCriterion(criterion) {
        criterion.visible = false;
        delete this.rawQuery[criterion.id];
        if (criterion.id === 'repo') {
            _.filter(this.selectionRepoList, (repo) => repo.isSelected).forEach((selRepo)=>{
                selRepo.isSelected=false
            })
            delete this.selectedRepos;
        }
        else if (criterion.id === 'dockerV1') {
            this.selectedPackageType.id = 'dockerV2';
            this.onPackageTypeChange();
        }

        this.refreshAvailableCriteria();
    }

    onRepoSelectionChange(criterion) {
        let selectedRepos = _.filter(this.selectionRepoList, (repo) => repo.isSelected);
        this.selectedRepos = _.map(selectedRepos,'text');
    }

    getFilteredRepoList(unfiltered, packageType) {

        //        let lastIncluded = this.query.selectedRepositories || [];// (this.$stateParams.searchParams && this.$stateParams.searchParams.selectedRepos) ? this.$stateParams.searchParams.selectedRepos : [];

        if (!packageType) return unfiltered;

        let filterFunc = (repo)=>{
            let ret;
            if (packageType.startsWith('docker')) {
                if (packageType.endsWith('V1')) ret = repo.repoType.toLowerCase() === 'docker' && repo.dockerApiVersion === 'V1';
                else if (packageType.endsWith('V2')) ret = repo.repoType.toLowerCase() === 'docker' && repo.dockerApiVersion === 'V2';
            }
            else if (packageType === 'rpm') {
                ret = repo.repoType.toLowerCase() === 'yum';
            }
            else if (packageType === 'gavc') {
                ret = repo.repoType.toLowerCase() === 'maven' || repo.repoType.toLowerCase() === 'ivy' || repo.repoType.toLowerCase() === 'sbt' || repo.repoType.toLowerCase() === 'gradle';
            }
            else ret = repo.repoType.toLowerCase() === packageType.toLowerCase();

            return ret;
        };

        return _.filter(unfiltered,filterFunc);
    }

}

export function packageSearch() {
    return {
        restrict: 'E',
        scope: {
            query: '=',
            parentController: '=',
            repoList: '='
        },
        controller: packageSearchController,
        controllerAs: 'packageSearch',
        bindToController: true,
        templateUrl: 'states/search/package_search.html'
    }
}
