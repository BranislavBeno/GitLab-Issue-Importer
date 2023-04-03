package com.issue.importer.cdk;

import com.issue.importer.cdk.construct.ApplicationEnvironment;
import com.issue.importer.cdk.construct.Network;
import com.issue.importer.cdk.construct.Service;
import com.issue.importer.cdk.util.CdkUtil;
import com.issue.importer.cdk.util.Validations;
import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;

import java.util.HashMap;

public class ServiceApp {

    public static void main(final String[] args) {
        var app = new App();

        String accountId = Validations.requireNonEmpty(app, "accountId");
        String region = Validations.requireNonEmpty(app, "region");
        String environmentName = Validations.requireNonEmpty(app, "environmentName");
        String applicationName = Validations.requireNonEmpty(app, "applicationName");
        String dockerRepositoryName = Validations.requireNonEmpty(app, "dockerRepositoryName");
        String dockerImageTag = Validations.requireNonEmpty(app, "dockerImageTag");

        Environment awsEnvironment = CdkUtil.makeEnv(accountId, region);

        var applicationEnvironment = new ApplicationEnvironment(
                applicationName,
                environmentName
        );

        Stack serviceStack = new Stack(app, "ServiceStack", StackProps.builder()
                .stackName(environmentName + "-Service")
                .env(awsEnvironment)
                .build());

        Network.NetworkOutputParameters networkOutputParameters =
                Network.getOutputParametersFromParameterStore(serviceStack, applicationEnvironment.environmentName());

        new Service(
                serviceStack,
                "Service",
                awsEnvironment,
                applicationEnvironment,
                new Service.ServiceInputParameters(
                        new Service.DockerImageSource(dockerRepositoryName, dockerImageTag),
                        new HashMap<>()),
                networkOutputParameters
        );

        app.synth();
    }
}
