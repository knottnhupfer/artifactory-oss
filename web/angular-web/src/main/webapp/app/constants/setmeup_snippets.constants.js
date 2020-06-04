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
    debian: {
        read: [{
            before: "To use Artifactory repository to install Debian package you need to add it to your <i>sources.list</i> file. You can do that using the following command:",
            snippet: "sudo sh -c \"echo 'deb $2/$1 <DISTRIBUTION> <COMPONENT>' >> /etc/apt/sources.list\""
        }, {
            before: "For accessing Artifactory using credentials you can specify it in the <i>source.list</i> file like so:",
            snippet: "http://<USERNAME>:<PASSWORD>@$4/artifactory/$1 <DISTRIBUTION> <COMPONENTS>"
        }, {
            before: "Your apt-get client will use the specified Artifactory repositories to install the package",
            snippet: "apt-get install <PACKAGE>"
        }],
        deploy: [{
            before: "To deploy a Debian package into Artifactory you can either use the deploy option in the Artifact’s module or upload with cURL using matrix parameters. The required parameters are package name, distribution, component, and architecture in the following way:",
            snippet: "curl <CURL_AUTH> -XPUT \"$2/$1/pool/<DEBIAN_PACKAGE_NAME>;deb.distribution=<DISTRIBUTION>;deb.component=<COMPONENT>;deb.architecture=<ARCHITECTURE>\" -T <PATH_TO_FILE>"
        }, {
            before: "You can specify multiple layouts by adding semicolon separated multiple parameters, like so:",
            snippet: "curl <CURL_AUTH> -XPUT \"$2/$1/pool/<DEBIAN_PACKAGE_NAME>;deb.distribution=<DISTRIBUTION>;deb.distribution=<DISTRIBUTION>;deb.component=<COMPONENT>;deb.component=<COMPONENT>;deb.architecture=<ARCHITECTURE>;deb.architecture=<ARCHITECTURE>\" -T <PATH_TO_FILE>",
            after: "To add an architecture independent layout use deb.architecture=all. This will cause your package to appear in the Packages index of all the architectures under the same Distribution and Component, as well as under a new index branch called binary-all which holds all Debian packages that are marked as \"all\"."
        }]
    },
    opkg: {
        read: [{
            before: "To use the Artifactory repository to install Ipk packages you need to add an indexed path (a feed) to your <i>opkg.conf</i> file. You can do that using the following command:",
            snippet: "echo 'src <FEED_NAME> http://$4/artifactory/$1/<PATH_TO_FEED>' >> /etc/opkg/opkg.conf",
            after: "If you want your client to download the .gz variant of the Packages index file instead, change the src part to src/gz"
        }, {
            before: "For accessing Artifactory using credentials you can specify it in the <i>opkg.conf</i> file like so:",
            snippet: "echo 'option http_auth <USERNAME>:<PASSWORD>' >> /etc/opkg/opkg.conf"
        }, {
            before: "Your Opkg client will use the specified Artifactory repositories to install the package",
            snippet: "opkg install <PACKAGE>"
        }],
        deploy: [{
            before: "To deploy an ipk package into Artifactory, run the following:",
            snippet: "curl <CURL_AUTH> -XPUT \"http://$4/artifactory/$1/<PATH_TO_FEED>/<IPK_PACKAGE_NAME>\" -T <PATH_TO_FILE>"
        }]
    },
    pypi: {
        read: [{
            before: "To resolve packages using pip, add the following to ~/.pip/pip.conf:",
            snippet: "[global]\nindex-url = http://<USERNAME>:<PASSWORD>@$4/artifactory/api/pypi/$1/simple"
        }, {
            before: "If credentials are required they should be embedded in the URL. To resolve packages using pip, run:",
            snippet: "pip install <PACKAGE>"
        }],
        deploy: [{
            before: "To deploy packages using setuptools you need to add an Artifactory repository to the <i>.pypirc</i> file (usually located in your home directory):",
            snippet: "[distutils]\n" + "index-servers = local\n" + "[local]\n" + "repository: $2/api/pypi/$1\n" + "username: <USERNAME>\n" + "password: <PASSWORD>"
        }, {
            before: "To deploy a python egg to Artifactory, after changing the <i>.pypirc</i> file, run the following command:",
            snippet: "python setup.py sdist upload -r local"
        }, {
            before: "To deploy a python wheel to Artifactory, after changing the <i>.pypirc</i> file, run the following command:",
            snippet: "python setup.py bdist_wheel upload -r local",
            after: "where <i>local</i> is the index server you defined in <i>.pypirc</i>."
        }]
    },
    puppet: {
        general: [{
            before: "In order for your Puppet client to work with Artifactory you will need to add  following in your puppet.conf file:",
            snippet: "[main]\nmodule_repository=http://<USERNAME>:<PASSWORD>@$4/artifactory/api/puppet/$1"
        }],
        read: {
            before: "To install a module by specifying Artifactory repository use the following puppet command:",
            snippet: "puppet module install  --module_repository=http://<USERNAME>:<PASSWORD>@$4/artifactory/api/puppet/$1 <MODULE_NAME>"
        },
        deploy: {
            before: "To deploy a Puppet module into an Artifactory repository you need to use Artifactory's REST API or the Web UI.<br/>For example, to deploy a Puppet module into this repository using the REST API, use the following command:",
            snippet: "curl <CURL_AUTH> -XPUT $2/$1/<TARGET_FILE_PATH> -T <PATH_TO_FILE>"
        }
    },
    bower: {
        general: [{
            before: "In order to use Bower with Artifactory you will need to add 'bower-art-resolver' as one of the resolvers in your .bowerrc file. To install <a href=\"https://www.npmjs.com/package/bower-art-resolver\" target=\"_blank\">bower-art-resolver</a> (custom Bower resolver dedicated to integrate with Artifactory), run the following command:",
            snippet: "npm install -g bower-art-resolver"
        }, {
            before: "And add the bower-art-resolver as one of the resolvers in your <i>.bowerrc</i> file:",
            snippet: "{\n\t\"resolvers\" : [\n\t\t\"bower-art-resolver\"\n\t]\n}"
        },{
            before: "Now replace the default Bower registry with the following in your <i>.bowerrc</i> file:",
            snippet: "{\n\t\"registry\" : \"$2/api/bower/$1\",\n\t\"resolvers\" : [\n\t\t\"bower-art-resolver\"\n\t]\n}"
        }, {
            before: "If authentication is required use:",
            snippet: "{\n\t\"registry\" : \"http://<USERNAME>:<PASSWORD>@$4/artifactory/api/bower/$1\",\n\t\"resolvers\" : [\n\t\t\"bower-art-resolver\"\n\t]\n}"
        }, {
            before: "The instructions above apply to <b>Bower version 1.5</b> or higher. For older versions see instructions <a href=\"http://www.jfrog.com/confluence/display/RTF/Bower+Repositories#BowerRepositories-UsingOlderVersionsofBower\" target=\"_blank\">here</a>."
        }],
        read: {
            before: "To install bower packages execute the following command:",
            snippet: "bower install <PACKAGE>"
        },
        deploy: {
            before: "To deploy a Bower package into an Artifactory repository you need to use Artifactory's REST API or the Web UI.<br/>For example, to deploy a Bower package into this repository using the REST API, use the following command:",
            snippet: "curl <CURL_AUTH> -XPUT $2/$1/<TARGET_FILE_PATH> -T <PATH_TO_FILE>"
        }
    },
    cocoapods: {
        general: [{
            before: "In order to use CocoaPods with Artifactory you will need to install the <a href=\"https://github.com/JFrogDev/cocoapods-art\" target=\"_blank\">'cocoapods-art'</a>. plugin. To install cocoapods-art run the following command:",
            snippet: "gem install cocoapods-art",
        },{
            before: "repo-art uses authentication as specified in your standard <a href=\"https://www.gnu.org/software/inetutils/manual/html_node/The-_002enetrc-file.html\" target=\"_blank\">netrc file</a>.",
            snippet: "machine $4\nlogin <USERNAME>\npassword <PASSWORD>"
        },{
            before: "To add an Artifactory Specs repo:",
            snippet: "pod repo-art add $1 \"$2/api/pods/$1\""
        }],
        read: [{
            before: "To resolve pods from an Artifactory specs repo that you added, you must add the following to your Podfile:",
            snippet: "plugin 'cocoapods-art', :sources => [\n  '$1'\n]"
        },{
            before: "Then you can use install as usual:",
            snippet: "pod install"
        }],
        deploy: [{
            before: "To deploy a pod into an Artifactory repository you need to use Artifactory's REST API or the Web UI.<br/>For example, to deploy a pod into this repository using the REST API, use the following command:",
            snippet: "curl <CURL_AUTH> -XPUT $2/$1/<TARGET_FILE_PATH> -T <PATH_TO_FILE>"
        }/*,  {
            before: "Artifactory can also function as a standalone Specs repo, which does not need to be backed by a Git repository.<br/>To push an index entry to the Specs repo on this repository use the following command:",
            snippet: "pod repo-art push $1 <NAME.podspec>",
            after: "Running the command without specifying a podspec will push all podspecs in the current working directory."
        }*/]
    },
    conan: {
        general: [{
            before: "For your Conan command line client to work with this Conan repository, you first need to add the repository to your client configuration using the following command:",
            snippet: "conan remote add <REMOTE> $2/api/conan/$1",
            after: "And replace &lt;REMOTE&gt; with a name that identifies the repository (for example: \"my-conan-repo\")"
        },{
            before: "To login use the <i>conan user</i> command:",
            snippet: "conan user -p <PASSWORD> -r <REMOTE> <USERNAME>",
            after: "And provide your Artifactory username and password or API key.<br/>If anonymous access is enabled you do not need to login."
        },{
            before: "For complete Conan cli reference see documentation at <a href=\"http://docs.conan.io\">docs.conan.io</a>."
        }],
        read: [{
            before: "To install the dependencies defined in your project's <i>conanfile.txt</i> from an Artifactory Conan repository, use the following command:",
            snippet: "conan install . -r <REMOTE>"
        }],
        deploy: [{
            before: "To deploy a Conan recipe with its binary packages to this repository use the following command:",
            snippet: "conan upload <RECIPE> -r <REMOTE> --all",
            after: "&lt;RECIPE&gt; is the Conan recipe reference you want to upload in the format: &lt;NAME&gt;/&lt;VERSION&gt;@&lt;USER&gt;/&lt;CHANNEL&gt;<br/>" +
                    "For example: lib/1.0@conan/stable<br/><br/>" +
                    "<b>Note:</b> You need to deploy Conan recipes only through the Conan client. Artifactory will index the recipe only if it was deployed through the Conan client. Deploying through the Artifactory UI or through the Deploy Artifact REST API will not index the recipe."
        }]
    },
    cran: {
        general: [{
            before: "<br/>In order to use CRAN with Artifactory, add the repository to your Rprofile.site file by adding the following lines:",
            snippet: "local({\n\t" +
                    "r <- list(\"$1\" = \"http://<USERNAME>:<PASSWORD>@$4/artifactory/$1/\")\n\t" +
                    "options(repos = r)\n" +
                    "})"
        }],
        read: {
            before: "To install a CRAN package from this repository use the R command line, and run the following command:",
            snippet: "install.packages(\"<PACKAGE_NAME>\")"
        },
        deploy: [{
            before: "<br/>To deploy a CRAN package to an Artifactory repository use the Artifactory REST API.</br>To deploy a source package, run the following command:",
            snippet: "curl <CURL_AUTH> -T <PATH_TO_FILE> -XPOST \"$2/api/cran/$1/sources\""
        },{
            before: "To deploy a binary package, run the following command:",
            snippet: '"curl <CURL_AUTH> -T <PATH_TO_FILE> -XPOST \"$2/api/cran/$1/binaries?distribution=<DISTRIBUTION>&rVersion=<R_VERSION>"'
        }]
    },
    docker: {
        general: [{
            title_reverse_proxy: "Using Docker with Artifactory requires a reverse proxy such as Nginx or Apache. For more details please visit our <a href=\"http://www.jfrog.com/confluence/display/RTF/Docker+Repositories#DockerRepositories-RequirementforaReverseProxy(Nginx/Apache)\" target=\"_blank\">Docker Repositories</a> documentation.",
            title_insecure: "<br/>Not using an SSL certificate requires Docker clients to add an --insecure-registry flag to the <b>DOCKER_OPTS</b>",
            snippet_insecure: 'export DOCKER_OPTS+=" --insecure-registry <INSECURE_SNIP>"',
            after_example_server: "<br/>In this example we use <b>artprod.mycompany</b> to represent the Docker repository in Artifactory.",
        },{
            before: "To login use the <i>docker login</i> command.",
            snippet: "docker login <DOCKER_SERVER>",
            after: "And provide your Artifactory username and password or API key.<br/>If anonymous access is enabled you do not need to login."
        },{
            before: "To manually set your credentials, or if you are using Docker v1, copy the following snippet to your ~/.docker/config.json file.",
            snippet: "{\n\t\"auths\": {\n\t\t\"!https://<DOCKER_SERVER>\" : {\n\t\t\t\"auth\": \"<USERNAME>:<PASSWORD> (converted to base 64)\",\n\t\t\t\"email\": \"youremail@email.com\"\n\t\t}\n\t}\n}",
            after: "To enter multiple registries see the <a href=\"https://www.jfrog.com/confluence/display/RTF/Using+Docker+V1#UsingDockerV1-3.SettingUpAuthentication\" target=\"_blank\">following example</a>."
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
    gitlfs: {
        general: [{
            before: '<div class="alert alert-warning"><b>Note:</b> it is recommended not to upload sensitive information, such as credentials, to Git.</div>'
        }],
        read: [{
            before: "In order for your client to upload and download LFS blobs from artifactory, the [lfs] clause should be added to the <i>.lfsconfig</i> file of your Git repository in the following format:",
            snippet: "[lfs]\n" + "url = \"http://<USERNAME>:<PASSWORD>@$4/artifactory/api/lfs/$1\""
        }, {
            before: "You can also set LFS endpoints for different remotes on your repo (as supported by the Git LFS client). For example:",
            snippet: "[remote \"origin\"]\n" + "url = <URL>\n" + "fetch = +refs/heads/*:refs/remotes/origin/*\n" + "lfsurl = \"http://<USERNAME>:<PASSWORD>@$4/artifactory/api/lfs/$1\""
        }]
    },
    helm: {
        info_msg: '<div class="alert alert-info">Artifactory only supports resolving Helm Charts from a virtual repository.<br/>To resolve Helm Charts from this repository, it must be included in the virtual repository your Helm client points to.</div>',
        general: [{
            before: 'To work with Helm repositories, first install and configure your Helm client. <br/>You need to use Helm version 2.9.0 or above that supports authentication against Artifactory.<br/>Set your default Artifactory Helm repository/registry with the following command:',
            snippet: "helm repo add $1 $2/$1 --username <USERNAME> --password <PASSWORD>"
        }],
        read: {
            before: "To install a Helm Chart from this repository using your Helm command line client, use the following command:",
            snippet: "helm repo update\n" + "helm install $1/[chartName]"
        },
        deploy: {
            before: "To deploy a Helm Chart into an Artifactory repository you need to use Artifactory's REST API.<br/>For example, to deploy a Chart into this repository, use the following command:",
            snippet: "curl <CURL_AUTH> -T <PATH_TO_FILE> \"$2/$1/<TARGET_FILE_PATH>\""
        }
    },
    go: {
        general: [{
            before: `To work with Go repositories, first install and configure your Go client. 
                    To deploy Go packages into an Artifactory repository, you first need to install and configure 
                     <a href='https://jfrog.com/getcli' target='_blank'>JFrog CLI</a>.`,
        }],
        deploy: [
            {
               before: `To deploy a Go package into an Artifactory repository, you need to run the following <a href='https://jfrog.com/getcli' target='_blank'>JFrog CLI</a>
                        command from your project’s root directory:`,
               snippet: 'jfrog rt go-publish $1 <VERSION> --url=$2 --user=<USERNAME> --password=<PASSWORD>'
            }
        ],
        read: [
            {
                before: `There are two ways to resolve a Go package from Artifactory: using JFrog CLI or using the Go client. <br>
                        <h2>Using JFrog CLI</h2>To resolve a Go package using JFrog CLI, run the following command under your project’s root directory:`,
                snippet: 'jfrog rt go build $1 --url=$2 --user=<USERNAME> --password=<PASSWORD>'
            },
            {
                before: `<h2>Using Go</h2>To resolve a Go package from this repository using Go, first set your default Artifactory Go repository/registry by running the following command:`,
                snippet: `export GOPROXY="http://<USERNAME>:<PASSWORD>@$4/artifactory/api/go/$1"`
            },
            {
                before: `Then, from your project’s root directory, run:`,
                snippet: `go build`
            }
        ]
    },
    nuget: {
        general: [{
            before: "When using Artifactory as a NuGet repository you can either work with the NuGet CLI directly or with Visual Studio."
        },{
            before: "<b>NuGet CLI Configuration</b><br/>" +
            "Note: If this repository is configured as a NuGet API v3 repository (you may need to contact your Artifactory administrator)," +
            "you should skip to the NuGet CLI Configuration (API v3) section.<br/><br/>" +
            "To configure the NuGet CLI to work with Artifactory, you need to add this repository to the list of sources.<br/>" +
            "To add this repository, use the following command:",
            snippet: "nuget sources Add -Name Artifactory -Source $2/api/nuget/$1 -username <USERNAME> -password <PASSWORD>"
        },{
          before: "Then, to authenticate against Artifactory with the NuGet API key, run the following command:",
          snippet: "nuget setapikey <USERNAME>:<PASSWORD> -Source Artifactory"
        },{
            before:`<b>NuGet CLI Configuration (API v3)</b><br/>`+
            `If this repository is configured as a NuGet API v3 repository (you may need to contact your Artifactory administrator), `+
            `manually add the following line to the <b>NuGet.config</b> file:<br/>`+
            `The <b>NuGet.config</b> file can be found at <b>%appdata%\\NuGet\\NuGet.Config</b> (Windows) or <b>~/.config/NuGet/NuGet.Config</b> (Mac/Linux)`,
            snippet:`<add key="ArtifactoryNuGetV3" value="$2/api/nuget/v3/$1" protocolVersion="3" />`
        },{
            before: "Then, to authenticate against Artifactory with the NuGet API key, run the following command:",
            snippet: "nuget setapikey <USERNAME>:<PASSWORD> -Source ArtifactoryNuGetV3"
        },{
            before: "<b>Visual Studio Configuration</b><br/>" +
            "To configure the NuGet Visual Studio Extension to use Artifactory, you need to add this repository as another Package Source under NuGet Package Manager." +
            "<ol>" +
            "<li>Go to the \"Package Manager Settings\" in your Visual Studio (Tools > NuGet Package Manager > Package Manager Settings > Package Sources) and add another Package Source.</li>" +
            "<li>Name: Add a name for the package source (e.g. Artifactory NuGet repository)</li>" +
            "<li>Paste the snippet below in the URL field</li>" +
            "</ol>",
            snippet: "$2/api/nuget/$1"
        },{
            before:`<ol start="4"><li>(Optional) If this repository is configured as a NuGet API v3 repository (you may need to contact your Artifactory administrator), manually add the following line to the <b>NuGet.config</b> file:<br/>`+
            `The <b>NuGet.config</b> file can be found at <b>%appdata%\\NuGet\\NuGet.Config</b> (Windows) or <b>~/.config/NuGet/NuGet.Config</b> (Mac/Linux)</li></ol>`,
            snippet:`<add key="ArtifactoryNuGetV3" value="$2/api/nuget/v3/$1" protocolVersion="3" />`
        }],
        deploy: [{
            before: "Deploying to this repository can be done by running the following command:",
            snippet: "nuget push <PACKAGE_NAME> -Source Artifactory"
        },{
            before: "To support more manageable layouts and additional features such as cleanup, NuGet repositories support custom layouts. When pushing a package, you need to ensure that its layout matches the target repository's layout:",
            snippet: "nuget push <PACKAGE> -Source $2/api/nuget/$1/<PATH_TO_FOLDER>"
        }],
        read:[{
            before: "<b>NuGet CLI Resolve</b><br/> To resolve a package using the NuGet CLI, run the following command:",
            snippet: "nuget install <PACKAGE_NAME>"
        },{
            before: "To make sure your client resolves from this repository, verify it is the first in the list of sources in your <i>NuGet.Config</i> file, or run the following command:",
            snippet: "nuget install <PACKAGE_NAME> -Source Artifactory"
        }]
    },
    ivy: {
        general: {
            title: "Click on \"Generate Ivy Settings\" in order to use Virtual or Remote repositories for resolution."
        }
    },
    maven: {
        general: {
            title: "Click on \"Generate Maven Settings\" in order to resolve artifacts through Virtual or Remote repositories."
        },
        deploy: {
            before: "To deploy build artifacts through Artifactory you need to add a deployment element with the URL of a target local repository to which you want to deploy your artifacts. For example:"
        }
    },
    npm: {
        general: [{
            before: "For your npm command line client to work with Artifactory, you first need to set the default npm registry with an Artifactory npm repository using the following command:",
            snippet: "npm config set registry $2/api/npm/$1/"
        }, {
            before: "If you are working with scoped packages, run the following command:",
            snippet: "npm config set @<SCOPE>:registry $2/api/npm/$1/"
        }, {
           before: `There are two ways to authenticate your npm client against Artifactory: using the npm login command or using basic authentication.<br>
                    <br><b>Using npm login</b><br>
                    Run the following command in your npm client. When prompted, provide your Artifactory login credentials:`,
            snippet: 'npm login'
        }, {
           before: `<b>Using basic authentication</b><br>
                    Alternatively, you can paste the following into the <i>~/.npmrc</i> file (in Windows %USERPROFILE%/<i>.npmrc</i>):`,
            snippet: "_auth = <USERNAME>:<PASSWORD> (converted to base 64)\n" + "email = youremail@email.com\n" + "always-auth = true"
        },{
            before: `If you are working with scoped packages, while using basic authentication, you also need to paste the following into the <i>~/.npmrc</i> file (in Windows %USERPROFILE%/<i>.npmrc</i>):`,
            snippet: "@<SCOPE>:registry=$2/api/npm/$1/\n" + "////$6/api/npm/$1/:_password=<BASE64_PASSWORD>\n" + "////$6/api/npm/$1/:username=<USERNAME>\n" + "////$6/api/npm/$1/:email=youremail@email.com\n" + "////$6/api/npm/$1/:always-auth=true"
        },/* {
            before: "Artifactory also support scoped packages. For getting authentication details run the following command:",
            snippet: "curl -u<USERNAME>:<PASSWORD> \"$2/api/npm/$1/auth/<SCOPE>\""
        }*/],
        read: [{
            before: "After adding Artifactory as the default repository you can install a package using the npm install command:",
            snippet: "npm install <PACKAGE_NAME>"
        }, {
            before: "To install a package by specifying Artifactory repository use the following npm command:",
            snippet: "npm install <PACKAGE_NAME> --registry $2/api/npm/$1/"
        }],
        deploy: [{
            before: "To deploy your package to an Artifactory repository you can either add the following to the <i>package.json</i> file:",
            snippet: "\"publishConfig\":{\"registry\":\"$2/api/npm/$1/\"}"
        }, {
            before: "And then you can simply run the default npm publish command:",
            snippet: "npm publish"
        }, {
            before: "Or provide the local repository to the npm publish command:",
            snippet: "npm publish --registry $2/api/npm/$1/"
        }]
    },
    conda: {
        general: [{
            before: "For your Conda command line client to work with Artifactory, you first need to set Artifactory as a Conda repository in your .condarc file. The following is an example of a full .condarc file that uses Artifactory:",
            snippet: "channel_alias: http://<URL_ENCODED_USERNAME>:<PASSWORD>@$4/artifactory/api/conda/$1\n" +
            "channels:\n" +
            "  - http://<URL_ENCODED_USERNAME>:<PASSWORD>@$4/artifactory/api/conda/$1\n" +
            "default_channels:\n" +
            "  - http://<URL_ENCODED_USERNAME>:<PASSWORD>@$4/artifactory/api/conda/$1"
        }, {
            before: "This line makes the Conda client use the specified URL when specifying the \"-c\" flag during package installation:",
            snippet: "channel_alias: http://<URL_ENCODED_USERNAME>:<PASSWORD>@$4/artifactory/api/conda/$1"
        }, {
            before: "This line adds Artifactory to the existing list of Conda channels to be used by the client:",
            snippet:
            "channels:\n" +
            "  - http://<URL_ENCODED_USERNAME>:<PASSWORD>@$4/artifactory/api/conda/$1"
        },{
            before: "This line re-defines the list of default channels to be used by the client, restricting it to just Artifactory:",
            snippet:
            "default_channels:\n" +
            "  - http://<URL_ENCODED_USERNAME>:<PASSWORD>@$4/artifactory/api/conda/$1"
        },{
            before: "If you want to disable the client SSL validations, you may add the following .condarc directive:",
            snippet: "ssl_verify: false"
        },{
            before: `<b>Using basic authentication</b><br>
                    To use Basic Authentication, you may embed your credentials as part of the channel URL, as shown in the above snippets.`,
        }],
        read: [{
            before: "After setting up your .condarc, you may use the following command to resolve packages from Artifactory:",
            snippet: "conda install <PACKAGE_NAME>"
        }, {
            before: "To install a package from a specific conda sub-channel, use the \"-c\" flag with the install command:",
            snippet: "conda install -c <CHANNEL_NAME> <PACKAGE_NAME>"
        }],
        deploy: [{
            before: "To deploy your package to an Artifactory repository you can either use the Artifactory web UI, or upload the package using an HTTP client like cURL:",
            snippet: "curl <CURL_AUTH> -T <PATH_TO_FILE> \"$2/$1/<TARGET_FILE_PATH>\""
        }]
    },
    gems: {
        general: [{
            title: "For your gem client to upload and download Gems from this repository you need to add it to your <i>~/.gemrc</i> file using the following command:",
            snippet: "gem source -a http://<USERNAME>:<PASSWORD>@$4/artifactory/api/gems/$1/"
        }, {
            before: "If anonymous access is enabled you can also use the following:",
            snippet: "gem source -a $2/api/gems/$1/"
        }, {
            before: "To view a list of your effective sources and their order of resolution, run the following command:",
            snippet: "gem source --list",
            after: "Make sure that this repository is at the top of the list."
        }, {
            before: "If you want to setup the credentials for your gem tool either include your API_KEY in the <i>~/.gem/credentials</i> file, or run the following command:",
            snippet: "curl -u<USERNAME>:<PASSWORD> $2/api/gems/$1/api/v1/api_key.yaml > ~/.gem/credentials"
        }, {
            before: "<b>Running on Linux</b><br/>On Linux you may need to change the permissions of the credentials file to 600 by navigating to <i>~/.gem</i> directory and running:",
            snippet: "chmod 600 credentials"
        }, {
            before: "<b>Running on Windows</b><br/>On Windows, the credentials file is located at <i>%USERPROFILE%/.gem/credentials</i>. Note that you also need to set the API key encoding to be \"ASCII\".<br/> To generate the creadentials file run the following command from PowerShell:",
            snippet: "curl.exe -u<USERNAME>:<PASSWORD> $2/api/gems/$1/api/v1/api_key.yaml | Out-File ~/.gem/credentials -Encoding \"ASCII\""
        }, {
            before: "<b>API keys</b><br/>You can modify the credentials file manually and add different API keys. You can then use the following command to choose the relevant API key:",
            snippet: "gem push -k <KEY>"
        }],
        deploy: [{
            before: "In order to push gems to this repository, you can set the global variable $RUBYGEMS_HOST to point to it as follows:",
            snippet: "export RUBYGEMS_HOST=$2/api/gems/$1"
        }, {
            before: "You can also specify the target repository when pushing the gem by using the --host option:",
            snippet: "gem push <PACKAGE> --host $2/api/gems/$1"
        }],
        read: [{
            before: "After completing the configuration under General section above, simply execute the following command:",
            snippet: "gem install <PACKAGE>"
        }, {
            before: "The package will be resolved from the repository configured in your <i>~/.gemrc</i> file. You can also specify a source with the following command:",
            snippet: "gem install <PACKAGE> --source $2/api/gems/$1"
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
    vagrant: {
        read: {
            before: "To provision a Vagrant box, all you need is to construct it's name in the following manner.",
            snippet: "vagrant box add \"$2/api/vagrant/$1/{boxName}\""
        },
        deploy: {
            before: "To deploy Vagrant boxes to this Artifactory repository using an explicit URL with Matrix Parameters use:",
            snippet: "curl <CURL_AUTH> -T <PATH_TO_FILE> \"$2/$1/{vagrantBoxName.box};box_name={name};box_provider={provider};box_version={version}\""
        }
    },
    vcs: {
        general: {
            title: "Artifactory supports downloading tags or branches using a simple GET request. You can also specify to download a specific tag or branch as a tar.gz or zip, and a specific file within a tag or branch as a zip file."
        },
        read: [{
            before: "Use the following command to list all tags:",
            snippet: "curl <CURL_AUTH> -XGET $2/api/vcs/tags/$1/<USER_ORG>/<REPO>"
        }, {
            before: "Use the following command to list all branches:",
            snippet: "curl <CURL_AUTH> -XGET $2/api/vcs/branches/$1/<USER_ORG>/<REPO>"
        }, {
            before: "Use the command below to download a tag. You can specify if the package will be downloaded as a tar.gz or a zip; default is tar.gz.",
            snippet: "curl <CURL_AUTH> -XGET $2/api/vcs/downloadTag/$1/<USER_ORG>/<REPO>/<TAG_NAME>?ext=<tar.gz/zip>"
        }, {
            before: "Use the following command to download a file within a tag as a zip:",
            snippet: "curl <CURL_AUTH> -XGET $2/api/vcs/downloadTagFile/$1/<USER_ORG>/<REPO>/<TAG_NAME>!<PATH_TO_FILE>?ext=zip"
        }, {
            before: "Use the command below to download a branch. You can specify a tar.gz or a zip by adding a parameter in the URL; default is tar.gz. (Downloading can be executed conditionally according to properties by specifying the properties query param. In this case only cached artifacts are searched.)",
            snippet: "curl <CURL_AUTH> -XGET $2/api/vcs/downloadBranch/$1/<USER_ORG>/<REPO>/<BRANCH_NAME>?ext=<tar.gz/zip>[&properties=key=value]"
        }, {
            before: "Use the following command to download a file within a branch as a zip:",
            snippet: "curl <CURL_AUTH> -XGET $2/api/vcs/downloadBranchFile/$1/<USER_ORG>/<REPO>/<BRANCH_NAME>!<PATH_TO_FILE>?ext=zip"
        }]
    },
    yum: {
        general: [{
            before: "To resolve <i>.rpm</i> files using the YUM client, edit or create the <i>artifactory.repo</i> file with root privileges:",
            snippet: "sudo vi /etc/yum.repos.d/artifactory.repo"
        }, {
            before: "Then edit the baseurl to point to the path of the <a href=\"https://www.jfrog.com/confluence/display/RTF/YUM+Repositories#YUMRepositories-YUMrepodataFolderDepth\" target=\"_blank\">repodata folder</a> according to configured repository depth.<br />If the configured depth is 0 the baseurl should point to the root of the repository.",
            snippet: "[Artifactory]\n" + "name=Artifactory\n" + "baseurl=http://<URL_ENCODED_USERNAME>:<PASSWORD>@$4/artifactory/$1/<PATH_TO_REPODATA_FOLDER>\n" + "enabled=1\n" + "gpgcheck=0\n#Optional - if you have GPG signing keys installed, use the below flags to verify the repository metadata signature:\n#gpgkey=http://<URL_ENCODED_USERNAME>:<PASSWORD>@$4/artifactory/$1/<PATH_TO_REPODATA_FOLDER>/repomd.xml.key\n#repo_gpgcheck=1"
        }],
        read: [{

        }, {
            before: "After adding the RPM repository you can install a package using the following yum install command:",
            snippet: "yum install <PACKAGE>"
        }],
        deploy: {
            before: "To deploy an RPM package into an Artifactory repository you need to use Artifactory's REST API or Web UI.<br/>For example, to deploy an RPM package into this repository using the REST API, use the following command:",
            snippet: "curl <CURL_AUTH> -XPUT $2/$1/<PATH_TO_METADATA_ROOT> -T <TARGET_FILE_PATH>",
            after: "The PATH_TO_METADATA_ROOT is according to the repository configured metadata folder depth."
        }
    },
    sbt: {
        general: [{
            before: "You can define proxy repositories in the <i>~/.sbt/repositories</i> file in the following way:",
            snippet: "[repositories]\n" + "local\n" + "my-ivy-proxy-releases: $2/$1/, [organization]/[module]/(scala_[scalaVersion]/)(sbt_[sbtVersion]/)[revision]/[type]s/[artifact](-[classifier]).[ext]\n" + "my-maven-proxy-releases: $2/$1/"
        }, {
            before: "In order to specify that all resolvers added in the sbt project should be ignored in favor of those configured in the repositories configuration, add the following configuration option to the sbt launcher script:",
            snippet: "-Dsbt.override.build.repos=true",
            after: "You can add this setting to the <i>/usr/local/etc/sbtopts</i> file"
        }],
        read: {
            before: "Add the following to your <i>build.sbt</i> file:",
            snippet: "resolvers += \n" + "\"Artifactory\" at \"$2/$1/\""
        },
        deploy: [{
            before: "To publish <b>releases</b> add the following to your build.sbt:",
            snippet: "publishTo := Some(\"Artifactory Realm\" at \"$2/$1\")\n" + "credentials += Credentials(\"Artifactory Realm\", \"$5\", \"<USERNAME>\", \"<PASSWORD>\")"
        }, {
            before: "To publish <b>snapshots</b> add the following to your build.sbt:",
            snippet: "publishTo := Some(\"Artifactory Realm\" at \"$2/$1;build.timestamp=\" + new java.util.Date().getTime)\n" + "credentials += Credentials(\"Artifactory Realm\", \"$5\", \"<USERNAME>\", \"<PASSWORD>\")"
        }]
    },
    gradle: {
        general: {
            title: "Click on \"Generate Gradle Settings\" in order to use Virtual or Remote repositories for resolution."
        }
    },
    composer: {
        general: [{
            before: "In order to configure your Composer client to work with Aritfactory, you need to edit its <i>config.json</i> file (which can usually be found under <i>&lt;user-home-dir&gt;/.composer/config.json</i>) and add a repository reference to your Artifactory Composer repository. For example:",
            snippet: "{\n" +
            "    \"repositories\": [\n" +
            "        {\"type\": \"composer\", \"url\": \"$2/api/composer/$1\"},\n" +
            "        {\"packagist\": false}\n" +
            "    ]\n" +
            "}"
        }, {
            before: "When working with a non-secure URL (i.e. HTTP instead of HTTPS), you need to add the below configuration to the <i>config.json</i> file as well:",
            snippet: "\"config\": {\n" +
            "   \"secure-http\" : false\n" +
            "}"
        }, {
            before: "To access Artifactory using credentials, you can specify them in the <i>auth.json</i> file as follows:",
            snippet: "{\n"+
            "    \"http-basic\": {\n"+
            "        \"$4\": {\n" +
            "            \"username\": \"<USERNAME>\",\n"+
            "            \"password\": \"<PASSWORD>\"\n"+
            "        }\n"+
            "    }\n"+
            "}"
        }],
        read: [{
            before: "To install your composer.json dependencies, use the below command:",
            snippet: "composer install --prefer-dist"
        }],
        deploy: [{
            before: "To deploy a Composer package into an Artifactory repository you need to use Artifactory's REST API or the Web UI.<br/>For example, to deploy a Composer package into this repository using the REST API, use the following command:",
            snippet: "curl <CURL_AUTH> $2/$1/<TARGET_FILE_PATH> -T <PATH_TO_FILE>",
        }, {
            before: "If the package composer.json file does not include a version attribute, you should specify the version as a <i>composer.version</i> property (matrix parameter):",
            snippet: "curl <CURL_AUTH> \"$2/$1/<TARGET_FILE_PATH>;composer.version=1.0.0\" -T <PATH_TO_FILE>",
        }]
    },
    chef: {
        general: [{
            before: "In order to configure your Knife client to work with Artifactory, you need to edit its <i>knife.rb</i> file (which can usually be found under <i>&lt;user-home-dir&gt;/.chef/</i>) and add a reference to your Artifactory Chef repository as a \"supermarket_site\". For example:",
            snippet: "knife[:supermarket_site] = '$2/api/chef/$1'",
        }, {
            before: "To support authentication which may be required by Artifactory, you need to install the <i>knife-art</i> plugin. " +
            "For installation instructions, please refer to the <a href=\"https://www.jfrog.com/confluence/display/RTF/Chef+Supermarket\" target=\"_blank\">Artifactory User Guide</a>). " +
            "Once the plugin is installed, you can specify your credentials at the beginning of the url as shown below:",
            snippet: "knife[:supermarket_site] = http://<USERNAME>:<PASSWORD>@$4/artifactory/api/chef/$1"
        }],
        read: [{
            before: "To install cookbook using Knife, use the below command:",
            snippet: "knife artifactory install <cookbook-name> [VERSION]"
        }],
        deploy: [{
            before: "To deploy a cookbook using Knife, run:",
            snippet: "knife artifactory share <cookbook-name> [CATEGORY]",
        }]
    },
    p2: {
        general: [{
            before: 'To configure Eclipse to get available packages through Artifactory, take the following steps:<br/>' +
                    '<ol>' +
                    '<li>In the Eclipse menu, select Help | Install new Software and then click Add.</li>' +
                    '<li>In the Add Repository popup, enter a name for your repository (we recommend using the same name used in Artifactory) and its URL: &lt;repository URL&gt;</li>' +
                    '<li>Eclipse will then query Artifactory and display the packages available in the repository.</li>' +
                    '</ol>' +
                    '<br><b>Integration with Tycho Plugins</b><br>' +
                    'To resolve all dependencies through Artifactory, simply change the repository URL tag of your build pom.xml file as displayed in the snippet below:',
            snippet: '<repository>\n' +
                     '\t<id>eclipse-indigo</id>\n' +
                     '\t<layout>p2</layout>\n' +
                     '\t<url>$2/$1/</url>\n' +
                     '</repository>'
        }]
    },
    buildinfo: {
        general: [{
            before: "The <i>artifactory-build-info</i> repository contains all builds deployed to Artifactory, either directly through the UI or via REST API, CLI or the different CI server plugins.<br><br>" +
                    "When deploying a build info JSON file, Artifactory will automatically place it in the repository using the following structure:<br>" +
                    "<i>artifactory-build-info > [build-name] > [build-number]-[upload timestamp].json</i>"
        }, {
            title: '<label class="snippet">Deploy</label>',
            before: "There are different ways to upload a build:" +
            "<ol>" +
            "<li>Through the different <a href=\"https://www.jfrog.com/confluence/display/RTF/Build+Integration\" target=\"_blank\">CI server integrations</a></li>" +
            "<li>Through JFrog CLI <a href=\"https://www.jfrog.com/confluence/display/CLI/CLI+for+JFrog+Artifactory#CLIforJFrogArtifactory-BuildIntegration\" target=\"_blank\">Build Integration</a></li>" +
            "<li>Using the <a href=\"https://www.jfrog.com/confluence/display/RTF/Artifactory+REST+API#ArtifactoryRESTAPI-BuildUpload\" target=\"_blank\">Upload Build</a> REST API endpoint</li>" +
            "<li>Using the Upload button in the UI</li>" +
            "</ol>",
        }]
    }



};

export default snippets;
