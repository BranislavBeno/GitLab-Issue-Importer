package com.issue.importer.service;

public class CsvImportException extends RuntimeException {

    public CsvImportException() {
        super();
    }

    public CsvImportException(String message, Throwable cause) {
        super(message, cause);
    }
}
