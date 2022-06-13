package com.issue.importer.service;

import com.issue.importer.domain.CsvType;
import com.issue.importer.domain.IssueData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CsvFetchServiceTest {

    @Mock
    private MultipartFile file;
    @InjectMocks
    private CsvFetchService dataService;

    @Test
    void testFailingProvideIssueData() {
        assertThrows(IllegalArgumentException.class, () -> dataService.provideIssueData("", "", file));
    }

    @Test
    void testNotExistingInputFile() {
        assertThrows(CsvReadingException.class, () -> dataService.provideIssueData("USER", ",", null));
    }

    @ParameterizedTest
    @EnumSource(CsvType.class)
    void testProvideIssueData(CsvType csvType) throws IOException {
        InputStream is = mock(InputStream.class);
        when(file.getInputStream()).thenReturn(is);

        List<IssueData> issueData = dataService.provideIssueData(csvType.name(), ";", file);

        assertThat(issueData).isEmpty();
    }
}