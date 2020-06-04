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
export class AdminController {

    constructor($state, AdminMenuItems, ArtifactoryState, User, ArtifactoryFeatures) {
        this.items = AdminMenuItems;
        this.state = $state;
        this.user = User.getCurrent();
        this.artifactoryState = ArtifactoryState;
        this.features = ArtifactoryFeatures;
        this._goToSpecificAdminState();
    }

    _goToSpecificAdminState() {
        if (this.state.current.name !== 'admin') {
            if (!this.state.current.name.match(/(?:.new|.edit)\b/)) {
                this.artifactoryState.setState('lastAdminState', this.state.current);
                this.artifactoryState.setState('lastAdminStateParams', this.state.params);
            }
            return;
        }
        
        let state = this.artifactoryState.getState('lastAdminState');
        let stateParams = this.artifactoryState.getState('lastAdminStateParams');
        let feature = state && state.params && state.params.feature;
        if (!state ||
            !this.user.canView(state.name) ||
            this.features.isDisabled(feature) ||
            this.features.isHidden(feature)) {
            state = this.user.isAdmin() ? 'admin.repositories.list' : 'admin.security.permissions';
            stateParams = this.user.isAdmin() ? {repoType: 'local'} : {};
        }
        this.state.go(state, stateParams);
    }

    isDashboard() {
        return this.state.$current.includes['admin.dashboard']
    }

}