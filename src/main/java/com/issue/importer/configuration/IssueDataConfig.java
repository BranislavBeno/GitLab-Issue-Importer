package com.issue.importer.configuration;

import com.issue.importer.io.csv.CsvDataReader;
import com.issue.importer.io.csv.DataReader;
import com.issue.importer.io.props.PropsSettingsReader;
import com.issue.importer.io.props.SettingsReader;
import com.issue.importer.service.AppSettingsService;
import com.issue.importer.service.IssueTrackingService;
import com.issue.importer.webclient.AccessData;
import com.issue.importer.webclient.IssueWebClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class IssueDataConfig {

    @Bean
    public AccessData accessData(@Value("${issue.tracker.issues-url}") String issuesUrl,
                                 @Value("${issue.tracker.scope}") String scope,
                                 @Value("${issue.tracker.per-page-limit}") String perPageLimit,
                                 @Value("${issue.tracker.state}") String state) {
        return new AccessData(issuesUrl, scope, perPageLimit, state);
    }

    @Bean
    @Primary
    public SettingsReader settingsReader() {
        return new PropsSettingsReader();
    }

    @Bean
    public AppSettingsService appSettingsService(@Autowired SettingsReader reader) {
        return new AppSettingsService(reader);
    }

    @Bean
    public IssueWebClient issueWebClient(@Autowired AccessData accessData) {
        return new IssueWebClient(accessData);
    }

    @Bean
    public DataReader dataReader() {
        return new CsvDataReader();
    }

    @Bean
    public IssueTrackingService issueTrackingService(@Autowired IssueWebClient issueWebClient,
                                                     @Autowired DataReader reader) {
        return new IssueTrackingService(issueWebClient, reader);
    }
}
