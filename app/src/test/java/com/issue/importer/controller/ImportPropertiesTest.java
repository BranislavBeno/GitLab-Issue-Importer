package com.issue.importer.controller;

import com.codeborne.selenide.Selenide;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;

class ImportPropertiesTest extends AbstractControllerTest implements WithAssertions {

    @Override
    String getPagePath() {
        return "/";
    }

    @Test
    @DisplayName("PROPERTIES file: Upload non existing file")
    void testUploadNonExistingProperties() {
        Selenide.$("#importSettings > div > button").click();
        assertThat(Selenide.$(".alert > span").text()).isEqualTo("Please select a PROPERTIES file to upload.");

        takeScreenshot("uploadNonExistingProperties");
    }

    @Test
    @DisplayName("PROPERTIES file: Upload empty file")
    void testUploadEmptyProperties() {
        uploadPropertiesFile("settings/empty.properties");
        assertThat(Selenide.$(".alert > span").text()).isEqualTo("PROPERTIES file is empty.");

        takeScreenshot("uploadEmptyProperties");
    }

    @Test
    @DisplayName("PROPERTIES file: Upload complying file")
    void testUploadProperties() {
        uploadPropertiesFile("settings/project.properties");
        assertThat(Selenide.$("#uploadIssues").exists()).isTrue();

        takeScreenshot("uploadProperties");
    }

    private void uploadPropertiesFile(String path) {
        // upload the file
        File file = Selenide.$("#propFile").uploadFromClasspath(path);
        assertThat(file).exists();
        // process the file
        Selenide.$("#importSettings > div > button").click();
    }
}