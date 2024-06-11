package com.issue.importer.service;

import com.issue.importer.configuration.IssueDataTestConfig;
import com.issue.importer.domain.ApplicationSettings;
import com.issue.importer.io.props.PropertiesReadingException;
import com.issue.importer.io.props.SettingsReader;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@SpringBootTest(classes = AppSettingsService.class)
@Import(IssueDataTestConfig.class)
class AppSettingsServiceTest implements WithAssertions {

    @Autowired
    private SettingsReader reader;
    private AppSettingsService settingsService;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("project.access.token", () -> "token");
    }

    @BeforeEach
    void setUp() {
        settingsService = new AppSettingsService(reader);
    }

    @Test
    void testNotExistingInputFile() {
        Assertions.assertThrows(PropertiesReadingException.class, () -> settingsService.readApplicationSettings(null));
    }

    @Test
    void testEmptyFileSettingsReading() throws IOException {
        ApplicationSettings settings = getApplicationSettings("/settings/empty.properties");

        // access token read from dynamic property source, hence not empty
        assertThat(settings.accessToken()).isNotEmpty();
        assertThat(settings.projectId()).isEmpty();
        assertThat(settings.csvType()).isEmpty();
        assertThat(settings.delimiter()).isEmpty();
    }

    @Test
    void testNonemptyFileSettingsReading() throws IOException {
        ApplicationSettings settings = getApplicationSettings("/settings/project.properties");

        // access token read from dynamic property source, hence not empty
        assertThat(settings.accessToken()).isNotEmpty();
        assertThat(settings.projectId()).isEqualTo("31643739");
        assertThat(settings.csvType()).isEqualTo("ClearQuest");
        assertThat(settings.delimiter()).isEqualTo(";");
    }

    private ApplicationSettings getApplicationSettings(String path) throws IOException {
        File file = new ClassPathResource(path).getFile();
        FileInputStream fis = new FileInputStream(file);
        MultipartFile multipartFile = Mockito.mock(MultipartFile.class);
        Mockito.when(multipartFile.getInputStream()).thenReturn(fis);

        return settingsService.readApplicationSettings(multipartFile);
    }
}