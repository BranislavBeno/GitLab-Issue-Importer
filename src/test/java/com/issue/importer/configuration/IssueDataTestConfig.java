package com.issue.importer.configuration;

import com.issue.importer.io.props.PropsSettingsReader;
import com.issue.importer.io.props.SettingsReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public
class IssueDataTestConfig {

    @Bean(value = "Test")
    public SettingsReader settingsReader(@Value("${project.access.token}") String accessToken) {
        return new PropsSettingsReader(accessToken);
    }

}