package org.artifactory.security;

/**
 * This interface is the parent interface for all ACLs that are of Repo type.
 * Repository, builds and release bundle permissions are determined by this base and have the same functionality.
 *
 * @author Yuval Reches
 */
interface BaseRepoAcl<T extends RepoPermissionTarget> extends Acl<T> {

}
