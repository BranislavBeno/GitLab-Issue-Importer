package com.issue.importer.controller;

import com.issue.importer.Application;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.codeborne.selenide.Selenide.screenshot;
import static com.codeborne.selenide.Selenide.title;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = Initializer.class)
class ImportControllerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImportControllerTest.class);

    @LocalServerPort
    private Integer port;

    @BeforeEach
    void setUp() {
        String url = WebBrowserInitializer.URL + port + "/";
        WebBrowserInitializer.DRIVER.get(url);
    }

    @Test
    void testPageTitle() {
        assertThat(title()).isEqualTo("Import issues");

        String screenshotPath = screenshot("import");
        LOGGER.info(() -> "Screenshot is available under %s".formatted(screenshotPath));
    }
}