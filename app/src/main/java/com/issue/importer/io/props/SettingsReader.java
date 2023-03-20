package com.issue.importer.io.props;

import com.issue.importer.domain.ApplicationSettings;
import org.springframework.web.multipart.MultipartFile;

public interface SettingsReader {

    ApplicationSettings provideSettings(MultipartFile file);
}
