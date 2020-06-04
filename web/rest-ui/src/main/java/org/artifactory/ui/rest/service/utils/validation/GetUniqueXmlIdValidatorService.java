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

package org.artifactory.ui.rest.service.utils.validation;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.exception.ValidationException;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Validations service for unique central config xml name.
 *
 * @author Yossi Shaul
 * @see UniqueXmlIdValidator
 */
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class GetUniqueXmlIdValidatorService implements RestService {

    @Autowired
    private CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String id = request.getQueryParamByKey("id");
        try {
            new UniqueXmlIdValidator(centralConfigService.getMutableDescriptor()).validate(id);
            response.info("Unique id validated");
        } catch (ValidationException e) {
            response.error(e.getMessage());
        }
    }
}
