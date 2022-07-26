package com.issue.importer.controller;

import com.issue.importer.domain.ApplicationSettings;
import com.issue.importer.domain.CsvType;
import com.issue.importer.io.props.PropertiesReadingException;
import com.issue.importer.service.AppSettingsService;
import com.issue.importer.service.IssueImportException;
import com.issue.importer.service.IssueTrackingService;
import com.issue.importer.service.ResultData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class ImportController {

    private final AppSettingsService settingsService;
    private final IssueTrackingService issueTrackingService;

    ImportController(@Autowired AppSettingsService settingsService,
                     @Autowired IssueTrackingService issueTrackingService) {
        this.settingsService = settingsService;
        this.issueTrackingService = issueTrackingService;
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
            throw new PropertiesReadingException("Properties file reading failed.");
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
            return handleError(model, "Please select an issue resource file to upload.");
        }

        try {
            ApplicationSettings settings = new ApplicationSettings(url, projectId, accessToken, type, delimiter);
            ResultData results = issueTrackingService.importIssueData(settings, file);

            model.addAttribute("newFound", !results.newData().isEmpty());
            model.addAttribute("imported", results.newData());
            model.addAttribute("oldFound", !results.existingData().isEmpty());
            model.addAttribute("ignored", results.existingData());
        } catch (Exception ex) {
            throw new IssueImportException("Issues import failed.");
        }

        return "show-results";
    }

    private String handleError(Model model, String message) {
        model.addAttribute("message", message);
        return "error/custom";
    }

    private void populateModel(Model model, ApplicationSettings settings) {
        model.addAttribute("settings", settings);
        model.addAttribute("csvTypes", CsvType.values());
    }
}
