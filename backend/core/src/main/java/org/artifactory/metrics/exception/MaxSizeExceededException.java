package org.artifactory.metrics.exception;

/**
 *  Signifies the resource a {@link org.apache.http.HttpStatus#SC_REQUEST_TOO_LONG} status code should be returned
 *
 * @author Dan Feldman
 */
public class MaxSizeExceededException extends Exception{

    public MaxSizeExceededException(String message) {
        super(message);
    }
}
