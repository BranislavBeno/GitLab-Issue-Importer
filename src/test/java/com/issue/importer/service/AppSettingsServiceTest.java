package com.issue.importer.service;

import com.issue.importer.domain.ApplicationSettings;
import com.issue.importer.io.props.PropertiesReadingException;
import com.issue.importer.io.props.PropsSettingsReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AppSettingsServiceTest {

    private AppSettingsService settingsService;

    @BeforeEach
    void setUp() {
        settingsService = new AppSettingsService(new PropsSettingsReader());
    }

    @Test
    void testNotExistingInputFile() {
        assertThrows(PropertiesReadingException.class, () -> settingsService.readApplicationSettings(null));
    }

    @Test
    void testEmptyFileSettingsReading() throws IOException {
        ApplicationSettings settings = getApplicationSettings("/settings/empty.properties");

        assertThat(settings.projectId()).isEmpty();
        assertThat(settings.accessToken()).isEmpty();
        assertThat(settings.csvType()).isEmpty();
        assertThat(settings.delimiter()).isEmpty();
    }

    @Test
    void testNonemptyFileSettingsReading() throws IOException {
        ApplicationSettings settings = getApplicationSettings("/settings/project.properties");

        assertThat(settings.projectId()).isEqualTo("31643739");
        assertThat(settings.accessToken()).isEqualTo("glpat-pAvB2p8-r8XxV1vKaFEB");
        assertThat(settings.csvType()).isEqualTo("User");
        assertThat(settings.delimiter()).isEqualTo(",");
    }

    private ApplicationSettings getApplicationSettings(String path) throws IOException {
        File file = new ClassPathResource(path).getFile();
        FileInputStream fis = new FileInputStream(file);
        MultipartFile multipartFile = mock(MultipartFile.class);
        when(multipartFile.getInputStream()).thenReturn(fis);

        return settingsService.readApplicationSettings(multipartFile);
    }
}