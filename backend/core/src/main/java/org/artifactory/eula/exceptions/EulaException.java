package org.artifactory.eula.exceptions;

public class EulaException extends RuntimeException {

    public EulaException(String message) {
        super(message);
    }

    public EulaException(String message, Throwable cause) {
        super(message, cause);
    }

}
