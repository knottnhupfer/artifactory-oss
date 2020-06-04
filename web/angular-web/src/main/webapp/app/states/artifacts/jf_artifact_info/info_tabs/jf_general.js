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
import EVENTS from "../../../../constants/artifacts_events.constants";
import DICTIONARY from "./../../constants/artifact_general.constant";
import TOOLTIP from "../../../../constants/artifact_tooltip.constant";

class jfGeneralController {
    constructor($state,$scope, ArtifactGeneralDao, JFrogNotifications, ArtifactLicensesDao, ChecksumsDao, ArtifactActionsDao, ArtifactoryFeatures,
            FilteredResourceDao, JFrogEventBus, JFrogModal, DependencyDeclarationDao, $compile, User, FooterDao,
            ArtifactoryStorage) {
        this.generalData = {
            dependencyDeclaration: []
        };
        this.$state = $state;
        this.$scope = $scope;
        this.artifactLicensesDao = ArtifactLicensesDao;
        this.DICTIONARY = DICTIONARY;
        this.artifactActionsDao = ArtifactActionsDao;
        this.TOOLTIP = TOOLTIP.artifacts.browse;
        this.artifactoryNotifications = JFrogNotifications;
        this.artifactGeneralDao = ArtifactGeneralDao;
        this.filteredResourceDao = FilteredResourceDao;
        this.dependencyDeclarationDao = DependencyDeclarationDao;
        this.modal = JFrogModal;
        this.footerDao = FooterDao;
        this.currentDeclaration = 'Maven';
        this.JFrogEventBus = JFrogEventBus;
        this.features = ArtifactoryFeatures;
        this.$compile = $compile;
        this.userService = User;
        this.SearchForArchiveLicense = "Search Archive License File";
        this.ChecksumsDao = ChecksumsDao;
        this.editorOptions = {
            lineNumbers: true,
            readOnly: 'nocursor',
            lineWrapping: true,
            height: 'auto',
            mode: 'links',
            mimeType: 'text/xml'
        };
        this.artifactoryStorage = ArtifactoryStorage;

    }

    $onInit() {
        this._getGeneralData();
        this._registerEvents();
        this._initModalScope();
        this.isInVirtual();
    }

    _registerEvents() {

        this.JFrogEventBus.registerOnScope(this.$scope, EVENTS.TAB_NODE_CHANGED, (node) => {
            if (this.currentNode != node) {
                this.showArtifactsCount = false;
                this.calculatingArtifactsCount = false;
                this.finishedArtifactCount = false;
                this.currentNode = node;
                this._getGeneralData();
            }
        });
        this.JFrogEventBus.registerOnScope(this.$scope, [EVENTS.ACTION_WATCH, EVENTS.ACTION_UNWATCH], () => {
            this._getGeneralData();
        });
    }

    _initModalScope() {
        this.generalScope = this.$scope.$new();
        this.generalScope.closeModal = () => this.modalInstance.close();
        this.generalScope.saveLicenses = (licenses)=>this.saveLicenses(licenses);
        this.generalScope.attachLicenses = ()=>this.attachLicenses();
    }

    getGeneralTab() {
        return _.findWhere(this.currentNode.data.tabs, {name: 'General'});
    }

    _getGeneralData() {
        this.sha256Calculated = false;

        let repoData = this.currentNode.data;
        let generalTab = this.getGeneralTab();
        if (generalTab && generalTab.info) { // If general data already exists on the node (for archive children)
            this.generalData = generalTab;
        }
        else if (repoData.className === 'TreeNode') {
            this.getGeneralTabPromise()
                    .then((response) => {

                        if (response.type == 'file' && repoData && repoData.fileType == 'archive') {
                            response.fileType = 'archive';
                        }
                        if(response.type && response.type === 'virtualRemoteFile' &&
                            repoData && repoData.getRoot().repoType === 'virtual') {
                            response.info = this.sortedInsertionToOject(response.info,response.originPath,
                                                                        'originPath', 'repositoryPath');
                        }

                        if (response.info.repoType === "YUM") {
                            response.info.repoType = "RPM"
                        } else if (response.info.repoType === "Go") {
                            response.info.repoType = "golang"
                        }

                        this.showArtifactsCount = this.artifactsCountEnabled();
                        if (response.info.externalUrl) {
                            response.info = this._moveObjectElement(response.info, "externalUrl", "repositoryPath");
                        }
                        this.generalData = response;
                        if (this.generalData.dependencyDeclaration) {
                            this.selectDeclaration(this.currentDeclaration);
                        }
                        if (this.generalData.bintrayInfoEnabled) {
                            this.loadPackageDescription();
                        }

                        this.trimVirtualAssociations();

                        this.userService.canAnnotate(this.currentNode.data.repoKey,
                                this.currentNode.data.path).then((response) => {
                            this.canAnnotate = response.data;
                        });

                    });
        }
    }

    sortedInsertionToOject(unsortedObject,newFieldValue,newFieldName,fieldBefore){
        if(!newFieldValue) {
            return unsortedObject;
        }
        let sortedObject = {};
        for(let field in unsortedObject){
            sortedObject[field] = unsortedObject[field];
            if(field===fieldBefore){
                sortedObject[newFieldName] = newFieldValue;
            }
        }
        return sortedObject;
    }

    shouldDisplayInfoEntry(key){
        return  key != 'licenses' &&
                key != 'filtered' &&
                key != 'smartRepo' &&
                key != 'showFilteredResourceCheckBox' &&
                key != 'artifactsCount' &&
                key != 'artifactsCount' &&
                key != 'currentlyDownloadable' &&
                key != 'bintrayUrl' &&
                key != 'originPath';
    }

    goToOriginPath(value){
        let browser = this.artifactoryStorage.getItem('BROWSER') || 'tree';
        if (browser === 'stash') browser = 'tree';
        let path = value;
        this.$state.go('artifacts.browsers.path', {
            tab: "General",
            artifact: path,
            browser: browser
        });
    }

    getGeneralTabPromise(){
        let newPath = (this.currentNode.data.type === "folder" && this.currentNode.data.path.substr(-1) != "/") ? this.currentNode.data.path + "/" : this.currentNode.data.path;
        return this.artifactGeneralDao.fetch({
            "type": this.currentNode.data.type,
            "repoKey": this.currentNode.data.repoKey,
            "path": newPath
        }).$promise;
    }

    trimVirtualAssociations() {
        const LIMIT = 5;
        let fields = ['virtualRepositories','includedRepositories'];
        fields.forEach(f=>{
            if (this.generalData[f] && this.generalData[f].length > LIMIT) {
                let more = this.generalData[f].length - (LIMIT-1);
                this.generalData['all' + _.capitalize(f)] = _.cloneDeep(this.generalData[f]);
                this.generalData[f] = this.generalData['all' + _.capitalize(f)].slice(0,LIMIT-1);
                let THIS = this;
                this.generalData[f].push({
                    showAll: true,
                    text: `(Show ${more} more)`,
                    action: 'show',
                    toggle: function() {
                        if (this.action==='show') {
                            this.action = 'hide';
                            this.text = '(Show Less)';
                            THIS.generalData[f] = _.cloneDeep(THIS.generalData['all' + _.capitalize(f)]);
                            THIS.generalData[f].push(this);
                        }
                        else {
                            this.action = 'show';
                            this.text = `(Show ${more} more)`;
                            THIS.generalData[f] = THIS.generalData['all' + _.capitalize(f)].slice(0,LIMIT-1);
                            THIS.generalData[f].push(this);
                        }
                    }
                })
            }
        })

    }
    calculateArtifactsCount() {
        this.calculatingArtifactsCount = true;
        let {name, repositoryPath} = this.generalData.info;
        this.artifactGeneralDao.artifactsCount({name, repositoryPath}).$promise
                .then((response) => {
                    this.generalData.info.artifactsCount = response.artifactsCount;
                })
                .finally(() => {
                    this.calculatingArtifactsCount = false;
                    this.finishedArtifactCount = true;
                });
    }

    calculateArtifactsCountAndSize() {
        this.calculatingArtifactsCount = true;
        let {name, repositoryPath} = this.generalData.info;
        this.artifactGeneralDao.artifactsCount({name, repositoryPath}).$promise
                .then((response) => {
                    this.generalData.info.artifactsCount = '' + response.artifactsCount + ' / ' + response.artifactSize;
                })
                .finally(() => {
                    this.calculatingArtifactsCount = false;
                    this.finishedArtifactCount = true;
                });
    }

    artifactsCountEnabled() {
        return _.contains(['local', 'cached'], this.currentNode.data.repoType);
    }

    onFilteredResourceCB() {
        let payload = {repoKey: this.currentNode.data.repoKey, path: this.currentNode.data.path};
        this.filteredResourceDao.setFiltered({setFiltered: this.generalData.info.filtered},
                payload).$promise.then((res)=> {
            //console.log(res);
        });
    }

    fixChecksum() {
        this.ChecksumsDao.fix({}, {repoKey: this.currentNode.data.repoKey, path: this.currentNode.data.path})
                .$promise.then((data) => {
            this._getGeneralData();
        })
    }

    isDeclarationSelected(item) {
        return this.currentDeclaration == item;
    }

    selectDeclaration(item) {
        let self = this;
        this.currentDeclaration = item;
        this.dependencyDeclarationDao.get({
            buildtool: item.toLowerCase(),
            repoKey: this.currentNode.data.repoKey,
            path: this.currentNode.data.path

        }).$promise.then((data)=> {
            if (data.dependencyData) {
                self.generalData.dependencyDeclaration.dependencyData = data.dependencyData;
            }
        });
    }

    loadPackageDescription() {
        this.bintrayData = {};
        this.artifactGeneralDao.bintray({sha1: this.generalData.checksums.sha1Value}).$promise.then((data)=> {
            if (!data.name && !data.errorMessage) this.generalData.bintrayInfoEnabled = false;
            else this.bintrayData = data;
        });
    }

    /**Licenses actions and display
     * saving all default licenses on the generalScope for modal display
     * **/
    openAddLicenseModal() {
        this.generalScope.modalTitle = 'Add Artifactory License Property';
        this.modalInstance = this.modal.launchModal('add_license_modal', this.generalScope);
    }
    openFoundLicenseModal() {


        let associatedLicenses = this.generalData.info.licenses[0].name === 'Not Found' ? [] : _.map(this.generalData.info.licenses, (lic) => { return lic.name });

        this.generalScope.foundLicenses = _.difference(this.generalScope.selectedLicenses, associatedLicenses);

        if (this.generalScope.foundLicenses.length === 0) {
            this.artifactoryNotifications.create({info: 'Found licenses are already attached'})
        } else {
            this.generalScope.modalTitle = this.generalScope.foundLicenses.length + ' ' + (this.generalScope.foundLicenses.length < 2 ? ' License' : ' Licenses') + ' Found';
            this.modalInstance = this.modal.launchModal('found_license_modal', this.generalScope);
        }
    }

    editLicenses(scan) {
        this.artifactLicensesDao.getLicenses().$promise.then((licenses)=> {
            this.generalScope.licenses = _.map(licenses, (rec)=> {
                return rec.name
            });
            this.generalScope.selectedLicenses = _.filter(this.generalData.info.licenses,(lic)=>{
                return lic.name !== 'Not Found';
            });
            this.openAddLicenseModal();
        })
    }

    saveLicenses(selectedLicenses) {
        this.artifactLicensesDao.setLicenses({
            repoKey: this.currentNode.data.repoKey,
            path: this.currentNode.data.path
        }, selectedLicenses).$promise.then((result)=> {
            this._getGeneralData();
            this.modalInstance.close();
        });
    }

    attachLicenses() {
        this.artifactLicensesDao.setLicenses({
            repoKey: this.currentNode.data.repoKey,
            path: this.currentNode.data.path
        }, this.generalScope.selectedLicenses).$promise.then((result)=> {
            this._getGeneralData();
            this.modalInstance.close();
        });
    }

    deleteLicenses() {
        this.modal.confirm("Are you sure you want to delete the license information attached to " + this.generalData.info.name + "?")
                .then(() => {
                    this.saveLicenses([]);
                });
    }

    scanForLicenses() {
        this.artifactLicensesDao.scanArtifact({
            repoKey: this.currentNode.data.repoKey,
            path: this.currentNode.data.path
        }).$promise.then((result)=> {
            if (result.data.length > 0) {
                this.generalScope.selectedLicenses = _.map(result.data, (rec)=> {
                    return rec.name;
                });

                this.openFoundLicenseModal();
            }
            else {
                this.artifactoryNotifications.create({info: 'No licenses found in scan'})
            }
        });
    }

    searchForArchiveFile() {
        this.artifactLicensesDao.getArchiveLicenseFile({
            repoKey: this.currentNode.data.repoKey,
            path: this.currentNode.data.path
        }).$promise.then((data)=> {
            this.modal.launchCodeModal('License File', data.data);
        }, ()=> {
            this.SearchForArchiveLicense = "(No archive license file found)";
            this.noArchiveLicense = true;
        });

    }

    queryCodeCenter() {
        this.artifactLicensesDao.queryCodeCenter({
            repoKey: this.currentNode.data.repoKey,
            path: this.currentNode.data.path
        }).$promise.then((result)=> {
            this._getGeneralData();
        });
    }

    getFullFilePath() {
        /*
         if (!window.location.origin) { // IE compatibility
         window.location.origin = window.location.protocol + "//" + window.location.hostname + (window.location.port ? ':' + window.location.port: '');
         }
         return window.location.origin+this.currentNode.data.actualDownloadPath;
         */
        return this.currentNode.data.actualDownloadPath;
    }

    getSha256() {
        if (this.features.isOss() || !this.canCalculateSha256()) return;

        this.sha256Calculated = true;
        this.calculatingSha256 = true;
        this.artifactActionsDao.getSha256({$spinner_domain: "sha256"}, {
            repoKey: this.currentNode.data.repoKey,
            path: this.currentNode.data.path
        }).$promise
                .then((result)=> {
                    // Instead of calling this._getGeneralData() and refreshing the entire tab , just replace the SHA-256
                    this.getGeneralTabPromise()
                            .then((response) => {
                                let generalTab = response;
                                this.generalData.checksums.sha2 = generalTab.checksums.sha2;
                                this.calculatingSha256 = false;
                            });
                });
    }

    canCalculateSha256() {
        return this.canAnnotate && this.userService.currentUser.getCanDeploy() && !this.isInVirtual();
    }

    isInVirtual() {
        return this.currentNode && this.currentNode.data && this.currentNode.data.getRoot && this.currentNode.data.getRoot().repoType === 'virtual';
    }

    getChecksumKey(keyval) {
        return keyval.split(':')[0];
    }
    getChecksumVal(keyval) {
        let splitted = keyval.split(':');
        splitted.shift();
        return splitted.join(':');
    }

    isTrashcanEnabled() {
        return !this.footerDao.getInfo().trashDisabled;
    }
    isTrashcan() {
        return this.currentNode.data && this.currentNode.data.isTrashcan && this.currentNode.data.isTrashcan();
    }
    isInTrashcan() {
        return this.currentNode.data && this.currentNode.data.isInTrashcan && this.currentNode.data.isInTrashcan();
    }

    isDistRepo() {
        return this.currentNode.data && this.currentNode.data.repoType === 'distribution';
    }

    _moveObjectElement(obj, currentKey, afterKey) {
        var result = {};
        var val = obj[currentKey];
        delete obj[currentKey];
        var next = -1;
        var i = 0;
        if(typeof afterKey == 'undefined' || afterKey == null) afterKey = '';
        $.each(obj, (k, v) => {
            if((afterKey == '' && i == 0) || next == 1) {
                result[currentKey] = val;
                next = 0;
            }
            if(k == afterKey) { next = 1; }
            result[k] = v;
            ++i;
        });
        if(next == 1) {
            result[currentKey] = val;
        }
        if(next !== -1) return result; else return obj;
    }


    isCurrentNodeAFolderInArchive() {
        let isFolder = this.currentNode.data.folder;
        let isInArchive = !!this.currentNode.data.archivePath;
        return isFolder && isInArchive;
    }
}

export function jfGeneral() {
    return {
        restrict: 'EA',
        scope: {
            currentNode: '='
        },
        controller: jfGeneralController,
        controllerAs: 'jfGeneral',
        bindToController: true,
        templateUrl: 'states/artifacts/jf_artifact_info/info_tabs/jf_general.html'
    }
}