package com.issue.importer.service;

import com.issue.importer.domain.ApplicationSettings;
import com.issue.importer.domain.IssueData;
import com.issue.importer.io.csv.CsvReadingException;
import com.issue.importer.io.csv.DataReader;
import com.issue.importer.webclient.IssueWebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record IssueTrackingService(IssueWebClient issueWebClient, DataReader dataReader) {

    private static final Logger LOGGER = LoggerFactory.getLogger(IssueTrackingService.class);

    private List<IssueData> readCsvData(ApplicationSettings settings, MultipartFile file) {
        try {
            return dataReader.readCsvData(settings, file);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw new CsvReadingException("Csv file reading failed.");
        }
    }

    private List<IssueData> fetchIssueData(ApplicationSettings settings) {
        try {
            return issueWebClient.fetchIssues(settings);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw new IssueFetchingException("Issues fetching from GitLab failed.");
        }
    }

    private List<IssueData> importIssues(ApplicationSettings settings, List<IssueData> issueDataList) {
        try {
            return issueWebClient.importIssues(settings, issueDataList);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw new IssueImportException("Issues import into GitLab failed.");
        }
    }

    public ResultData importIssueData(ApplicationSettings settings, MultipartFile file) {
        // fetch data from csv
        List<IssueData> csvData = readCsvData(settings, file);
        LOGGER.info("Issue resource file '{}' was read successfully", file.getOriginalFilename());
        // fetch existing issues
        List<IssueData> issueData = fetchIssueData(settings);
        LOGGER.info("{} issues are currently in the backlog", issueData.size());
        // get new data to ignore
        List<IssueData> dataToIgnore = getDataToIgnore(csvData, issueData);
        LOGGER.info("{} issues in resource file are duplicate and will not be imported", dataToIgnore.size());
        // get new data to import
        List<IssueData> dataToImport = getDataToImport(csvData, issueData);
        LOGGER.info("{} issues in resource file are new", dataToImport.size());
        // import data and get result
        List<IssueData> result = importIssues(settings, dataToImport);
        LOGGER.info("{} issues were successfully imported", result.size());

        return new ResultData(result, dataToIgnore);
    }

    private List<IssueData> getDataToImport(List<IssueData> csvData, List<IssueData> issueData) {
        List<String> issueTitles = issueData.stream().map(IssueData::title).toList();

        return csvData.stream()
                .filter(c -> !issueTitles.contains(c.title()))
                .toList();
    }

    private List<IssueData> getDataToIgnore(List<IssueData> csvData, List<IssueData> issueData) {
        List<String> csvTitles = csvData.stream().map(IssueData::title).toList();

        return issueData.stream()
                .filter(i -> csvTitles.contains(i.title()))
                .toList();
    }
}
