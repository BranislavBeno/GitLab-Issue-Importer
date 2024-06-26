package com.issue.importer.webclient;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.issue.importer.domain.ApplicationSettings;
import com.issue.importer.domain.IssueData;
import org.assertj.core.api.WithAssertions;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

@WireMockTest
@Disabled("Due to incompatibility issue")
class IssueWebClientTest implements WithAssertions {

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
        WireMock.stubFor(
                WireMock.post(WireMock.urlEqualTo(POST_URL))
                        .willReturn(WireMock.aResponse()
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBodyFile("response.json")));

        String title = "Add ticket CXF000123456";
        String description = "## Description\nImport CXF ticket CFX000123456.\n## Note\nThis issue must be closed in CXF as well.";
        IssueData data = new IssueData(title, description);

        List<IssueData> issues = webClient.importIssues(settings, List.of(data));

        assertThat(issues).hasSize(1);
        IssueData result = issues.getFirst();
        assertThat(result.iid()).isEqualTo("28");
    }

    @Test
    void testSuccessfulIssuesFetching() {
        WireMock.stubFor(
                WireMock.get(WireMock.urlEqualTo(GET_URL))
                        .willReturn(WireMock.aResponse()
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBodyFile("issues.json")));

        List<IssueData> issues = webClient.fetchIssues(settings);

        assertThat(issues).hasSize(25);
    }

    @ParameterizedTest
    @ValueSource(ints = {HttpStatus.UNAUTHORIZED_401,HttpStatus.FORBIDDEN_403,HttpStatus.NOT_FOUND_404,HttpStatus.SERVICE_UNAVAILABLE_503})
    void testFailingIssuesFetching(int httpStatus) {
        WireMock.stubFor(
                WireMock.get(GET_URL)
                        .willReturn(WireMock.aResponse()
                                .withStatus(httpStatus)
                                .withFixedDelay(2000)) // milliseconds
        );

        assertThatExceptionOfType(WebClientResponseException.class).
                isThrownBy(() -> webClient.fetchIssues(settings));
    }
}