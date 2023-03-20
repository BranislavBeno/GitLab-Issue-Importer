package com.issue.importer.controller;

import com.issue.importer.configuration.IssueDataTestConfig;
import com.issue.importer.domain.ApplicationSettings;
import com.issue.importer.io.props.SettingsReader;
import com.issue.importer.service.AppSettingsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;

import static com.codeborne.selenide.Selenide.$;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Import(IssueDataTestConfig.class)
class UploadIssuesTest extends AbstractControllerTest {

    @Autowired
    @Qualifier("Test")
    private SettingsReader reader;

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

    private void uploadIssuesFile(String path) {
        ApplicationSettings settings = getApplicationSettings();
        $("#url").setValue("https://gitlab.com");
        $("#projectId").setValue("31643739");
        $("#accessToken").setValue(settings.accessToken());
        $("#csvDelimiter").setValue(";");
        // upload the file
        File file = $("#csvFile").uploadFromClasspath(path);
        assertThat(file).exists();
        // process the file
        $("#uploadIssues > div > button").click();
    }

    private ApplicationSettings getApplicationSettings() {
        try {
            AppSettingsService settingsService = new AppSettingsService(reader);
            File file = new ClassPathResource("/settings/project.properties").getFile();
            FileInputStream fis = new FileInputStream(file);
            MultipartFile multipartFile = mock(MultipartFile.class);
            when(multipartFile.getInputStream()).thenReturn(fis);
            return settingsService.readApplicationSettings(multipartFile);
        } catch (Exception e) {
            throw new RuntimeException("Properties reading has failed.", e);
        }
    }
}
