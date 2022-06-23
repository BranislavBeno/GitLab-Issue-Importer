package com.issue.importer.service;

import com.issue.importer.domain.ApplicationSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppSettingsServiceTest {

    @InjectMocks
    private AppSettingsService settingsService;

    @Test
    void testNotExistingInputFile() {
        assertThrows(PropertiesImportException.class, () -> settingsService.readApplicationSettings(null));
    }

    @Test
    void testEmptyFileSettingsReading() throws IOException {
        ApplicationSettings settings = getApplicationSettings("/empty.properties");

        assertThat(settings.projectId()).isEmpty();
        assertThat(settings.accessToken()).isEmpty();
        assertThat(settings.csvType()).isEmpty();
        assertThat(settings.delimiter()).isEmpty();
    }

    @Test
    void testNonemptyFileSettingsReading() throws IOException {
        ApplicationSettings settings = getApplicationSettings("/project.properties");

        assertThat(settings.projectId()).isEqualTo("1234");
        assertThat(settings.accessToken()).isEqualTo("ab12cd34");
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