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
/**
 * Created by tomere on 2/22/2017.
 */
export default {
    admin:{
        repositories:{
            local:{
                noReplicationsMessage:`This repository has not been configured for replication. To learn about replicating repositories, refer to the <a class="jf-link" href="https://www.jfrog.com/confluence/display/RTF/Repository+Replication" target="_blank">JFrog Artifactory User Guide <i class="icon icon-external-link"></i></a>.`,
                noReposMessage:`Artifactory has not been configured with local repositories. To learn about local repositories, refer to the <a class="jf-link" href="https://www.jfrog.com/confluence/display/RTF/Local+Repositories" target="_blank">JFrog Artifactory User Guide <i class="icon icon-external-link"></i></a>.`,
                noReposMessageJCR:`JFrog Container Registry has not been configured with local repositories. To learn about local repositories, refer to the <a class="jf-link" href="https://www.jfrog.com/confluence/display/JCR/Local+Repositories" target="_blank">JFrog Container Registry User Guide <i class="icon icon-external-link"></i></a>.`,
            },
            remote:{
                noReposMessage:`Artifactory has not been configured with remote repositories. To learn about remote repositories, refer to the <a class="jf-link" href="https://www.jfrog.com/confluence/display/RTF/Remote+Repositories" target="_blank">JFrog Artifactory User Guide <i class="icon icon-external-link"></i></a>.`,
                noXrayIntegrationMessage:`This Artifactory instance is not connected to <a class="jf-link" href="https://www.jfrog.com/confluence/display/XRAY/Welcome+to+JFrog+Xray" target="_blank">Xray <i class="icon icon-external-link"></i></a>.`,
                noXrayIntegrationMessageJCR:`This JFrog Container Registry instance is not connected to <a class="jf-link" href="https://www.jfrog.com/confluence/display/XRAY/Welcome+to+JFrog+Xray" target="_blank">Xray <i class="icon icon-external-link"></i></a>.`
            },
            virtual:{
                noReposMessage:`Artifactory has not been configured with virtual repositories. To learn about virtual repositories, refer to the <a class="jf-link" href="https://www.jfrog.com/confluence/display/RTF/Virtual+Repositories" target="_blank">JFrog Artifactory User Guide <i class="icon icon-external-link"></i></a>.`,
                noReposMessageJCR:`JFrog Container Registry has not been configured with virtual repositories. To learn about virtual repositories, refer to the <a class="jf-link" href="https://www.jfrog.com/confluence/display/JCR/Virtual+Repositories" target="_blank">JFrog Container Registry User Guide <i class="icon icon-external-link"></i></a>.`,
            },
            distribution:{
                noReposMessage:`Bintray Distribution repositories can be used to distribute your products or packages with <a class="jf-link" href="https://bintray.com" target="_blank">JFrog Bintray <i class="icon icon-external-link"></i></a> - the universal distribution platform.`,
                noReposMessageJCR:`Bintray Distribution repositories can be used to distribute your products or packages with <a class="jf-link" href="https://bintray.com" target="_blank">JFrog Bintray <i class="icon icon-external-link"></i></a> - the universal distribution platform.`,
                ePlusNoReposMessage:`Release bundle repositories store release bundles that have either been received or created for distribution using <a class="jf-link" href="https://www.jfrog.com/confluence/display/DIST/Welcome+to+JFrog+Distribution" target="_blank">JFrog Distribution <i class="icon icon-external-link"></i></a>.`
            }
        },
        configuration:{
            propertySets:{
                noSetsMessage:`Artifactory has not been configured with property sets. To learn about configuring Artifactory to work with property sets <a class="jf-link" href="https://www.jfrog.com/confluence/display/RTF/Properties#Properties-PropertySets" target="_blank">JFrog Artifactory User Guide <i class="icon icon-external-link"></i></a>.`,
                noSetsMessageJCR:`JFrog Container Registry has not been configured with property sets. To learn about configuring JFrog Container Registry to work with property sets <a class="jf-link" href="https://www.jfrog.com/confluence/display/JCR/Properties" target="_blank">JFrog Container Registry User Guide <i class="icon icon-external-link"></i></a>.`,
            },
            proxies:{
                noSetsMessage:`Artifactory has not been configured with proxy servers. To learn about configuring Artifactory to work with proxy servers, refer to the <a class="jf-link" href="https://www.jfrog.com/confluence/display/RTF/Managing+Proxies" target="_blank">JFrog Artifactory User Guide <i class="icon icon-external-link"></i></a>.`,
            },
            xray:{
              notConnnectedToXrayMessage:`This Artifactory instance is not connected to Xray. To connect this instance to Xray and index artifacts for analysis, refer to the <a class="jf-link" href="https://www.jfrog.com/confluence/display/XRAY/Welcome+to+JFrog+Xray" target="_blank">Xray User Guide <i class="icon icon-external-link"></i></a>`
            },
            ha:{
                haNotConfiguredMessage:`High Availability license is installed but HA feature is not configured.<br>
                                      Visit <a class="jf-link" target="_blank" href="https://www.jfrog.com/confluence/display/RTF/Installation+and+Setup#InstallationandSetup-ConfiguringArtifactoryHA">Artifactory High Availability Installation and Setup <i class="icon icon-external-link"></i></a> page in <a class="jf-link" target="_blank" href="https://www.jfrog.com/confluence/display/RTF/Welcome+to+Artifactory">JFrog's wiki <i class="icon icon-external-link"></i></a> for detailed instructions.`
            }
        },
        security:{
            general:{
                passwordDecrypted:`All passwords in your configuration are currently visible in plain text. To encrypt the passwords through REST API, refer to the <a class="jf-link" href="https://www.jfrog.com/confluence/display/RTF/Artifactory+REST+API#ArtifactoryRESTAPI-DeactivateArtifactoryKeyEncryption" target="_blank">JFrog Artifactory User Guide <i class="icon icon-external-link"></i></a>.`,
                passwordEncrypted:`All passwords in your configuration are currently encrypted. To decrypt the passwords through REST API, refer to the <a class="jf-link" href="https://www.jfrog.com/confluence/display/RTF/Artifactory+REST+API#ArtifactoryRESTAPI-ActivateArtifactoryKeyEncryption" target="_blank">JFrog Artifactory User Guide <i class="icon icon-external-link"></i></a>.`

            },
            users:{
                userForm:{
                    userIsAdmin:'This user has Admin privileges and is, therefore, not restricted by any of the permission targets specified in the table below.',
                },
            },
            groups:{
                noGroupsMessage: `No groups found. To learn about managing groups in Artifactory, refer to the Artifactory User Guide.  <a class="jf-link" href="https://www.jfrog.com/confluence/display/RTF/Managing+Users#ManagingUsers-CreatingandEditingGroups" target="_blank">JFrog Artifactory User Guide <i class="icon icon-external-link"></i></a>.`,
                groupForm:{
                    groupIsAdmin:'This group has Admin privileges and is, therefore, not restricted by any of the permission targets specified in the table below.',
                },
            },
            accessTokens:{
                noTokensMessage:`Artifactory has no access tokens to display. To learn how to generate access tokens, refer to the <a class="jf-link" href="https://www.jfrog.com/confluence/display/RTF/Access+Tokens" target="_blank">JFrog Artifactory User Guide <i class="icon icon-external-link"></i></a>.`,
            },
            ldap:{
                noLdapConfigurationMessage:`Artifactory has not been configured with an LDAP server. To learn about configuring Artifactory to work with LDAP, refer to the <a class="jf-link" href="https://www.jfrog.com/confluence/display/RTF/Managing+Security+with+LDAP" target="_blank">JFrog Artifactory User Guide <i class="icon icon-external-link"></i></a>.`
            }
        },
        services:{
            backups:{
                noBackupsMessage:`Artifactory has not been configured with backups jobs. To learn about backing up Artifactory, refer to the <a class="jf-link" href="https://www.jfrog.com/confluence/display/RTF/Managing+Backups" target="_blank">JFrog Artifactory User Guide <i class="icon icon-external-link"></i></a>.`
            }
        },
        advanced:{
            logAnalytics:{
                sumoLogicIntegrationMessage: `The JFrog Artifactory / Sumo Logic integration gives you a centralized overview of your artifact repositories with the ability to drill down and quickly identify recent changes, check application dependencies and identify potential issues. Through dashboards, queries and searches that are pre-enabled out-of-the-box, Sumo Logic allows you to analyze all data that Artifactory generates. For a complete overview, <a class="jf-link" href="https://www.jfrog.com/confluence/display/RTF/Log+Analytics" target="_blank">click here <i class="icon icon-external-link"></i></a>.`,
                sumoLogicIntegrationMessageJCR: `The JFrog Container Registry / Sumo Logic integration gives you a centralized overview of your artifact repositories with the ability to drill down and quickly identify recent changes, check application dependencies and identify potential issues. Through dashboards, queries and searches that are pre-enabled out-of-the-box, Sumo Logic allows you to analyze all data that JFrog Container Registry generates. For a complete overview, <a class="jf-link" href="https://www.jfrog.com/confluence/display/JCR/Log+Analytics" target="_blank">click here <i class="icon icon-external-link"></i></a>.`,
            }
        }
    },
    builds: {
        noBuildsDataMessage: `No builds have been published to Artifactory. To learn about build integration, refer to the`,
        callToAction: 'JFrog Artifactory User Guide',
        actionUrl: 'https://www.jfrog.com/confluence/display/RTF/Build+Integration'
    }
}