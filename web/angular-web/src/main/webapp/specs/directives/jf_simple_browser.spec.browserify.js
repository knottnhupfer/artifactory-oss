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
import StateParamsMock from '../../mocks/state_params_mock.browserify';
import UserMock from '../../mocks/user_mock.browserify';
import TreeNodeMock from '../../mocks/tree_node_mock.browserify';
import JFTreeObject from '../page_objects/jf_tree_object.browserify';
import mockStorage from '../../mocks/artifactory_storage_mock.browserify';

describe('unit test:jf_simple_browser directive', () => {
    let simpleBrowserElement,
            EVENTS,
            $scope,
            $timeout,
            $q,
            httpBackend,
            RESOURCE,
            TreeBrowserDao,
            repo1,
            repo2,
            child,
            jfTreeObject,
            stateParams,
            JFrogEventBus,
            ArtifactoryState;

    mockStorage();

    function setup(_TreeBrowserDao_, TreeNode, _$timeout_, _$q_, $httpBackend, _RESOURCE_, _JFrogEventBus_, _ArtifactoryState_) {
        httpBackend = $httpBackend;
        RESOURCE = _RESOURCE_;
        $timeout = _$timeout_;
        $q = _$q_;
        TreeBrowserDao = _TreeBrowserDao_;
        repo1 = new TreeNode(TreeNodeMock.repo('repo1'));
        repo2 = new TreeNode(TreeNodeMock.repo('repo2'));
        child = new TreeNode(TreeNodeMock.file({text: 'file', path: 'file', repoKey: 'repo1', parent: repo1}));

        JFrogEventBus = _JFrogEventBus_;
        EVENTS = JFrogEventBus.getEventsDefinition();
        ArtifactoryState = _ArtifactoryState_;
        spyOn(JFrogEventBus, 'dispatch').and.callThrough();
        UserMock.mockCurrentUser();
    }

    function compileDirective() {
        $scope = compileHtml('<jf-tree-browser browser-controller="Browsers" simple-mode="true"></jf-tree-browser>', {Browsers: {}});
        flush();
        simpleBrowserElement = angular.element(document.body).find('jf-tree-browser')[0];
        $(simpleBrowserElement).find('.default-tree-container').height(400);
        jfTreeObject = new JFTreeObject(simpleBrowserElement);
    }

    function twoDotsItem() {
        return _.find($('.jf-tree-item'), function(el) {
            return $(el).text().match(new RegExp(/\.\./));
        });
    }
    function repo1Item() {
        return jfTreeObject.getNodeWithText('repo1');
    }
    function repo2Item() {
        return jfTreeObject.getNodeWithText('repo2');
    }
    function fileItem() {
        return jfTreeObject.getNodeWithText('file');
    }

    function flush() {
        httpBackend.flush();
    }

    function drillDownRepo1() {
        $(repo1Item()).parent().dblclick();
        flush();
        $timeout.flush();
    }

    beforeEach(m('artifactory.templates', 'artifactory.states', 'conf.fixer'));
    beforeEach(() => {
        stateParams = {};
        StateParamsMock(stateParams);
    });

    beforeEach(inject(setup));

    beforeEach(() => {
        TreeNodeMock.expectGetFooterData();
    });
    beforeEach(() => {
        TreeNodeMock.expectGetRoots();
    });

    describe('with no artifact in stateParams', () => {
        beforeEach(compileDirective);
        beforeEach(() => {
            try { $timeout.flush() } catch(e) {}
        });
        beforeEach(() => {
            repo1.expectGetChildren([child]);
        });

        it('should show tree', () => {
            expect(simpleBrowserElement).toBeDefined();
            expect(repo1Item()).toBeDefined();
            expect(repo2Item()).toBeDefined();
            expect(fileItem()).not.toBeDefined();
            expect(twoDotsItem()).not.toBeDefined();
        });

        it('should allow to drill down to a repo', (done) => {
            drillDownRepo1();

            expect(repo1Item()).toBeDefined();
            expect(fileItem()).toBeDefined();
            expect(repo2Item()).not.toBeDefined();
            TreeBrowserDao.getRoots()
                    .then((roots) => {
                        let node = jfTreeObject.findNodeWithData(roots[0]);
                        expect(JFrogEventBus.dispatch).toHaveBeenCalledWith('tree:node:select', node);
                        done();
                    });
            $scope.$digest();
        });

        it('should not drill down to a file', (done) => {
            drillDownRepo1();
            child.expectLoad(TreeNodeMock.data());
            fileItem().click();
            flush();
            expect(repo1Item()).toBeDefined();
            expect(fileItem()).toBeDefined();
            TreeBrowserDao.getRoots()
                    .then((roots) => {
                        return roots[0].getChildren();
                    })
                    .then((children) => {
                        let node = jfTreeObject.findNodeWithData(children[0]);
                        expect(JFrogEventBus.dispatch).toHaveBeenCalledWith('tree:node:select', node);
                        done();
                    });
            $scope.$digest();
        });

        it('should allow to go up', () => {
            drillDownRepo1();
            twoDotsItem().click();
            $scope.$digest();
            expect(repo1Item()).toBeDefined();
            expect(repo2Item()).toBeDefined();
        });
    });
    describe('with artifact in stateParams, tree untouched', () => {
        beforeEach(() => {
            stateParams.artifact = 'repo1';
            compileDirective();
            repo1.expectGetChildren([child]);
            $timeout.flush();
        });

        it('should activate repo1 but not drill down', (done) => {
            expect(simpleBrowserElement).toBeDefined();
            expect(repo1Item()).toBeDefined();
            expect(repo2Item()).toBeDefined();
            expect(fileItem()).not.toBeDefined();
            expect(twoDotsItem()).not.toBeDefined();
            expect($(repo1Item()).parents('.jf-tree-item')).toHaveClass('selected');
            TreeBrowserDao.getRoots()
                    .then((roots) => {
                        let node = jfTreeObject.findNodeWithData(roots[0]);
                        expect(JFrogEventBus.dispatch).toHaveBeenCalledWith('tree:node:select', node);
                        done();
                    });
            $scope.$digest();
        });
    });
    xdescribe('with artifact state, tree touched', () => {
        beforeEach(() => {
            ArtifactoryState.setState('tree_touched', true);
            stateParams.artifact = 'repo1';
            compileDirective();
            repo1.expectGetChildren([child]);
            $timeout.flush();
        });
        it('should activate repo1 & drill down into it', (done) => {
            expect(simpleBrowserElement).toBeDefined();
            expect(repo1Item()).toBeDefined();
            expect(fileItem()).toBeDefined();
            expect(repo2Item()).not.toBeDefined();
            expect(twoDotsItem()).toBeDefined();
            expect($(repo1Item())).toHaveClass('jstree-clicked');
            TreeBrowserDao.getRoots()
                    .then((roots) => {
                        expect(JFrogEventBus.dispatch
                        ).
                        toHaveBeenCalledWith('tree:node:select', {data: roots[0]});
                        done();
                    });
            $scope.$digest();
        });
    });
    describe('with artifact in stateParams of file', () => {
        beforeEach(() => {
            stateParams.artifact = 'repo1/file';
            compileDirective();
            repo1.expectGetChildren([child]);
            $timeout.flush();
            flush();
            child.expectLoad();
            $timeout.flush();
            flush();
        });

        it('should activate repo1/file', (done) => {
            expect(simpleBrowserElement).toBeDefined();
            expect(repo1Item()).toBeDefined();
            expect(repo2Item()).not.toBeDefined();
            expect(fileItem()).toBeDefined();
            expect(twoDotsItem()).toBeDefined();
            expect($(fileItem()).parents('.jf-tree-item')).toHaveClass('selected');
            TreeBrowserDao.getRoots()
                    .then((roots) => {
                        return roots[0].getChildren();
                    })
                    .then((children) => {
                        let node = jfTreeObject.findNodeWithData(children[0]);
                        expect(JFrogEventBus.dispatch).toHaveBeenCalledWith('tree:node:select', node);
                        done();
                    });
            flush();
        });
    });
    describe('events', () => {
        beforeEach(compileDirective);
        describe('search', () => {
            beforeEach(() => {
                $timeout.flush();
                JFrogEventBus.dispatch(EVENTS.TREE_SEARCH_CHANGE, "rep");
                $scope.$apply();
                $scope.$digest();
            });
            it('should mark search results', () => {
                expect($(repo1Item()).parents('.jf-tree-item')).toHaveClass('quick-find-match');
                expect($(repo2Item()).parents('.jf-tree-item')).toHaveClass('quick-find-match');
                expect($(repo1Item()).parents('.jf-tree-item')).toHaveClass('pre-selected');
                expect($(repo2Item()).parents('.jf-tree-item')).not.toHaveClass('pre-selected');
            });
            it('should search next result when pressing arrow key down', () => {
                let event = new KeyboardEvent('keydown', {which:40,key:'Down'})
                JFrogEventBus.dispatch(EVENTS.TREE_SEARCH_KEYDOWN, event);
                $timeout.flush();
                expect($(repo1Item()).parents('.jf-tree-item')).not.toHaveClass('pre-selected');
                expect($(repo2Item()).parents('.jf-tree-item')).toHaveClass('pre-selected');
            });
            it('should search next result when pressing arrow key up', () => {
                let event = new KeyboardEvent('keydown', {which:38,key:'Up'})
                JFrogEventBus.dispatch(EVENTS.TREE_SEARCH_KEYDOWN, event);
                $timeout.flush();
                expect($(repo1Item()).parents('.jf-tree-item')).not.toHaveClass('pre-selected');
                expect($(repo2Item()).parents('.jf-tree-item')).toHaveClass('pre-selected');
            });
            it('should cancel search', () => {
                JFrogEventBus.dispatch(EVENTS.TREE_SEARCH_CANCEL);
                expect($(repo1Item())).not.toHaveClass('quick-find-match');
                expect($(repo2Item())).not.toHaveClass('quick-find-match');
            });
        });
        it('should drill down to repo after deploy', () => {
            $timeout.flush();
            expect(simpleBrowserElement).toBeDefined();
            TreeNodeMock.expectGetRoots();
            repo1.expectGetChildren([child]);
            JFrogEventBus.dispatch(EVENTS.ACTION_DEPLOY, ["repo1",{targetPath: '/file'}]);
            flush();
            $timeout.flush();
            expect(repo1Item()).toBeDefined();
            expect(fileItem()).toBeDefined();
            expect(repo2Item()).not.toBeDefined();
        });
        it('should reload node after refresh', (done) => {
            $timeout.flush();
            expect(simpleBrowserElement).toBeDefined();
            repo1.expectGetChildren([child]);
            drillDownRepo1();
            TreeBrowserDao.getRoots()
                    .then((roots) => {
                        JFrogEventBus.dispatch(EVENTS.ACTION_REFRESH, roots[0]);
                        done();
                    });
            $scope.$digest();
        });
        it('should go up after delete', (done) => {
            $timeout.flush();
            expect(simpleBrowserElement).toBeDefined();
            repo1.expectGetChildren([child]);
            drillDownRepo1();
            expect(repo1Item()).toBeDefined();
            expect(fileItem()).toBeDefined();
            expect(repo2Item()).not.toBeDefined();

            TreeBrowserDao.getRoots()
                    .then((roots) => {
                        return roots[0].getChildren();
                    })
                    .then((children) => {
                        let node = jfTreeObject.findNodeWithData(children[0]);
                        TreeNodeMock.expectGetRoots();
                        repo1.expectGetChildren([]);
                        JFrogEventBus.dispatch(EVENTS.ACTION_DELETE, node);
                        setTimeout(() => { // Must put in timeout, because can't call $timeout.flush when digest is going on
                            flush();
                            $timeout.flush();
                            expect(repo1Item()).toBeDefined();
                            expect(repo2Item()).toBeDefined();
                            done();
                        });
                    });
            $scope.$digest();
        });
        let targetOptions = {
            target: {
                targetRepoKey: 'repo1',
                targetPath: 'file',
            },
            node: {
                data: {
                    text: 'file'
                }
            }
        };
        it('should open target node after move', () => {
            $timeout.flush();
            expect(simpleBrowserElement).toBeDefined();
//            ArtifactoryState.setState('tree_touched', true);
            TreeNodeMock.expectGetRoots();
            repo1.expectGetChildren([child]);
            repo1.expectGetChildren([child]); // Shouldn't happen !
            JFrogEventBus.dispatch(EVENTS.ACTION_MOVE, targetOptions);
            flush();
            child.expectLoad(TreeNodeMock.data());
            $timeout.flush();

            expect(repo1Item()).toBeDefined();
            expect(fileItem()).toBeDefined();
            expect(repo2Item()).not.toBeDefined();
            expect($(fileItem()).parents('.jf-tree-item')).toHaveClass('selected');
        });

        it('should open target node after copy', () => {
            $timeout.flush();
            expect(simpleBrowserElement).toBeDefined();
            //            ArtifactoryState.setState('tree_touched', true);
            TreeNodeMock.expectGetRoots();
            repo1.expectGetChildren([child]);
            repo1.expectGetChildren([child]); // Yeah, this shouldn't happen
            JFrogEventBus.dispatch(EVENTS.ACTION_COPY, targetOptions);
            flush();
            child.expectLoad(TreeNodeMock.data());
            $timeout.flush();

            expect(repo1Item()).toBeDefined();
            expect(fileItem()).toBeDefined();
            expect(repo2Item()).not.toBeDefined();
            expect($(fileItem()).parents('.jf-tree-item')).toHaveClass('selected');
        });

        it('should reload node after switching compact mode', () => {
            $timeout.flush();
            expect(simpleBrowserElement).toBeDefined();
            TreeNodeMock.expectGetRoots();
            JFrogEventBus.dispatch(EVENTS.TREE_COMPACT);
            flush();
            expect(repo1Item()).toBeDefined();
            expect(repo2Item()).toBeDefined();
            expect(fileItem()).not.toBeDefined();
        });
        it('should reload node after change url', () => {
            $timeout.flush();
            expect(simpleBrowserElement).toBeDefined();
            repo1.expectGetChildren([child]);
            JFrogEventBus.dispatch(EVENTS.TABS_URL_CHANGED, {browser: 'simple', artifact: 'repo1/file'});
            flush();
            child.expectLoad(TreeNodeMock.data());
            $timeout.flush();
            expect(repo1Item()).toBeDefined();
            expect(repo2Item()).not.toBeDefined();
            expect(fileItem()).toBeDefined();
            expect($(fileItem()).parents('.jf-tree-item')).toHaveClass('selected');
        });
    });
});
