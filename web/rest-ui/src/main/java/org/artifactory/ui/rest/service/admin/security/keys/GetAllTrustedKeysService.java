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

import com.google.common.collect.Lists;
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
import java.util.List;

/**
 * @author Inbar Tal
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetAllTrustedKeysService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(GetAllTrustedKeysService.class);

    @Autowired
    private AddonsManager addonsManager;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        KeysAddon addon = addonsManager.addonByType(KeysAddon.class);
        List<TrustedKey> trustedKeys = addon.findAllTrustedKeys();

        List<TrustedKeyIModel> foundTrustedKeys = Lists.newArrayList();
        trustedKeys.forEach(trustedKey -> foundTrustedKeys.add(createTrustedKeyIModel(trustedKey)));

        try {
            response.iModel(JacksonWriter.serialize(foundTrustedKeys));
        } catch (IOException e) {
            log.error("Error occurred while getting all trusted keys", e);
            setError(response);
        }
    }

    private void setError(RestResponse response) {
        response.error("Error occurred while getting all trusted keys");
        response.responseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    private TrustedKeyIModel createTrustedKeyIModel(TrustedKey trustedKey) {
        TrustedKeyIModel trustedKeyIModel = new TrustedKeyIModel();
        trustedKeyIModel.setAlias(trustedKey.getAlias());
        trustedKeyIModel.setKid(trustedKey.getKid());
        trustedKeyIModel.setExpiry(trustedKey.getExpiry());
        trustedKeyIModel.setFingerprint(trustedKey.getFingerprint());
        trustedKeyIModel.setIssued(trustedKey.getIssued());
        trustedKeyIModel.setIssuedBy(trustedKey.getIssuedBy());
        return trustedKeyIModel;
    }

}






