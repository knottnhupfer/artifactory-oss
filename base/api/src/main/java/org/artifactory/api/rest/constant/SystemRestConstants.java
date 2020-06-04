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

package org.artifactory.api.rest.constant;

/**
 * @author yoavl
 */
public interface SystemRestConstants {
    String PATH_ROOT = "system";
    String PATH_IMPORT = "import";
    String PATH_EXPORT = "export";
    String PATH_CONFIGURATION = "configuration";
    String PATH_SECURITY = "security";
    String PATH_CERTIFICATES = "certificates";
    String PATH_STORAGE = "storage";
    String PATH_STORAGE_COMPRESS = "compress";
    String PATH_REPOSITORIES = "repositories";
    String PATH_STATUS = "status";
    String PATH_VERSION = "version";
    String PATH_PING = "ping";
    String PATH_METRICS = PATH_ROOT + "/metrics";
    String PATH_ENCRYPT = "encrypt";
    String PATH_DECRYPT = "decrypt";
    String PATH_GC = "gc";
    // Artifactory license
    String PATH_LICENSE = "license";
    String PATH_NEW_LICENSES = "licenses";
    String PATH_REPLICATIONS = "replications";
    String PATH_GATEWAY = PATH_ROOT + "/gateway";
    String PATH_OPENID = PATH_GATEWAY + "/openid";
    String OPENID_LOGIN_RESOURCE = "login";
    String OPENID_LOGOUT_RESOURCE = "logout";
    String OPENID_TOKEN_RESOURCE = "token";
    String OPENID_USERINFO_RESOURCE = "userinfo";
    String OPENID_LOGIN_PATH = PATH_OPENID + "/" + OPENID_LOGIN_RESOURCE;
    String OPENID_LOGOUT_PATH = PATH_OPENID + "/" + OPENID_LOGOUT_RESOURCE;
    String OPENID_TOKEN_PATH = PATH_OPENID + "/" + OPENID_TOKEN_RESOURCE;
    String OPENID_USERINFO_PATH = PATH_OPENID + "/" + OPENID_USERINFO_RESOURCE;
    String PATH_VERIFY_CONNECTION = "verifyconnection";
    String MT_VERSION_RESULT = RestConstants.MT_ARTIFACTORY_APP + PATH_ROOT + ".Version+json";
    String MT_IMPORT_SETTINGS = RestConstants.MT_ARTIFACTORY_APP + PATH_ROOT + ".ImportSettings+json";
    String MT_EXPORT_SETTINGS = RestConstants.MT_ARTIFACTORY_APP + PATH_ROOT + ".ExportSettings+json";
    String PATH_SEARCH = "search";
    String PATH_USAGE = "usage";
    String PATH_METADATA_SERVER = "metadata_server";
    String JFROG_RELEASE = "release";
    String PATH_BINARY_SERVICES = "binary/services";
    String PATH_BLOB = "blob/info";
    String PATH_TRUSTED_KEYS = PATH_SECURITY + "/keys/trusted";
    String BUNDLE_TRANSACTION = "bundle/transaction";
    String BUNDLE_TRANSACTION_CLOSE = "bundle/transaction/close";
    String BUNDLE_TRANSACTION_CLOSE_ASYNC = "bundle/transaction/async/close";
    String BUNDLE_CHECK_CLOSE_STATUS = "bundle/transaction/async/close/status";
    String PATH_CHECKUP = "checkup";
    String SERVICE = "service";
    String SERVICE_INFO = PATH_ROOT + "/" + SERVICE;
    String SERVERS_STATUS = PATH_ROOT + "/nodes";
    String PATH_ACCESS_PROXY = "/api/access/";
    String REGISTRY = "registry";
    String PATH_SIGNED_URLS = "signed/url";
    String PATH_PARAM_SIGNATURE = "sig";
    String PATH_SUPPORT_BUNDLE = "support/bundle";
    String PATH_SUPPORT_BUNDLES = "support/bundles";
}
