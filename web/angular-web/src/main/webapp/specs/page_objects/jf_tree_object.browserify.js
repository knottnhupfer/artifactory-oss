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
function JFTreeObject(jfTreeBrowserElement) {
  this.ctrl = angular.element(jfTreeBrowserElement).controller('jfTreeBrowser');
  this.treeApi = this.ctrl.treeApi;

  this.getNode = function(node) {
    return node;
  };
  
  this.expandFirstItem = function() {
    $('.jf-tree-item:first-child .node-expander .action-icon').click();
  };

  this.loadNodeItem = function(text) {
      $(this.getNodeWithText(text)).parent().click();
  };

  this.getNodeWithText = function(text) {
    return _.find($('.jf-tree-item .node-text'), function(el) {
      return $(el).text().match(new RegExp(text));
    });
  };
  this.getRootItem = function() {
    return {children: this.treeApi.$root};
  }
  this.findNodeWithData = function(data) {
    return this.treeApi.findNode(n => n.data === data);
  }
}
module.exports = JFTreeObject;