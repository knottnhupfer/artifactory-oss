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

package org.artifactory.aql.model;

/**
 * @author Gidi Shabat
 */
public enum AqlTableFieldsEnum {
    modified,
    created,
    updated,
    node_name,
    node_path,
    entry_path,
    entry_name,
    repo,
    node_type,
    depth,
    node_id,
    bin_length,
    sha1_original,
    sha1_actual,
    md5_original,
    md5_actual,
    sha256,
    repo_path_checksum,
    created_by,
    modified_by,
    last_downloaded,
    last_downloaded_by,
    download_count,
    path,
    origin,
    prop_key,
    prop_value,
    prop_id,
    artifact_name,
    artifact_type,
    dependency_name_id,
    dependency_scopes,
    dependency_type,
    sha1,
    md5,
    artifact_id,
    dependency_id,
    module_name_id,
    ci_url,
    build_name,
    build_number,
    build_date,
    archive_sha1,
    indexed_archives_id,
    name_id,
    entry_name_id,
    path_id,
    entry_path_id,
    build_id,
    module_id,
    status,
    promotion_comment,
    ci_user,
    unknown,
    id,
    name,
    version,
    date_created,
    signature,
    storing_repo,
    type,
    repo_path,
    bundle_id,
}
