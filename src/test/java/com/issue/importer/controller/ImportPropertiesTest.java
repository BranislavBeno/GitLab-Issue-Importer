package com.issue.importer.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;

import static com.codeborne.selenide.Selenide.$;
import static org.assertj.core.api.Assertions.assertThat;

class ImportPropertiesTest extends AbstractControllerTest {

    @Override
    String getPagePath() {
        return "/";
    }

    @Test
    @DisplayName("PROPERTIES file: Upload non existing file")
    void testUploadNonExistingProperties() {
        $("#importSettings > div > button").click();
        assertThat($(".alert > span").text()).isEqualTo("Please select a PROPERTIES file to upload.");

        takeScreenshot("uploadNonExistingProperties");
    }

    @Test
    @DisplayName("PROPERTIES file: Upload empty file")
    void testUploadEmptyProperties() {
        uploadPropertiesFile("settings/empty.properties");
        assertThat($(".alert > span").text()).isEqualTo("PROPERTIES file is empty.");

        takeScreenshot("uploadEmptyProperties");
    }

    @Test
    @DisplayName("PROPERTIES file: Upload complying file")
    void testUploadProperties() {
        uploadPropertiesFile("settings/project.properties");
        assertThat($("#uploadIssues").exists()).isTrue();

        takeScreenshot("uploadProperties");
    }

    private static void uploadPropertiesFile(String path) {
        // upload the file
        File file = $("#propFile").uploadFromClasspath(path);
        assertThat(file).exists();
        // process the file
        $("#importSettings > div > button").click();
    }
}