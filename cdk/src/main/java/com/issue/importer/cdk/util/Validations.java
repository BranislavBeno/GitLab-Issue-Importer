package com.issue.importer.cdk.util;

import software.amazon.awscdk.App;

public class Validations {

    private Validations() {
    }

    public static String requireNonEmpty(App app, String parameterName) {
        String parameter = (String) app.getNode().tryGetContext(parameterName);

        if (parameter.isBlank()) {
            String message = "Context variable '%s' must be defined.".formatted(parameterName);
            throw new IllegalArgumentException(message);
        }

        return parameter;
    }
}
