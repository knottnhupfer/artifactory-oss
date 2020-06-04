package org.artifactory.importexport;

public class ImportIsDisabledException extends IllegalStateException {
    public static final String REPOSITORIES_IMPORT_IS_DISABLED_MESSAGE = "Repositories Import is disabled.";
    public static final String SYSTEM_IMPORT_IS_DISABLED_MESSAGE = "System Import is disabled.";

    public ImportIsDisabledException(String message) {
        super(message);
    }

    public static ImportIsDisabledException buildRepositoriesException() {
        return new ImportIsDisabledException(REPOSITORIES_IMPORT_IS_DISABLED_MESSAGE);
    }

    public static ImportIsDisabledException buildSystemException() {
        return new ImportIsDisabledException(SYSTEM_IMPORT_IS_DISABLED_MESSAGE);
    }
}
