package com.issue.importer.controller;

import com.issue.importer.domain.ApplicationSettings;
import com.issue.importer.domain.CsvType;
import com.issue.importer.domain.IssueData;
import com.issue.importer.service.AppSettingsService;
import com.issue.importer.service.CsvFetchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
public class ImportController {

    public static final String STATUS = "status";

    private final AppSettingsService settingsService;
    private final CsvFetchService fetchService;

    ImportController(@Autowired AppSettingsService settingsService,
                     @Autowired CsvFetchService fetchService) {
        this.settingsService = settingsService;
        this.fetchService = fetchService;
    }

    @GetMapping("/")
    public String showIndex() {
        return "index";
    }

    @GetMapping("/upload-properties-file")
    public String showCsvForm(Model model) {
        model.addAttribute("csvTypes", CsvType.values());

        return "upload-issues";
    }

    @PostMapping("/upload-properties-file")
    public String uploadPropertiesFile(@RequestParam("file") MultipartFile file,
                                       Model model) {
        ApplicationSettings settings = settingsService.readApplicationSettings(file);
        populateModel(model, settings);

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
}
