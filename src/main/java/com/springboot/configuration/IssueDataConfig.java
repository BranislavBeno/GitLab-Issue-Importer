package com.springboot.configuration;

import com.springboot.service.CsvFetchService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IssueDataConfig {

    @Bean
    public CsvFetchService issueDataService() {
        return new CsvFetchService();
    }
}
