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
export default {
    "/**": [
        {
            title: "User Guide",
            link: "https://www.jfrog.com/confluence/display/JCR/Welcome+to+JFrog+Container+Registry",
            priority: 0
        },
        {
            title: "REST API",
            link: "https://www.jfrog.com/confluence/display/JCR/JFrog+Container+Registry+REST+API",
            priority: 0
        }
    ],

    "/home": [
        {
            title: "General Information",
            link: "https://www.jfrog.com/confluence/display/JCR/General+Information",
            priority: 1
        }
    ],
    "/profile": [
        {
            title: "Updating Your Profile",
            link: "https://www.jfrog.com/confluence/display/JCR/Updating+Your+Profile",
            priority: 1
        }
    ],
    "/forgot-password": [
        {
            title: "Forgot Password",
            link: "https://www.jfrog.com/confluence/display/JCR/Updating+Your+Profile#UpdatingYourProfile-ChangingYourPasswordandEmail",
            priority: 1
        }
    ],

    "/artifacts/browse/tree/**": [
        {
            title: "Browsing Artifacts",
            link: "https://www.jfrog.com/confluence/display/JCR/Browsing+JFrog+Container+Registry",
            priority: 1
        },
    ],
    "/artifacts/browse/simple/**": [
        {
            title: "Browsing Artifacts",
            link: "https://www.jfrog.com/confluence/display/JCR/Browsing+JFrog+Container+Registry",
            priority: 1
        },
    ],
    "/artifacts/browse/**": [
        {
            title: "Deploying Artifacts",
            link: "https://www.jfrog.com/confluence/display/JCR/Deploying+Artifacts",
            priority: 2
        },
        {
            title: "Set Me Up",
            link: "https://www.jfrog.com/confluence/display/JCR/User+Guide",
            priority: 5
        },
        {
            title: "Deploy Artifact with REST API",
            link: "https://www.jfrog.com/confluence/display/JCR/JFrog+Container+Registry+REST+API#JFrogContainerRegistryRESTAPI-DeployArtifact",
            priority: 200
        }
    ],


    "/artifacts/browse/tree/search/**": [
        {
            title: "Searching Artifacts",
            link: "https://www.jfrog.com/confluence/display/JCR/Searching+for+Artifacts",
            priority: 3
        },
        {
            title: "Artifactory Query Language",
            link: "https://www.jfrog.com/confluence/display/JCR/Artifactory+Query+Language",
            priority: 200
        }
    ],

    // Every search type needs a duplicated entry, one under the tree search and one for simple

    "/artifacts/browse/tree/search/quick/**": [
        {
            title: "Execute Quick Search with REST API",
            link: "https://www.jfrog.com/confluence/display/JCR/JFrog+Container+Registry+REST+API#JFrogContainerRegistryRESTAPI-ArtifactSearch(QuickSearch)",
            priority: 200
        }
    ],
    "/artifacts/browse/simple/search/quick/**": [
        {
            title: "Execute Quick Search with REST API",
            link: "https://www.jfrog.com/confluence/display/JCR/JFrog+Container+Registry+REST+API#JFrogContainerRegistryRESTAPI-ArtifactSearch(QuickSearch)",
            priority: 200
        }
    ],
    "/artifacts/browse/tree/search/class/**": [
        {
            title: "Execute Archive Search with REST API",
            link: "https://www.jfrog.com/confluence/display/JCR/JFrog+Container+Registry+REST+API#JFrogContainerRegistryRESTAPI-ArchiveEntriesSearch(ClassSearch)",
            priority: 200
        }
    ],
    "/artifacts/browse/simple/search/class/**": [
        {
            title: "Execute Archive Search with REST API",
            link: "https://www.jfrog.com/confluence/display/JCR/JFrog+Container+Registry+REST+API#JFrogContainerRegistryRESTAPI-ArchiveEntriesSearch(ClassSearch)",
            priority: 200
        }
    ],
    "/artifacts/browse/tree/search/property/**": [
        {
            title: "Execute Property Search with REST API",
            link: "https://www.jfrog.com/confluence/display/JCR/JFrog+Container+Registry+REST+API#JFrogContainerRegistryRESTAPI-PropertySearch",
            priority: 200
        }
    ],
    "/artifacts/browse/simple/search/property/**": [
        {
            title: "Execute Property Search with REST API",
            link: "https://www.jfrog.com/confluence/display/JCR/JFrog+Container+Registry+REST+API#JFrogContainerRegistryRESTAPI-PropertySearch",
            priority: 200
        }
    ],

    "/builds/**": [
        {
            title: "Build Integration",
            link: "https://www.jfrog.com/confluence/display/JCR/Build+Integration",
            priority: 1
        },
        {
            title: "Upload Build with REST API",
            link: "https://www.jfrog.com/confluence/display/JCR/JFrog+Container+Registry+REST+API#JFrogContainerRegistryRESTAPI-BuildUpload",
            priority: 200
        }
    ],


    "/admin/repositories/**": [
        {
            title: "Configuring Repositories",
            link: "https://www.jfrog.com/confluence/display/JCR/Configuring+Repositories",
            priority: 100
        }
    ],

    "/admin/repositories/local": [
        {
            title: "Local Repositories",
            link: "https://www.jfrog.com/confluence/display/JCR/Local+Repositories",
            priority: 1
        }
    ],
    "/admin/repository/local/**": [
        {
            title: "Local Repositories",
            link: "https://www.jfrog.com/confluence/display/JCR/Local+Repositories",
            priority: 1
        }
    ],

    "/admin/repositories/remote": [
        {
            title: "Remote Repositories",
            link: "https://www.jfrog.com/confluence/display/JCR/Remote+Repositories",
            priority: 1
        }
    ],
    "/admin/repository/remote/**": [
        {
            title: "Remote Repositories",
            link: "https://www.jfrog.com/confluence/display/JCR/Remote+Repositories",
            priority: 1
        }
    ],

    "/admin/repositories/virtual": [
        {
            title: "Virtual Repositories",
            link: "https://www.jfrog.com/confluence/display/JCR/Virtual+Repositories",
            priority: 1
        },
        {
            title: "Deploying to a Virtual Repository",
            link: "https://www.jfrog.com/confluence/display/JCR/Virtual+Repositories#VirtualRepositories-DeployingtoaVirtualRepository",
            priority: 2
        }
    ],
    "/admin/repository/virtual/**": [
        {
            title: "Virtual Repositories",
            link: "https://www.jfrog.com/confluence/display/JCR/Virtual+Repositories",
            priority: 1
        },
        {
            title: "Deploying to a Virtual Repository",
            link: "https://www.jfrog.com/confluence/display/JCR/Virtual+Repositories#VirtualRepositories-DeployingtoaVirtualRepository",
            priority: 2
        }
    ],
    "/admin/repositories/distribution": [
        {
            title: "Distribution Repositories",
            link: "https://www.jfrog.com/confluence/display/JCR/Distribution+Repository",
            priority: 1
        }
    ],
    "/admin/repositories/distribution/**": [
        {
            title: "Distribution Repositories",
            link: "https://www.jfrog.com/confluence/display/JCR/Distribution+Repository",
            priority: 1
        }
    ],


    "/admin/repo_layouts**": [
        {
            title: "Repository Layouts",
            link: "https://www.jfrog.com/confluence/display/JCR/Repository+Layouts",
            priority: 1
        }
    ],

    "/admin/configuration/property_sets**": [
        {
            title: "Property Sets",
            link: "https://www.jfrog.com/confluence/display/JCR/Properties",
            priority: 1
        }
    ],
    "/admin/configuration/proxies**": [
        {
            title: "Managing Proxies",
            link: "https://www.jfrog.com/confluence/display/JCR/Managing+Proxies",
            priority: 1
        }
    ],
    "/admin/configuration/mail": [
        {
            title: "Mail Configuration",
            link: "https://www.jfrog.com/confluence/display/JCR/Mail+Server+Configuration",
            priority: 1
        }
    ],

    "/admin/security/users**": [
        {
            title: "User Management",
            link: "https://www.jfrog.com/confluence/display/JCR/Users+and+Groups",
            priority: 1
        }
    ],
    "/admin/security/groups**": [
        {
            title: "Group Management",
            link: "https://www.jfrog.com/confluence/display/JCR/Users+and+Groups#UsersandGroups-CreatingandEditingGroups",
            priority: 1
        }
    ],
    "/admin/security/permissions**": [
        {
            title: "Permission Management",
            link: "https://www.jfrog.com/confluence/display/JCR/Permissions",
            priority: 1
        }
    ],


    // Remove after fixing the url of the new permission form

    "/admin/security/permission/**": [
        {
            title: "Permission Management",
            link: "https://www.jfrog.com/confluence/display/JCR/Permissions",
            priority: 1
        }
    ],



    "/admin/security/ldap_settings": [
        {
            title: "LDAP Settings",
            link: "https://www.jfrog.com/confluence/display/JCR/Managing+Security+with+LDAP#ManagingSecuritywithLDAP-Configuration",
            priority: 1
        }
    ],
    "/admin/security/ldap_settings/**": [
        {
            title: "LDAP Settings",
            link: "https://www.jfrog.com/confluence/display/JCR/Managing+Security+with+LDAP#ManagingSecuritywithLDAP-Configuration",
            priority: 1
        }
    ],

    "/admin/security/ssh_server": [
        {
            title: "SSH Server",
            link: "https://www.jfrog.com/confluence/display/JCR/SSH+Integration",
            priority: 1
        }
    ],
    "/admin/security/signing_keys": [
        {
            title: "Signing Keys",
            link: "https://www.jfrog.com/confluence/display/JCR/Centrally+Secure+Passwords",
            priority: 1
        }
    ],

    "/admin/services/backups**": [
        {
            title: "Backup Management",
            link: "https://www.jfrog.com/confluence/display/JCR/Managing+Backups",
            priority: 1
        }
    ],

    "/admin/import_export**": [
        {
            title: "Importing & Exporting",
            link: "https://www.jfrog.com/confluence/display/JCR/Importing+and+Exporting",
            priority: 1
        }
    ],
    "/admin/import_export/repositories": [
        {
            title: "Repository Import with REST API",
            link: "https://www.jfrog.com/confluence/display/JCR/JFrog+Container+Registry+REST+API#JFrogContainerRegistryRESTAPI-ImportRepositoryContent",
            priority: 200
        }
    ],
    "/admin/import_export/system": [
        {
            title: "System Import with REST API",
            link: "https://www.jfrog.com/confluence/display/JCR/JFrog+Container+Registry+REST+API#JFrogContainerRegistryRESTAPI-FullSystemImport",
            priority: 200
        },
        {
            title: "System Export with REST API",
            link: "https://www.jfrog.com/confluence/display/JCR/JFrog+Container+Registry+REST+API#JFrogContainerRegistryRESTAPI-ExportSystem",
            priority: 200
        }
    ],

    "/admin/advanced/system_info": [
        {
            title: "System Info",
            link: "https://www.jfrog.com/confluence/display/JCR/JFrog+Container+Registry+REST+API#JFrogContainerRegistryRESTAPI-SystemInfo",
            priority: 1
        },
        {
            title: "Get Sysmtem Info with REST API",
            link: "https://www.jfrog.com/confluence/display/JCR/JFrog+Container+Registry+REST+API#JFrogContainerRegistryRESTAPI-SystemInfo",
            priority: 200
        }
    ],
    "/admin/advanced/system_logs": [
        {
            title: "Jfrog Container Registry Logs",
            link: "https://www.jfrog.com/confluence/display/JCR/JFrog+Container+Registry+Log+Files",
            priority: 1
        }
    ],
    "/admin/advanced/maintenance": [
        {
            title: "Maintenance",
            link: "https://www.jfrog.com/confluence/display/JCR/Regular+Maintenance+Operations",
            priority: 1
        }
    ],
    "/admin/advanced/storage_summary": [
        {
            title: "Monitoring Storage",
            link: "https://www.jfrog.com/confluence/display/JCR/Monitoring+Storage",
            priority: 1
        }
    ],
    "/admin/advanced/config_descriptor": [
        {
            title: "Configuration Files",
            link: "https://www.jfrog.com/confluence/display/JCR/Configuration+Files",
            priority: 1
        },
        {
            title: "Get Config Descriptor with REST API",
            link: "https://www.jfrog.com/confluence/display/JCR/JFrog+Container+Registry+REST+API#JFrogContainerRegistryRESTAPI-GeneralConfiguration",
            priority: 200
        }
    ],
    "/admin/advanced/security_descriptor": [
        {
            title: "Security Configuration",
            link: "https://www.jfrog.com/confluence/display/JCR/Configuring+Security",
            priority: 1
        }
    ]
}