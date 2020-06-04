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

import {GLOBAL_KEYBOARD_SHORTCURS, PAGE_DEPENDENT_KEYBOARD_SHORTCUTS} from './keyboard_shortcuts_service.constants';

export default class KeyboardShortcutsModalService {

	constructor(JFrogModal, JFrogEventBus, $rootScope) {
		this.modal = JFrogModal;
		this.$rootScope = $rootScope;
		this.jFrogEventBus = JFrogEventBus;
	}

	/**
	 *  launch the confirmation modal
	 */
	showhSortcutsModal() {
		this.modalScope = this.$rootScope.$new();
		this.modalScope.globalShortcuts = GLOBAL_KEYBOARD_SHORTCURS;
		this.modalScope.pageDependentShortcuts = PAGE_DEPENDENT_KEYBOARD_SHORTCUTS;
		if (!$('.shortcuts-modal').length) {
			this.modalInstance = this.modal.launchModalWithTemplateMarkup(require('./keyboard_shortcuts_service.modal.html'),
				this.modalScope, 800, false);
		}
	}
}