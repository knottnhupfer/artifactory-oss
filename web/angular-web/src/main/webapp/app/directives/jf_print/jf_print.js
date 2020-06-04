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
export function jfPrint () {
    return {
        restrict: 'E',
        scope: {
            content: '@'
        },
        controller: jfPrintController,
        controllerAs: 'jfPrint',
        bindToController: true,
        templateUrl: 'directives/jf_print/jf_print.html'
    }
}


class jfPrintController {

    constructor($element, $window, $scope) {
        this.$element = $element;
        this.$window = $window;
        this.$scope = $scope;

        this._registerEvents();
    }

    _registerEvents() {
        this.$element.on('click', () => this.print());
        this.$scope.$on('$destroy', () => this.$element.off('click'));
    }

    print() {
        let printWindow = this.$window.open('', '_blank', 'height=380,width=750');
        printWindow.document.write('<html><head><title>Artifactory</title></head><body >');
        printWindow.document.write('<pre>' + this._escapeHTML(this.content) + '</pre>');
        printWindow.document.write('</body></html>');
        printWindow.print();
        printWindow.close();
        return true;
    }

    _escapeHTML(content) {
        let escape = document.createElement('textarea');
        escape.innerHTML = content;
        return escape.innerHTML;
    }


}
