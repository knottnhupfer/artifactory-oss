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
'use strict';
var UserMock = require('../../mocks/user_mock.browserify.js');
var TreeNodeMock = require('../../mocks/tree_node_mock.browserify.js');
var JFTreeObject = require('../page_objects/jf_tree_object.browserify.js');
var mockStorage = require('../../mocks/artifactory_storage_mock.browserify.js');
describe('unit test:jf_tree_browser directive', function () {
  var element,
    $scope,
    $timeout,
    httpBackend,
    RESOURCE,
    repo1,
    repo2,
    child,
    jfTreeObject,
      JFrogEventBus;

  mockStorage();

  function setup(TreeBrowserDao, TreeNode, $q, $httpBackend, _RESOURCE_, _JFrogEventBus_, _$timeout_) {
    httpBackend = $httpBackend;
    $timeout = _$timeout_;
    RESOURCE = _RESOURCE_;
    repo1 = new TreeNode(TreeNodeMock.repo('repo1'));
    repo2 = new TreeNode(TreeNodeMock.repo('repo2'));
    child = new TreeNode(TreeNodeMock.file({text: 'file'}));
    JFrogEventBus = _JFrogEventBus_;
    spyOn(JFrogEventBus, 'dispatch').and.callThrough();
    UserMock.mockCurrentUser();
  }

  function compileDirective() {
    $scope = compileHtml('<jf-tree-browser browser-controller="Browsers" simple-mode="false"></jf-tree-browser>', {Browsers: {}});
    flush();
    element = angular.element(document.body).find('jf-tree-browser')[0];
    jfTreeObject = new JFTreeObject(element);
  }

  function repo1Item() {
    return jfTreeObject.getNode(jfTreeObject.getRootItem().children[0]);
  }
  function childItem() {
    return jfTreeObject.getNode(repo1Item().$childrenCache[0]);
  }

  function flush() {
      try { httpBackend.flush() } catch(e) {};
  }

  beforeEach(m('artifactory.templates', 'artifactory.states', 'conf.fixer'));
  beforeEach(inject(setup));
  beforeEach(() => {
    TreeNodeMock.expectGetFooterData();
  });
  beforeEach(function() {
    TreeNodeMock.expectGetRoots();
  });
  beforeEach(compileDirective);
  beforeEach(() => $timeout.flush());

  it('should show tree', function() {
    expect(element).toBeDefined();
    expect(jfTreeObject.getNodeWithText('repo1')).toBeDefined();
  });
  it('should allow to expand a repo', function() {
    repo1.expectGetChildren([child]);
    jfTreeObject.expandFirstItem();
    flush();
    expect(jfTreeObject.getNodeWithText('file')).toBeDefined();
  });
  it('should allow to load a repo and its children on click', function() {
    repo1.expectGetChildren([child]);
    jfTreeObject.loadNodeItem('repo1');
    $scope.$digest();
    expect(JFrogEventBus.dispatch).toHaveBeenCalledWith('tree:node:select', repo1Item());
  });
  it('should allow to load a node', function() {
    repo1.expectGetChildren([child]);
    jfTreeObject.expandFirstItem();
    flush();
    child.expectLoad(TreeNodeMock.data());
    jfTreeObject.loadNodeItem('file');
    flush();
    expect(JFrogEventBus.dispatch).toHaveBeenCalledWith('tree:node:select', childItem());
  });

});
