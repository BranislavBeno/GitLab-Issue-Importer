package com.issue.importer.controller;

import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Selenide.$;
import static org.assertj.core.api.Assertions.assertThat;

class BasicUiTest extends AbstractControllerTest {

    @Override
    String getPagePath() {
        return "/";
    }

    @Test
    void testGeneralComponents() {
        $("#uploadIssuesButton").click();
        assertThat($("#uploadIssues").exists()).isTrue();
        takeScreenshot("uploadIssuesBasicView");

        $("#importSettingsButton").click();
        assertThat($("#importSettings").exists()).isTrue();
        takeScreenshot("uploadPropertiesBasicView");
    }
}
