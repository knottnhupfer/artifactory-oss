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

package org.artifactory.ui.rest.model.admin.security.ssl;

import org.artifactory.rest.common.model.BaseModel;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * @author Shay Bagants
 */
public class CertificateDetailsModel extends BaseModel {
    private Details subject;
    private Details issuer;
    private BaseInfo certificate;

    public CertificateDetailsModel(Details subject,
            Details issuer, BaseInfo certificate) {
        this.subject = subject;
        this.issuer = issuer;
        this.certificate = certificate;
    }

    public Details getSubject() {
        return subject;
    }

    public void setSubject(Details subject) {
        this.subject = subject;
    }

    public Details getIssuer() {
        return issuer;
    }

    public void setIssuer(Details issuer) {
        this.issuer = issuer;
    }

    public BaseInfo getCertificate() {
        return certificate;
    }

    public void setCertificate(BaseInfo certificate) {
        this.certificate = certificate;
    }

    public static class Details extends BaseModel {
        @JsonProperty("common_name")
        private String commonName;
        private String organization;
        private String unit;

        public Details(String commonName, String organization, String unit) {
            this.commonName = commonName;
            this.organization = organization;
            this.unit = unit;
        }

        public String getCommonName() {
            return commonName;
        }

        public void setCommonName(String commonName) {
            this.commonName = commonName;
        }

        public String getOrganization() {
            return organization;
        }

        public void setOrganization(String organization) {
            this.organization = organization;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }
    }

    public static class BaseInfo extends BaseModel {
        @JsonProperty("issued_on")
        private String issuedOn;
        @JsonProperty("valid_until")
        private String valieUntil;
        private String fingerprint;

        public BaseInfo(String issuedOn, String valieUntil, String fingerprint) {
            this.issuedOn = issuedOn;
            this.valieUntil = valieUntil;
            this.fingerprint = fingerprint;
        }

        public String getIssuedOn() {
            return issuedOn;
        }

        public void setIssuedOn(String issuedOn) {
            this.issuedOn = issuedOn;
        }

        public String getValieUntil() {
            return valieUntil;
        }

        public void setValieUntil(String valieUntil) {
            this.valieUntil = valieUntil;
        }

        public String getFingerprint() {
            return fingerprint;
        }

        public void setFingerprint(String fingerprint) {
            this.fingerprint = fingerprint;
        }
    }


}
