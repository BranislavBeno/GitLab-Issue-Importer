package com.issue.importer.configuration;

import com.issue.importer.service.AppSettingsService;
import com.issue.importer.service.CsvFetchService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IssueDataConfig {

    @Bean
    public CsvFetchService issueDataService() {
        return new CsvFetchService();
    }

    @Bean
    public AppSettingsService appSettingsService() {
        return new AppSettingsService();
    }
}
