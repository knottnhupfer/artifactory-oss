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

package org.artifactory.update.security;

import org.artifactory.update.security.v1.AclsConverter;
import org.artifactory.update.security.v1.UserPermissionsConverter;
import org.artifactory.update.security.v2.RepoPathAclConverter;
import org.artifactory.update.security.v2.SimpleUserConverter;
import org.artifactory.update.security.v3.AclRepoKeysConverter;
import org.artifactory.update.security.v3.AnyRemoteConverter;
import org.artifactory.update.security.v4.AnnotatePermissionXmlConverter;
import org.artifactory.update.security.v6.LdapGroupSettingXmlConverter;
import org.artifactory.update.security.v6.LowercaseUsernameXmlConverter;
import org.artifactory.update.security.v8.Md5TemplateHashPasswordConverter;
import org.artifactory.version.ArtifactoryVersion;
import org.artifactory.version.ArtifactoryVersionProvider;
import org.artifactory.version.VersionWrapper;
import org.artifactory.version.XmlConverterUtils;
import org.artifactory.version.converter.XmlConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author freds
 * @date Nov 9, 2008
 */
public enum SecurityVersion implements VersionWrapper {
    unsupported(ArtifactoryVersionProvider.v122rc0.get()) {

        @Override
        public String convert(String in) {
            throw new IllegalStateException(
                    "Reading security data from backup of Artifactory version older than 1.2.5-rc6 is not supported!");
        }
    },
    v1(ArtifactoryVersionProvider.v125.get(), new UserPermissionsConverter(), new AclsConverter()),
    v2(ArtifactoryVersionProvider.v130beta1.get(),
            new SimpleUserConverter(), new RepoPathAclConverter()),
    v3(ArtifactoryVersionProvider.v130beta3.get(), new AnyRemoteConverter(), new AclRepoKeysConverter()),
    v4(ArtifactoryVersionProvider.v210.get(), new AnnotatePermissionXmlConverter()),
    v5(ArtifactoryVersionProvider.v211.get()),
    // due to a bug 2.1.2 was released with new security version that is the same as 4
    v6(ArtifactoryVersionProvider.v213.get(), new LdapGroupSettingXmlConverter(),
            new LowercaseUsernameXmlConverter()),
    v7(ArtifactoryVersionProvider.v220.get()),
    v8(ArtifactoryVersionProvider.v265.get(), new Md5TemplateHashPasswordConverter()),
    v9(ArtifactoryVersionProvider.v560m001.get()),
    v10(ArtifactoryVersionProvider.v660m001.get());

    private static final String VERSION_ATT = "version=\"";

    private ArtifactoryVersion version;
    private final XmlConverter[] xmlConverters;

    /**
     * Represents Artifactory security version. For each change in the security files new security version is created.
     *
     * @param version          Artifactory version this security version started at
     * @param xmlConverters List of converters needed to convert the security.xml of this version to the next
     *                      one
     */
    SecurityVersion(ArtifactoryVersion version, XmlConverter... xmlConverters) {
        this.version = version;
        this.xmlConverters = xmlConverters;
    }

    public boolean isLast() {
        return this == SecurityVersion.values()[SecurityVersion.values().length-1];
    }

    public XmlConverter[] getXmlConverters() {
        return xmlConverters;
    }

    public String convert(String securityXmlAsString) {
        // First create the list of converters to apply
        List<XmlConverter> converters = new ArrayList<>();

        // All converters of versions above me needs to be executed in sequence
        SecurityVersion[] versions = SecurityVersion.values();
        for (SecurityVersion version : versions) {
            if (version.ordinal() >= ordinal() && version.getXmlConverters() != null) {
                converters.addAll(Arrays.asList(version.getXmlConverters()));
            }
        }

        return XmlConverterUtils.convert(converters, securityXmlAsString);
    }

    public static SecurityVersion last() {
        SecurityVersion[] versions = SecurityVersion.values();
        for (SecurityVersion version : versions) {
            if (version.isLast()) {
                return version;
            }
        }
        throw new IllegalStateException("Should have a current version!");
    }

    @Override
    public ArtifactoryVersion getVersion() {
        return version;
    }

    public static SecurityVersion findVersion(String securityData) {
        // Version exists since v4
        int versionIdx = securityData.indexOf(VERSION_ATT);
        if (versionIdx != -1) {
            int versionStartIndex = versionIdx + VERSION_ATT.length();
            String cutVersion = securityData.substring(versionStartIndex);
            String isolatedVersion = cutVersion.substring(0, cutVersion.indexOf('"'));
            return valueOf(isolatedVersion);
        } else {
            // Hack to find old versions
            int groupsIdx = securityData.indexOf("<groups>");
            int userIdx = securityData.indexOf("<user>");
            int simpleUserIdx = securityData.indexOf("SimpleUser>");
            int aclIdIdx = securityData.indexOf("<aclObjectIdentity");
            if (userIdx != -1 || groupsIdx != -1) {
                return v3;
            }
            if (aclIdIdx != -1) {
                return v1;
            }
            if (simpleUserIdx != -1) {
                return v2;
            }
            return unsupported;
        }
    }
}
