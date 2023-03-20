package com.issue.importer.io.csv;

import com.issue.importer.domain.ApplicationSettings;
import com.issue.importer.domain.IssueData;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DataReader {

    List<IssueData> readCsvData(ApplicationSettings settings, MultipartFile file);
}
