package com.issue.importer.service;

import com.issue.importer.domain.IssueData;

import java.util.List;

public record ResultData(List<IssueData> newData, List<IssueData> existingData) {
}
