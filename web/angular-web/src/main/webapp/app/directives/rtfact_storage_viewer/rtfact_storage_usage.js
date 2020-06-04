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
import SpecialValues from './special_values';

class rtfactStorageUsageController {
    constructor() {
        this.SpecialValues = SpecialValues;
    }

    getPercents() {
        if (!SpecialValues.isSpecialValue(this.total)) return ((this.used / this.total) * 100) + '%';
        else return 'calc(100% - 40px)'
    }
    isStorageFull(){
        return (!SpecialValues.isSpecialValue(this.total)
                && (Math.trunc((this.used / this.total)*100) == 100));
    }
}

export function rtfactStorageUsage() {
    return {
        restrict: 'E',
        scope: {
            total: '=',
            used: '=',
            thresholds: '='
        },
        controller: rtfactStorageUsageController,
        controllerAs: 'StorageUsage',
        templateUrl: 'directives/rtfact_storage_viewer/rtfact_storage_usage.html',
        bindToController: true
    };
}
