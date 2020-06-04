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
export class ArtifactorySidebarDriver {
    constructor($timeout, $rootScope, $state, User, FooterDao, ArtifactoryFeatures, JFrogEventBus,
                ArtifactoryStorage, ArtifactoryState, KeyboardShortcutsModalService) {

        this.$timeout = $timeout;
        this.User = User;
        this.user = User.getCurrent();
        this.storage = ArtifactoryStorage;
        this.features = ArtifactoryFeatures;
        this.footerDao = FooterDao;
        this.ArtifactoryState = ArtifactoryState;
        this.JFrogEventBus = JFrogEventBus;
        this.EVENTS = JFrogEventBus.getEventsDefinition();
        this.$rootScope = $rootScope;
        this.$state = $state;

        this.licenseType = this.features.getCurrentLicense();
        this.isAol = this.features.isAol();
        this.isDedicatedAol = this.features.isDedicatedAol();
        this.keyboardShortcutsModalService = KeyboardShortcutsModalService;
    }

    setMenu(_menu) {
        this.theMenu = _menu;
    }

    registerEvents() {
        this.User.whenLoadedFromServer.then(() => this.theMenu.refreshMenu());
        if (!this.ArtifactoryState.getState('sidebarEventsRegistered')) {
            this.JFrogEventBus.register(this.EVENTS.USER_CHANGED, () => this.theMenu.refreshMenu());
            this.JFrogEventBus.register(this.EVENTS.FOOTER_REFRESH, () => {
                this.getFooterData(true).then((footerData) => {
                    this.theMenu.refreshMenu();
                    this.theMenu.footerData = footerData;
                });
            });
            this.ArtifactoryState.setState('sidebarEventsRegistered', true);
        }
    }

    getMenuItems() {
        return [
            {
                label: 'Home',
                stateParent: "home",
                state: "home",
                icon: 'icon icon-home-new',
                selected: true
            },
            {
                label: 'Artifacts',
                state: "artifacts.browsers.path",
                stateParent: "artifacts",
                stateParams: {tab: 'General', artifact: '', browser: 'tree'},
                icon: 'icon icon-artifacts-new',
                isDisabled: !this.user.canView('artifacts'),
                selected: false
            },
	        {
		        label: 'Packages',
		        stateParent: "packages",
		        state: "packagesNative",
		        icon: 'icon icon-navigation-products',
                feature: 'native-ui',
                isDisabled: !this.user.canView('packages') || this.features.isOss() || this.features.isConanCE(),
                selected: false
	        },
            {
                label: 'Search',
                state: "search",
                id: 'search',
                icon: 'icon icon-menu-search',
                isDisabled: !this.user.canView('search'),
                selected: false
            },
            {
                label: 'Release Bundles',
                state: "bundles.list",
	            stateParent: "bundles",
	            stateParams: {tab: 'target'},
                icon: 'icon icon-navigation-bundle',
                isDisabled: !this.user.isAdmin(),
                isHidden: this.features.isNonCommercial() || this.features.isAol(),
                selected: false
            },
            {
                label: 'Builds',
                stateParent: "builds",
                state: "builds.all",
                icon: 'icon icon-builds-new',
                selected: false,
                isDisabled: !this.user.buildBasicView,
                isHidden: this.features.isHidden('builds')
            },
            {
                label: 'Admin',
                icon: 'icon icon-admin-new',
                id: 'admin',
                stateParent: "admin",
                state: 'admin',
                selected: false,
                children: this._getAdminMenuItems(),
                isDisabled: !this.user.getCanManage()
            }
        ]
    }

    _getAdminMenuItems() {
        let adminItems = [
            {
                "label": "Repositories",
                "state": "admin.repositories",
                "subItems": [
                    {"label": "Local", "state": "admin.repositories.list", "stateParams": {"repoType": "local"}},
                    {"label": "Remote", "state": "admin.repositories.list", "stateParams": {"repoType": "remote"}},
                    {"label": "Virtual", "state": "admin.repositories.list", "stateParams": {"repoType": "virtual"}},
                    {"label": "Distribution", "state": "admin.repositories.list", "stateParams": {"repoType": "distribution"}, "feature": "distribution"},
                    {"label": "Layouts", "state": "admin.repositories.repo_layouts"}
                ]
            },

            {
                "label": "Configuration",
                "state": "admin.configuration",
                "subItems": [
                    {"label": "General Configuration", "state": "admin.configuration.general"},
                    {"label": "JFrog Xray", "state": "admin.configuration.xray", "feature": "xray"},
                    {"label": "Licenses", "state": "admin.configuration.licenses", "feature": "licenses"},
                    {"label": "Property Sets", "state": "admin.configuration.property_sets", "feature": "properties"},
                    {"label": "Proxies", "state": "admin.configuration.proxies", "feature": "proxies"},
                    {
                        "label": "HTTP Settings",
                        "state": "admin.configuration.reverse_proxy",
                        "feature": "reverse_proxies"
                    },
                    {"label": "Mail", "state": "admin.configuration.mail", "feature": "mail"},
                    {"label": "High Availability", "state": "admin.configuration.ha", "feature": "highavailability"},
                    //{"label": "Bintray", "state": "admin.configuration.bintray"},
                    {
                        "label": "Artifactory Licenses",
                        "state": "admin.configuration.register_pro",
                        "feature": "register_pro"
                    }
                ]
            },

            {
                "label": "Security",
                "state": "admin.security",
                "subItems": [
                    {"label": "Security Configuration", "state": "admin.security.general"},
                    {"label": "Users", "state": "admin.security.users"},
                    {"label": "Groups", "state": "admin.security.groups"},
                    {"label": "Permissions", "state": "admin.security.permissions"},
                    {"label": "Access Tokens", "state": "admin.security.access_tokens", "feature": "access_tokens"},
                    {"label": "LDAP", "state": "admin.security.ldap_settings"},
                    {"label": "Crowd / JIRA", "state": "admin.security.crowd_integration", "feature": "crowd"},
                    {"label": "SAML SSO", "state": "admin.security.saml_integration", "feature": "samlsso"},
                    {"label": "OAuth SSO", "state": "admin.security.oauth", "feature": "oauthsso"},
                    {"label": "HTTP SSO", "state": "admin.security.http_sso", "feature": "httpsso"},
                    {"label": "SSH Server", "state": "admin.security.ssh_server", "feature": "sshserver"},
                    {"label": "Signing Keys", "state": "admin.security.signing_keys", "feature": "signingkeys"},
                    {"label": "Trusted Keys", "state": "admin.security.trusted_keys", "feature": "trustedkeys"},
                    {"label": "Certificates", "state": "admin.security.ssl_certificates", "feature": "sslcertificates"}
                ]
            },

            {
                "label": "Services",
                "state": "admin.services",
                "subItems": [
                    {"label": "Backups", "state": "admin.services.backups", "feature": "backups"},
                    {"label": "Maven Indexer", "state": "admin.services.indexer", "feature": "indexer"}
                ]

            },

            {
                "label": "Import & Export",
                "state": "admin.import_export",
                "subItems": [
                    {"label": "Repositories", "state": "admin.import_export.repositories", "feature": "repositories"},
                    {"label": "System", "state": "admin.import_export.system", "feature": "system"}

                ]

            },

            {
                "label": "Advanced",
                "state": "admin.advanced",
                "subItems": [
                    {"label": "Support Zone", "state": "admin.advanced.support_page", "feature": "supportpage"},
                    {"label": "Log Analytics", "state": "admin.advanced.log_analytics"},
                    {"label": "System Logs", "state": "admin.advanced.system_logs"},
                    {"label": "System Info", "state": "admin.advanced.system_info", "feature": "systeminfo"},
                    {"label": "Maintenance", "state": "admin.advanced.maintenance", "feature": "maintenance"},
                    {"label": "Storage", "state": "admin.advanced.storage_summary"},
                    {
                        "label": "Config Descriptor",
                        "state": "admin.advanced.config_descriptor",
                        "feature": "configdescriptor"
                    },
                    {
                        "label": "Security Descriptor",
                        "state": "admin.advanced.security_descriptor",
                        "feature": "securitydescriptor"
                    }

                ]
            }

        ]
        this._fixAdminMenuItems(adminItems);
        return adminItems;

    }

    onBeforeStateSwitch(item) {
        // Fix browser param according to user preference
        if (item.state === "artifacts.browsers.path") {
            let storedBrowser = this.storage.getItem('BROWSER');
            item.stateParams.browser = storedBrowser || 'tree';
            item.stateParams.tab = storedBrowser === 'stash' ? 'StashInfo' : 'General';
        }

        this.ArtifactoryState.setState('clearErrorsOnStateChange', true)

    }

    getFooterData(force) {
        let prom = this.footerDao.get(force);
        return prom;
    }

    onKeyDown(e) {
        if (e.keyCode === 82 && (e.ctrlKey || e.metaKey) && e.altKey) { // Ctrl+Alt+R
            e.preventDefault();
            this.theMenu.goToState(_.find(this.theMenu.menuItems, {state: 'artifacts.browsers.path'}));
            this.theMenu.closeSubMenu(0, true);
        }
        if (e.keyCode === 83 && (e.ctrlKey || e.metaKey) && e.altKey) { // Ctrl+Alt+S
            e.preventDefault();
            this.theMenu.goToState(_.find(this.theMenu.menuItems, {state: 'search'}));
            this.theMenu.closeSubMenu(0, true);
        }
        if (e.keyCode === 66 && (e.ctrlKey || e.metaKey) && e.altKey) { // Ctrl+Alt+B
            if (this.features.isEdgeNode()) return;
            e.preventDefault();
            this.theMenu.goToState(_.find(this.theMenu.menuItems, {state: 'builds.all'}));
            this.theMenu.closeSubMenu(0, true);
        }
        if (e.keyCode === 76 && (e.ctrlKey || e.metaKey) && e.altKey) { // Ctrl+Alt+L
            e.preventDefault();
            this._logout();
            this.theMenu.closeSubMenu(0, true);

        }
        if (e.keyCode === 78 && (e.ctrlKey || e.metaKey) && e.altKey) { // Ctrl+Alt+N
            e.preventDefault();
            if ($('.admin-grid-buttons').find('a#new-button, a#repositories-new, a#new-rule').length) {
                this.$timeout(() => {
                    angular.element(
                            document.querySelector('a#new-button, a#repositories-new, a#new-rule')).triggerHandler(
                            'click')
                }, 0);
            }
        }
	    if (e.keyCode === 191 && (e.ctrlKey || e.metaKey) && e.altKey) { // Ctrl+Alt+/ - open shortcuts pop up
		    e.preventDefault();
		    this.keyboardShortcutsModalService.showhSortcutsModal();
		    this.theMenu.closeSubMenu(0, true);
	    }
    }

    _logout() {
        this.User.logout().then(() => {
            this.$state.go('login');
        });
    }

    _fixAdminMenuItems(adminItems) {
        let ind = 0;

        this.footerDao.get(false).then(footerData => {
            let xrayDetails = {
                "licenseType": this.licenseType,
                "xrayConfigured": footerData.xrayConfigured,
                "isXrayEnabled": footerData.xrayEnabled,
                "isXrayLicensed": footerData.xrayLicense,
                "supportedLicenses": ["OSS", "PRO", "ENT"]
            }


            adminItems.forEach((item) => {
                item.isDisabled = true;
                // if all subitems are hidden then hide item
                item.isHidden = _.every(item.subItems, (subitem) => this.features.isHidden(subitem.feature));
                item.subItems.forEach((subitem) => {
                    subitem.id = ind++;

                    if ((this.features.isConanCE() || this.features.isJCR()) && subitem.label === 'Maven Indexer') {
                        subitem.isHidden = true;
                        return;
                    }
                    if (subitem.label != 'JFrog Xray') {
                        subitem.isHidden = this.features.isHidden(subitem.feature);
                    }
                    if ((!this.user.canView(subitem.state) ||
                                    this.features.isDisabled(subitem.feature)) && subitem.label != "JFrog Xray") {
                        subitem.isDisabled = true;
                    } else { // if one subitem is enabled then item is enabled
                        item.isDisabled = false;
                    }

                    if (subitem.label === 'JFrog Xray') {

                        if (!xrayDetails.isXrayLicensed) {
                            if (xrayDetails.xrayConfigured) {
                                subitem.isDisabled = false;
                                return;
                            }
                            if (xrayDetails.licenseType != "ENT") {
                                subitem.isDisabled = true;
                            }
                        } else {
                            subitem.isDisabled = false;
                        }

                        //In any case, if we don't have admin perms, Xray item should not be enabled
                        if (!this.user.canView(subitem.state)) {
                            subitem.isDisabled = true;
                        }
                    }
                });
            });
        });
    }

}
