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
import TreeNodeMock from '../../mocks/tree_node_mock.browserify';

describe('native browser', () => {
	let nativeBrowser, TreeNode;
	function setup(NativeBrowser, _TreeNode_) {
		nativeBrowser = NativeBrowser;
		TreeNode = _TreeNode_;
	}
	beforeEach(m('artifactory.services', 'artifactory.dao'));
	beforeEach(inject(setup));
	describe('isAllowed', () => {
		it('should be true for repo', () => {
			let node = new TreeNode(TreeNodeMock.repo());
			expect(nativeBrowser.isAllowed(node)).toBe(true);
		});
		it('should be true for folder', () => {
			let node = new TreeNode(TreeNodeMock.folder());
			expect(nativeBrowser.isAllowed(node)).toBe(true);
		});
		it('should be false for file', () => {
			let node = new TreeNode(TreeNodeMock.file());
			expect(nativeBrowser.isAllowed(node)).toBe(false);
		});
		it('should be false for folder inside archive', () => {
			let node = new TreeNode(TreeNodeMock.folder({archivePath: 'archive'}));
			expect(nativeBrowser.isAllowed(node)).toBe(false);
		});
	});
	describe('pathFor', () => {
		it('should return the path of the native browser', () => {
			let node = new TreeNode(TreeNodeMock.file({repoKey: 'repo1', path: 'file'}));
			expect(nativeBrowser.pathFor(node)).toBe('../list/repo1/file/');
		});
		it('should not add a / in the end if it already exists', () => {
			let node = new TreeNode(TreeNodeMock.file({repoKey: 'repo1', path: 'file'}));
			expect(nativeBrowser.pathFor(node)).toBe('../list/repo1/file/');
		});
	});
});