package com.issue.importer.service;

import com.issue.importer.domain.ApplicationSettings;
import com.issue.importer.domain.CsvRow;
import com.issue.importer.domain.CsvType;
import com.issue.importer.domain.IssueData;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class CsvFetchService {

    public List<IssueData> uploadIssueData(ApplicationSettings settings, MultipartFile file) {
        CsvType csvType = CsvType.valueOf(settings.csvType());
        char csvDelimiter = settings.delimiter().charAt(0);
        List<? extends CsvRow> rows = fetchCsvRows(csvType.getClazz(), file, csvDelimiter);

        return rows.stream()
                .map(r -> new IssueData(r.provideTitle(), r.provideDescription()))
                .toList();
    }

    private <T extends CsvRow> List<T> fetchCsvRows(Class<T> clazz, MultipartFile file, char delimiter) {
        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            CsvToBean<T> csvToBean = new CsvToBeanBuilder<T>(reader)
                    .withType(clazz)
                    .withSeparator(delimiter)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            return csvToBean.parse();
        } catch (Exception e) {
            throw new CsvImportException("Csv file reading has failed.", e);
        }
    }
}
