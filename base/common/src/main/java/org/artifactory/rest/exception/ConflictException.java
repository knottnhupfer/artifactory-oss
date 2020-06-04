package org.artifactory.rest.exception;

/**
 * Represents a REST API client error request which will get responded by a 409 HTTP status code.
 * {@see ConflictExceptionMapper}
 *
 * @author Dan Feldman
 */
public class ConflictException extends RuntimeException  {

    public ConflictException(String message) {
        super(message);
    }
}
