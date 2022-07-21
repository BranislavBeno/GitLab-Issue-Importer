package com.issue.importer.webclient;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.issue.importer.domain.ApplicationSettings;
import com.issue.importer.domain.IssueData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.eclipse.jetty.http.HttpStatus.*;

class IssueWebClientTest {

    public static final String TEST_URL = "/123?scope=all&per_page=30&state=all";
    private static ApplicationSettings settings;
    private static IssueWebClient webClient;
    @RegisterExtension
    private static final WireMockExtension MOCK_SERVER = WireMockExtension.newInstance()
            .options(wireMockConfig()
                    .dynamicPort())
            .build();

    @BeforeAll
    static void setUpForAll() {
        AccessData accessData = new AccessData("/{projectId}", "all", "30", "all");
        webClient = new IssueWebClient(accessData);
        settings = new ApplicationSettings(MOCK_SERVER.baseUrl(), "123", "", "", "");
    }

    @AfterEach
    void resetAll() {
        MOCK_SERVER.resetAll();
    }

    @Test
    void testSuccessfulIssuesFetching() {
        MOCK_SERVER.stubFor(
                WireMock.get(WireMock.urlEqualTo(TEST_URL))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBodyFile("issues.json")));

        List<IssueData> issues = webClient.fetchIssues(settings);
        assertThat(issues).hasSize(25);
    }

    @ParameterizedTest
    @ValueSource(ints = {UNAUTHORIZED_401, FORBIDDEN_403, NOT_FOUND_404, SERVICE_UNAVAILABLE_503})
    void testFailingIssuesFetching(int httpStatus) {
        MOCK_SERVER.stubFor(
                WireMock.get(TEST_URL)
                        .willReturn(aResponse()
                                .withStatus(httpStatus)
                                .withFixedDelay(2000)) // milliseconds
        );

        assertThatExceptionOfType(WebClientResponseException.class).
                isThrownBy(() -> webClient.fetchIssues(settings));
    }
}