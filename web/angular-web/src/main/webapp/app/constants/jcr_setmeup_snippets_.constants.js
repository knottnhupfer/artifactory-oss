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
let snippets = {
    docker: {
        general: [{
            title_reverse_proxy: "Using Docker with JFrog Container Registry requires a reverse proxy such as Nginx or Apache. For more details please visit our <a href=\"https://www.jfrog.com/confluence/display/JCR/Docker+Registry\" target=\"_blank\">Docker Repositories</a> documentation.",
            title_insecure: "<br/>Not using an SSL certificate requires Docker clients to add an --insecure-registry flag to the <b>DOCKER_OPTS</b>",
            snippet_insecure: 'export DOCKER_OPTS+=" --insecure-registry <INSECURE_SNIP>"',
            after_example_server: "<br/>In this example we use <b>artprod.mycompany</b> to represent the Docker repository in JFrog Container Registry.",
        },{
            before: "To login use the <i>docker login</i> command.",
            snippet: "docker login <DOCKER_SERVER>",
            after: "And provide your JFrog Container Registry username and password or API key.<br/>If anonymous access is enabled you do not need to login."
        },{
            before: "To manually set your credentials, or if you are using Docker v1, copy the following snippet to your ~/.docker/config.json file.",
            snippet: "{\n\t\"auths\": {\n\t\t\"!https://<DOCKER_SERVER>\" : {\n\t\t\t\"auth\": \"<USERNAME>:<PASSWORD> (converted to base 64)\",\n\t\t\t\"email\": \"youremail@email.com\"\n\t\t}\n\t}\n}",
            after: "To enter multiple registries see the <a href=\"https://www.jfrog.com/confluence/display/JCR/Using+Docker+V1#UsingDockerV1-3.SettingUpAuthentication\" target=\"_blank\">following example</a>."
        }],
        read: [{
            before: "To pull an image use the <i>docker pull</i> command specifying the docker image and tag.",
            snippet: "docker pull <DOCKER_SERVER>/<DOCKER_REPOSITORY>:<DOCKER_TAG>"
        }],
        deploy: [{
            before: "To push an image tag an image using the <i>docker tag</i> and then <i>docker push</i> command.",
            snippet: "docker tag <IMAGE_ID>"+" "+"<DOCKER_SERVER>/<DOCKER_REPOSITORY>:<DOCKER_TAG>"
        }, {
            before: "",
            snippet: "docker push <DOCKER_SERVER>/<DOCKER_REPOSITORY>:<DOCKER_TAG>"
        }]
    },
    generic: {
        read: {
            before: "You can download a file directly using the following command:",
            snippet: "curl <CURL_AUTH> -O \"$2/$1/<TARGET_FILE_PATH>\""
        },
        deploy: {
            before: "You can upload any file using the following command:",
            snippet: "curl <CURL_AUTH> -T <PATH_TO_FILE> \"$2/$1/<TARGET_FILE_PATH>\""
        }
    },
    helm: {
        info_msg: '<div class="alert alert-info">JFrog Container Registry only supports resolving Helm Charts from a virtual repository.<br/>To resolve Helm Charts from this repository, it must be included in the virtual repository your Helm client points to.</div>',
        general: [{
            before: 'To work with Helm repositories, first install and configure your Helm client. <br/>You need to use Helm version 2.9.0 or above that supports authentication against JFrog Container Registry.<br/>Set your default JFrog Container Registry Helm repository/registry with the following command:',
            snippet: "helm repo add $1 $2/$1 --username <USERNAME> --password <PASSWORD>"
        }],
        read: {
            before: "To install a Helm Chart from this repository using your Helm command line client, use the following command:",
            snippet: "helm repo update\n" + "helm install $1/[chartName]"
        },
        deploy: {
            before: "To deploy a Helm Chart into an JFrog Container Registry repository you need to use JFrog Container Registry's REST API.<br/>For example, to deploy a Chart into this repository, use the following command:",
            snippet: "curl <CURL_AUTH> -T <PATH_TO_FILE> \"$2/$1/<TARGET_FILE_PATH>\""
        }
    },
    buildinfo: {
        general: [{
            before: "The <i>artifactory-build-info</i> repository contains all builds deployed to JFrog Container Registry, either directly through the UI or via REST API, CLI or the different CI server plugins.<br><br>" +
                    "When deploying a build info JSON file, JFrog Container for the Registry will automatically place it in the repository using the following structure:<br>" +
                    "<i>artifactory-build-info > [build-name] > [build-number]-[upload timestamp].json</i>"
        }, {
            title: '<label class="snippet">Deploy</label>',
            before: "There are different ways to upload a build:" +
            "<ol>" +
            "<li>Through the different <a href=\"https://www.jfrog.com/confluence/display/JCR/Build+Integration\" target=\"_blank\">CI server integrations</a></li>" +
            "<li>Through JFrog CLI <a href=\"https://www.jfrog.com/confluence/display/CLI/CLI+for+JFrog+Artifactory#CLIforJFrogArtifactory-BuildIntegration\" target=\"_blank\">Build Integration</a></li>" +
            "<li>Using the <a href=\"https://www.jfrog.com/confluence/display/JCR/JFrog+Container+Registry+REST+API#JFrogContainerRegistryRESTAPI-BuildUpload\" target=\"_blank\">Upload Build</a> REST API endpoint</li>" +
            "<li>Using the Upload button in the UI</li>" +
            "</ol>",
        }]
    }



};

export default snippets;