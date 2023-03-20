package com.issue.importer.service;

import com.issue.importer.domain.ApplicationSettings;
import com.issue.importer.domain.IssueData;
import com.issue.importer.io.csv.DataReader;
import com.issue.importer.webclient.IssueWebClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IssueTrackingServiceTest {

    @Mock
    private IssueWebClient webClient;
    @Mock
    private DataReader reader;
    @Mock
    private ApplicationSettings settings;
    @Mock
    private MultipartFile file;
    @InjectMocks
    private IssueTrackingService service;

    @Test
    void testNotProvidingCsvData() {
        when(reader.readCsvData(any(ApplicationSettings.class), any(MultipartFile.class))).thenThrow(RuntimeException.class);
        assertThrows(Exception.class, () -> service.importIssueData(any(ApplicationSettings.class), any(MultipartFile.class)));
    }

    @Test
    void testNotFetchingIssueData() {
        when(webClient.fetchIssues(any(ApplicationSettings.class))).thenThrow(RuntimeException.class);
        assertThrows(IssueFetchingException.class, () -> service.importIssueData(settings, file));
    }

    @Test
    void testNotImportingIssueData() {
        when(webClient.importIssues(settings, List.of())).thenThrow(RuntimeException.class);
        assertThrows(IssueImportException.class, () -> service.importIssueData(settings, file));
    }

    @Test
    void testSuccessfulIssueDataProviding() {
        when(reader.readCsvData(any(ApplicationSettings.class), any(MultipartFile.class))).thenReturn(provideCsvData());
        when(webClient.fetchIssues(any(ApplicationSettings.class))).thenReturn(provideIssues());
        when(webClient.importIssues(settings, importIssues())).thenReturn(importIssues());

        ResultData results = service.importIssueData(settings, file);

        List<IssueData> newData = results.newData();
        assertThat(newData).hasSize(1);
        assertThat(newData.get(0).title()).isEqualTo("Third issue (Origin: 3)");

        List<IssueData> existingData = results.existingData();
        assertThat(existingData).hasSize(1);
        assertThat(existingData.get(0).title()).isEqualTo("Second issue (Origin: 2)");
    }

    private List<IssueData> provideCsvData() {
        return List.of(
                new IssueData("Second issue (Origin: 2)", "## Description"),
                new IssueData("Third issue (Origin: 3)", "## Description"));
    }

    private List<IssueData> provideIssues() {
        return List.of(
                new IssueData("First issue (Origin: 1)", "## Description"),
                new IssueData("Second issue (Origin: 2)", "## Description"));
    }

    private List<IssueData> importIssues() {
        return List.of(
                new IssueData("Third issue (Origin: 3)", "## Description"));
    }
}