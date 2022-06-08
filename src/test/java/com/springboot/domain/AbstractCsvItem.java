package com.springboot.domain;

import com.springboot.service.CsvFetchService;
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

    CsvFetchService fetchService;

    @BeforeEach
    void setUp() {
        fetchService = new CsvFetchService();
    }

    List<IssueData> readIssueData(String path, String type, String delimiter) throws IOException {
        File file = new ClassPathResource(path).getFile();
        FileInputStream input = new FileInputStream(file);
        MultipartFile multipartFile =
                new MockMultipartFile("file", file.getName(), "text/plain", IOUtils.toByteArray(input));

        return fetchService.provideIssueData(type, delimiter, multipartFile);
    }
}
