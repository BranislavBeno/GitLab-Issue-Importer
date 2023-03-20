package com.issue.importer.io.props;

public class PropertiesReadingException extends RuntimeException {

    public PropertiesReadingException(String message) {
        super(message);
    }

    public PropertiesReadingException(String message, Throwable cause) {
        super(message, cause);
    }
}
