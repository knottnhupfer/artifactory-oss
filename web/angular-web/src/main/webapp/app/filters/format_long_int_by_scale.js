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
export function FormatLongIntByScale() {
	return function(num) {
		let ONE_K = 1000, ONE_MILLION = 1000000, ONE_BILLION = 1000000000;
		if(Math.round(num / ONE_K) > 0 && Math.round(num / ONE_MILLION) <= 0) {
            return (num / ONE_K).toFixed(1) +  ' K';
		}
		if(Math.round(num / ONE_MILLION) > 0 && Math.round(num / ONE_BILLION) <= 0) {
			return (num / ONE_MILLION).toFixed(2) +  ' M';
		}
		if(Math.round(num / ONE_BILLION) > 0) {
			return (num / ONE_BILLION).toFixed(2) +  ' B';
		}
		return num;
	}
}