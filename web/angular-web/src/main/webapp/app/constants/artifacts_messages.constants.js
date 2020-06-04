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
export default{
    actions:{
        distribute:{
            noRepos:{
                admin: {
                    message: `No distribution repositories are configured. To distribute artifacts and builds, <a class="jf-link" href="#/admin/repositories/distribution">create a Distribution repository</a>.
                              To learn about distribution repositories, refer to the Artifactory <a class="jf-link" href="https://www.jfrog.com/confluence/display/RTF/Distribution+Repository">User Guide <i class="icon icon-external-link"></i></a>.`,
                    messageType: 'alert-info'
                },
                nonAdmin: {
                    message: `No distribution repositories are configured.
                              To learn about distribution repositories, refer to the Artifactory <a class="jf-link" href="https://www.jfrog.com/confluence/display/RTF/Distribution+Repository">User Guide <i class="icon icon-external-link"></i></a>.`,
                    messageType: 'alert-info'
                }
            },
            inOfflineMode: {
                message: `Global offline mode is enabled. To allow distribution, disable the global offline mode through the General Configuration page.`,
                messageType: 'alert-danger'
            },
            noPermissions: {
                message: `You do not have distribute and deploy permissions.`,
                messageType: 'alert-danger'
            }
        },
        deploy:{
            deployToDistRepoErrorMessage:{
                message:`File(s) cannot be directly deployed to a distribution repository. Instead, use the "Distribute" action on the relevant repository or select an alternative target repository.`,
                messageType:`alert-danger`
            },
            deployPermissionsErrorMessage:{
                message:`You do not have deploy permission`,
                messageType:`alert-danger`
            },
            hasNoDefaultDeployRepo:{
                message:`This virtual repository is not configured with a default deployment repository. To learn about configuring virtual repositories, refer to the <a class="jf-link" href="https://www.jfrog.com/confluence/display/RTF/Deploying+Artifacts#DeployingArtifacts-DeployingtoaVirtualRepository" target="_blank">Artifactory User Guide <i class="icon icon-external-link"></i></a>.`,
                messageType:`alert-warning`
            },
            cannotDeployToRemote:{
                message:`Cannot deploy to a remote repository. To learn about remote repositories, refer to the <a class="jf-link" href="https://www.jfrog.com/confluence/display/RTF/Remote+Repositories" target="_blank">Artifactory User Guide <i class="icon icon-external-link"></i></a>.`,
                messageJCR:`Cannot deploy to a remote repository. To learn about remote repositories, refer to the <a class="jf-link" href="https://www.jfrog.com/confluence/display/JCR/Remote+Repositories" target="_blank">JFrog Container Registry User Guide <i class="icon icon-external-link"></i></a>.`,
                messageType:`alert-danger`
            },
            cannotDeployToTrashCan:{
                message:`Cannot deploy to Trash Can. To learn about the Trash Can, refer to the <a class="jf-link" href="https://www.jfrog.com/confluence/display/RTF/Browsing+Artifactory#BrowsingArtifactory-TrashCan" target="_blank">Artifactory User Guide <i class="icon icon-external-link"></i></a>.`,
                messageType:`alert-danger`
            },
        }
    },
    set_me_up:{
        puppet:{
            puppetClientVersion:`If you are using Puppet version 4.9.1 and below, you need to modify your reverse proxy configuration. For details, refer to <a class="jf-link" href="https://www.jfrog.com/confluence/display/RTF/Puppet+Repositories#PuppetRepositories-UsingPuppet4.9.1andBelow" target="_blank">JFrog Artifactory User Guide <i class="icon icon-external-link"></i></a>.`
        },
        hasNoDeployPermissions:{
            message:`You do not have deploy permissions to this repository`
        },
        hasNoRepositoriesOfType:{
            message:`No repositories match the selected tool`
        }
    },
    xray_tab: {
        blocked_artifact: "Xray has a policy blocking this artifact for download.",
        blocked_artifact_ignored: "Artifactory’s Xray configuration has overridden Xray’s policy blocking the download of this artifact."
    }
};