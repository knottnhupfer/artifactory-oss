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
export const AdminMenuItems = [
    {
        "label": "Repositories",
        "state": "admin.repositories",
        "subItems": [
            {"label": "Local", "state": "admin.repositories.list", "stateParams": {"repoType": "local"}},
            {"label": "Remote", "state": "admin.repositories.list", "stateParams": {"repoType": "remote"}},
            {"label": "Virtual", "state": "admin.repositories.list", "stateParams": {"repoType": "virtual"}},
            {"label": "Distribution", "state": "admin.repositories.list", "stateParams": {"repoType": "distribution"}},
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
            {"label": "HTTP Settings", "state": "admin.configuration.reverse_proxy", "feature": "reverse_proxies"},
            {"label": "Mail", "state": "admin.configuration.mail", "feature": "mail"},
            {"label": "High Availability", "state": "admin.configuration.ha", "feature": "highavailability"},
            //{"label": "Bintray", "state": "admin.configuration.bintray"},
            {"label": "Artifactory Licenses", "state": "admin.configuration.register_pro", "feature": "register_pro"}
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
            {"label": "LDAP", "state": "admin.security.ldap_settings"},
            {"label": "Crowd / JIRA", "state": "admin.security.crowd_integration", "feature": "crowd"},
            {"label": "SAML SSO", "state": "admin.security.saml_integration", "feature": "samlsso"},
            {"label": "OAuth SSO", "state": "admin.security.oauth", "feature": "oauthsso"},
            {"label": "HTTP SSO", "state": "admin.security.http_sso", "feature": "httpsso"},
            {"label": "SSH Server", "state": "admin.security.ssh_server", "feature": "sshserver"},
            {"label": "Signing Keys", "state": "admin.security.signing_keys", "feature": "signingkeys"}
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
            {"label": "Support Zone", "state": "admin.advanced.support_page", "feature":"supportpage"},
            {"label": "Log Analytics", "state": "admin.advanced.log_analytics"},
            {"label": "System Logs", "state": "admin.advanced.system_logs"},
            {"label": "System Info", "state": "admin.advanced.system_info", "feature":"systeminfo"},
            {"label": "Maintenance", "state": "admin.advanced.maintenance", "feature":"maintenance"},
            {"label": "Storage", "state": "admin.advanced.storage_summary"},
            {"label": "Config Descriptor", "state": "admin.advanced.config_descriptor", "feature":"configdescriptor"},
            {"label": "Security Descriptor", "state": "admin.advanced.security_descriptor", "feature":"securitydescriptor"}

        ]
    }

];