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
var faker = require('faker');
function TreeNodeMock(data) {
  data = data || {};
  var name = data.text || faker.name.firstName();
  var treeNodeMock = angular.extend(
    {
      type: _.sample(['file', 'folder', 'archive', 'repository']),
      repoKey: faker.name.firstName(),
      path: faker.name.firstName() + '/' + name,
      archivePath: undefined,
      repoPkgType: 'Maven',
      repoType: 'local',
      local: true,
      text: name,
      trashcan: false
    },
    data
  );
  treeNodeMock.withChildren = function(number) {
    this.children = TreeNodeMock.array(number);
    return this;
  };
  treeNodeMock.expectGetChildren = function(children) {
    inject(function ($httpBackend, RESOURCE) {
      $httpBackend.expectPOST(RESOURCE.API_URL + RESOURCE.TREE_BROWSER + '?compacted=true&$no_spinner=true',
          {type: 'junction', repoKey: treeNodeMock.repoKey, repoType: treeNodeMock.repoType, path: treeNodeMock.path, text: treeNodeMock.text, trashcan: treeNodeMock.trashcan})
          .respond(children);            
    });
  };
  treeNodeMock.expectLoad = function(data) {
    data = data || [{tabs: [], actions: []}];
    inject(function ($httpBackend, RESOURCE) {
      $httpBackend.expectPOST(RESOURCE.API_URL + RESOURCE.TREE_BROWSER + '/tabsAndActions',
              {type: treeNodeMock.type, repoPath: treeNodeMock.repoKey + '/' + treeNodeMock.path, repoType: treeNodeMock.repoType, repoPkgType: treeNodeMock.repoPkgType})
          .respond(data[0]);
    });
  };
  return treeNodeMock;
}
TreeNodeMock.array = function(length) {
  var result = [];
  for (var i = 0; i < length; i++) {
    result[i] = TreeNodeMock();
  }
  return result;
};
TreeNodeMock.data = function(data) {
  return [angular.extend(
    {
      tabs: [],
      actions: []
    },
    data || {}
  )];
};
TreeNodeMock.repo = function(name) {
  name = name || 'repo';
  return TreeNodeMock({repoKey: name, path: '', text: name, type: 'repository', hasChild: true, tabs: [], actions: []});
};
TreeNodeMock.folder = function(options = {}) {
    let mock = TreeNodeMock(angular.extend(options, {type: 'folder', hasChild: true}));
    if (mock.path.substr(-1) != "/") mock.path += '/';
  return mock;
};
TreeNodeMock.file = function(options = {}) {
  return TreeNodeMock(angular.extend(options, {type: 'file', hasChild: false}));
};
TreeNodeMock.archive = function(options = {}) {
  return TreeNodeMock(angular.extend(options, {type: 'archive', hasChild: true}));
};

TreeNodeMock.expectGetRoots = function(compacted = true, repos = null) {
  repos = repos || [TreeNodeMock.repo('repo1'), TreeNodeMock.repo('repo2')];
  inject(function ($httpBackend, RESOURCE) {
    $httpBackend.expectPOST(RESOURCE.API_URL + RESOURCE.TREE_BROWSER + '?compacted=' + compacted + '&$no_spinner=true', {"type": "root"})
            .respond(repos);
  });
};
TreeNodeMock.expectGetFooterData = function() {
  inject(function ($httpBackend, RESOURCE) {
    $httpBackend.expectGET(RESOURCE.API_URL + RESOURCE.FOOTER)
            .respond(200);
  });
};
TreeNodeMock.expectGetChildren = function(children, compacted = true) {
  inject(function ($httpBackend, RESOURCE) {
    $httpBackend.expectPOST(RESOURCE.API_URL + RESOURCE.TREE_BROWSER + '?compacted=' + compacted + '&$no_spinner=true')
        .respond(children);            
  });
};
module.exports = TreeNodeMock;
