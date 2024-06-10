package com.issue.importer.controller;

import com.issue.importer.domain.ApplicationSettings;
import com.issue.importer.domain.CsvType;
import com.issue.importer.io.props.PropertiesReadingException;
import com.issue.importer.service.AppSettingsService;
import com.issue.importer.service.IssueImportException;
import com.issue.importer.service.IssueTrackingService;
import com.issue.importer.service.ResultData;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Controller
public class ImportController {

    private final AppSettingsService settingsService;
    private final IssueTrackingService issueTrackingService;

    ImportController(AppSettingsService settingsService,
                     IssueTrackingService issueTrackingService) {
        this.settingsService = settingsService;
        this.issueTrackingService = issueTrackingService;
    }

    @GetMapping("/")
    public String showIndex() {
        return "index";
    }

    @GetMapping("/upload-properties")
    public String showIssuesUploadForm(Model model) {
        populateModel(model, new ApplicationSettings());

        return "upload-issues";
    }

    @PostMapping("/upload-properties")
    public String uploadProperties(@RequestParam MultipartFile file,
                                   Model model) {
        RuntimeException exception = new PropertiesReadingException("PROPERTIES file reading failed.");

        boolean result = verifyInput(file, model, "Please select a PROPERTIES file to upload.",
                "PROPERTIES file is empty.", exception);
        if (result) {
            return "error/custom";
        }

        try {
            ApplicationSettings settings = settingsService.readApplicationSettings(file);
            populateModel(model, settings);
        } catch (Exception ex) {
            throw exception;
        }

        return "upload-issues";
    }

    @PostMapping("/upload-issues")
    public String uploadIssues(@RequestParam String url,
                               @RequestParam String projectId,
                               @RequestParam String accessToken,
                               @RequestParam String type,
                               @RequestParam String delimiter,
                               @RequestParam MultipartFile file,
                               Model model) {
        RuntimeException exception = new IssueImportException("Issues import failed.");

        boolean result = verifyInput(file, model, "Please select an ISSUE RESOURCE file to upload.",
                "ISSUE RESOURCE file is empty.", exception);
        if (result) {
            return "error/custom";
        }

        try {
            ApplicationSettings settings = new ApplicationSettings(url, projectId, accessToken, type, delimiter);
            ResultData results = issueTrackingService.importIssueData(settings, file);

            model.addAttribute("newFound", !results.newData().isEmpty());
            model.addAttribute("imported", results.newData());
            model.addAttribute("oldFound", !results.existingData().isEmpty());
            model.addAttribute("ignored", results.existingData());
        } catch (Exception ex) {
            throw exception;
        }

        return "show-results";
    }

    private boolean verifyInput(MultipartFile file, Model model, String messageForNull, String messageForEmpty,
                                RuntimeException exception) {
        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isEmpty()) {
            populateModel(model, messageForNull);
            return true;
        }

        if (file.isEmpty()) {
            populateModel(model, messageForEmpty);
            return true;
        }

        try (InputStream is = file.getInputStream()) {
            String content = new String(is.readAllBytes());
            if (content.isBlank()) {
                populateModel(model, messageForEmpty);
                return true;
            }
        } catch (Exception e) {
            throw exception;
        }

        return false;
    }

    private static void populateModel(Model model, String messageForEmpty) {
        model.addAttribute("message", messageForEmpty);
    }

    private void populateModel(Model model, ApplicationSettings settings) {
        model.addAttribute("settings", settings);
        model.addAttribute("csvTypes", CsvType.FOR_PRODUCTION);
    }
}
