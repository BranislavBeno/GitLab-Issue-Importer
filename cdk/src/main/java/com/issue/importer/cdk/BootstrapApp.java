package com.issue.importer.cdk;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;

import static com.issue.importer.cdk.Validations.requireNonEmpty;

/**
 * This is an empty app that we can use for bootstrapping the CDK with the "cdk bootstrap" command.
 * We could do this with other apps, but this would require us to enter all the parameters
 * for that app, which is uncool.
 */
public class BootstrapApp {

    public static void main(final String[] args) {
        App app = new App();

        String accountId = requireNonEmpty(app, "accountId");
        String region = requireNonEmpty(app, "region");

        Environment awsEnvironment = makeEnv(accountId, region);

        new Stack(app, "Bootstrap", StackProps.builder()
                .env(awsEnvironment)
                .build());

        app.synth();
    }

    static Environment makeEnv(String account, String region) {
        return Environment.builder()
                .account(account)
                .region(region)
                .build();
    }
}
