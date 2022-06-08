package com.issue.importer.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.FileNotFoundException;

import static org.junit.jupiter.api.Assertions.assertThrows;

class CsvItemTest extends AbstractCsvItem {

    @Test
    void testNonExistingFile() {
        assertThrows(FileNotFoundException.class, () -> readIssueData("/no_file.csv", "", ""));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "UNKNOWN"})
    void testUnknownType(String type) {
        assertThrows(IllegalArgumentException.class, () -> readIssueData("/users.csv", type, ""));
    }
}
