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
class jfInputTextV2Controller {

    constructor($element, $scope, $compile,$timeout) {
        this.$element = $element;
        this.$scope = $scope;
        this.$compile = $compile;
        this.$timeout=$timeout;
    }

    $onInit() {
        this._initInput();
    }

    _initInput() {
        if (this.ismandatory) {
            $(this.$element.find('input')[0]).attr('required','');
            this.$compile(this.$element.find('input'))(this.$scope);
        }
        if (this.autofocus) {
            this.$timeout($(this.$element.find('input')[0]).focus());

        }
    }

    isInputEmpty() {
        return this.model !== "" &&
                this.model !== undefined &&
                this.model !== null;

    }
}

export function jfInputTextV2() {
    return {
        scope: {
            type: '@',
            name: '@',
            text: '@',
            autocomplete:'@',
            autofocus: '=',
            ismandatory: '=',
            model: '=',
            form: '='

        },
        controller: jfInputTextV2Controller,
        controllerAs: 'jfInputTextV2',
        templateUrl: 'directives/jf_input_text_v2/jf_input_text_v2.html',
        terminal:true,
        priority:1000,
        bindToController: true

    }
}