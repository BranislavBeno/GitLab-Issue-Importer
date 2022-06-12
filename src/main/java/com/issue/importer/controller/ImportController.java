package com.issue.importer.controller;

import com.issue.importer.domain.ApplicationSettings;
import com.issue.importer.domain.CsvType;
import com.issue.importer.domain.IssueData;
import com.issue.importer.service.CsvFetchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;

@Controller
public class ImportController {

    public static final String STATUS = "status";

    private final CsvFetchService fetchService;

    ImportController(@Autowired CsvFetchService fetchService) {
        this.fetchService = fetchService;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/upload-properties-file")
    public String uploadPropertiesFile(@RequestParam("file") MultipartFile file,
                                       Model model) {
        if (file.isEmpty()) {
            populateModel(model, "Please select a PROPERTIES file to upload.");
        } else {
            try {
                ApplicationSettings settings = readApplicationSettings(file);
                populateModel(model, settings);
            } catch (Exception ex) {
                populateModel(model, "An error occurred while processing the PROPERTIES file.");
            }
        }

        return "upload-issues";
    }

    @PostMapping("/upload-csv-file")
    public String uploadCsvFile(@RequestParam("projectId") String projectId,
                                @RequestParam("accessToken") String accessToken,
                                @RequestParam("type") String type,
                                @RequestParam("delimiter") String delimiter,
                                @RequestParam("file") MultipartFile file,
                                Model model) {
        if (file.isEmpty()) {
            populateModel(model, "Please select a CSV file to upload.");
        } else {
            try {
                List<IssueData> items = fetchService.provideIssueData(type, delimiter, file);
                populateModel(model, items);

            } catch (Exception ex) {
                populateModel(model, "An error occurred while processing the CSV file.");
            }
        }

        return "show-results";
    }

    private <T> void populateModel(Model model, List<T> items) {
        model.addAttribute("items", items);
        model.addAttribute(STATUS, true);
    }

    private void populateModel(Model model, String errorMessage) {
        model.addAttribute("message", errorMessage);
        model.addAttribute(STATUS, false);
    }

    private void populateModel(Model model, ApplicationSettings settings) {
        model.addAttribute("projectId", settings.projectId());
        model.addAttribute("accessToken", settings.accessToken());
        model.addAttribute("delimiter", settings.delimiter());
        model.addAttribute("csvType", settings.csvType());
        model.addAttribute("csvTypes", CsvType.values());
    }

    private ApplicationSettings readApplicationSettings(MultipartFile file) throws IOException {
        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            Properties properties = new Properties();
            properties.load(reader);
            String projectId = properties.getProperty("project.id", "");
            String accessToken = properties.getProperty("project.access.token", "");
            String csvType = properties.getProperty("csv.type", "");
            String delimiter = properties.getProperty("csv.delimiter", "");

            return new ApplicationSettings(projectId, accessToken, csvType, delimiter);
        }
    }
}
