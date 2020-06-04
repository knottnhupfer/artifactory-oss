package org.artifactory.api.config;

import org.artifactory.common.ConstantValues;

/**
 * @author Hezi Cohen
 */
public class ImportExportPathValidator {

    /**
     * insecure paths defined as described in RTFACT-20118
     */
    public static boolean isValidPath(String path) {
        return (ConstantValues.allowExportImportInsecurePaths.getBoolean()
                || (!path.startsWith("$") && !path.startsWith("\\\\") && !path.startsWith("//")));
    }
}
