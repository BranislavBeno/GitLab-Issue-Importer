package com.issue.importer.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;

import static com.codeborne.selenide.Selenide.$;
import static org.assertj.core.api.Assertions.assertThat;

class UploadIssuesTest extends AbstractControllerTest {

    @Override
    String getPagePath() {
        return "/upload-properties";
    }

    @Test
    @DisplayName("ISSUE RESOURCES: Upload non existing file")
    void testUploadNonExistingIssues() {
        $("#uploadIssues > div > button").click();
        assertThat($(".alert > span").text()).isEqualTo("Please select an ISSUE RESOURCE file to upload.");

        takeScreenshot("uploadNonExistingIssues");
    }

    @Test
    @DisplayName("ISSUE RESOURCES: Upload empty file")
    void testUploadEmptyIssues() {
        uploadIssuesFile("csv/empty.csv");
        assertThat($(".alert > span").text()).isEqualTo("ISSUE RESOURCE file is empty.");

        takeScreenshot("uploadEmptyIssues");
    }

    @Test
    @DisplayName("ISSUE RESOURCES: Upload only header row file")
    void testUploadOnlyHeaderRowIssues() {
        uploadIssuesFile("csv/only_header_QueryResult.csv");
        assertThat($(".card-header > h5").text()).isEqualTo("File Upload Status");

        takeScreenshot("uploadOnlyHeaderRowIssues");
    }

    @Test
    @DisplayName("ISSUE RESOURCES: Upload complying file")
    void testUploadProperties() {
        uploadIssuesFile("csv/QueryResult.csv");
        assertThat($(".card-header > h5").text()).isEqualTo("File Upload Status");

        takeScreenshot("uploadIssues");
    }

    private static void uploadIssuesFile(String path) {
        $("#url").setValue("https://gitlab.com");
        $("#projectId").setValue("31643739");
        $("#accessToken").setValue("glpat-pAvB2p8-r8XxV1vKaFEB");
        $("#csvDelimiter").setValue(";");
        // upload the file
        File file = $("#csvFile").uploadFromClasspath(path);
        assertThat(file).exists();
        // process the file
        $("#uploadIssues > div > button").click();
    }
}
