package com.issue.importer.controller;

import com.codeborne.selenide.Selenide;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

class BasicUiTest extends AbstractControllerTest implements WithAssertions {

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
