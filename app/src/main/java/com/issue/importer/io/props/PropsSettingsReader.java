package com.issue.importer.io.props;

import com.issue.importer.domain.ApplicationSettings;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class PropsSettingsReader implements SettingsReader {

    private final String accessToken;

    public PropsSettingsReader() {
        this.accessToken = "";
    }

    public PropsSettingsReader(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public ApplicationSettings provideSettings(MultipartFile file) {
        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            Properties properties = new Properties();
            properties.load(reader);
            String projectUrl = properties.getProperty("project.url", "");
            String projectId = properties.getProperty("project.id", "");
            String token = properties.getProperty("project.access.token", accessToken);
            String csvType = properties.getProperty("csv.type", "");
            String delimiter = properties.getProperty("csv.delimiter", "");

            return new ApplicationSettings(projectUrl, projectId, token, csvType, delimiter);
        } catch (Exception e) {
            throw new PropertiesReadingException("Settings reading has failed.", e);
        }
    }
}
