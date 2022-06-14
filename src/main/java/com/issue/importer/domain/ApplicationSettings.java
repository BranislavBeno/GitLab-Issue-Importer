package com.issue.importer.domain;

public record ApplicationSettings(String projectId, String accessToken, String csvType, String delimiter) {

    public ApplicationSettings() {
        this("", "", "", "");
    }
}
