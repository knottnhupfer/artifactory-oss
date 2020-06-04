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

export const GLOBAL_KEYBOARD_SHORTCURS = [
	{title:'Go to tree', type:'global', keyCombination: ['Ctrl','Alt','R'], macKeyCombination: ['Cmd','Option','R']},
	{title:'Go to search', type:'global', keyCombination: ['Ctrl','Alt','S'], macKeyCombination: ['Cmd','Option','S']},
	{title:'Go to builds', type:'global', keyCombination: ['Ctrl','Alt','B'], macKeyCombination: ['Cmd','Option','B']},
	{title:'Login / Logout', type:'global', keyCombination: ['Ctrl','Alt','L'], macKeyCombination: ['Cmd','Option','L']},
	{title:'Keyboard shortcuts index', type:'global', keyCombination: ['Ctrl','Alt','/'], macKeyCombination: ['Cmd','Option','/']}
];
export const PAGE_DEPENDENT_KEYBOARD_SHORTCUTS = [
	{title:'Create new entity', type:'pageDependent', keyCombination: ['Ctrl','Alt','N'], macKeyCombination: ['Cmd','Option','N']}
];