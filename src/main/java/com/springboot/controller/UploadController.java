package com.springboot.controller;

import com.springboot.domain.IssueData;
import com.springboot.service.CsvFetchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
public class UploadController {

    public static final String STATUS = "status";

    private final CsvFetchService dataService;

    UploadController(@Autowired CsvFetchService dataService) {
        this.dataService = dataService;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/upload-csv-file")
    public String uploadCSVFile(@RequestParam("projectId") String projectId,
                                @RequestParam("accessToken") String accessToken,
                                @RequestParam("type") String type,
                                @RequestParam("delimiter") String delimiter,
                                @RequestParam("file") MultipartFile file,
                                Model model) {
        if (file.isEmpty()) {
            populateModel(model, "Please select a CSV file to upload.");
        } else {
            try {
                List<IssueData> items = dataService.provideIssueData(type, delimiter, file);
                populateModel(model, items);

            } catch (Exception ex) {
                populateModel(model, "An error occurred while processing the CSV file.");
            }
        }

        return "upload-csv";
    }

    private <T> void populateModel(Model model, List<T> items) {
        model.addAttribute("items", items);
        model.addAttribute(STATUS, true);
    }

    private void populateModel(Model model, String errorMessage) {
        model.addAttribute("message", errorMessage);
        model.addAttribute(STATUS, false);
    }
}
