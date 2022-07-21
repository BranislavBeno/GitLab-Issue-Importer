package com.issue.importer.service;

import com.issue.importer.domain.ApplicationSettings;
import com.issue.importer.io.props.SettingsReader;
import org.springframework.web.multipart.MultipartFile;

public record AppSettingsService(SettingsReader reader) {

    public ApplicationSettings readApplicationSettings(MultipartFile file) {
        return reader.provideSettings(file);
    }
}
