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
    'name': 'Name',
    'description': 'Description',
    'artifactsCount': 'Artifact Count / Size',
    'repositoryPath': 'Repository Path',
    'originPath': 'Source Path',
    'bintrayOrg': 'Bintray Organization',
    'bintrayProduct': 'Bintray Product',
    'bintrayUrl': 'Bintray Link',
    'repositoryLayout': 'Repository Layout',
    'repoType': 'Package Type',
    'remoteRepoUrl': 'Remote Repository URL',
    'created': 'Created',
    'deployedBy': 'Deployed By',
    'licenses': 'Licenses',
    'filtered': 'Filtered',
    'downloaded': 'Downloads',
    'remoteDownloaded': 'Remote Downloads',
    'moduleID': 'Module ID',
    'size': 'Size',
    'lastModified': 'Last Modified',
    'lastDownloaded': 'Last Downloaded',
    'lastRemoteDownloaded': 'Last Downloaded Remotely',
    'compressed': 'Compressed',
    'crc': 'CRC',
    'modificationTime': 'Modification Time',
    'path': 'Path',
    'watchingSince': 'Watching Since',
    'lastDownloadedBy': 'Last Downloaded By',
    'lastRemoteDownloadedBy': 'Last Downloaded Remotely By',
    'lastReplicationStatus': 'Last Replication Status',
    'signingKeyLink': 'Signing Key',
    'externalUrl': 'External URL',
    tabs: {
        General: 'General',
        Properties: 'Properties',
        EffectivePermission: 'Effective Permissions',
        Watch: 'Watchers',
        Builds: 'Builds',
        Xray: 'Xray',
        'GeneralXml': 'XML View',
        'ViewSource': 'View Source',
        'NuPkgInfo': 'NuPkg Info',
        'PomView': 'Pom View',
        'IVYXml': 'Ivy View',
        'RubyGems': 'RubyGems',
        'NpmInfo': 'Npm Info',
        'DebianInfo': 'Debian Info',
        'OpkgInfo': 'Opkg Info',
        'ChefInfo': 'Chef Info',
        'ComposerInfo': 'Composer Info',
        'CranInfo': 'CRAN Info',
        'CondaInfo': 'Conda Info',
        'PyPIInfo': 'PyPI Info',
        'HelmInfo': 'Chart Info',
        'GoInfo': 'Go Info',
        'PuppetInfo': 'Puppet Info',
        'BowerInfo': 'Bower Info',
        'DockerInfo': 'Docker Info',
        'DockerAncestryInfo': 'Docker Ancestry',
        'DockerV2Info': 'Docker Info',
        'Rpm': 'Rpm Info',
        'Cocoapods': 'Cocoapods Info',
        'ConanInfo': 'Conan Info',
        'ConanPackageInfo': 'Conan Package Info',
        'StashInfo': 'Stash Info'
    },
    nuget: {
        authors: 'Authors',
        owners: 'Owners',
        pkgTitle: 'Title',
        tags: 'Tags',
        version: 'Version',
        requireLicenseAcceptance: 'Require License Acceptance',
        id: 'ID',
        title: 'Title',
        languages: 'Languages',
        releaseNotes: 'Release Notes',
        summary: 'Summary',
        projectUrl: 'Project URL',
        copyright: 'Copyright',
        licenseUrl: 'License URL'

    },
    composer: {
        name: 'Name',
        version: 'Version',
        authors: 'Authors',
        licenses: 'Licenses',
        type: 'Type',
        keywords: 'Keywords'

    },
    chef: {
        name: 'Name',
        version: 'Version',
        maintainer: 'Maintainer',
        sourceUrl: 'Source URL',
        license: 'License'
    },
    conan: {
        name: 'Name',
        version: 'Version',
        user: 'User',
        channel: 'Channel',
        reference: 'Reference',
        author: 'Author',
        license: 'License',
        url: 'URL',
        //Conan Package Info
        os: 'OS',
        arch: 'Architecture',
        buildType: 'Build Type',
        compiler: 'Compiler',
        compilerVersion: 'Compiler Version',
        compilerRuntime: 'Compiler Runtime',
        shared: 'Shared'
    },
    conda: {
        name: 'Name',
        version: 'Version',
        license: 'License',
        licenseFamily: 'License Family',
        trackFeatures: 'Track Features',
        features: 'Features',
        arch: 'Architecture',
        noarch: 'No Architecture (noarch)',
        platform: 'Platform',
        build: 'Build',
        buildNumber: 'Build Number',
        timestamp: ''
    },
    docker: {
        //Info:
        imageId: 'Image Id',
        parent: 'Parent Id',
        created: 'Created',
        container: 'Container',
        dockerVersion: 'Docker Version',
        author: 'Author',
        architecture: 'Architecture',
        os: 'OS',

        //Config:
        size: 'Size',
        hostname: 'Hostname',
        domainName: 'DomainName',
        user: 'User',
        memory: 'Memory',
        memorySwap: 'MemorySwap',
        cpuShares: 'CpuShares',
        cpuSet: 'CpuSet',
        attachStdin: 'AttachStdin',
        attachStdout: 'AttachStdout',
        attachStderr: 'AttachStderr',
        portSpecs: 'portSpecs',
        exposedPorts: 'exposedPorts',
        tty: 'Tty',
        openStdin: 'OpenStdin',
        stdinOnce: 'StdinOnce',
        env: 'Env',
        cmd: 'Cmd',
        image: 'Image',
        volumes: 'Volumes',
        workingDir: 'WorkingDir',
        entryPoint: 'EntryPoint',
        networkDisabled: 'NetworkDisabled',
        onBuild: 'OnBuild'

    },
    dockerAncestry: {
        size: 'Virtual Size'
    },
    dockerV2: {
        title: 'Title',
        digest: 'Digest',
        ports: 'Ports',
        totalSize: 'Total Size',
        volumes: 'Volumes'
    },
    cran: {
        name: 'Name',
        version: 'Version',
        title: 'Title',
        author: 'Author',
        maintainer: 'Maintainer',
        priority: 'Priority',
        license: 'License',
        needsCompilation: 'Need Compilation',
        path: 'Path',
        osType: 'OS type',
        licenseRestrictsUse: 'License Restricts Use',
        licenseIsFoss: 'License is Foss',
        archs: 'Architectures'
    },
    pyPi: {
        name: 'Name',
        author: 'Author',
        authorEmail : 'Author Email',
        homepage: 'Homepage',
        downloadUrl: 'Download URL',
        platform: 'Platform',
        version: 'Version',
        license: 'License',
        keywords: 'Keywords',
        summary: 'Summary',
        requiresPython: 'Requires Python'
    },
    helm: {
        name: 'Name',
        version: 'Version',
        appVersion: 'Application Version',
        keywords: 'Keywords',
        maintainers: 'Maintainers',
        sources: 'Sources',
        deprecated: 'Deprecated'
    },
    go: {
        name: 'Name',
        version: 'Version'
    },
    puppet: {
        name: 'Name',
        version: 'Version',
        license: 'License',
        keywords: 'Keywords',
        description: 'Description'
    },
    bower: {
        name: 'Name',
        description: 'Description',
        version: 'Version',
        license: 'License',
        keywords: 'Keywords',
        repository: 'Repository'
    },
    rubyGems: {
        authors: 'Authors',
        owners: 'Owners',
        description: 'Description',
        homepage: 'Homepage',
        name: 'Name',
        platform: 'Platform',
        summary: 'Summary',
        repositoryPath: 'Repository Path',
        version: 'Version'

    },
    npm: {
        name: 'Name',
        version: 'Version',
        license: 'License',
        description: 'Description',
        keywords: 'Keywords',
        repository: 'Repository'
    },
    debian: {
        packageName: 'Name',
        version: 'Version',
        architecture: 'Architecture',
        description: 'Description',
        section: 'Section',
        priority: 'Priority',
        maintainer: 'Maintainer',
        website: 'Website',
        size: 'Size',
        license: 'License'
    },
    opkg: {
        packageName: 'Name',
        version: 'Version',
        architecture: 'Architecture',
        description: 'Description',
        section: 'Section',
        priority: 'Priority',
        maintainer: 'Maintainer',
        website: 'Website',
        size: 'Size',
        license: 'License'

    },
    rpm: {
        buildDate: 'Build Date',
        epoch: 'Epoch',
        name: "Name",
        release: "Release",
        size: 'Size',
        summary: "Summary",
        version: "Version",
        buildHost: "Build Host",
        packager: "Packager",
        sourceRpm: "Source Rpm",
        url: "URL",
        vendor: "Vendor"

    },
    cocoapods: {
        name: 'Name',
        description: 'Description',
        version: 'Version',
        license: 'License',
        keywords: 'Keywords'
    },
    trash: {
        deletedTime: 'Deleted Time',
        deletedBy: 'Deleted By',
        originalRepository: "Original Repository",
        originalRepositoryType: "Original Repository Type",
        originalPath: "Original Path"
    }
}