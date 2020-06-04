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
const ARCHIVE_MARKER = '!';
const COMPACT_FOLDERS_KEY = 'COMPACT_FOLDERS';

export class TreeBrowserDao {
    constructor(RESOURCE, ArtifactoryHttpClient, $q, $injector, ArtifactoryStorage, FooterDao) {
        this.$q = $q;
        this.TreeNode = $injector.get('TreeNode');
        this.RESOURCE = RESOURCE;
        this.footerDao = FooterDao;
        this.artifactoryHttpClient = ArtifactoryHttpClient;
        this.artifactoryStorage = ArtifactoryStorage;
        this.compactFolders = ArtifactoryStorage.getItem(COMPACT_FOLDERS_KEY, /* defaultValue = */ true);
        this.roots = null;
    }

    setCompactFolders(value) {
        this.compactFolders = value;
        this.artifactoryStorage.setItem(COMPACT_FOLDERS_KEY, this.compactFolders);
    }

    getCompactFolders() {
        return this.compactFolders;
    }

    // get all repositories (roots) and cache them
    // or return the cached promise
    getRoots(force = false, continueState, filter, sorting, mustInclude, repositoryKeys) {
        if (force || !this.roots) {
            this.roots = this._loadChildren({type: 'root'}, null, continueState, filter, sorting, mustInclude, repositoryKeys);
        }
        return this.roots;
    }

	getSetMeUpRepos(payload) {
		this.roots = this._loadSetMeUpRepos(payload);
		return this.roots;
	}

	_loadSetMeUpRepos(node, parent) {
		return this.artifactoryHttpClient.post(
			this.RESOURCE.TREE_BROWSER + '?compacted=true&$no_spinner=true', node)
		           .then(result => {
				           const response = this._transformData(result.data.data, parent);
				           response.continueState = result.data.continueState;
				           return response;
			           }
		           );

	}

	// invalidate the cached promise
	invalidateRoots() {
		this.roots = null;
	}

    findRepo(repoKey) {
        return this.getRoots().then((roots) => {
            return _.findWhere(roots, {repoKey: repoKey});
        });
    }

    findNodeByFullPath(path, includeArchives = true) {
        if (typeof(path) === 'string') path = path.split('/');
        path = path.map((pathElement) => _.trimRight(pathElement, ARCHIVE_MARKER));
        if (!path.length) return this.$q.when(null);
        let pathElement = path.shift();
        // Find child:
        return this.getRoots(false, null, null, null, pathElement).then((roots) => {
            roots = roots.data;
            return _.findWhere(roots, {text: pathElement});
        })
        .then((root) => {
            // Recursively look for rest of fullpath:
            if (root) return root.findNodeByPath(path, 0, includeArchives);
            else return this;
        }).catch(() => null);
    }

    _loadChildren(node, parent, continueState, filter = {}, sorting, mustInclude, repositoryKeys) {
        let compact = this.compactFolders && !node.trashcan; //Don't compact the repos level under trashcan

        let footerData = this.footerDao.getInfo();

        if (!footerData.treebrowserFolderCompact) compact = false;

        //return this.artifactoryHttpClient.post(this.RESOURCE.TREE_BROWSER + '?compacted=' + (compact), node)
        return this.artifactoryHttpClient.post(this.RESOURCE.TREE_BROWSER + '?compacted=' + (compact) + '&$no_spinner=true', _.extend({}, node, {/*limit: 5, */continueState, mustInclude, repositoryKeys}, filter, sorting))
                .then(result => {
                    return {
                        data: this._transformData(result.data.data, parent),
                        continueState: result.data.continueState
                    }
                });

    }

    // Wrap with TreeNode
    // Recursively go over children if exist
    // Assign the parent node
    _transformData(data, parent) {
        return data.map((node) => {
            node = new this.TreeNode(node);
            node.parent = parent;
            if (_.isArray(node.children)) {
                node.children = this.$q.when(this._transformData(node.children, node));
            }
            return node;
        });
    }
}
