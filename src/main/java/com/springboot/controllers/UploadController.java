package com.springboot.controllers;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.springboot.domains.CsvType;
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
import java.util.List;

@Controller
public class UploadController {

    public static final String STATUS = "status";

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/upload-csv-file")
    public String uploadCSVFile(@RequestParam("type") String type,
                                @RequestParam("delimiter") String delimiter,
                                @RequestParam("file") MultipartFile file,
                                Model model) {
        if (file.isEmpty()) {
            populateModel(model, "Please select a CSV file to upload.");
        } else {
            try {
                CsvType csvType = CsvType.valueOf(type);
                char csvDelimiter = delimiter.charAt(0);

                List<?> items = fetchItems(csvType.getClazz(), file, csvDelimiter);
                populateModel(model, items);

            } catch (Exception ex) {
                populateModel(model, "An error occurred while processing the CSV file.");
            }
        }

        return "file-upload-status";
    }

    private <T> List<T> fetchItems(Class<T> clazz, MultipartFile file, char separator) throws IOException {
        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            CsvToBean<T> csvToBean = new CsvToBeanBuilder<T>(reader)
                    .withType(clazz)
                    .withSeparator(separator)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            return csvToBean.parse();
        }
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
