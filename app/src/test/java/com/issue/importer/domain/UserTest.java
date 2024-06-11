package com.issue.importer.domain;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

class UserTest extends AbstractCsvItem implements WithAssertions {

    public static final String TITLE = "Jovan Lee (Origin: 102)";
    public static final String DESCRIPTION = """
            ## Description
            Please create new user with following data:
            - name: Jovan Lee
            - email: jovan@example.com
            - countryCode: FR
            - age: 25
            - job: driver""";

    @Test
    void testFileReading() throws IOException {
        ApplicationSettings settings = new ApplicationSettings("USER", ",");
        List<IssueData> items = readIssueData(settings, "/csv/users.csv");

        assertThat(items).hasSize(4);

        IssueData item = items.get(2);

        assertThat(item.title()).isEqualTo(TITLE);
        assertThat(item.description()).isEqualTo(DESCRIPTION);
    }
}