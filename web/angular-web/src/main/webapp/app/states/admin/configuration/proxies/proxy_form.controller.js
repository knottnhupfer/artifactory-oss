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
import TOOLTIP from '../../../../constants/artifact_tooltip.constant';

let $state, $stateParams, ProxiesDao, JFrogModal, ArtifactoryModelSaver;

export class AdminConfigurationProxyFormController {

    constructor(_$state_, _$stateParams_, _ProxiesDao_, _JFrogModal_, _ArtifactoryModelSaver_) {
        ProxiesDao = _ProxiesDao_;
        $stateParams = _$stateParams_;
        $state = _$state_;
        JFrogModal = _JFrogModal_;
        ArtifactoryModelSaver = _ArtifactoryModelSaver_.createInstance(this,['proxy']);;


        this.isNew = !$stateParams.proxyKey;
        this.formTitle = `${this.isNew && 'New' || 'Edit ' + $stateParams.proxyKey } Proxy`;
        this.TOOLTIP = TOOLTIP.admin.configuration.proxyForm;
        this._initProxy();
    }

    _initProxy() {
        if (this.isNew) {
            this.proxy = {};
        }
        else {
            ProxiesDao.getSingleProxy({key: $stateParams.proxyKey}).$promise
                .then((proxy) => {
                        this.proxy = proxy;
                        this.proxy.redirectedToHostsArray = this.proxy.redirectedToHosts ? this.proxy.redirectedToHosts.split(',') : [];
                        ArtifactoryModelSaver.save();
                    });
        }
    }

    onChangeDefault() {
        if (!this.proxy.defaultProxy) return;
        JFrogModal.confirm('Do you wish to use this proxy with existing remote repositories (and override any assigned proxies)?',
                '',
                {confirm: "OK"})
            .catch(() => this.proxy.defaultProxy = false);
    }

    save() {
        if (this.savePending) return;

        this.savePending = true;

        this.proxy.redirectedToHosts = this.proxy.redirectedToHostsArray ? this.proxy.redirectedToHostsArray.join(',') : undefined;

        let whenSaved = this.isNew ? ProxiesDao.save(this.proxy) : ProxiesDao.update(this.proxy);
        whenSaved.$promise.then(() => {
            ArtifactoryModelSaver.save();
            this._end()
            this.savePending = false;
        }).catch(()=>this.savePending = false);
    }

    cancel() {
        this._end();
    }

    _end() {
        $state.go('^.proxies');
    }
}