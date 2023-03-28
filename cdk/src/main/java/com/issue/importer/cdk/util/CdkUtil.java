package com.issue.importer.cdk.util;

import software.amazon.awscdk.Environment;

public class CdkUtil {

    private CdkUtil() {
    }

    public static Environment makeEnv(String account, String region) {
        return Environment.builder()
                .account(account)
                .region(region)
                .build();
    }
}
