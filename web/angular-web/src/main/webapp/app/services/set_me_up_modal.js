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
import EVENTS from "../constants/artifacts_events.constants";
import SNIPPETS from "../constants/setmeup_snippets.constants";
import SNIPPETS_JCR from "../constants/jcr_setmeup_snippets_.constants";
import FIELD_OPTIONS from "../constants/field_options.constats";
import MESSAGES from "../constants/artifacts_messages.constants";

export class SetMeUpModal {

    constructor(JFrogModal, ArtifactoryState, SetMeUpDao, ArtifactDeployDao, RepoDataDao, JFrogEventBus,
            JFrogNotifications, FilteredResourceDao, GoogleAnalytics,
            RepositoriesDao, ReverseProxiesDao, GeneralConfigDao, ArtifactoryFeatures, ArtifactViewsDao, User,
            UserProfileDao, parseUrl, $sce, $rootScope, $timeout, $compile, DockerStatusDao) {
        this.modal = JFrogModal;
        this.setMeUpDao = SetMeUpDao;
        this.artifactDeployDao = ArtifactDeployDao;
        this.userProfileDao = UserProfileDao;
        this.repoDataDao = RepoDataDao;
        this.JFrogEventBus = JFrogEventBus;
        this.artifactoryNotifications = JFrogNotifications;
        this.filteredResourceDao = FilteredResourceDao;
        this.repositoriesDao = RepositoriesDao;
        this.dockerStatusDao = DockerStatusDao.getInstance();
        this.reverseProxiesDao = ReverseProxiesDao;
        this.artifactoryFeatures = ArtifactoryFeatures;
        this.artifactViewsDao = ArtifactViewsDao;
        this.generalConfigDao = GeneralConfigDao;
        this.artifactoryState = ArtifactoryState;
        this.GoogleAnalytics = GoogleAnalytics;
        this.user = User.getCurrent();
        this.parseUrl = parseUrl;
        this.$sce = $sce;
        this.$rootScope = $rootScope;
        this.$timeout = $timeout;
        this.$compile = $compile;
        this.MESSAGES = MESSAGES.set_me_up;

        this.repoPackageTypes = FIELD_OPTIONS.repoPackageTypes.slice(0);//make a copy

        this.resolveOnlyWithVirtualRepo = ['helm'];

        this._removeDisabledFeatures();
    }

    _removeDisabledFeatures() {
        this.repoPackageTypes = _.filter(this.repoPackageTypes,
                (item) => !(item.value === 'supportbundle' && this.artifactoryFeatures.isJCR()) && !this.artifactoryFeatures.isDisabled(item.value));
    }

    launch(node, disableToolAndRepoChange = false) {
        this.node = node;
        this.disableToolAndRepoChange = disableToolAndRepoChange;
        this._initSetMeUpScope();
        this.modalInstance = this.modal.launchModal('set_me_up_modal', this.setMeUpScope);
        this.setMeUpScope.gotoInjectionMode();
    }

    _getSetMeUpData() {
        this.setMeUpDao.get().$promise.then((data)=> {
            //            let url = new URL(data.baseUrl) //CAUSES PROBLEM ON IE, NOT REALY NEEDED...

            let parser = this.parseUrl(data.baseUrl);
            this.setMeUpScope.baseUrl = parser.href;
            this.setMeUpScope.baseUrlNoHttp = this.setMeUpScope.baseUrl.split('://')[1];
            this.setMeUpScope.host = this.artifactoryFeatures.isAol() ? parser.host.split(':')[0] : parser.host; //split by ':' in aol to remove the port number that IE returns in .host
            this.setMeUpScope.cleanHost = parser.host.split(':')[0];
            this.setMeUpScope.serverId = data.serverId;
            this.setMeUpScope.protocol = parser.protocol + '//';
            this.setMeUpScope.path = parser.pathname;
            this.setMeUpScope.aolHostName = data.hostname;

            if (!this.setMeUpScope.path.startsWith('/')) {
                this.setMeUpScope.path = '/' + this.setMeUpScope.path;
            }

            data.repoKeyTypes.sort((a, b) => {
                return (a.repoKey > b.repoKey) ? 1 : -1;
            });
            this.setMeUpScope.reposAndTypes = data.repoKeyTypes.map((item) => {
                return {
                    text: item.repoKey,
                    value: item.repoType.toLowerCase(),
                    read: item.canRead,
                    deploy: item.canDeploy,
                    local: item.isLocal,
                    remote: item.isRemote,
                    virtual: item.isVirtual,
                    defaultDeploymentConfigured: item.isDefaultDeploymentConfigured
                }
            });

            // Select the repo according to current node
            for (let i = 0; i < this.setMeUpScope.reposAndTypes.length; i++) {

                // console.log(this.setMeUpScope.reposAndTypes[i]);
                if (this.setMeUpScope.reposAndTypes[i].text.toLowerCase() == this.setMeUpScope.node.text.toLowerCase() ||
                        this.setMeUpScope.reposAndTypes[i].text.concat(
                                "-cache").toLowerCase() == this.setMeUpScope.node.text.toLowerCase()) {
                    this.setMeUpScope.selection.repo = this.setMeUpScope.reposAndTypes[i];
                    this.setMeUpScope.resolveSnippet();
                    break;
                }
            }

            let repoData = this._getRepoData(this.setMeUpScope);

            //Populate general snippets
            this._setGeneralSnippets(repoData);

            this._setRepositories(this.setMeUpScope);

            this._setShowSettings(this.setMeUpScope);

        })
    }

    _getBaseUrl() {
        this.generalConfigDao.get().$promise.then((data) => {
            this.setMeUpScope.noBaseUrl = !data.customUrlBase;
        });
    }

    _initSetMeUpScope() {
        let setMeUpDao = this.setMeUpDao;
        this.setMeUpScope = this.$rootScope.$new();

        this.setMeUpScope.MESSAGES = this.MESSAGES;
        this.setMeUpScope.settingPage = false;
        this.setMeUpScope.id = this.setMeUpScope.$id;
        this.setMeUpScope.$sce = this.$sce;
        this.setMeUpScope.settings = {};
        this.setMeUpScope.status = {};
        this.setMeUpScope.selection = {};
        this.setMeUpScope.disableToolAndRepoChange = this.disableToolAndRepoChange;
        this.setMeUpScope.close = ()=>this.modalInstance.close();
        this.setMeUpScope.title = "Set Me Up";
        this.setMeUpScope.shownRepos = [];
        this.setMeUpScope.deploySnippets = [];
        this.setMeUpScope.readSnippets = [];
        this.setMeUpScope.generalSnippets = [];
        this.setMeUpScope.adminUser = this.user.isAdmin();
        this.setMeUpScope.passwordInputPlaceholder = 'Type Password';
        this.setMeUpScope.insertCredentialsText = "Type password to insert your credentials to the code snippets";
        this.setMeUpScope.removeCredentialsText = "Remove your credentials from code snippet";
        this.setMeUpScope.node = this.node.data ? this.node.data.getRoot() : this.node;
        this._prepareSnippets();
        if (this.user.isAdmin()) {
            this._getBaseUrl();
        }
        //      this.$state.go('admin.configuration.general',{focusOnBaseUrl: true});

        let previousInjectionData = this.artifactoryState.getState('setMeUpUserData');
        if (previousInjectionData) {
            this.injectionData = previousInjectionData;
            this.useApiKey = !!previousInjectionData.apiKey;
            this.setMeUpScope.userDataInjected = true;
            this._getUserData(null, true);
        }
        else {
            this.injectionData = {};
            this._getUserData();
        }


        this.setMeUpScope.repoTypes = this.repoPackageTypes;

        // Select the repo type according to current node
        for (let i = 0; i < this.setMeUpScope.repoTypes.length; i++) {
            if (this.setMeUpScope.node.repoPkgType && this.setMeUpScope.node.repoPkgType.toLowerCase() == this.setMeUpScope.repoTypes[i].value.toLowerCase()) {
                this.setMeUpScope.selection.repoType = this.setMeUpScope.repoTypes[i];
                break;
            }
        }

        this._getSetMeUpData();

        let sc = this.setMeUpScope;

        this.setMeUpScope.$watch('selection', () => {
            if (sc.generateSettings && sc.snippet) {
                sc.generateBuildSettings();
            }
        }, true);


        this.setMeUpScope.me = () => {
            let scope = this.setMeUpScope;
            while (scope.$id != this.setMeUpScope.id && scope.$parent) {
                scope = scope.$parent;
            }
            return scope;
        };


        this.setMeUpScope.canInjectUserData = this.user.existsInDB && this.user.name !== 'anonymous' && this.user.requireProfileUnlock && this.user.requireProfilePassword;


        this.setMeUpScope.injection = {};

        /**
         * User Data Injection
         * */
        this.setMeUpScope.gotoInjectionMode = () => {
            if (this.user.requireProfileUnlock === false) {
                this.setMeUpScope.injection.password = '';
                this.setMeUpScope.injectUserData();
            }
            else {
                this.setMeUpScope.injectionMode = true;
                this.setMeUpScope.toggleInjectUserData(true);
            }
        };

        this.setMeUpScope.cancelInjection = () => {
            this.setMeUpScope.injectionMode = false;
            this.setMeUpScope.toggleInjectUserData(false);
        };

        this.setMeUpScope.injectUserData = () => {

            this.setMeUpScope.status.snippetResolved = false;

            this._getUserData(this.setMeUpScope.injection.password, true);

            // this.setMeUpScope.injectionMode = false;
            // this.setMeUpScope.toggleInjectUserData(false);

            this.setMeUpScope.injection.password = '';

        };

        this.setMeUpScope.removeUserData = () => {
            this.setMeUpScope.status.snippetResolved = false;
            this._prepareSnippets();
            this.artifactoryState.removeState('setMeUpUserData');
        };

        this.setMeUpScope.toggleInjectUserData = (bShow) => {
            let insertCredentialsBox = $('#insert-credentials-box');
            if (bShow) {
                insertCredentialsBox.show().find('.input-text').focus();
            }
            else {
                insertCredentialsBox.hide().find('.icon-clear').hide();
            }
        };

        /**
         * Filter by 'Tool' type and 'Repository' name
         * */
        this.setMeUpScope.filterByType = (selectRepo = false) => {
            if (!this.setMeUpScope.reposAndTypes) {
                return false;
            }

            let scope = this.setMeUpScope.me();
            scope.settingPage = false;

            this.GoogleAnalytics.trackEvent('Artifacts' , 'Set me up - Change' , this.setMeUpScope.node.repoPkgType , '' , 'from: ' + this.setMeUpScope.node.repoPkgType , 'to: ' + scope.selection.repoType.text , this.setMeUpScope.node.repoPkgType + '>' + scope.selection.repoType.text);

            if (scope.selection && scope.selection.repo && scope.selection.repo.value !== scope.selection.repoType.value) {
                scope.selection.repo = null;
            }
            scope.snippet = scope.readSnippet = scope.deploySnippet = null;
            scope.generateSettings = false;
            scope.generate = {};

            scope.deploySettingsMode = false;


            scope.generalSnippets = [];
            scope.readSnippets = [];
            scope.deploySnippets = [];

            this._setShowSettings(scope);
            this._setRepositories(scope);
            if (selectRepo) {
                this._selectRepoByType(scope);
                this.setMeUpScope.status.snippetResolved = false;
            }
            this.setMeUpScope.resolveSnippet();
            let repoData = this._getRepoData(scope);
            //Populate general snippets
            this._setGeneralSnippets(repoData);
        };

        /**
         * 'Generate Settings' settings
         * */
        this.setMeUpScope.checkLayoutSettings = (settings, repoType) => {
            if (this.setMeUpScope.select && this.setMeUpScope.select.selected) {
                if (repoType == 'ivy') {
                    this.setMeUpScope.selection.gradle[settings + 'UseIvy'] = true;
                    this.setMeUpScope.selection.gradle[settings + 'UseMaven'] = false;
                }
                else if (repoType == 'maven') {
                    this.setMeUpScope.selection.gradle[settings + 'UseMaven'] = true;
                    this.setMeUpScope.selection.gradle[settings + 'UseIvy'] = false;
                }
            }
            else {
                if (repoType == 'ivy') {
                    if (!this.setMeUpScope.selection.gradle[settings + 'UseMaven']) {
                        this.setMeUpScope.selection.gradle[settings + 'UseMaven'] = true;
                    }
                }
                else if (repoType == 'maven') {
                    if (!this.setMeUpScope.selection.gradle[settings + 'UseIvy']) {
                        this.setMeUpScope.selection.gradle[settings + 'UseIvy'] = true;
                    }
                }
            }
        };

        this.setMeUpScope.getMavenProps = () => {
            let scope = this.setMeUpScope.me();
            return JSON.stringify({
                release: scope.selection.maven.releases,
                snapshot: scope.selection.maven.snapshots,
                pluginRelease: scope.selection.maven.pluginReleases,
                pluginSnapshot: scope.selection.maven.pluginSnapshots,
                mirror: (scope.selection.maven.mirror) ? scope.selection.maven.mirrorAny : '',
                password: (scope.userDataInjected) ? this.injectionData.password : ''
            })
        };

        this.setMeUpScope.getGradleProps = () => {
            let scope = this.setMeUpScope.me();
            return JSON.stringify({
                pluginRepoKey: scope.selection.gradle.pluginResolver,
                libsResolverRepoKey: scope.selection.gradle.libsResolver,
                libsPublisherRepoKey: scope.selection.gradle.libsPublisher,
                pluginUseMaven: scope.selection.gradle.pluginUseMaven,
                resolverUseMaven: scope.selection.gradle.libsUseMaven,
                publisherUseMaven: scope.selection.gradle.publishUseMaven,
                pluginUseIvy: scope.selection.gradle.pluginUseIvy,
                resolverUseIvy: scope.selection.gradle.libsUseIvy,
                publisherUseIvy: scope.selection.gradle.publishUseIvy,
                pluginResolverLayout: scope.selection.gradle.pluginLayout,
                libsResolverLayout: scope.selection.gradle.libsLayout,
                libsPublisherLayouts: scope.selection.gradle.publishLayout,
                password: (scope.userDataInjected) ? this.injectionData.password : ''
            })
        };

        this.setMeUpScope.getIvyProps = () => {
            let scope = this.setMeUpScope.me();
            return JSON.stringify({
                libsRepo: scope.selection.ivy.libsRepository,
                libsRepoLayout: scope.selection.ivy.libsRepositoryLayout,
                libsResolverName: scope.selection.ivy.libsResolverName,
                useIbiblioResolver: !!(scope.selection.ivy.ibiblio),
                m2Compatible: !!(scope.selection.ivy.maven2),
                password: (scope.userDataInjected) ? this.injectionData.password : ''
            })
        };

        this.setMeUpScope.generateBuildSettings = () => {
            let scope = this.setMeUpScope.me();
            if (!scope.generate) {
                return false;
            }

            if (scope.generate.maven) {
                setMeUpDao.maven_snippet({
                    release: scope.selection.maven.releases,
                    snapshot: scope.selection.maven.snapshots,
                    pluginRelease: scope.selection.maven.pluginReleases,
                    pluginSnapshot: scope.selection.maven.pluginSnapshots,
                    mirror: (scope.selection.maven.mirror) ? scope.selection.maven.mirrorAny : ''
                }).$promise.then((result)=> {
                    scope.snippet = result.mavenSnippet;
                })
            }
            else if (scope.generate.gradle) {
                setMeUpDao.gradle_snippet({
                    pluginRepoKey: scope.selection.gradle.pluginResolver,
                    libsResolverRepoKey: scope.selection.gradle.libsResolver,
                    libsPublisherRepoKey: scope.selection.gradle.libsPublisher,
                    pluginUseMaven: scope.selection.gradle.pluginUseMaven,
                    resolverUseMaven: scope.selection.gradle.libsUseMaven,
                    publisherUseMaven: scope.selection.gradle.publishUseMaven,
                    pluginUseIvy: scope.selection.gradle.pluginUseIvy,
                    resolverUseIvy: scope.selection.gradle.libsUseIvy,
                    publisherUseIvy: scope.selection.gradle.publishUseIvy,
                    pluginResolverLayout: scope.selection.gradle.pluginLayout,
                    libsResolverLayout: scope.selection.gradle.libsLayout,
                    libsPublisherLayouts: scope.selection.gradle.publishLayout
                }).$promise.then((result)=> {
                    scope.snippet = result.gradleSnippet;
                })
            }
            else if (scope.generate.ivy) {
                setMeUpDao.ivy_snippet({
                    libsRepo: scope.selection.ivy.libsRepository,
                    libsRepoLayout: scope.selection.ivy.libsRepositoryLayout,
                    libsResolverName: scope.selection.ivy.libsResolverName,
                    useIbiblioResolver: !!(scope.selection.ivy.ibiblio),
                    m2Compatible: !!(scope.selection.ivy.maven2)
                }).$promise.then((result)=> {
                    scope.snippet = result.ivySnippet;
                })
            }
        };

        this.setMeUpScope.getGeneratorRepos = (type) => {
            let scope = this.setMeUpScope.me();
            scope.settingPage = true;
            if (!scope.generate) {
                scope.generate = {};
            }

            scope.readSnippet = scope.deploySnippet = null;

            switch (type) {
                case 'Maven':
                    setMeUpDao.maven().$promise.then((result)=> {
                        scope.generateSettings = true;
                        scope.generate = {maven: true};
                        scope.settings.maven = result;
                        // Get a repo that is maven, virtual and maybe has 'release' in it's name
                        let releases = this.getDefaultRepoKeyByParams('maven','virtual','release',true);
                        let snapshots = this.getDefaultRepoKeyByParams('maven','virtual','snapshot',true);
                        let pluginSnapshots = this.getDefaultRepoKeyByParams('maven','virtual','plugin',false) || snapshots;
                        let pluginReleases = this.getDefaultRepoKeyByParams('maven','virtual','plugin',false) || releases;
                        this.setMeUpScope.selection.maven = {
                            releases: releases,//scope.settings.maven.releases[0],
                            snapshots: snapshots,//scope.settings.maven.snapshots[0],
                            pluginReleases: pluginReleases,//scope.settings.maven.pluginReleases[0],
                            pluginSnapshots: pluginSnapshots,//scope.settings.maven.pluginSnapshots[0],
                            mirrorAny: scope.settings.maven.anyMirror[0],
                            mirror: false
                        };
                    });
                    break;
                case 'Gradle':
                    setMeUpDao.gradle().$promise.then((result)=> {
                        // Get all repos (for default type selection
                        scope.generateSettings = true;
                        scope.generate = {gradle: true};
                        scope.settings.gradle = result;
                        let defaultLibsPublisher = this.getDefaultRepoKey(this.setMeUpScope.node,'gradle','local');
                        let defaultLibsResolver = this.getDefaultRepoKey(this.setMeUpScope.node,'gradle','virtual');
                        let defaultLibsLayout =  scope.settings.gradle.layouts[0];
                        this.setMeUpScope.selection.gradle = {
                            pluginResolver: scope.settings.gradle.pluginResolver[0],
                            pluginUseMaven: true,
                            pluginUseIvy: false,
                            pluginLayout: defaultLibsLayout,
                            libsResolver: defaultLibsResolver,
                            libsUseMaven: true,
                            libsUseIvy: false,
                            libsLayout:defaultLibsLayout,
                            libsPublisher:defaultLibsPublisher,
                            publishUseMaven: true,
                            publishUseIvy: false,
                            publishLayout: defaultLibsLayout
                        };
                    });
                    break;
                case 'Ivy':
                    setMeUpDao.ivy().$promise.then((result)=> {
                        // Get all repos (for default type selection
                        scope.generateSettings = true;
                        scope.generate = {ivy: true};
                        scope.settings.ivy = result;
                        let defaultLibsRepository =  this.getDefaultRepoKey(this.setMeUpScope.node,'ivy','virtual');
                        let defaultLibsRepositoryLayout = this.getDefaultLayout('ivy',scope.settings.ivy.libsRepositoryLayout);
                        this.setMeUpScope.selection.ivy = {
                            libsRepository: defaultLibsRepository,
                            libsRepositoryLayout: defaultLibsRepositoryLayout,
                            ibiblio: true,
                            maven2: true
                        }
                    });
                    break;
                default:
                    scope.generateSettings = false;
                    break;
            }

        };

        /**
         * 'Generate Settings' validations
         * */
        this.setMeUpScope.validateToolSettings = () => {
            let scope = this.setMeUpScope.me();
            if (scope.generate.maven) {
                let mavenSettings = scope.selection.maven;
                return this.validateMavenSettings(mavenSettings)
            }

            if (scope.generate.gradle) {
                let gradleSettings = scope.selection.gradle;
                return this.validateGradleSettings(gradleSettings);
            }

            if (scope.generate.ivy) {
                let ivySettings = scope.selection.ivy;
                return this.validateIvySettings(ivySettings);
            }
        };

        /**
         * 'Resolve' snippet settings
         * */
        this.setMeUpScope.resolveSnippet = (resolveDockerReverseProxy = true) => {

            if (this.setMeUpScope.status.snippetResolved) {
                this.$timeout(()=> {
                    if (!this.setMeUpScope.deploySnippets.length && !this.setMeUpScope.generalSnippets.length && !this.setMeUpScope.readSnippets.length) {
                        this.setMeUpScope.status.snippetResolved = false;
                        this.setMeUpScope.resolveSnippet();
                    }
                });
                return;
            }
            else {
            }
            this.setMeUpScope.status.snippetResolved = true;

            if (!this.setMeUpScope.selection.repoType) {
                return;
            }
            let scope = this.setMeUpScope.me();
            let repoData = this._getRepoData(scope);
            let repoType = this.setMeUpScope.selection.repoType.value;

            if (!repoData) {
                return;
            }

            scope.deploySnippets = [];
            scope.readSnippets = [];
            scope.generalSnippets = [];

            this.setMeUpScope.hideRemoveAndGeneral = false;
            if (repoData.local && _.includes(this.resolveOnlyWithVirtualRepo, this.setMeUpScope.selection.repoType.value)) {
                this.setMeUpScope.hideRemoveAndGeneral = true;
            }

            if (this.setMeUpScope.snippets[repoType]) {
                this._setDeploySnippets(repoData);
                this._setReadSnippets(repoData);
                this._setGeneralSnippets(repoData);
            }

            //Warn the user if he doesn't have deploy permissions
            scope.hasNoDeployPermissions = (!repoData.deploy && (repoData.local || repoData.defaultDeploymentConfigured));

            if (this.setMeUpScope.selection.repoType.value === 'docker' && resolveDockerReverseProxy && !this.artifactoryFeatures.isAol() && !this.artifactoryFeatures.isNonCommercial() && this.user.name !== 'anonymous') {
                this.artifactViewsDao.getDockerProxySnippet({},
                        {repoKey: "dummy" /*this.selection.repo.text*/}).$promise.then((data)=> {
                    this.setMeUpDao.reverse_proxy_data({repoKey: this.setMeUpScope.selection.repo.text}).$promise.then(
                            (reverseProxiesData)=> {

                                if (reverseProxiesData.methodSelected) {
                                    this.setMeUpScope.reverseProxySnippet = data.template;
                                }

                                let snip;
                                if (reverseProxiesData.usingPorts) {
                                    snip = `${reverseProxiesData.serverName}:${reverseProxiesData.repoPort || '<port>'}`;
                                } else {
                                    snip = `${this.setMeUpScope.selection.repo.text}.${reverseProxiesData.serverName}`;
                                }

                                if (reverseProxiesData.methodSelected && !reverseProxiesData.usingHttps) {
                                    this.setMeUpScope.snippets.docker.general[0].title = this.setMeUpScope.snippets.docker.general[0].title_reverse_proxy + this.setMeUpScope.snippets.docker.general[0].title_insecure;
                                    this.setMeUpScope.snippets.docker.general[0].snippet = this.setMeUpScope.snippets.docker.general[0].snippet_insecure.split(
                                            '<INSECURE_SNIP>').join(snip);
                                }
                                else {
                                    this.setMeUpScope.snippets.docker.general[0].title = this.setMeUpScope.snippets.docker.general[0].title_reverse_proxy;
                                    delete this.setMeUpScope.snippets.docker.general[0].snippet;
                                }
                                this.setMeUpScope.status.snippetResolved = false;
                                this.setMeUpScope.resolveSnippet(false);
                            });
                })
                        .catch(()=> {
                            if (!this.artifactoryFeatures.isAol()) {
                                this.setMeUpScope.snippets.docker.general[0].title = this.setMeUpScope.snippets.docker.general[0].title_reverse_proxy;
                            }
                            this.setMeUpScope.status.snippetResolved = false;
                            this.setMeUpScope.resolveSnippet(false);
                        });
            }
            else if (resolveDockerReverseProxy) {
                if (this.setMeUpScope.selection.repoType.value === 'docker' && this.artifactoryFeatures.isAol()) {
                    this.setMeUpScope.status.snippetResolved = false;
                }
                delete this.setMeUpScope.reverseProxySnippet;
                delete this.setMeUpScope.snippets.docker.general[0].snippet;
            }

            if (this.setMeUpScope.selection.repoType.value === 'docker' && this.artifactoryFeatures.isAol()) {
                this._resolveDockerAolSnippets(this.setMeUpScope.selection.repo.text);
            }
            else if (this.setMeUpScope.selection.repoType.value === 'docker') {
                this._resolveDockerAolSnippets(null);
            }

        };

        /**
         * 'Deploy' snippet settings
         * */
        this.setMeUpScope.setDeploySettingsMode = () => {

            let defaultTargetPath;

            switch (this.setMeUpScope.selection.repoType.value) {
                case "maven":
                    defaultTargetPath = "settings.xml";
                    break;
                case "gradle":
                    defaultTargetPath = "build.gradle";
                    break;
                case "ivy":
                    defaultTargetPath = "ivysettings.xml";
                    break;
            }

            this.setMeUpScope.deploySettingsMode = true;
            this.setMeUpScope.snippetDeploy = {
                targetPath: defaultTargetPath,
                targetRepo: ''
            };

            this.repoDataDao.get({user: 'true'}).$promise.then((result)=> {
                this.setMeUpScope.snippetDeploy.reposList = result.repoTypesList;
            });

        };

        this.setMeUpScope.deploySettingsSnippet = () => {
            let doActualDeployment;
            let scope = this.setMeUpScope.me();
            if (scope.generate.maven) {
                setMeUpDao.maven_snippet({deploy: true}, {
                    release: scope.selection.maven.releases,
                    snapshot: scope.selection.maven.snapshots,
                    pluginRelease: scope.selection.maven.pluginReleases,
                    pluginSnapshot: scope.selection.maven.pluginSnapshots,
                    mirror: (scope.selection.maven.mirror) ? scope.selection.maven.mirrorAny : ''
                }).$promise.then((result)=> {
                    doActualDeployment(result);
                })
            }
            else if (scope.generate.gradle) {
                setMeUpDao.gradle_snippet({deploy: true}, {
                    pluginRepoKey: scope.selection.gradle.pluginResolver,
                    libsResolverRepoKey: scope.selection.gradle.libsResolver,
                    libsPublisherRepoKey: scope.selection.gradle.libsPublisher,
                    pluginUseMaven: scope.selection.gradle.pluginUseMaven,
                    resolverUseMaven: scope.selection.gradle.libsUseMaven,
                    publisherUseMaven: scope.selection.gradle.publishUseMaven,
                    pluginUseIvy: scope.selection.gradle.pluginUseIvy,
                    resolverUseIvy: scope.selection.gradle.libsUseIvy,
                    publisherUseIvy: scope.selection.gradle.publishUseIvy,
                    pluginResolverLayout: scope.selection.gradle.pluginLayout,
                    libsResolverLayout: scope.selection.gradle.libsLayout,
                    libsPublisherLayouts: scope.selection.gradle.publishLayout
                }).$promise.then((result)=> {
                    doActualDeployment(result);
                })
            }
            else if (scope.generate.ivy) {
                setMeUpDao.ivy_snippet({deploy: true}, {
                    libsRepo: scope.selection.ivy.libsRepository,
                    libsRepoLayout: scope.selection.ivy.libsRepositoryLayout,
                    libsResolverName: scope.selection.ivy.libsResolverName,
                    useIbiblioResolver: !!(scope.selection.ivy.ibiblio),
                    m2Compatible: !!(scope.selection.ivy.maven2)
                }).$promise.then((result)=> {
                    doActualDeployment(result);
                })
            }


            doActualDeployment = (config) => {
                let singleDeploy = {};

                singleDeploy.action = "deploy";
                singleDeploy.unitInfo = {
                    artifactType: "base",
                    path: this.setMeUpScope.snippetDeploy.targetPath
                };
                singleDeploy.fileName = config.savedSnippetName;
                singleDeploy.repoKey = this.setMeUpScope.snippetDeploy.targetRepo.repoKey;

                this.artifactDeployDao.post(singleDeploy).$promise.then((result)=> {
                    if (result.data) {
                        this.JFrogEventBus.dispatch(EVENTS.TREE_REFRESH);
                        this.artifactoryNotifications.createMessageWithHtml({
                            type: 'success',
                            body: `<div id="toaster-with-link">Successfully deployed <a ui-sref="artifacts.browsers.path({tab: 'General', browser: 'tree', artifact: '${result.data.repoKey}/${result.data.artifactPath}'})">${result.data.artifactPath} into ${result.data.repoKey}</a></div>`,
                            timeout: 10000
                        });
                        this.$timeout(()=> { //compile the element, so the ui-sref will work
                            let e = angular.element($('#toaster-with-link'));
                            this.$compile(e)(this.$rootScope);
                        });

                        this.filteredResourceDao.setFiltered({setFiltered: true}, {
                            repoKey: result.data.repoKey,
                            path: result.data.artifactPath
                        });
                    }
                });
            }
        };
    }

    getDefaultLayout(pkgType,layouts){
        for(let i in layouts){
            let layout = layouts[i];
            if(layout.indexOf(pkgType.toLowerCase()) !== -1){
                return layout;
            }
        }
        return layouts[0];
    }

    // Get the default repository key for a setting select box
    getDefaultRepoKey(node,repoPkgType,repoType){
        // If the selected node in tree has the same type as the requested repo
        // then return the node key
        if(repoType === node.repoType &&
                repoPkgType === node.repoPkgType.toLowerCase()){
            return this.setMeUpScope.node.repoKey;
        }

        // Search and return the first match of a repo with the same node type and repo type
        let allRepos =  this.setMeUpScope.reposAndTypes;
        for(let i in allRepos){
            let repo = allRepos[i];
            let currPkgType = repo.value;
            if(currPkgType === repoPkgType &&
                    (repo.virtual && repoType === 'virtual' ||
                    repo.local && repoType === 'local')){
                return repo.text;
            }
        }
    }

    // Search and return the first match of a repo with the same node type and maybe name
    getDefaultRepoKeyByParams(repoPkgType,repoType,nameFragment,secondBestAllowed){
        let allRepos =  this.setMeUpScope.reposAndTypes;
        let secondBestResult;
        for(let i in allRepos){
            let repo = allRepos[i];
            let currPkgType = repo.value;
            if(currPkgType === repoPkgType &&
                    (repo.virtual && repoType === 'virtual' ||
                    repo.local && repoType === 'local')){
                // If exact match was found return it
                if(nameFragment && repo.text.indexOf(nameFragment) !== -1) {
                    return repo.text;
                }
                // While exact match was not found and second best is allowed
                else if(secondBestAllowed){
                    secondBestResult = repo.text;
                }
            }
        }
        return secondBestResult;
    }

    validateIvySettings(ivySettings){
        return (typeof ivySettings.libsRepository === 'undefined' ||
                typeof ivySettings.libsRepositoryLayout === 'undefined');
    }

    validateGradleSettings(gradleSettings){
        let fieldValues = {
            pluginRepoKey: gradleSettings.pluginResolver,
            libsResolverRepoKey: gradleSettings.libsResolver,
            libsPublisherRepoKey: gradleSettings.libsPublisher,

            pluginUseIvy: gradleSettings.pluginUseIvy,
            resolverUseIvy: gradleSettings.libsUseIvy,
            publisherUseIvy: gradleSettings.publishUseIvy,

            pluginResolverLayout: gradleSettings.pluginLayout,
            libsResolverLayout: gradleSettings.libsLayout,
            libsPublisherLayouts: gradleSettings.publishLayout
        };

        return (typeof fieldValues.pluginRepoKey === 'undefined' ||
                typeof fieldValues.libsResolverRepoKey === 'undefined' ||
                typeof fieldValues.libsPublisherRepoKey === 'undefined' ||
                (fieldValues.pluginUseIvy && typeof fieldValues.pluginResolverLayout === 'undefined') ||
                (fieldValues.resolverUseIvy && typeof fieldValues.libsResolverLayout === 'undefined') ||
                (fieldValues.publisherUseIvy && typeof fieldValues.libsPublisherLayouts === 'undefined')
        );
    }

    validateMavenSettings(mavenSettings){
        let mirrorHasValidValue = (mavenSettings.mirror ? (typeof mavenSettings.mirrorAny !== 'undefined'
                                    && mavenSettings.mirrorAny !== '') : true);
        return (!mirrorHasValidValue ||
                typeof mavenSettings.releases === 'undefined' ||
                typeof mavenSettings.snapshots === 'undefined' ||
                typeof mavenSettings.pluginReleases === 'undefined' ||
                typeof mavenSettings.pluginSnapshots === 'undefined');
    }

    _fixTPL(tpl) {
        let temp = tpl;
        let protocol;
        if (_.contains(tpl, 'http://')) {
            protocol = 'http://';
        }
        else if (_.contains(tpl, 'https://')) {
            protocol = 'https://';
        }
        temp = temp.split('!' + protocol).join('@@keep_protocol@@');
        temp = temp.split(protocol).join('@@protocol@@');
        temp = temp.split('//').join('/');
        temp = temp.split('@@protocol@@').join(this.setMeUpScope.protocol);
        temp = temp.split('@@keep_protocol@@').join(protocol);

        if (_.contains(temp, this.setMeUpScope.host + "/artifactory") && this.setMeUpScope.path !== "/artifactory") {
            let newAbsoulutHost = this.setMeUpScope.host + (this.setMeUpScope.path === '/' ? '' : this.setMeUpScope.path);
            temp = temp.replace(new RegExp(this.setMeUpScope.host + "/artifactory", 'g'),
                    newAbsoulutHost);
        }

        return temp;
    }

    _setShowSettings(scope) {
        let selection = this.setMeUpScope.selection;
        if (scope.selection && selection.repoType && scope.selection.repoType.value.match('(ivy|maven|gradle)')) {
            scope.showSettings = selection.repoType.text;
        }
        else {
            scope.showSettings = false;
        }
    }

    _setRepositories(scope) {
        scope.shownRepos = this.setMeUpScope.reposAndTypes.filter((d) => {
            if (!this.setMeUpScope.selection || !this.setMeUpScope.selection.repoType || this.setMeUpScope.selection.repoType.value == 'generic') {
                return d;
            }
            if (this.setMeUpScope.selection.repoType.value == 'maven' && !d.local && !d.defaultDeploymentConfigured) {
                return false;
            }
            let isRepoMavenish = this.setMeUpScope.selection.repoType.value.match(/(maven|ivy|gradle|sbt)/gi) ? true :
                    false;
            let isSelectionMavenish = d.value.match(/(maven|ivy|gradle|sbt)/gi) ? true : false;
            if (d.value == this.setMeUpScope.selection.repoType.value || d.value == this.setMeUpScope.selection.repoType.value
                    || (isRepoMavenish && isSelectionMavenish)) {
                return d;
            }
        })
    }

    _selectRepoByType(scope) {
        // Select the repo according to current node
        for (let i = 0; i < scope.reposAndTypes.length; i++) {
            if (scope.reposAndTypes[i].value.toLowerCase() == scope.selection.repoType.value) {
                scope.selection.repo = scope.reposAndTypes[i];
                scope.resolveSnippet();
                break;
            }
        }
    }

    _getRepoData(scope) {
        let repoData = this.setMeUpScope.reposAndTypes.filter((item) => {
            if (scope.selection.repo && item.text == scope.selection.repo.text) {
                return item;
            }
        });
        repoData = (repoData.length > 0) ? repoData[0] : null;

        return repoData;
    }

    _setDeploySnippets(repoData) {
        if (!repoData) {
            return;
        }

        let scope = this.setMeUpScope.me();
        let repoType = this.setMeUpScope.selection.repoType.value;

        // Maven from server
        if (repoType == 'maven') {
            this.setMeUpDao.maven_distribution({repoKey: repoData.text}).$promise.then((result)=> {
                scope.deploySnippets = [];
                if (repoData.local || repoData.defaultDeploymentConfigured) {
                    scope.deploySnippets.push({
                        before: (this.setMeUpScope.snippets[repoType]['deploy']) ?
                                this.setMeUpScope.snippets[repoType]['deploy']['before'] : '',
                        snippet: result.distributedManagement,
                        after: (this.setMeUpScope.snippets[repoType]['deploy']) ?
                                this.setMeUpScope.snippets[repoType]['deploy']['after'] : ''
                    })
                }
            })
        }

        if (repoType != 'maven' && (repoData.local || repoData.defaultDeploymentConfigured) && this.setMeUpScope.snippets[repoType]['deploy']) {
            scope.deploySnippets = [];
            if (this.setMeUpScope.snippets[repoType]['deploy'] instanceof Array) {
                for (let i = 0; i < this.setMeUpScope.snippets[repoType]['deploy'].length; i++) {
                    let tpl = (this.setMeUpScope.snippets[repoType]['deploy']) ?
                            this.setMeUpScope.snippets[repoType]['deploy'][i]['snippet'] : null;
                    if (tpl) {
                        tpl = tpl.replace(/\$1/g, repoData.text)
                                .replace(/\$2/g, this.setMeUpScope.baseUrl)
                                .replace(/\$3/g, this.setMeUpScope.serverId)
                                .replace(/\$4/g, this.setMeUpScope.host)
                                .replace(/\$5/g, this.setMeUpScope.cleanHost)
                                .replace(/\$6/g, this.setMeUpScope.baseUrlNoHttp);
                        tpl = this._fixTPL(tpl);
                        scope.deploySnippets.push({
                            before: this.$sce.trustAsHtml(this.setMeUpScope.snippets[repoType]['deploy'][i]['before']),
                            snippet: tpl,
                            after: this.setMeUpScope.$sce.trustAsHtml(
                                    this.setMeUpScope.snippets[repoType]['deploy'][i]['after'])
                        })
                    }
                }
            } else {
                let tpl = (this.setMeUpScope.snippets[repoType]['deploy']) ?
                        this.setMeUpScope.snippets[repoType]['deploy']['snippet'] : null;
                if (tpl) {
                    tpl = tpl.replace(/\$1/g, repoData.text)
                            .replace(/\$2/g, this.setMeUpScope.baseUrl)
                            .replace(/\$3/g, this.setMeUpScope.serverId)
                            .replace(/\$4/g, this.setMeUpScope.host)
                            .replace(/\$5/g, this.setMeUpScope.cleanHost)
                            .replace(/\$6/g, this.setMeUpScope.baseUrlNoHttp);
                    tpl = this._fixTPL(tpl);
                    scope.deploySnippets.push({
                        before: this.setMeUpScope.$sce.trustAsHtml(
                                this.setMeUpScope.snippets[repoType]['deploy']['before']),
                        snippet: tpl,
                        after: this.setMeUpScope.$sce.trustAsHtml(
                                this.setMeUpScope.snippets[repoType]['deploy']['after'])
                    })
                }
            }
        }
    }

    _setReadSnippets(repoData) {
        if (!repoData) {
            return;
        }

        if (this.setMeUpScope.hideRemoveAndGeneral) return;


        let scope = this.setMeUpScope.me();
        scope.readSnippets = [];
        let repoType = this.setMeUpScope.selection.repoType.value;

        if (repoData.read && this.setMeUpScope.snippets[repoType]['read']) {
            if (this.setMeUpScope.snippets[repoType]['read'] instanceof Array) {
                for (let i = 0; i < this.setMeUpScope.snippets[repoType]['read'].length; i++) {
                    let tpl = (this.setMeUpScope.snippets[repoType]['read']) ?
                            this.setMeUpScope.snippets[repoType]['read'][i]['snippet'] : null;
                    if (tpl) {
                        tpl = tpl.replace(/\$1/g, repoData.text)
                                .replace(/\$2/g, this.setMeUpScope.baseUrl)
                                .replace(/\$3/g, this.setMeUpScope.serverId)
                                .replace(/\$4/g, this.setMeUpScope.host)
                                .replace(/\$5/g, this.setMeUpScope.cleanHost)
                                .replace(/\$6/g, this.setMeUpScope.baseUrlNoHttp);
                        tpl = this._fixTPL(tpl);
                        scope.readSnippets.push({
                            before: this.setMeUpScope.$sce.trustAsHtml(
                                    this.setMeUpScope.snippets[repoType]['read'][i]['before']),
                            snippet: tpl,
                            after: this.setMeUpScope.$sce.trustAsHtml(
                                    this.setMeUpScope.snippets[repoType]['read'][i]['after'])
                        });
                    }
                }
            }
            else {
                let tpl = (this.setMeUpScope.snippets[repoType]['read']) ?
                        this.setMeUpScope.snippets[repoType]['read']['snippet'] : null;
                if (tpl) {
                    tpl = tpl.replace(/\$1/g, repoData.text)
                            .replace(/\$2/g, this.setMeUpScope.baseUrl)
                            .replace(/\$3/g, this.setMeUpScope.serverId)
                            .replace(/\$4/g, this.setMeUpScope.host)
                            .replace(/\$5/g, this.setMeUpScope.cleanHost)
                            .replace(/\$6/g, this.setMeUpScope.baseUrlNoHttp);
                    tpl = this._fixTPL(tpl);
                    scope.readSnippets.push({
                        before: this.setMeUpScope.$sce.trustAsHtml(
                                this.setMeUpScope.snippets[repoType]['read']['before']),
                        snippet: tpl,
                        after: this.setMeUpScope.$sce.trustAsHtml(this.setMeUpScope.snippets[repoType]['read']['after'])
                    });
                }
            }
        }
    }

    _setGeneralSnippets(repoData) {
        if (!repoData) {
            return;
        }

        if (!this.setMeUpScope.selection.repoType) {
            return;
        }


        let scope = this.setMeUpScope.me();
        let repoType = this.setMeUpScope.selection.repoType.value;

        if (this.setMeUpScope.hideRemoveAndGeneral) {
            this.setMeUpScope.generalSnippets = [{
                before: this.setMeUpScope.snippets[repoType]['info_msg']
            }]
            return;
        }


        scope.generalSnippets = [];
        if (this.setMeUpScope.snippets[repoType]['general']) {
            if (this.setMeUpScope.snippets[repoType]['general'] instanceof Array) {
                for (let i = 0; i < this.setMeUpScope.snippets[repoType]['general'].length; i++) {
                    let tpl = (this.setMeUpScope.snippets[repoType]['general']) ?
                            this.setMeUpScope.snippets[repoType]['general'][i]['snippet'] : null;
                    if (tpl && repoData) {
                        tpl = tpl.replace(/\$1/g, repoData.text).replace(/\$2/g, this.setMeUpScope.baseUrl).replace(
                                /\$3/g,
                                this.setMeUpScope.serverId).replace(/\$4/g,
                                (repoType === 'cocoapods' ? this.setMeUpScope.host.split(':')[0] :
                                        this.setMeUpScope.host)).replace(/\$5/g, this.setMeUpScope.cleanHost).replace(/\$6/g, this.setMeUpScope.baseUrlNoHttp);
                        tpl = this._fixTPL(tpl);
                    }
                    scope.generalSnippets.push({
                        title: this.setMeUpScope.$sce.trustAsHtml(
                                this.setMeUpScope.snippets[repoType]['general'][i]['title']),
                        before: this.setMeUpScope.$sce.trustAsHtml(
                                this.setMeUpScope.snippets[repoType]['general'][i]['before']),
                        snippet: tpl,
                        after: this.setMeUpScope.$sce.trustAsHtml(
                                this.setMeUpScope.snippets[repoType]['general'][i]['after'])
                    });
                }
            }
            else {
                let tpl = (this.setMeUpScope.snippets[repoType]['general']) ?
                        this.setMeUpScope.snippets[repoType]['general']['snippet'] : null;
                if (tpl && repoData) {
                    tpl = tpl.replace(/\$1/g, repoData.text).replace(/\$2/g, this.setMeUpScope.baseUrl).replace(/\$3/g,
                            this.setMeUpScope.serverId).replace(/\$4/g, this.setMeUpScope.host).replace(/\$5/g, this.setMeUpScope.cleanHost).replace(/\$6/g, this.setMeUpScope.baseUrlNoHttp);
                    tpl = this._fixTPL(tpl);
                }
                scope.generalSnippets.push({
                    title: this.setMeUpScope.$sce.trustAsHtml(this.setMeUpScope.snippets[repoType]['general']['title']),
                    before: this.setMeUpScope.$sce.trustAsHtml(
                            this.setMeUpScope.snippets[repoType]['general']['before']),
                    snippet: tpl,
                    after: this.setMeUpScope.$sce.trustAsHtml(this.setMeUpScope.snippets[repoType]['general']['after'])
                });
            }
        }
    }

    _prepareSnippets(injectUserData) {

        let stringified = this.artifactoryFeatures.isJCR() ? JSON.stringify(SNIPPETS_JCR) : JSON.stringify(SNIPPETS);

        let curlAuthString = this.useApiKey ? "-H 'X-JFrog-Art-Api:<API_KEY>'" : "-u<USERNAME>:<PASSWORD>";

        stringified = stringified.split('<CURL_AUTH>').join(curlAuthString);

        if (injectUserData) {

            if (this.injectionData.userName && (this.injectionData.password || this.injectionData.apiKey)) {
                stringified = stringified.split(
                        '<USERNAME>:<PASSWORD> (converted to base 64)').join(
                        btoa(this.injectionData.userName.toLowerCase() + ':' + (this.injectionData.password || this.injectionData.apiKey)));
            }
            if (this.injectionData.userName) {
                stringified = stringified.split('<USERNAME>').join(
                        this.injectionData.userName);
            }
            if (this.injectionData.userName) {
                stringified = stringified.split('<URL_ENCODED_USERNAME>').join(
                        this.injectionData.userName.split('@').join('%40'));
            }
            if (this.injectionData.password && !this.injectionData.apiKey) {
                stringified = stringified.split(
                        '<PASSWORD>').join(this.injectionData.password);
            }
            if (this.injectionData.password) {
                stringified = stringified.split('<BASE64_PASSWORD>').join(
                        btoa(this.injectionData.password));
            }
            if (this.injectionData.apiKey) {
                stringified = stringified.split('<PASSWORD>').join(this.injectionData.apiKey);
                stringified = stringified.split('<API_KEY>').join(this.injectionData.apiKey);
            }
            if (this.injectionData.email) {
                stringified = stringified.split('youremail@email.com').join(this.injectionData.email);
            }
            this.setMeUpScope.userDataInjected = true;
            this.artifactoryState.setState('setMeUpUserData', this.injectionData);
        }
        else {
            this.setMeUpScope.userDataInjected = false;
        }

        this.setMeUpScope.snippets = JSON.parse(stringified);

        if (this.setMeUpScope.filterByType) {
            this.setMeUpScope.filterByType();
        }
    }

    _getUserData(password, inject) {

        let checkApiKeyExistance = () => {
            return this.userProfileDao.hasApiKey().$promise.then(()=> {
                this.useApiKey = true;
            }).catch(() => {
                this.useApiKey = false;
            }).finally(() => {
                if (this.user.requireProfileUnlock === false && !this.useApiKey) {
                    this.setMeUpScope.canInjectUserData = false;
                }
            })
        };

        let getApiKey = (password) => {
            this.userProfileDao.getApiKey.authenticate({username: this.user.name, password})
            this.userProfileDao.getApiKey().$promise.then((res)=> {
                this.useApiKey = !!res.apiKey;
                this.injectionData.apiKey = res.apiKey;
                this.injectionData.userName = this.user.name;

                if (this.user.requireProfileUnlock === false && !this.useApiKey) {
                    this.setMeUpScope.canInjectUserData = false;
                }

                this.setMeUpScope.status.snippetResolved = false;
                this._prepareSnippets(inject);
            });
        };

        if (_.isEmpty(this.injectionData) && password && this.user.requireProfileUnlock !== false) {
            this.userProfileDao.fetch({password: password || ''}).$promise.then(res => {
                this.injectionData.password = res.data.user.password;
                this.injectionData.email = res.data.user.email;
                getApiKey(password);
            }).catch((err)=> {
                if (err.status === 400) {
                    this.setMeUpScope.injectionMode = true;
                }
            });
        }
        else if (inject && !_.isEmpty(this.injectionData)) {
            this.useApiKey = !!this.injectionData.apiKey;
            if (this.user.requireProfileUnlock === false && !this.useApiKey) {
                this.setMeUpScope.canInjectUserData = false;
            }
            this.setMeUpScope.status.snippetResolved = false;
            this._prepareSnippets(inject);
        }
        else if (!inject && !password) {
            checkApiKeyExistance().then(() => {
                this.setMeUpScope.status.snippetResolved = false;
                this._prepareSnippets(false);
            })
        }


    }

    _resolveDockerAolSnippets(repoKey) {
        if (repoKey === null) {
            this.setMeUpScope.snippets.docker.general[0].after = this.setMeUpScope.snippets.docker.general[0].after_example_server;
        }

        let snippets = ['deploySnippets', 'readSnippets', 'generalSnippets'];
        let snippetsParts = ['before', 'after', 'snippet'];

        let loopRun = (serverName) => {
            snippets.forEach((snippet) => {
                this.setMeUpScope[snippet].forEach((snip)=> {
                    snippetsParts.forEach((part) => {
                        if (snip[part]) {
                            snip[part] = snip[part].toString();
                            if (repoKey === null) { //inject the default
                                snip[part] = snip[part].split('<DOCKER_SERVER>').join('artprod.mycompany');
                            }
                            else {
                                if (part === 'after' && snip.after_example_server) {
                                    delete snip[part];
                                }
                                else {
                                    snip[part] = snip[part].split('<DOCKER_SERVER>').join(serverName);
                                }
                            }
                        }
                    })
                })
            });
        };

        if (repoKey) {
            let serverName = this.setMeUpScope.aolHostName + '-' + repoKey + '.jfrog.io';
            this.$timeout(()=> {
                loopRun(serverName);
            })
        }
        else {
            loopRun(null);
        }

    }

}
