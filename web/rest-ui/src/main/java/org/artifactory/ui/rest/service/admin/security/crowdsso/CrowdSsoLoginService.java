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

package org.artifactory.ui.rest.service.admin.security.crowdsso;

import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.sso.crowd.CrowdAddon;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.security.login.UserLoginSso;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;

/**
 * @author Yoaz Menda
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class CrowdSsoLoginService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(CrowdSsoLoginService.class);

    private AddonsManager addonsManager;

    @Autowired
    public CrowdSsoLoginService(AddonsManager addonsManager) {
        this.addonsManager = addonsManager;
    }

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        UserLoginSso ssoLogin = (UserLoginSso) request.getImodel();
        if (ssoLogin == null || StringUtils.isEmpty(ssoLogin.getSsoToken()) || ssoLogin.getSsoToken().length() < 4) {
            log.debug("SSO cookie must be present");
            response.error("SSO login info must be present");
            response.responseCode(SC_UNAUTHORIZED);
            return;
        }
        if (log.isDebugEnabled()) {
            String cookiePrefix = ssoLogin.getSsoToken().substring(0, 4);
            log.debug("Executing login SSO request with crowd cookie {}",
                    cookiePrefix + StringUtils.repeat("X", ssoLogin.getSsoToken().length() - 4));
        }
        CrowdAddon crowdAddon = addonsManager.addonByType(CrowdAddon.class);
        crowdAddon.loginSso(ssoLogin.getSsoToken(), request.getServletRequest(), response.getServletResponse());
    }
}
