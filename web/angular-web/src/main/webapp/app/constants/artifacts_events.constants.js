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
let events = {
    ACTIVATE_TREE_SEARCH:   'tree:search:activate',
    TREE_SEARCH_CHANGE:     'tree:search:change',
    TREE_NODE_SELECT:       'tree:node:select',
    TREE_NODE_OPEN:         'tree:node:open',
    TREE_SEARCH_KEYDOWN:    'tree:search:keydown',
    TREE_SEARCH_CANCEL:     'tree:search:cancel',
    TREE_SEARCH_EDIT:       'tree:search:edit',
    TREE_SEARCH_RUNNING:    'tree:search:running',
    TREE_COMPACT:           'tree:compact',
    TREE_REFRESH:           'tree:refresh',
    TREE_REFRESH_SORTING:   'tree:refresh:sorting',
    TREE_REFRESH_FILTER:   'tree:refresh:filter',
    TREE_REFRESH_FAVORITES:   'tree:refresh:favorites',
    TREE_NODE_CM_REFRESH:   'tree:node:cm:refresh',
    TREE_COLLAPSE_ALL:      'tree:collapse:all',
    TREE_DATA_IS_SET:       'tree:hasdata',
    SEARCH_COLLAPSE:        'search:collapse',
    SEARCH:                 'search',
    CLEAR_SEARCH:           'search:clear',

    ACTION_WATCH:           'action:watch',    // node
    ACTION_UNWATCH:         'action:unwatch',  // node
    ACTION_COPY:            'action:copy',     // node, target
    ACTION_MOVE:            'action:move',     // node, target
    ACTION_COPY_STASH:            'action:copystash',     // repoKey
    ACTION_MOVE_STASH:            'action:movestash',     // repoKey
    ACTION_DISCARD_STASH:         'action:discardstash',     //
    ACTION_DISCARD_FROM_STASH:         'action:discardfromstash',     //node
    ACTION_REFRESH_STASH:         'action:refreshstash',     //
    ACTION_EXIT_STASH:         'action:exitstash',     //
    ACTION_DELETE:          'action:delete',   // node
    ACTION_DELETE_CONTENT:          'action:delete:content',   // node
    ACTION_REFRESH:         'action:refresh',   // node
    ACTION_DEPLOY:         'action:deploy',   // repoKey
    ACTION_FAVORITES:    'action:markfavorites',
    DO_FAVORITES:    'action:dofavorites',

    BUILDS_TAB_REFRESH:     'builds:tab:refresh',

    FOOTER_DATA_UPDATED: 'footer:data:updated',

    SHOW_SPINNER: 'spinner:show',
    HIDE_SPINNER: 'spinner:hide',
    CANCEL_SPINNER: 'spinner:cancel',

    USER_CHANGED:           'user:changed',
    USER_LOGOUT:            'user:logout', //confirmDiscard (true/false)

    TAB_NODE_CHANGED:       'tabs:node:changed',

    SEARCH_URL_CHANGED:     'search:url:changed',

    FOOTER_REFRESH:         'footer:refresh',

    REFRESH_SETMEUP_WIZARD:  'refresh:setmeup:wizard',

    REFRESH_PAGE_CONTENT:  'refresh:page:content',
};

export default events;
