package com.issue.importer.domain;

import com.issue.importer.io.csv.CsvDataReader;
import org.apache.commons.compress.utils.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

abstract class AbstractCsvItem {

    private CsvDataReader reader;

    @BeforeEach
    void setUp() {
        reader = new CsvDataReader();
    }

    List<IssueData> readIssueData(ApplicationSettings settings, String path) throws IOException {
        File file = new ClassPathResource(path).getFile();
        FileInputStream input = new FileInputStream(file);
        MultipartFile multipartFile =
                new MockMultipartFile("file", file.getName(), "text/plain", IOUtils.toByteArray(input));

        return reader.readCsvData(settings, multipartFile);
    }
}
