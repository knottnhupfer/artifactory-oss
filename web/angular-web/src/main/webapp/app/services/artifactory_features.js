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
// For debugging only:
window._aolSimulate = function (value) {
    localStorage._aol = value ? "true" : "false";
}
window._aolOff = function () {
    delete localStorage._aol;
}
window._licenseSimulate = function (value) {
    localStorage._license = value;
}
window._licenseOff = function () {
    delete localStorage._license;
}

// Order of license levels
const LICENSES_LEVELS = {
    'OSS': 1,
    'ConanCE': 1,
    'JCR': 1,
    'PRO': 2,
    'ENT': 2,
	'EDGE': 3,
    'ENTPLUS': 5
}

// Minimum license needed per feature
export const FEATURES = {
    // This is the default for all other features:
    'default': {
        license: 'OSS'
    },
    // features:
    'gradle': {
        license: 'OSS',
        label: 'Gradle',
        path: 'gradle' // TODO: Add valid path
    },
    'maven': {
        license: 'OSS',
        label: 'Maven',
        path: 'maven' // TODO: Add valid path
    },
    'sbt': {
        license: 'OSS',
        label: 'SBT',
        path: 'sbt' // TODO: Add valid path
    },
    'ivy': {
        license: 'OSS',
        label: 'Ivy',
        path: 'ivy' // TODO: Add valid path
    },
    'stash': {
        license: 'PRO',
        label: 'Smart search',
        path: 'search'
    },

    'properties': {
        license: 'JCR',
        label: 'Properties',
        path: 'properties'
    },
    'builds': {
        license: 'PRO',
        label: 'Builds',
        path: 'build'
    },

    'watches': {
        license: 'PRO',
        label: 'Watches',
        path: 'watches'
    },
    'diff': {
        license: 'PRO',
        label: 'Build Diff',
        path: 'build'
    },

    'licenses': {
        license: 'PRO',
        label: 'Licenses',
        path: 'license'
    },
    'publishedmodule': {
        license: 'PRO',
        label: 'Published Module',
        path: 'build'
    },

    'highavailability': {
        license: 'ENT',
        label: 'High Availability',
        path: 'ha'
    },
    'crowd': {
        license: 'PRO',
        label: 'Crowd',
        path: 'sso'
    },
    'samlsso': {
        license: 'PRO',
        label: 'Saml & SSO',
        path: 'sso'
    },
    'oauthsso': {
        license: 'PRO',
        label: 'OAuth SSO',
        path: 'sso' // ???
    },
    'httpsso': {
        license: 'PRO',
        label: 'Http SSO',
        path: 'sso'
    },
	'signingkeys': {
		license: 'PRO',
		label: 'Signing Keys & WebStart',
		path: 'webstart'
	},
	'trustedkeys': {
		license: 'EDGE',
        label: 'Trusted Keys',
        path: 'trustedkeys'
	},
    'replications': {
        license: 'PRO',
        label: 'Replications',
        path: 'replication'
    },
    'distribution': {
        license: 'OSS',
        label: 'Distribution Repository',
        path: 'distribution'
    },
    'distribution-map-properties': {
        license: 'PRO',
        label: 'Map Properties to Bintray Version Attributes'
    },
    'ldap': {
        license: 'PRO',
        label: 'LDAP Groups',
        path: 'ldap'
    },
    'register_pro': {
        license: 'PRO',
        label: 'Register Pro',
        path: 'register pro'
    },
    'sha256': {
        license: 'PRO',
        label: 'Sha256 Calculation',
        path: 'sha256'
    },
    'supportpage': {
        license: 'PRO',
        label: 'Support Page'
    },
    'reverse_proxies': {
        license: 'JCR',
        label: 'Reverse Proxies'
    },
    'xray': {
        license: 'PRO',
        label: 'Xray Integration'
    },
    'accesstokens': {
        license: 'OSS',
        label: 'Access Tokens',
        path: 'accesstokens'
    },

    // repo types:
    'bower': {
        license: 'PRO',
        label: 'Bower',
        path: 'bower'
    },
    'chef': {
        license: 'PRO',
        label: 'Chef',
        path: 'chef'
    },
    'cocoapods': {
        license: 'PRO',
        label: 'CocoaPods',
        path: 'cocoapods'
    },
    'composer': {
        license: 'PRO',
        label: 'Composer',
        path: 'composer'
    },
    'conan': {
        license: 'ConanCE',
        label: 'Conan',
        path: 'conan'
    },
    'conda': {
        license: 'PRO',
        label: 'Conda',
        path: 'conda'
    },
    'cran': {
        license: 'PRO',
        label: 'CRAN',
        path: 'cran'
    },
    'debian': {
        license: 'PRO',
        label: 'Debian',
        path: 'debian'
    },
    'docker': {
        license: 'JCR',
        label: 'Docker',
        path: 'docker'
    },
    'gems': {
        license: 'PRO',
        label: 'Gems',
        path: 'gems'
    },
    'gitlfs': {
        license: 'PRO',
        label: 'GitLfs',
        path: 'gitlfs'
    },
    'npm': {
        license: 'PRO',
        label: 'Npm',
        path: 'npm'
    },
    'nuget': {
        license: 'PRO',
        label: 'NuGet',
        path: 'nuget'
    },
    'opkg': {
        license: 'PRO',
        label: 'Opkg',
        path: 'opkg'
    },
    'p2': {
        license: 'PRO',
        label: 'P2',
        path: 'p2'
    },
    'puppet': {
        license: 'PRO',
        label: 'Puppet',
        path: 'puppet'
    },
    'helm': {
        license: 'JCR',
        label: 'Helm',
        path: 'helm'
    },
    'go': {
        license: 'PRO',
        label: 'Go',
        path: 'go'
    },
    'pypi': {
        license: 'PRO',
        label: 'pypi',
        path: 'pypi'
    },
    'vagrant': {
        license: 'PRO',
        label: 'Vagrant',
        path: 'vagrant'
    },
    'vcs': {
        license: 'PRO',
        label: 'VCS',
        path: 'vcs'
    },
    'yum': {
        license: 'PRO',
        label: 'RPM',
        path: 'yum'
    },
    'sshserver': {
        license: 'OSS',
        label: 'SSH Authentication'
    },
    'sslcertificates': {
        license: 'PRO',
        label: 'SSL Certificates'
    },
    'native-ui': {
        license: 'JCR',
        label: 'Package Viewer'
    },
    'bundles': {
        license: 'ENTPLUS',
        label: 'Release Bundles'
    }
};

// Features that are hidden for AOL
export const HIDDEN_AOL_FEATURES = [
    'backups',
    'highavailability',
    'httpsso',
    'proxies',
    'register_pro',
    'indexer',
    'services',
    'systeminfo',
    'maintenance',
    'configdescriptor',
    'securitydescriptor',
    'system',
    'mail',
    'supportpage',
    'reverse_proxies',
    'sshserver'
];

export const HIDDEN_EDGE_FEATURES = [
    'replications',
    'distribution',
    'builds',
    'xray',
    'licenses',
    'sslcertificates'
];

// Features that are not hidden for dedicated AOL
export const SHOW_ON_DEDICATED_AOL = [
    'indexer','xray'
];


// Features that are hidden for OSS
export const HIDDEN_OSS_FEATURES = [
    'register_pro'
];

// Service for accessing allowed features and licenses
export class ArtifactoryFeatures {
    constructor(FooterDao, ArtifactoryState, $location, $q, GoogleAnalytics) {
        this.footerDao = FooterDao;
        this.ArtifactoryState = ArtifactoryState;
        this.$location = $location;
        this.GoogleAnalytics = GoogleAnalytics;
        let defer = $q.defer();
        this.whenReady = defer.promise;
        this.footerDao.get().then(() => {
            this.GoogleAnalytics._setUpGA();
            defer.resolve();
        });
    }

    getAllowedLicense(featureName) {
        featureName = featureName && featureName.toLowerCase();
        let feature = FEATURES[featureName] || FEATURES['default'];
        return feature.license;
    }

    isEnabled(feature) {
        if (!feature) {
            return true;
        }
        let allowedLicense = this.getAllowedLicense(feature);
        let currentLicense = this.getCurrentLicense();
	    if (feature === 'trustedkeys' && !this.isEdgeNode() && !this.isEnterprisePlus()) {
		    return false;
	    }
        if (LICENSES_LEVELS[currentLicense] === 1 && LICENSES_LEVELS[allowedLicense] === 1) {
            if ((currentLicense === 'ConanCE' || currentLicense === 'JCR') && _.includes(['ivy','sbt','gradle','maven'], feature)) {
                return false;
            }
            if ((currentLicense === 'OSS' || currentLicense === 'ConanCE') && _.includes(['docker','helm'], feature)) {
                return false;
            }
            if ((currentLicense === 'OSS' || currentLicense === 'JCR') && feature === 'conan') {
                return false;
            }
            if (currentLicense === 'JCR' && feature === 'reverse_proxies') {
                return true;
            }
        }

        return LICENSES_LEVELS[currentLicense] >= LICENSES_LEVELS[allowedLicense];
    }

    isDisabled(feature) {
        return !this.isEnabled(feature);
    }

    isHidden(feature) {
        if (!feature) {
            return false;
        }
        feature = feature.toLowerCase();
        if (feature === 'httpsso' && this.isAol() && this.footerDao.getInfo().httpSsoEnabledAOL) return;
        return (this.isAol() && _.contains(HIDDEN_AOL_FEATURES, feature) && !(this.isDedicatedAol() && _.contains(SHOW_ON_DEDICATED_AOL, feature))) ||
               (this.isOss() && _.contains(HIDDEN_OSS_FEATURES, feature)) ||
                (this.isEdgeNode() && _.contains(HIDDEN_EDGE_FEATURES, feature)) ||
                (this.isNonCommercial() && _.contains(HIDDEN_OSS_FEATURES, feature));
    }


    isVisible(feature) {
        return !this.isHidden(feature);
    }

    isAol() {
        if (localStorage._aol != undefined) {
            return localStorage._aol === "true";
        } // For debugging only
        return this.footerDao.getInfo() && this.footerDao.getInfo().isAol;
    }

    isDedicatedAol() {
        return this.footerDao.getInfo() && this.footerDao.getInfo().isDedicatedAol;
    }

    isCloudProviderConfigured(){
        return this.footerDao.getInfo() && this.footerDao.getInfo().isCloudProviderConfigured;
    }

    getCurrentLicense() {
        return this.footerDao.getInfo() && this.footerDao.getInfo().versionID;
    }

    isOss() {
        return this.getCurrentLicense() === 'OSS';
    }

    isEnterprisePlus() {
        return this.getCurrentLicense() === 'ENTPLUS';
    }
	isEdgeNode() {
		return this.getCurrentLicense() === 'EDGE';
    }

    isConanCE() {
        return this.getCurrentLicense() == 'ConanCE';
    }

    isJCR() {
        return this.getCurrentLicense() == 'JCR';
    }

    isJCRDemo(){

        return this.isJCR() && localStorage.JCRDemo
    }

    isNonCommercial() {
        return this.isOss() || this.isConanCE() || this.isJCR();
    }

    getFeatureName(feature) {
        feature = feature && feature.toLowerCase();
        return FEATURES[feature].label;
    }

    getFeatureLink(feature) {
        feature = feature && feature.toLowerCase();
        if (FEATURES[feature] && FEATURES[feature].path) {
            return `http://service.jfrog.org/artifactory/addons/info/${ FEATURES[feature].path }`;
        }
    }

    getGlobalName() {
        return this.isJCR() ? 'JFrog Container Registry' : 'Artifactory';
    }
}
