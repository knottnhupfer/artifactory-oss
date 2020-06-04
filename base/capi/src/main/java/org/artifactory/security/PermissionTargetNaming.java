package org.artifactory.security;

/**
 * Defines the way we read and write permission target actions:
 * Backend: (i.e "r","w","bc","bd")                             -> used by v1 api and internally (i.e. to/from Access).
 * Display: (i.e. "read","write","build-create","build-delete") -> used by v2 api.
 * UI:      (i.e. "read","write","buildCreate","buildDelete")   -> used by UI (duh)
 *
 * All possible values defined in {@link ArtifactoryPermission}
 *
 * @author The Illustrious Dan Feldman
 */
public enum  PermissionTargetNaming {

    NAMING_BACKEND, NAMING_DISPLAY, NAMING_UI

}
