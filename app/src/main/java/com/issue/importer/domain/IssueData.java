package com.issue.importer.domain;

public record IssueData(String iid, String title, String description, String webUrl) {
    public IssueData(String title, String description) {
        this("", title, description, "");
    }
}
