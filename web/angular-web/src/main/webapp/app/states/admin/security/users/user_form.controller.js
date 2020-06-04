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
import ACTIONS from "../../../../constants/user_actions.constants";
import TOOLTIP from "../../../../constants/artifact_tooltip.constant";
import MESSAGES from "../../../../constants/configuration_messages.constants";
import {default as merge} from 'lodash.merge';

export class AdminSecurityUserFormController {
    constructor($scope, $state, $stateParams, $timeout, $q, $http, RESOURCE, JFrogGridFactory, UserDao, GroupsDao, GroupPermissionsDao, AdminSecurityGeneralDao, User,
                uiGridConstants, commonGridColumns, ArtifactoryModelSaver, RepositoriesDao, UserProfileDao, JFrogModal, JFrogNotifications,ArtifactoryState, FooterDao) {

        this.$scope = $scope;
        this.$state = $state;
        this.$stateParams = $stateParams;
        this.$timeout = $timeout;
        this.$q = $q;
        this.$http = $http;
        this.RESOURCE = RESOURCE;
        this.User = User;
        this.repositoriesDao = RepositoriesDao;
        this.adminSecurityGeneralDao = AdminSecurityGeneralDao;
        this.modal = JFrogModal;
        this.userDao = UserDao.getInstance();
        this.groupsDao = GroupsDao.getInstance();
        this.groupPermissionsDao = GroupPermissionsDao.getInstance();
        this.artifactoryGridFactory = JFrogGridFactory;
        this.ArtifactoryModelSaver = ArtifactoryModelSaver.createInstance(this, ['userdata', 'input'], ['locked']);
        this.permissionsGridOptions = {};
        this.buildsPermissionsGridOptions = {};
        this.userProfileDao = UserProfileDao;
        this.uiGridConstants = uiGridConstants;
        this.commonGridColumns = commonGridColumns;
        this.artifactoryNotifications = JFrogNotifications;
        this.TOOLTIP = TOOLTIP.admin.security.usersForm;
        this.MESSAGES = MESSAGES.admin.security.users.userForm;
        this.input = {};
        this.artifactoryState = ArtifactoryState;
        this.emailChanged=false;
        this.setDisabledChecked = true;
        this.passwordRank = 0;
        this.lastSavedUserSettings = {};
        this.footerDao = FooterDao;


        this._getPasswordExpirationState();

        if ($stateParams.username) {
            this.mode = 'edit';
            this.username = $stateParams.username;
            this.title = 'Edit ' + this.username + ' User';
            this._getUserData();

        }
        else {
            this.mode = 'create';
            this.title = 'Add New User';
            this.userdata = {
                groups: [],
                profileUpdatable: true,
                disableUIAccess: false,
                internalPasswordDisabled: false
            };
            this.saveCheckboxes();
        }
        this._createGrid();
        this._getAllRepos();
        this._getAllGroups();

        this.footerDao.get().then((response) => {
            this.xrayEnabled = response.xrayEnabled;
        });
    }

    userIsEffectiveAdmin(){
        return (this.userdata.admin || this.isInAdminGroup);
    }

    _getAllRepos() {
        this.reposData = {};
        this.repositoriesDao.getRepositories({type:'local'}).$promise
                .then((data) => {
                    this.reposData.locals = _.map(data,(r)=>{return r.repoKey;});
                });
        this.repositoriesDao.getRepositories({type:'remote'}).$promise
                .then((data) => {
                    this.reposData.remotes = _.map(data,(r)=>{return r.repoKey;});
                });
        this.repositoriesDao.getRepositories({type:'distribution'}).$promise
                .then((data) => {
                    this.reposData.dists = _.map(data,(r)=>{return r.repoKey;});
                });
    }

    _createGrid() {
        this.permissionsGridOptions = this.artifactoryGridFactory.getGridInstance(this.$scope)
            .setColumns(this._getPermissionColumns())
            .setRowTemplate('default');

        this.buildsPermissionsGridOptions = this.artifactoryGridFactory.getGridInstance(this.$scope)
                .setColumns(this._getBuildPermissionCloumns())
                .setRowTemplate('default');

    }

    _getAllGroups() {
        this.userDao.getAllGroups().$promise.then((data)=> {
            this.groupsData = data;
            this.groupsList = _.map(this.groupsData, (group)=> {
                if (group.autoJoin && this.mode === 'create') {
                    this.userdata.groups.push(group.groupName);
                    this.ArtifactoryModelSaver.save();
                }
                return group.groupName;
            });
            if (this.mode === 'create') {
                this._getGroupsPermissions();
            }

            this.groupDndTemplate = `<div style="min-width:200px">
                                        {{getItemInfo().text}}
                                        <i ng-if="userScope.UserForm.groupIsAdmin(getItemInfo().text)" 
                                           jf-tooltip="Admin Privileges"
                                           class="icon icon-admin-new pull-left"></i>
                                        <i ng-if="!userScope.UserForm.groupIsAdmin(getItemInfo().text)" 
                                            class="icon icon-blank pull-left"></i>
                                    </div>`
        });
    }

    _getUserPermissions() {
        this.userDao.getPermissions({userOnly: true}, {name: this.username}).$promise.then((data)=> {
            this.userRepoPermissions = data.repoPermissions;
            this.userBuildPermissions = data.buildPermissions;
            if (!this.userdata.groups || !this.userdata.groups.length || this.groupsRepoPermissions || this.groupsBuildPermissions) {
                this._setGridData();
            }
        });
    }

    _getGroupsPermissions() {
        if (!this.userdata) return;
        if (!this.userdata.groups || !this.userdata.groups.length) {
            this.groupsRepoPermissions = [];
            this.groupsBuildPermissions = [];
            if (this.mode==='create') {
                this.permissionsGridOptions.setGridData(this.groupsRepoPermissions);
                this.buildsPermissionsGridOptions.setGridData(this.groupsBuildPermissions);
            } else if (this.userRepoPermissions || this.userBuildPermissions) {
                this._setGridData();
            }
        } else {
            this.groupPermissionsDao.get({groups: this.userdata.groups}).$promise.then((data)=> {

                let groupByPermissionName = (data) => {

                    let groups = _.groupBy(data, i => i.permissionName);
                    let permissionsKeys = Object.keys(groups);

                    let newPermissionObject = {};
                    _.forEach(permissionsKeys, key => {
                        // copy the first item as basis
                        newPermissionObject[key] = angular.copy(groups[key][0]);

                        let unifiedPermissions = [];
                        if (groups[key].length > 1) {
                            // if more than 1 permission re-create the object
                            delete newPermissionObject[key].effectivePermission;

                            _.forEach(groups[key], group => unifiedPermissions.push(group.effectivePermission));

                            let principals = _.pluck(unifiedPermissions, 'principal').join(', ');

                            let permissionsBase = {
                                delete: false,
                                deploy: false,
                                annotate: false,
                                read: false,
                                managed: false
                            };

                            _.forEach(unifiedPermissions, permissionsSet => {
                                let cleanObject = _.pick(permissionsSet, ["delete","deploy","annotate", "read", "managed"]);
                                _.forEach(cleanObject, (val,key) => {
                                    if (val) permissionsBase[key] = val;
                                })
                            });

                            newPermissionObject[key].effectivePermission = {
                                principal: principals
                            };

                            merge(newPermissionObject[key].effectivePermission, permissionsBase);
                        }
                    });

                    return Object.values(newPermissionObject);
                };

                this.groupsRepoPermissions = groupByPermissionName(data.repoPermissions);
                // create joined repositories list for the table
                _.forEach(this.groupsRepoPermissions, item => item.repoKeysList = item.repoKeys.join(', '));

                this.groupsBuildPermissions = groupByPermissionName(data.buildPermissions);

                if (this.mode === 'create') {
                    this.permissionsGridOptions.setGridData(this.groupsRepoPermissions);
                    this.buildsPermissionsGridOptions.setGridData(this.groupsBuildPermissions);
                }
                else if (this.userRepoPermissions || this.userBuildPermissions) {
                    this._setGridData();
                }
            });
        }
    }

    _setGridData() {
        let groupsRepoPermissions = this.groupsRepoPermissions || [];
        let groupsBuildPermissions = this.groupsBuildPermissions || [];

        this.buildsPermissionsGridOptions.setGridData(this.userBuildPermissions.concat(groupsBuildPermissions));

        this._fixDataFormat(this.userRepoPermissions).then((fixedData)=>{
            this.permissionsGridOptions.setGridData(fixedData.concat(groupsRepoPermissions));
        });
    }

    _fixDataFormat(data,defer = null) {
        defer = defer || this.$q.defer();
        if (this.reposData.locals && this.reposData.remotes) {
            data.forEach((record)=>{
                if (record.repoKeys && record.repoKeys.length === 1 && record.repoKeys[0] === 'ANY LOCAL') {
                    record.repoKeysView = 'ANY LOCAL';
                    record.reposList = angular.copy(this.reposData.locals);
                }
                else if (record.repoKeys && record.repoKeys.length === 1 && record.repoKeys[0] === 'ANY REMOTE') {
                    record.repoKeysView = 'ANY REMOTE';
                    record.reposList = angular.copy(this.reposData.remotes);
                }
                else if (record.repoKeys && record.repoKeys.length === 1 && record.repoKeys[0] === 'ANY DISTRIBUTION') {
                    record.repoKeysView = 'ANY DISTRIBUTION';
                    record.reposList = angular.copy(this.reposData.dists);
                }
                else if (record.repoKeys && record.repoKeys.length === 1 && record.repoKeys[0] === 'ANY') {
                    record.repoKeysView = 'ANY';
                    record.reposList = angular.copy(this.reposData.remotes).concat(this.reposData.locals).concat(this.reposData.dists);
                }
                else if (record.repoKeys) {
                    record.repoKeysView = record.repoKeys.join(', ');
                    record.reposList = angular.copy(record.repoKeys);
                }
            });
            defer.resolve(data);
        }
        else {
            this.$timeout(()=>{
                this._fixDataFormat(data,defer);
            })
        }
        return defer.promise;
    }

    _getUserData() {
        this.userDao.getSingle({name: this.username}).$promise.then((data) => {
            this.userdata = data.data;
            this._getUserPermissions();
            this.isInAdminGroup = this.userdata.groupAdmin;

            if (this.userdata.internalPasswordDisabled) {
                this.passwordOriginalyDisabled = true;
            }
            if (!this.userdata.groups) {
                this.userdata.groups = [];
            } else {
                this._getGroupsPermissions();
            }
            this.saveCheckboxes();
            this.ArtifactoryModelSaver.save();
        });
        this._getApiKeyState();
    }

    _getApiKeyState() {
        this.userProfileDao.hasApiKey({},{username: this.username}).$promise.then((res)=>{
            this.apiKeyExist = true;
        }).catch(() => {
            this.apiKeyExist = false;
        })
    }

    _fixGroups(userdata) {
        let groups = userdata.groups;
        let groupsObjects = [];
        groups.forEach((group)=> {
            let realm = _.findWhere(this.groupsData, {groupName: group}).realm;
            groupsObjects.push({groupName: group, realm: realm});
        });
        delete(userdata.groups);
        userdata.userGroups = groupsObjects;
    }

    onEmailChanged() {
        this.emailChanged = true;
    }

    updateUser() {
        let payload = angular.copy(this.userdata);
        _.extend(payload, this.input);
        this._fixGroups(payload);
        this.userDao.update({name: this.userdata.name}, payload).$promise.then((data) => {
            this.savePending = false;
            if (this.userdata.name === this.User.currentUser.name) {
                if(this.emailChanged) {
                    this.artifactoryState.removeState('setMeUpUserData');
                }
                this.User.reload();
            }
            this.ArtifactoryModelSaver.save();
            this.$state.go('^.users');
        }).catch(()=>this.savePending = false);
    }

    createNewUser() {
        let payload = angular.copy(this.userdata);
        _.extend(payload, this.input);
        this._fixGroups(payload);
        this.userDao.create(payload).$promise.then((data) => {
            this.savePending = false;
            this.ArtifactoryModelSaver.save();
            this.$state.go('^.users');
        }).catch(()=>this.savePending = false);
    }

    save() {
        if (this.savePending) return;

        this.savePending = true;

        if (this.mode == 'edit')
            this.updateUser();
        if (this.mode == 'create')
            this.createNewUser();
    }

    cancel() {
        this.$state.go('^.users');
    }

    deleteUser() {
        let json = {userNames:[this.username]};
        this.modal.confirm(`Are you sure you want to delete user '${this.username}?'`)
            .then(() => this.userDao.delete(json).$promise.then(()=>this.cancel()));
    }

    userIsInAdminGroup(){
        let groups = this.userdata.groups;
        let groupsData = this.groupsData;
        for(let i in groups){
            for(let j = 0; j < groupsData.length; j ++){
                 if(groupsData[j].name === groups[i]
                         && groupsData[j].adminPrivileges){
                    return true;
                 }
            }
        }
        return false;
    }

    groupIsAdmin(group){
        return _.find(this.groupsData, {groupName: group, adminPrivileges:true});
    }

    onCheckboxChanged(){
        this.saveCheckboxes();
    }

    saveCheckboxes(){
        this.lastSavedUserSettings = {
            disableUIAccess: this.userdata.disableUIAccess,
            profileUpdatable: this.userdata.profileUpdatable,
            internalPasswordDisabled: this.userdata.internalPasswordDisabled,
        };
    }
    onChangeGroups() {
        this._getGroupsPermissions();
        if (this.mode === 'edit') this._getUserPermissions();
        this.$timeout(()=>{
            this.isInAdminGroup = this.userIsInAdminGroup();
            // Save last user settings before making changes
            if(this.isInAdminGroup || this.userdata.admin){
                this.userdata.disableUIAccess = false;
                this.userdata.profileUpdatable = true;
                this.userdata.internalPasswordDisabled = false;
            } else {
                this.userdata.disableUIAccess = this.lastSavedUserSettings.disableUIAccess;
                this.userdata.profileUpdatable = this.lastSavedUserSettings.profileUpdatable;
                this.userdata.internalPasswordDisabled = this.lastSavedUserSettings.internalPasswordDisabled;
            }
        });
    }

    onClickAdmin() {
        if (this.userdata.admin) {
            this.userdata.profileUpdatable = true;
            this.userdata.internalPasswordDisabled = false;
            this.userdata.disableUIAccess = false;
        }
    }
    _getBuildPermissionCloumns() {
        let nameCellTemplate = '<div class="ui-grid-cell-contents"><a class="jf-link" href ui-sref="admin.security.permissions.edit({permission: row.entity.permissionName})">{{row.entity.permissionName}}</a></div>';

        let permissions=  [
            {
                field: "permissionName",
                name: "Permission Target",
                displayName: "Permission Target",
                sort: {
                    direction: this.uiGridConstants.ASC
                },
                cellTemplate: nameCellTemplate,
                width:'35%'
            },
            {
                field: "effectivePermission.principal",
                name: "Applied To",
                displayName: "Applied To",
                width: '40%'
            },
            {
                field: "effectivePermission.managed",
                cellTemplate: this.commonGridColumns.booleanColumn('row.entity.effectivePermission.managed'),
                name: "Manage",
                displayName: "Manage",
                width:'15%'
            },
            {
                field: "effectivePermission.delete",
                cellTemplate: this.commonGridColumns.booleanColumn('row.entity.effectivePermission.delete'),
                name: "Delete",
                displayName: "Delete",
                width:'15%'
            },
            {
                field: "effectivePermission.deploy",
                cellTemplate: this.commonGridColumns.booleanColumn('row.entity.effectivePermission.deploy'),
                name: "Deploy",
                displayName: "Deploy",
                width:'15%'
            },
            {
                field: "effectivePermission.annotate",
                cellTemplate: this.commonGridColumns.booleanColumn('row.entity.effectivePermission.annotate'),
                name: "Annotate",
                displayName: "Annotate",
                width:'15%'
            },
            {
                field: "effectivePermission.read",
                cellTemplate: this.commonGridColumns.booleanColumn('row.entity.effectivePermission.read'),
                name: "Read",
                displayName: "Read",
                width:'15%',
            }];

        // TODO: liorg
        // if (this.xrayEnabled) {
        //     permissions.push(
        //             {
        //                 field: "effectivePermission.managedXrayMeta",
        //                 cellTemplate: this.commonGridColumns.booleanColumn(
        //                         'row.entity.effectivePermission.managedXrayMeta'),
        //                 name: "ManagedXrayMeta",
        //                 displayName: "Managed Xray Meta",
        //                 width: '15%'
        //             },
        //             {
        //                 field: "effectivePermission.managedXrayWatchers",
        //                 cellTemplate: this.commonGridColumns.booleanColumn(
        //                         'row.entity.effectivePermission.managedXrayWatchers'),
        //                 name: "ManagedXrayWatchers",
        //                 displayName: "Managed Xray Watchers",
        //                 width: '15%',
        //             }
        //     );
        // }
        return permissions
    }
    _getPermissionColumns() {

        let nameCellTemplate = '<div class="ui-grid-cell-contents"><a class="jf-link" href ui-sref="admin.security.permissions.edit({permission: row.entity.permissionName})">{{row.entity.permissionName}}</a></div>';

        let permissions =  [
            {
                field: "permissionName",
                name: "Permission Target",
                displayName: "Permission Target",
                sort: {
                    direction: this.uiGridConstants.ASC
                },
                cellTemplate: nameCellTemplate,
                width:'16%'
            },
            {
                field: "repoKeys",
                name: "Repositories",
                displayName: "Repositories",
                cellTemplate: this.commonGridColumns.listableColumn('row.entity.repoKeys','row.entity.permissionName','row.entity.repoKeysList',true),
                width:'16%'

            },
            {
                field: "effectivePermission.managed",
                cellTemplate: this.commonGridColumns.booleanColumn('row.entity.effectivePermission.managed'),
                name: "Manage",
                displayName: "Manage",
                width:'9%'
            },
            {
                field: "effectivePermission.delete",
                cellTemplate: this.commonGridColumns.booleanColumn('row.entity.effectivePermission.delete'),
                name: "Delete/Overwrite",
                displayName: "Delete/Overwrite",
                width:'15%'
            },
            {
                field: "effectivePermission.deploy",
                cellTemplate: this.commonGridColumns.booleanColumn('row.entity.effectivePermission.deploy'),
                name: "Deploy/Cache",
                displayName: "Deploy/Cache",
                width:'14%'
            },
            {
                field: "effectivePermission.annotate",
                cellTemplate: this.commonGridColumns.booleanColumn('row.entity.effectivePermission.annotate'),
                name: "Annotate",
                displayName: "Annotate",
                width:'9%'
            },
            {
                field: "effectivePermission.read",
                cellTemplate: this.commonGridColumns.booleanColumn('row.entity.effectivePermission.read'),
                name: "Read",
                displayName: "Read",
                width:'8%'
            }];

        // TODO: liorg
        // if (this.xrayEnabled) {
        //     permissions.push(
        //             {
        //                 field: "effectivePermission.managedXrayMeta",
        //                 cellTemplate: this.commonGridColumns.booleanColumn(
        //                         'row.entity.effectivePermission.managedXrayMeta'),
        //                 name: "ManagedXrayMeta",
        //                 displayName: "Managed Xray Meta",
        //                 width: '15%'
        //             },
        //             {
        //                 field: "effectivePermission.managedXrayWatchers",
        //                 cellTemplate: this.commonGridColumns.booleanColumn(
        //                         'row.entity.effectivePermission.managedXrayWatchers'),
        //                 name: "ManagedXrayWatchers",
        //                 displayName: "Managed Xray Watchers",
        //                 width: '15%',
        //             });
        // }
        return permissions
    }

    isSaveDisabled() {
        return this.savePending || !this.userForm || this.userForm.$invalid || ((this.input.password || this.input.retypePassword) && (this.input.password !== this.input.retypePassword));
    }

    checkPwdMatch(retypeVal) {
        return !retypeVal || (retypeVal && this.input.password === retypeVal);
    }

    isAnonymous() {
        return this.userdata.name === 'anonymous';
    }

    revokeApiKey() {
        this.modal.confirm(`Are you sure you want to revoke API key for this user ?`)
                .then(() => {
                    this.userProfileDao.revokeApiKey({}, {username: this.username}).$promise.then(()=>{
                        this.apiKeyExist = false;
                    });

                });
    }

    unlockUser() {
        this.adminSecurityGeneralDao.unlockUsers({},[this.username]).$promise.then((res)=>{
            if(res.status === 200) {
                this.userdata.locked = false;
            }
        });
    }


    expirePassword() {
        this.modal.confirm(`Are you sure you want to expire this user's password?`)
            .then(() => {
                this.userDao.expirePassword({}, {username: this.username}).$promise.then(()=> {
                    this._getUserData();
                })
            });
    }

    unexpirePassword() {
        this.userDao.unExpirePassword({}, {username: this.username}).$promise.then(()=> {
            this._getUserData();
        })
    }

    _getPasswordExpirationState() {
        this.adminSecurityGeneralDao.get().$promise.then((data) => {
            this.passwordExpirationEnabled = data.passwordSettings.expirationPolicy.enabled;
            this.userLockEnabled = data.userLockPolicy.enabled;
        });
    }

    revokeApiKey() {
        this.modal.confirm(`Are you sure you want to revoke API key for user '${this.username}'?`)
            .then(() => {
                this.userProfileDao.revokeApiKey({}, {username: this.username}).$promise.then(()=>{
                    this._getApiKeyState();
                });
            });
    }

    clearPasswordFields() {
        delete this.input.password;
        delete this.input.retypePassword;
    }

    onChangePasswordDisabled() {
        if (this.userdata.internalPasswordDisabled) {
            this.changePassword = false;
            this.clearPasswordFields();
            this.passwordReEnabled = false;
        }
        else {
            if (this.passwordOriginalyDisabled) {
                this.passwordReEnabled = true;
            }
        }
        this.onCheckboxChanged();
    }

    initActions(actionsController) {

        this.actionsController = actionsController;
        actionsController.setActionsDictionary(ACTIONS);
        actionsController.setActions([
            {
                name:'RevokeApiKey',
                visibleWhen: () => this.userdata && this.apiKeyExist && this.userdata.name !== 'anonymous',
                action: ()=>this.revokeApiKey()
            },
            {
                name:'UnlockUser',
                visibleWhen: () => this.userdata && this.userdata.locked && this.userdata.name !== 'anonymous',
                action: ()=>this.unlockUser()
            },
            {
                name:'ExpirePassword',
                visibleWhen: () => this.userdata && this.passwordExpirationEnabled && this.mode==='edit' && !this.userdata.credentialsExpired && (this.userdata.realm === 'internal' || !this.userdata.realm)  && this.userdata.name !== 'anonymous',
                action: ()=>this.expirePassword()
            },
            {
                name:'UnexpirePassword',
                visibleWhen: () => this.userdata && this.userdata.credentialsExpired && !this.userdata.locked  && this.userdata.name !== 'anonymous',
                action: ()=>this.unexpirePassword()
            },
            {
                name:'DeleteUser',
                visibleWhen: () => this.mode === 'edit' && this.userdata && this.userdata.name !== 'anonymous',
                action: ()=>this.deleteUser()
            }
        ]);

    }

    // Validations
    checkUserName(value) {
        return !(/[A-Z]/.test(value));
    }

}
