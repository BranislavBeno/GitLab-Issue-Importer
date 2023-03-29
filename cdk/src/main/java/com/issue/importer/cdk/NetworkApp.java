package com.issue.importer.cdk;

import com.issue.importer.cdk.construct.Network;
import com.issue.importer.cdk.util.CdkUtil;
import com.issue.importer.cdk.util.Validations;
import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;

public class NetworkApp {

    public static void main(final String[] args) {
        var app = new App();

        String accountId = Validations.requireNonEmpty(app, "accountId");
        String region = Validations.requireNonEmpty(app, "region");
        String environmentName = Validations.requireNonEmpty(app, "environmentName");

        Environment awsEnvironment = CdkUtil.makeEnv(accountId, region);

        var networkStack = new Stack(app, "NetworkStack", StackProps.builder()
                .stackName(environmentName + "-Network")
                .env(awsEnvironment)
                .build());

        new Network(
                networkStack,
                "Network",
                environmentName,
                new Network.NetworkInputParameters());

        app.synth();
    }
}
