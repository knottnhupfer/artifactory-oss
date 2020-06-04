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
import TOOLTIP from "../../../../constants/artifact_tooltip.constant";
import EVENTS from "../../../../constants/artifacts_events.constants";

export class AdminSecurityPermissionsFormController {
    constructor($scope, $state, $stateParams, $q, JFrogModal, JFrogGridFactory, RepoDataDao,
            PermissionsDao, User, ArtifactoryModelSaver, JFrogEventBus, BuildsDao, $timeout) {
        this.$scope = $scope;
        this.$timeout = $timeout;
        this.$q = $q;
        this.JFrogEventBus = JFrogEventBus;
        this.$stateParams = $stateParams;
        this.user = User.getCurrent();
        this.modal = JFrogModal;
        this.ArtifactoryModelSaver = ArtifactoryModelSaver.createInstance(this, [
                                                                                    'permissionName',
                                                                                    'buildPermission',
                                                                                    'selectedRepositories',
                                                                                    'repoPermission',
                                                                                    'selectedBuilds',
                                                                                    'selectedUsers',
                                                                                    'selectedGroups'
                                                                                ]);
        this.$state = $state;
        this.TOOLTIP = TOOLTIP.admin.security.permissionsForm;

        // Daos
        this.repoDataDao = RepoDataDao;
        this.BuildsDao = BuildsDao;
        this.permissionsDao = PermissionsDao.getInstance();

        // Setups
        this.title = "New Permission";
        this.tabs = ['Add Repositories', 'Add Builds', 'Summary'];

        this.selectedRepositories = [];
        this.repoPermission = {};
        this.selectedBuilds = [];
        this.buildPermission = [];
        this.selectedUsers = [];
        this.selectedGroups = [];

        this.buildPermission = {
            includePatterns: [],
            excludePatterns: []
        };

        this.newPermission = false;


        // Drag & Drop Repositories columns
        this.repositoriesColumns = [
            {
                header: 'Name',
                field: 'repoKey',
                filterable: true,
                cellTemplate: `<div><i class="{{row.entity._iconClass}}"></i> {{row.entity.repoKey}}</div>`,
                width: '50%'
            }
        ];
        // Drag & Drop Builds columns
        this.buildsColumns = [
            {
                header: 'Build Name',
                field: 'buildName',
                filterable: true,
                cellTemplate: `<div><i class="icon-json"></i> {{row.entity.buildName}}</div>`,
                width: '100%'
            }
        ];

        let usersAndGroupsTemplate = {
            field: 'principal',
            filterable: true,
            cellTemplate: `<div disable-tooltip-on-overflow ng-if="row.entity.admin">
                                <span jf-tooltip="${this.TOOLTIP.adminIcon.user}">
                                    <i class="icon-admin-new"></i> 
                                    {{row.entity.principal}}
                                </span>
                               </div><div ng-if="!row.entity.admin">{{row.entity.principal}}</div>`,
            width: '100%'
        };

        this.usersColumns =  [_.assign({header: 'User Name'}, usersAndGroupsTemplate)];
        this.groupsColumns = [_.assign({header: 'Group Name'}, usersAndGroupsTemplate)];

        this.JFrogEventBus.registerOnScope(this.$scope, EVENTS.REFRESH_PAGE_CONTENT, () => {
            // TODO UPDATE DATA WHEN CREATING NEW REPOS FROM QUICK WIZARD
        });
    }

    $onInit() {
        this.initPermissionForm();
    }

    initPermissionForm() {

        this.permissionsDao.getBuildGlobalBasicReadAllowed().$promise
                .then(response => {
                    this.globalBasicReadAllowed = response.buildGlobalBasicReadAllowed
                });

        this._getAllRepos().then(() => {
            this.availableRepositories = _.map(this.allRepos, repo => this.getRepoWithIcon(repo));
        }).then(() => {

            this.$q.all([this._getAllBuildsNames(),this._getUsersAndGroups()]).then(() => {
                this.savePending = false;
                if (this.$stateParams.permission) {
                    this.newPermission = false;
                    this.title = "Edit " + this.$stateParams.permission + ' Permission';
                    this.initUpdatePermissionForm(this.$stateParams.permission);
                } else {
                    this.newPermission = true;
                    this._initNewPermissionForm();
                }
            })


        });


    }

    initUpdatePermissionForm(permissionName) {
        this.permissionName = permissionName;
        this.permissionsDao.getPermission({name: permissionName}).$promise.then((result) => {
            if (!this.user.isAdmin() && this.user.canManage) {
                if (!result.repoPermission) {
                    this.tabs.splice(this.tabs.indexOf('Add Repositories'), 1);
                    this.switchedValue = 'Add Builds';
                }
                if (!result.buildPermission) {
                    this.tabs.splice(this.tabs.indexOf('Add Builds'), 1);
                    this.switchedValue = 'Add Repositories';
                }
            }

            let setUpUsersAndGroups = (data, context) => {
                if (!data.length) return;

                // Get array of permissions and return object
                let formatPermissions = permissions => {
                    let response = {};
                    permissions.forEach(perm => {
                        _.assign(response, {[perm]: true})
                    });
                    return response;
                };


                // sorting data of users and groups with there's permissions
                let sortItems = (available,selected,target) => {
                    data.forEach(item => {
                        let index = _.findIndex(available, i => i.principal === item.principal);
                        if (index >= 0) {
                            let entity = available.splice(index, 1);
                            entity[0][target] = formatPermissions(item.actions);
                            entity[0].draggable = true;
                            selected.push(entity[0]);
                        } else {
                            index = _.findIndex(selected, i => i.principal === item.principal);
                            selected[index][target] = formatPermissions(item.actions);
                        }

                    });
                };

                if (context[0] === 'repo' && context[1] === 'user') sortItems(this.availableUsers,this.selectedUsers, 'repoPermissions');
                if (context[0] === 'repo' && context[1] === 'group') sortItems(this.availableGroups,this.selectedGroups, 'repoPermissions');
                if (context[0] === 'build' && context[1] === 'user') sortItems(this.availableUsers,this.selectedUsers, 'buildsPermissions');
                if (context[0] === 'build' && context[1] === 'group') sortItems(this.availableGroups,this.selectedGroups, 'buildsPermissions');
            };

            if (result.buildPermission) {
                this.buildPermission.includePatterns = result.buildPermission.includePatterns || [];
                this.buildPermission.excludePatterns = result.buildPermission.excludePatterns || [];

                if (_.includes(this.buildPermission.includePatterns, '**')) this.allBuilds = true;
                this.updateBuildsList(true);

                if (result.buildPermission.groupPermissionActions) setUpUsersAndGroups(result.buildPermission.userPermissionActions, ["build","user"]);
                if (result.buildPermission.groupPermissionActions) setUpUsersAndGroups(result.buildPermission.groupPermissionActions, ["build","group"]);
            }

            if (result.repoPermission) {
                this.repoPermission.includePatterns = result.repoPermission.includePatterns || [];
                this.repoPermission.excludePatterns = result.repoPermission.excludePatterns || [];

                let repoKeys = result.repoPermission.repoKeys || [];

                if (repoKeys[0] === 'ANY') {
                    repoKeys = ['ANY LOCAL', 'ANY REMOTE', 'ANY DISTRIBUTION'];
                }

                if (_.includes(repoKeys, 'ANY LOCAL') || _.includes(repoKeys, 'ANY REMOTE') || _.includes(repoKeys, 'ANY DISTRIBUTION')) {
                    // includes any
                    let anys = _.remove(repoKeys, repoKey => repoKey === 'ANY LOCAL' || repoKey === 'ANY REMOTE' || repoKey === 'ANY DISTRIBUTION');
                    _.forEach(anys, (any) => {
                        if (any === 'ANY LOCAL') {
                            this.anyLocal = true;
                            this.setAnyRepoOfType('local', this.anyLocal);
                        }
                        if (any === 'ANY REMOTE') {
                            this.anyRemote = true;
                            this.setAnyRepoOfType('remote', this.anyRemote);
                        }
                        if (any === 'ANY DISTRIBUTION') {
                            this.anyDistribution = true;
                            this.setAnyRepoOfType('distribution', this.anyDistribution);
                        }
                    });


                    if (repoKeys.length) {
                        repoKeys.forEach(repoKey => {

                            let moveToSelected = _.remove(this.availableRepositories, repo => repo.repoKey === repoKey);
                            this.selectedRepositories.unshift(moveToSelected[0]);

                        });
                    }
                } else if (repoKeys.length) {
                    repoKeys.forEach(repoKey => {
                        this.selectedRepositories.push(..._.remove(this.availableRepositories, repo => repo.repoKey === repoKey));
                    })
                }


                if (result.repoPermission.userPermissionActions) setUpUsersAndGroups(result.repoPermission.userPermissionActions, ["repo","user"]);
                if (result.repoPermission.groupPermissionActions) setUpUsersAndGroups(result.repoPermission.groupPermissionActions, ["repo","group"]);
            }



            this.ArtifactoryModelSaver.save();
        });
    }

    _getAllBuildsNames() {
        let deferred = this.$q.defer();
        this.BuildsDao.getAllBuildsNames().$promise.then((response) => {
            this.availableBuilds = [];
            _.forEach(response, item => this.availableBuilds.push({'buildName':item}))
            deferred.resolve();
        });
        return deferred.promise;
    }

    markAllBuilds(isChecked) {

        if (isChecked) {
            this.buildPermission.includePatterns.splice(0);
            this.buildPermission.includePatterns.push('**');
            this.buildsChange(true);
        } else {
            if (_.includes(this.buildPermission.includePatterns, '**')) this.buildPermission.includePatterns.splice(this.buildPermission.includePatterns.indexOf('**'),1);
            let buildsToMove = this.selectedBuilds.splice(0);
            this.availableBuilds.push(...buildsToMove);
            this.availableBuilds.forEach(build => delete build.draggable);
        }

    }

    buildsChange(markAll) {
        this.selectedBuilds.map(i => i.draggable = false);

        if (!markAll) {
            this.selectedBuilds.forEach(build => {
                this.buildPermission.includePatterns.push(build.buildName + '/**');
            });
        }

        this.buildPermission.includePatterns.splice(0,this.buildPermission.includePatterns.length,..._.uniq(this.buildPermission.includePatterns));
        this.updateBuildsList();
    }

    reposChanged() {
        if (this.selectedRepositories.length === 0) {
            this.selectedUsers.forEach(user => delete user.repoPermissions);
            this.selectedGroups.forEach(group => delete group.repoPermissions);
        }
    }

    updateBuildsList(saveModel) {
        let payload = {includePatterns: this.buildPermission.includePatterns, excludePatterns: this.buildPermission.excludePatterns};
        this.permissionsDao.buildPatterns(payload).$promise.then(response => {

            let selectedBuildsByPatterns = response.builds;

            // reset all items (move all items to available)
            let itemsToReset = this.selectedBuilds.splice(0);
            this.availableBuilds.push(...itemsToReset);
            this.availableBuilds.map(build => delete build.draggable);

            // Itereate all builds list and set them in the selected builds list
            if (!this.buildPermission.includePatterns.length && !this.buildPermission.excludePatterns.length) {
                this.selectedUsers.forEach(user => delete user.buildsPermissions);
                this.selectedGroups.forEach(group => delete group.buildsPermissions);
                return;
            }

            selectedBuildsByPatterns.forEach(buildName => {
                let buildToMove = _.remove(this.availableBuilds, build => build.buildName === buildName);
                if (buildToMove) this.selectedBuilds.push(...buildToMove);
                this.selectedBuilds.map(build => build.draggable = false);
            });

            if (saveModel) this.ArtifactoryModelSaver.save();
        });
    }

    _getUsersAndGroups() {
        let deferred = this.$q.defer();
        this.permissionsDao.getAllUsersAndGroups().$promise.then((response) => {
           this.availableUsers = response.allUsers;
           this.availableGroups = response.allGroups;
        }).then(() => {
            _.map(this.availableUsers, i => {
                if (i.admin) i.draggable = false;
            });
            this.availableUsers = _.sortBy(this.availableUsers, user => !user.admin);



            _.map(this.availableGroups, i => {
                if (i.admin) i.draggable = false;
            });

            this.availableGroups = _.sortBy(this.availableGroups, group => !group.admin);
            deferred.resolve();
        });
        return deferred.promise;
    }

    changeSelections(type) {
        if (type === 'user') {
            this.availableUsers.forEach(user => {
                delete user.customClass;
                user.draggable = !user.admin;
            });
            if (_.includes(this.availableUsers, this.activeUser)) {
                delete this.activeUser;
            }

            let isAnyUserSelected = _.filter(this.selectedUsers, user => user.customClass);
            if (this.selectedUsers.length && !isAnyUserSelected.length) {
                this.selectedUsers[0].customClass = 'active';
                this.activeUser = this.selectedUsers[0];
            }

        } else if (type === 'group') {
            this.availableGroups.forEach(group => delete group.customClass);
            if (_.includes(this.availableGroups, this.activeGroup)) {
                delete this.activeGroup;
            }

            let isAnyGroupSelected = _.filter(this.selectedGroups, group => group.customClass);
            if (this.selectedGroups.length && !isAnyGroupSelected.length) {
                this.selectedGroups[0].customClass = 'active';
                this.activeGroup = this.selectedGroups[0];
            }
        }
    }

    _getAllRepos() {
        let deferred = this.$q.defer();
        this.repoDataDao.getAllForPerms({"permission": true}).$promise.then((result) => {
            this.allRepos = result.repoTypesList;
            deferred.resolve();
        });
        return deferred.promise;
    }

    getRepoWithIcon(repo) {
        repo._iconClass = "icon " + (repo.type === 'local' ? "icon-local-repo" :
                (repo.type === 'remote' ? "icon-remote-repo" : (repo.type === 'virtual' ? "icon-virtual-repo" :
                        (repo.type === 'distribution' ? "icon-distribution-repo" :
                                "icon-notif-error"))));
        return repo;
    }

    setAnyRepoOfType(type, isAnyRepoOfThisTypeCheckboxIsChecked) {
        if (isAnyRepoOfThisTypeCheckboxIsChecked) {this.availableRepositories.forEach(repo => {
                if (type === repo.type) {
                    repo["draggable"] = false;
                    if (!_.contains(this.selectedRepositories, repo)) {  // if the repo isn't exist in selected add it
                        this.selectedRepositories.push(repo);
                    }
                }
            });
            this.selectedRepositories.forEach(repo => {
                if (type === repo.type) {
                    repo["draggable"] = false;
                }
            });
            _.remove(this.availableRepositories, {type});  // remove all this type from the available
        } else {

            this.selectedRepositories.forEach((repo) => {
                if (type === repo.type && !_.contains(this.availableRepositories, repo)) {
                    delete repo["draggable"];
                    this.availableRepositories.push(repo);
                }
            });
            _.remove(this.selectedRepositories, {type});

        }

        this.reposChanged();
    }

    save() {
        if (this.savePending) {
            return;
        }

        this.savePending = true;

        // Get selected repo keys
        let getSelectedRepoKeys = () => {
            let anyRepositories = _.pick({'ANY LOCAL' : this.anyLocal, 'ANY REMOTE' : this.anyRemote, 'ANY DISTRIBUTION': this.anyDistribution}, _.identity);
            let selectedRepositories = [...this.selectedRepositories];

            if (!_.isEmpty(anyRepositories)) {
                if (this.anyLocal && this.anyRemote && this.anyDistribution) return ['ANY'];

                Object.keys(anyRepositories).forEach(i => {
                    if (i === 'ANY LOCAL') _.remove(selectedRepositories, repo => repo.type === 'local');
                    if (i === 'ANY REMOTE') _.remove(selectedRepositories, repo => repo.type === 'remote');
                    if (i === 'ANY DISTRIBUTION') _.remove(selectedRepositories, repo => repo.type === 'distribution');
                });
            }

            let selectedRepositoriesRepoKeys = _.map(selectedRepositories, i => i.repoKey);
            let repoKeys = [...Object.keys(anyRepositories), ...selectedRepositoriesRepoKeys];

            return repoKeys;
        };

        // Get permissions object and return array
        let setPermissionsActionsArray = (actions) => {
            if (!actions) return [];

            let response = [];

            _.forEach(actions, (value,key) => {
                if (value) response.push(key);
            });

            return response;
        };

        // Return permissions objects for user/groups repo/build
        let getPermissionsActions = (data, context) => {
            let permissionsData = [];

            if (data.length) {
                if (context === 'repo') {
                    data.forEach(item => {
                        if (item.repoPermissions) permissionsData.push({principal: item.principal, actions: setPermissionsActionsArray(item.repoPermissions)});
                    });
                } else if (context === 'build') {
                    data.forEach(item => {
                        if (item.buildsPermissions) permissionsData.push({principal: item.principal, actions: setPermissionsActionsArray(item.buildsPermissions)});
                    });
                }
            }
            return permissionsData;
        };


        let permission = {
            name: this.permissionName,
            repoPermission: {
                repoKeys: getSelectedRepoKeys(),
                userPermissionActions: getPermissionsActions(this.selectedUsers, 'repo'),
                groupPermissionActions: getPermissionsActions(this.selectedGroups, 'repo'),
                includePatterns: this.repoPermission.includePatterns,
                excludePatterns: this.repoPermission.excludePatterns
            },
            buildPermission: {
                userPermissionActions: getPermissionsActions(this.selectedUsers, 'build'),
                groupPermissionActions: getPermissionsActions(this.selectedGroups, 'build'),
                includePatterns: this.buildPermission.includePatterns,
                excludePatterns: this.buildPermission.excludePatterns
            }
        };

        // delete repos/builds objects if non selected
        if (!this.isBuildsSelected()) delete permission.buildPermission;
        if (!this.isReposSelected()) delete permission.repoPermission;

        if (this.newPermission) {
            // SET NEW PERMISSION
            this.permissionsDao.create(permission).$promise.then(() => {
                this.savePending = false;
                this.ArtifactoryModelSaver.save();
                this.$state.go('^.permissions')
            }).catch(() => this.savePending = false);
        }
        else {
            // UPDATE EXISTING PERMISSION
            this.permissionsDao.update(permission).$promise.then(() => {
                this.savePending = false;
                this.ArtifactoryModelSaver.save();
                this.$state.go('^.permissions')
            }).catch(() => this.savePending = false);
        }
    }

    formInvalid() {
        return this.savePending || this.form.$invalid || !this.permissionName || (!this.isBuildsSelected() && !this.isReposSelected());
    }

    onRowClick(row,list,context) {
        if (list === 'selected') {
            if (context === 'user') {
                this.selectedUsers.forEach(i => delete i.customClass);
                this.activeUser = _.find(this.selectedUsers, user => user.principal === row.principal);


            } else if (context === 'group') {
                this.selectedGroups.forEach(i => delete i.customClass);
                this.activeGroup = _.find(this.selectedGroups, group => group.principal === row.principal);
            }
            row.customClass = 'active';
        }
    }

    _initNewPermissionForm() {
        this.anyLocal = this.anyRemote = this.anyDistribution = this.allBuilds = false;

        this.repoPermission.includePatterns = ['**'];
        this.repoPermission.excludePatterns = [];

        this.buildPermission.includePatterns = [];
        this.buildPermission.excludePatterns = [];
    }

    isBuildsSelected() {
        return this.buildPermission.includePatterns.length >= 1 || this.buildPermission.excludePatterns.length >= 1;
    }

    isReposSelected() {
        return this.selectedRepositories.length;
    }

}