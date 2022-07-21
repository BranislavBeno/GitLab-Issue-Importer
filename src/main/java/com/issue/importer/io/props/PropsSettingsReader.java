package com.issue.importer.io.props;

import com.issue.importer.domain.ApplicationSettings;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class PropsSettingsReader implements SettingsReader {

    @Override
    public ApplicationSettings provideSettings(MultipartFile file) {
        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            Properties properties = new Properties();
            properties.load(reader);
            String projectUrl = properties.getProperty("project.url", "");
            String projectId = properties.getProperty("project.id", "");
            String accessToken = properties.getProperty("project.access.token", "");
            String csvType = properties.getProperty("csv.type", "");
            String delimiter = properties.getProperty("csv.delimiter", "");

            return new ApplicationSettings(projectUrl, projectId, accessToken, csvType, delimiter);
        } catch (Exception e) {
            throw new PropertiesReadingException("Settings reading has failed.", e);
        }
    }
}
