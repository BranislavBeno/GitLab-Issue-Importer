package com.issue.importer.service;

import com.issue.importer.domain.ApplicationSettings;
import com.issue.importer.io.props.SettingsReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

public record AppSettingsService(SettingsReader reader) {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppSettingsService.class);

    public ApplicationSettings readApplicationSettings(MultipartFile file) {
        ApplicationSettings settings = reader.provideSettings(file);
        LOGGER.info("PROPERTIES file '{}' was read successfully", file.getOriginalFilename());

        return settings;
    }
}
