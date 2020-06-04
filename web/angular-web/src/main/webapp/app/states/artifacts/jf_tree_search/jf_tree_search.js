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
import EVENTS from '../../../constants/artifacts_events.constants';
import KEYS from '../../../constants/keys.constants';

export function jfTreeSearch() {
    return {
        restrict: 'E',
        controller: TreeSearchController,
        controllerAs: 'TreeSearch',
        templateUrl: 'states/artifacts/jf_tree_search/jf_tree_search.html',
        replace:true,
        bindToController: true
    }
}

class TreeSearchController {
    constructor(JFrogEventBus, $element, hotkeys, $scope, $timeout) {
        this.$scope = $scope;
        this.JFrogEventBus = JFrogEventBus;
        this.$element = $element;
        this.$timeout = $timeout;
        this.term = '';
        this.showSearch = false;
        this.hotkeys = hotkeys;
        this._setupHotkeys();


        this.JFrogEventBus.registerOnScope(this.$scope, EVENTS.ACTIVATE_TREE_SEARCH, (key) => {
            $timeout(()=>{
                this._activateSearch(key);
            });
        });
    }

    onChange() {
        this.JFrogEventBus.dispatch(EVENTS.TREE_SEARCH_CHANGE, this.term);
    }
    onKeydown($event) {
        // Send events to tree on key down / up / enter
        if (_.include([KEYS.ENTER, KEYS.DOWN_ARROW, KEYS.UP_ARROW, KEYS.ESC], $event.keyCode)) {
            $event.preventDefault();
            // Deactivate on enter press
            if (_.include([KEYS.ENTER, KEYS.ESC], $event.keyCode)) {
                this._deactivateSearch();
            }
        }
        this.JFrogEventBus.dispatch(EVENTS.TREE_SEARCH_KEYDOWN, $event);
    }
    _deactivateSearch() {
        this.term = '';
        this.showSearch = false;
        this.$searchInput().blur();
        $('body').off('click.outsideTreeSearch');
    }

    _activateSearch(key) {
        this.JFrogEventBus.dispatch(EVENTS.TREE_SEARCH_CANCEL);
        this.showSearch = true;
        this.$timeout(()=>{
            if (!this.term) { //fix for firefox not showing the first key
                this.term = key;
            this.JFrogEventBus.dispatch(EVENTS.TREE_SEARCH_CHANGE, this.term);
            }
        });
        this.$searchInput().focus();
        $('body').on('click.outsideTreeSearch', (e) => {
            if (!$(this.$element).has(e.target).length) {
            this.JFrogEventBus.dispatch(EVENTS.TREE_SEARCH_CANCEL);
                this._deactivateSearch();
                if (!this.$scope.$$phase) this.$scope.$digest();
            }
        });

    }

    $searchInput() {
        return this.$element.find('input')[0];
    }

    _setupHotkeys() {
        this.hotkeys.bindTo(this.$scope).add({
            combo: KEYS.HOTKEYS.ALPHANUMERIC.split(''),
            description: 'Any alphanumeric key to search the tree',
            callback: (event, hotkey) => {
                if (_.contains(event.target.classList, 'jstree-anchor')) {
//                    var key = hotkey.format()[0];
                    var key = String.fromCharCode(event.which);
                    this._activateSearch(key);
                }
            }
        });

    }

}