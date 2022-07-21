package com.issue.importer.webclient;

import com.issue.importer.domain.ApplicationSettings;
import com.issue.importer.domain.Issue;
import com.issue.importer.domain.IssueData;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;

public class IssueWebClient {

    private final AccessData accessData;

    public IssueWebClient(AccessData accessData) {
        this.accessData = Objects.requireNonNull(accessData);
    }

    public List<IssueData> fetchIssues(ApplicationSettings settings) {
        WebClient webClient = buildWebClient(settings);

        Issue[] issues = webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(accessData.issuesUrl())
                        .queryParam("scope", accessData.scope())
                        .queryParam("per_page", accessData.perPageLimit())
                        .queryParam("state", accessData.state())
                        .build(settings.projectId()))
                .retrieve()
                .bodyToMono(Issue[].class)
                .block();

        return Optional.ofNullable(issues)
                .map(i -> Arrays.stream(i)
                        .map(this::createIssueData)
                        .toList())
                .orElse(Collections.emptyList());
    }

    public List<IssueData> importIssues(ApplicationSettings settings, List<IssueData> issueDataList) {
        WebClient webClient = buildWebClient(settings);

        List<IssueData> issues = new ArrayList<>();
        for (IssueData data : issueDataList) {
            Issue issue = createIssue(data);
            Issue importedIssue = importOneIssue(settings.projectId(), issue, webClient);
            Optional.ofNullable(importedIssue).ifPresent(i -> {
                IssueData result = createIssueData(i);
                issues.add(result);
            });
        }

        return Collections.unmodifiableList(issues);
    }

    private IssueData createIssueData(Issue issue) {
        return new IssueData(issue.getIid(), issue.getTitle(), issue.getDescription(), issue.getWebUrl());
    }

    private Issue createIssue(IssueData data) {
        Issue issue = new Issue();
        issue.setTitle(data.title());
        issue.setDescription(data.description());

        return issue;
    }

    private Issue importOneIssue(String projectId, Issue issue, WebClient webClient) {
        return webClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path(accessData.issuesUrl())
                        .build(projectId))
                .body(Mono.just(issue), Issue.class)
                .retrieve()
                .bodyToMono(Issue.class)
                .block();
    }

    private WebClient buildWebClient(ApplicationSettings settings) {
        return WebClient.builder()
                .baseUrl(settings.url())
                .defaultHeaders(httpHeaders -> {
                    httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
                    httpHeaders.add("PRIVATE-TOKEN", settings.accessToken());
                })
                .build();
    }
}
