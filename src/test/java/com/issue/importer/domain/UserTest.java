package com.issue.importer.domain;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest extends AbstractCsvItem {

    public static final String TITLE = "Jovan Lee (Origin: 102)";
    public static final String DESCRIPTION = """
            User {
                "id=102",
                "name=Jovan Lee",
                "email=jovan@example.com",
                "countryCode=FR",
                "age=25",
                "job=driver"
            }""";

    @Test
    void testFileReading() throws IOException {
        List<IssueData> items = readIssueData("/users.csv", "USER", ",");

        assertThat(items).hasSize(4);

        IssueData item = items.get(2);

        assertThat(item.title()).isEqualTo(TITLE);
        assertThat(item.description()).isEqualTo(DESCRIPTION);
    }
}