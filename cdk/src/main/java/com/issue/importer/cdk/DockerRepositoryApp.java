package com.issue.importer.cdk;

import com.issue.importer.cdk.construct.DockerRepository;
import com.issue.importer.cdk.util.CdkUtil;
import com.issue.importer.cdk.util.Validations;
import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;

public class DockerRepositoryApp {

    public static void main(final String[] args) {
        var app = new App();

        String accountId = Validations.requireNonEmpty(app, "accountId");
        String region = Validations.requireNonEmpty(app, "region");
        String dockerRepositoryName = Validations.requireNonEmpty(app, "dockerRepositoryName");

        Environment awsEnvironment = CdkUtil.makeEnv(accountId, region);

        String stackName = dockerRepositoryName + "-docker-repository";
        var dockerRepositoryStack = new Stack(app, "DockerRepositoryStack", StackProps.builder()
                .stackName(stackName)
                .env(awsEnvironment)
                .build());

        new DockerRepository(
                dockerRepositoryStack,
                "DockerRepository",
                new DockerRepository.DockerRepositoryInputParameters(dockerRepositoryName, accountId));

        app.synth();
    }
}
