import { assign } from 'lodash';

const getRepos = (versions) => {
    const reposSet = new Set();
    versions.map(v => {
        v.repos.map(r => {
            reposSet.add(r.name);
        })
    });
    return [...reposSet];
};

const packagesFieldsAdapter = ({packages}) => ({
    results: packages.map(
        ({name, modified, tags, numberOfVersions = 0, stats}) => {
            return {
                name: name,
                lastModified: moment(modified).format('X'),
                keywords: tags,
                numOfVersions: numberOfVersions,
                totalDownloads: (stats && stats.downloads_count)?stats.downloads_count: 0,
                repositories: null,
                numOfRepos: null
            }
        }
    ),
    resultsCount: packages.length
});

const versionsFieldsAdapter = (versions, pathAndQuery) => {
    const repositories = getRepos(versions);
    const {packageType, packageName} = pathAndQuery;
    /*let adaptedVersions= versions.map(v => {
        return {
            name: v.name,
            packageName: packageName,
            lastModified: moment(v.created).format('X'),
            downloadCount: v.downloadCount || 0,
            repositories,
            repoKey: repositories[0],
            numOfRepos: repositories.length,
            latestPath: (v.repos && v.repos.loadFilePath && v.repos.loadFilePath.length) ?
                    v.repos.loadFilePath[0].leadFilePath : '',
        }
    });*/

    if (packageType === 'docker') {
        const adaptedVersions = versions.map(v => {
            return {
                name: v.name,
                packageName,
                packageId: packageName,
                totalDownloads: (v.stats && v.stats.downloads_count)?v.stats.downloads_count: 0,
                repoKey: v.repos.length ? v.repos[0].name : '',
                lastModified: moment(v.created).format('X'),
                size: 0,

            };
        });
        return {
            versions: adaptedVersions,
            packageName,
            lastModified: moment(adaptedVersions[0].created).format('X'),
            resultsCount: adaptedVersions.length
        }
    }
    else {
        const adaptedVersions = versions.map(v => {
            return {
                name: v.name,
                licenses: v.licenses.length? v.licenses[0]: '',
                lastModified: moment(v.created).format('X'),
                numOfRepos: v.repos.map(r=>r.name).length,
                repositories: v.repos.map(r=>r.name),
                latestPath: `${v.repos[0].name}/${v.repos[0].leadFilePath}`,
                totalDownloads: (v.stats && v.stats.downloads_count)?v.stats.downloads_count: 0,
            };
        });
        return {
            results: adaptedVersions
        }
    }
};

const versionExtraInfoAdapter = (version) => {
    const versionAdapted = assign({
        downloadCount: 0
    }, version);
    return versionAdapted;
};

const getPackageTotalDownloads = packageData =>
    packageData.versions.reduce((acc, version) => acc + version.downloadCount, 0);

export {
    packagesFieldsAdapter,
    versionsFieldsAdapter,
    versionExtraInfoAdapter,
    getPackageTotalDownloads
};