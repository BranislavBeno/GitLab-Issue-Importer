package com.issue.importer.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CsvItemTest extends AbstractCsvItem {

    @Test
    void testNonExistingFile() {
        ApplicationSettings settings = new ApplicationSettings();

        Assertions.assertThrows(FileNotFoundException.class, () -> readIssueData(settings, "/csv/no_file.csv"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "UNKNOWN"})
    void testUnknownType(String type) {
        ApplicationSettings settings = new ApplicationSettings(type, "");

        Assertions.assertThrows(IllegalArgumentException.class, () -> readIssueData(settings, "/csv/users.csv"));
    }

    @Test
    void testFileWithHeaderRowButNoData() throws IOException {
        ApplicationSettings settings = new ApplicationSettings("USER", ",");
        List<IssueData> data = readIssueData(settings, "/csv/empty_users.csv");

        assertThat(data).isEmpty();
    }

    @Test
    void testFileWithLessMissingColumn() throws IOException {
        ApplicationSettings settings = new ApplicationSettings("USER", ",");
        List<IssueData> data = readIssueData(settings, "/csv/incomplete_users.csv");

        data.forEach(d -> assertThat(d.description()).contains("countryCode: null"));
    }

    @Test
    void testFileWithImportantMissingColumn() throws IOException {
        ApplicationSettings settings = new ApplicationSettings("USER", ",");
        List<IssueData> data = readIssueData(settings, "/csv/incorrect_users.csv");

        data.forEach(d -> assertThat(d.title()).isEqualTo("null (Origin: 0)"));
    }
}
