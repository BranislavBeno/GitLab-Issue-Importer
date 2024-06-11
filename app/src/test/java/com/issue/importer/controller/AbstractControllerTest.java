package com.issue.importer.controller;

import com.codeborne.selenide.Selenide;
import com.issue.importer.Application;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.remote.LocalFileDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = Initializer.class)
abstract class AbstractControllerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractControllerTest.class);

    @LocalServerPort
    private Integer port;

    abstract String getPagePath();

    @BeforeEach
    void setUp() {
        String url = WebBrowserInitializer.URL + port + getPagePath();
        WebBrowserInitializer.DRIVER.get(url);
        // let WebDriver know that file is uploaded from local computer to a remote server
        WebBrowserInitializer.DRIVER.setFileDetector(new LocalFileDetector());
    }

    static void takeScreenshot(String fileName) {
        String screenshotPath = Selenide.screenshot(fileName);
        LOGGER.info("Screenshot is available under {}", screenshotPath);
    }
}
