package com.issue.importer.webclient;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.issue.importer.domain.ApplicationSettings;
import com.issue.importer.domain.IssueData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.eclipse.jetty.http.HttpStatus.*;

@WireMockTest
@Disabled("Due to incompatibility issue")
class IssueWebClientTest {

    public static final String GET_URL = "/123?scope=all&per_page=30&state=all";
    public static final String POST_URL = "/123";
    private static ApplicationSettings settings;
    private static IssueWebClient webClient;

    @BeforeAll
    static void setUpForAll(WireMockRuntimeInfo info) {
        AccessData accessData = new AccessData("/{projectId}", "all", "30", "all");
        webClient = new IssueWebClient(accessData);
        settings = new ApplicationSettings(info.getHttpBaseUrl(), "123", "", "", "");
    }

    @Test
    void testSuccessfulIssuesImport() {
        stubFor(
                WireMock.post(WireMock.urlEqualTo(POST_URL))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBodyFile("response.json")));

        String title = "Add ticket CXF000123456";
        String description = "## Description\nImport CXF ticket CFX000123456.\n## Note\nThis issue must be closed in CXF as well.";
        IssueData data = new IssueData(title, description);

        List<IssueData> issues = webClient.importIssues(settings, List.of(data));

        assertThat(issues).hasSize(1);
        IssueData result = issues.get(0);
        assertThat(result.iid()).isEqualTo("28");
    }

    @Test
    void testSuccessfulIssuesFetching() {
        stubFor(
                WireMock.get(WireMock.urlEqualTo(GET_URL))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBodyFile("issues.json")));

        List<IssueData> issues = webClient.fetchIssues(settings);

        assertThat(issues).hasSize(25);
    }

    @ParameterizedTest
    @ValueSource(ints = {UNAUTHORIZED_401, FORBIDDEN_403, NOT_FOUND_404, SERVICE_UNAVAILABLE_503})
    void testFailingIssuesFetching(int httpStatus) {
        stubFor(
                WireMock.get(GET_URL)
                        .willReturn(aResponse()
                                .withStatus(httpStatus)
                                .withFixedDelay(2000)) // milliseconds
        );

        assertThatExceptionOfType(WebClientResponseException.class).
                isThrownBy(() -> webClient.fetchIssues(settings));
    }
}