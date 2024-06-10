package com.issue.importer.controller;

import com.codeborne.selenide.Selenide;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BasicUiTest extends AbstractControllerTest {

    @Override
    String getPagePath() {
        return "/";
    }

    @Test
    void testGeneralComponents() {
        Selenide.$("#uploadIssuesButton").click();
        assertThat(Selenide.$("#uploadIssues").exists()).isTrue();
        takeScreenshot("uploadIssuesBasicView");

        Selenide.$("#importSettingsButton").click();
        assertThat(Selenide.$("#importSettings").exists()).isTrue();
        takeScreenshot("uploadPropertiesBasicView");
    }
}
