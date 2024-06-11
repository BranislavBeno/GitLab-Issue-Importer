package com.issue.importer.service;

import com.issue.importer.domain.ApplicationSettings;
import com.issue.importer.domain.CsvType;
import com.issue.importer.domain.IssueData;
import com.issue.importer.io.csv.CsvDataReader;
import com.issue.importer.io.csv.CsvReadingException;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class CsvDataReaderTest implements WithAssertions {

    @Mock
    private MultipartFile file;
    @InjectMocks
    private CsvDataReader reader;

    @Test
    void testFailingProvideIssueData() {
        ApplicationSettings settings = new ApplicationSettings();
        Assertions.assertThrows(IllegalArgumentException.class, () -> reader.readCsvData(settings, file));
    }

    @Test
    void testNotExistingInputFile() {
        ApplicationSettings settings = new ApplicationSettings("USER", ",");
        Assertions.assertThrows(CsvReadingException.class, () -> reader.readCsvData(settings, null));
    }

    @ParameterizedTest
    @EnumSource(CsvType.class)
    void testProvideIssueData(CsvType csvType) throws IOException {
        Mockito.when(file.getBytes()).thenReturn(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});

        ApplicationSettings settings = new ApplicationSettings(csvType.name(), ";");
        List<IssueData> issueData = reader.readCsvData(settings, file);

        assertThat(issueData).isEmpty();
    }
}