package com.issue.importer.controller;

import com.issue.importer.domain.ApplicationSettings;
import com.issue.importer.domain.CsvType;
import com.issue.importer.domain.IssueData;
import com.issue.importer.service.AppSettingsService;
import com.issue.importer.service.CsvFetchService;
import com.issue.importer.service.CsvImportException;
import com.issue.importer.service.PropertiesImportException;
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
        populateModel(model, new ApplicationSettings());

        return "upload-issues";
    }

    @PostMapping("/upload-properties-file")
    public String uploadPropertiesFile(@RequestParam("file") MultipartFile file,
                                       Model model) {
        if (file.isEmpty()) {
            return handleError(model, "Please select a PROPERTIES file to upload.");
        }

        try {
            ApplicationSettings settings = settingsService.readApplicationSettings(file);
            populateModel(model, settings);
        } catch (Exception ex) {
            throw new PropertiesImportException();
        }

        return "upload-issues";
    }

    @PostMapping("/upload-csv-file")
    public String uploadCsvFile(@RequestParam("url") String url,
                                @RequestParam("projectId") String projectId,
                                @RequestParam("accessToken") String accessToken,
                                @RequestParam("type") String type,
                                @RequestParam("delimiter") String delimiter,
                                @RequestParam("file") MultipartFile file,
                                Model model) {
        if (file.isEmpty()) {
            return handleError(model, "Please select a CSV file to upload.");
        }

        try {
            ApplicationSettings settings = new ApplicationSettings(url, projectId, accessToken, type, delimiter);
            List<IssueData> items = fetchService.uploadIssueData(settings, file);
            populateModel(model, items);
        } catch (Exception ex) {
            throw new CsvImportException();
        }

        return "show-results";
    }

    private String handleError(Model model, String message) {
        model.addAttribute("message", message);
        return "error/custom";
    }

    private <T> void populateModel(Model model, List<T> items) {
        model.addAttribute("items", items);
    }

    private void populateModel(Model model, ApplicationSettings settings) {
        model.addAttribute("settings", settings);
        model.addAttribute("csvTypes", CsvType.values());
    }
}
