package com.issue.importer.domain;

public record ApplicationSettings(String url, String projectId, String accessToken, String csvType, String delimiter) {

    public ApplicationSettings() {
        this("", "", "", "", "");
    }

    public ApplicationSettings(String csvType, String delimiter) {
        this("", "", "", csvType, delimiter);
    }
}
