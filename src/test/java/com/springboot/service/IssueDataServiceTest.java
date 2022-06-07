package com.springboot.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class IssueDataServiceTest {

    @InjectMocks
    private IssueDataService dataService;

    @Test
    void provideIssueData() {
        assertThat(dataService).isNotNull();
    }
}