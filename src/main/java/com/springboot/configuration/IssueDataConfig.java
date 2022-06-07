package com.springboot.configuration;

import com.springboot.service.IssueDataService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IssueDataConfig {

    @Bean
    public IssueDataService issueDataService() {
        return new IssueDataService();
    }
}
