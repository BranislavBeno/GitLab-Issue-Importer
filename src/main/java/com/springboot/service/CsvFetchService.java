package com.springboot.service;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.springboot.domain.CsvRow;
import com.springboot.domain.CsvType;
import com.springboot.domain.IssueData;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class CsvFetchService {

    public List<IssueData> provideIssueData(String type, String delimiter, MultipartFile file) throws IOException {
        CsvType csvType = CsvType.valueOf(type);
        char csvDelimiter = delimiter.charAt(0);
        List<? extends CsvRow> rows = fetchCsvRows(csvType.getClazz(), file, csvDelimiter);

        return rows.stream()
                .map(r -> new IssueData(r.provideTitle(), r.provideDescription()))
                .toList();
    }

    private <T extends CsvRow> List<T> fetchCsvRows(Class<T> clazz, MultipartFile file, char delimiter) throws IOException {
        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            CsvToBean<T> csvToBean = new CsvToBeanBuilder<T>(reader)
                    .withType(clazz)
                    .withSeparator(delimiter)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            return csvToBean.parse();
        }
    }
}
