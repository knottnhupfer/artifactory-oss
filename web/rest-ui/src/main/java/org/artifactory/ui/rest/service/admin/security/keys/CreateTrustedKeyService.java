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

package org.artifactory.ui.rest.service.admin.security.keys;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.keys.KeysAddon;
import org.artifactory.api.jackson.JacksonWriter;
import org.artifactory.keys.TrustedKey;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.security.keys.TrustedKeyIModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author Inbar Tal
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class CreateTrustedKeyService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(CreateTrustedKeyService.class);

    @Autowired
    private AddonsManager addonsManager;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        TrustedKeyIModel keyIModel = (TrustedKeyIModel)request.getImodel();

        KeysAddon addon = addonsManager.addonByType(KeysAddon.class);
        TrustedKey trustedKey = addon.createTrustedKey(keyIModel.getTrustedKey(), keyIModel.getAlias());

        log.debug("Trusted key with id: {} was created successfully", trustedKey.getKid());

        TrustedKeyIModel trustedKeyIModel = new TrustedKeyIModel();
        populateIModel(trustedKeyIModel, trustedKey);

        try {
            response.iModel(JacksonWriter.serialize(trustedKeyIModel)).responseCode(HttpStatus.CREATED.value());
        } catch (IOException e) {
            log.error("Error occurred while creating trusted key", e);
            setError(response);
        }
    }

    private void setError(RestResponse response) {
        response.error("Error occurred while creating trusted key");
        response.responseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    private void populateIModel(TrustedKeyIModel keyIModel, TrustedKey key) {
        keyIModel.setKid(key.getKid());
        keyIModel.setAlias(key.getAlias());
        keyIModel.setTrustedKey(key.getTrustedKey());
        keyIModel.setIssuedBy(key.getIssuedBy());
        keyIModel.setIssued(key.getIssued());
        keyIModel.setFingerprint(key.getFingerprint());
        keyIModel.setExpiry(key.getExpiry());
    }




}
