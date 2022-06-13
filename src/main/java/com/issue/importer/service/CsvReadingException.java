package com.issue.importer.service;

public class CsvReadingException extends RuntimeException {

    public CsvReadingException(String message, Throwable cause) {
        super(message, cause);
    }
}
