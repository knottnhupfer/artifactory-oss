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

package org.artifactory.ui.rest.service.admin.security.ssl;

import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.webstart.ArtifactWebstartAddon;
import org.artifactory.addon.webstart.KeyStoreNotFoundException;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.rest.common.util.RestUtils;
import org.artifactory.ui.rest.model.admin.security.ssl.CertificateDetailsModel;
import org.jfrog.security.ssl.CertificateHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import static org.artifactory.ui.rest.model.admin.security.ssl.CertificateDetailsModel.BaseInfo;
import static org.artifactory.ui.rest.model.admin.security.ssl.CertificateDetailsModel.Details;

/**
 * @author Shay Bagants
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetCertificateDetails implements RestService {
    private static final Logger log = LoggerFactory.getLogger(GetAllCertificateDataService.class);

    @Autowired
    private AddonsManager addonsManager;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String certificateAlias = request.getQueryParamByKey("certificate_alias");
        if (StringUtils.isBlank(certificateAlias)) {
            response.error("Invalid request. Certificate name cannot be empty.").responseCode(400);
            return;
        }
        ArtifactWebstartAddon webstartAddon = addonsManager.addonByType(ArtifactWebstartAddon.class);
        String internalAlias = ArtifactWebstartAddon.SSL_CERT_ALIAS_PREFIX + certificateAlias;
        try {
            KeyStore keystore = webstartAddon.getExistingKeyStore();
            String keystorePassword = webstartAddon.getKeystorePassword();
            if (keystore == null || StringUtils.isBlank(keystorePassword)) {
                log.warn("Unable to access certificate information, Keystore not found.");
                response.error("Key not found.").responseCode(400);
                return;
            }
            Certificate certificate = keystore.getCertificate(internalAlias);
            if (certificate != null) {
                X509Certificate x509cert = (X509Certificate) certificate;
                // issuer
                String issuerCn = CertificateHelper.getCertificateIssuerCommonName(x509cert);
                String issuerOu = CertificateHelper.getCertificateIssuerOrganizationUnit(x509cert);
                String issuerO = CertificateHelper.getCertificateIssuerOrganization(x509cert);
                Details issuer = new Details(issuerCn, issuerO, issuerOu);
                // subject
                String subjectCn = CertificateHelper.getCertificateSubjectCommonName(x509cert);
                String subjectOu = CertificateHelper.getCertificateSubjectOrganizationUnit(x509cert);
                String subjectO = CertificateHelper.getCertificateSubjectOrganization(x509cert);
                Details subject = new Details(subjectCn, subjectO, subjectOu);
                // certificate
                String fingerprint = CertificateHelper.getCertificateFingerprint(x509cert);
                String validUntil = RestUtils.toIsoDateString(CertificateHelper.getValidUntil(x509cert).getTime());
                String issuedAt = RestUtils.toIsoDateString(CertificateHelper.getIssuedAt(x509cert).getTime());
                BaseInfo baseInfo = new BaseInfo(issuedAt, validUntil, fingerprint);
                CertificateDetailsModel model = new CertificateDetailsModel(subject, issuer, baseInfo);
                response.iModel(model);
            }
        } catch (KeyStoreNotFoundException e) {
            log.error("Unable to load keystore. {}", e.getMessage());
            log.debug("Unable to load keystore.", e);
            response.error("Failed to retrieve certificate information, see logs for further details.")
                    .responseCode(400);
        } catch (KeyStoreException e) {
            log.error("Unable to retrieve keystore information for: '{}'. {}", certificateAlias, e.getMessage());
            log.debug("Unable to retrieve keystore information for: '{}'.", internalAlias, e);
            response.error("Failed to retrieve certificate information: certificate alias not found").responseCode(400);
        } catch (CertificateEncodingException e) {
            log.error("Unable to read certificate data for '{}'. {}", certificateAlias, e.getMessage());
        }
    }
}
