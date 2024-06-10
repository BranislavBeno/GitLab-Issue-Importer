package com.issue.importer.controller;

import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.Testcontainers;

class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        applicationContext.addApplicationListener((ApplicationListener<WebServerInitializedEvent>) event ->
                Testcontainers.exposeHostPorts(event.getWebServer().getPort()));
    }
}
